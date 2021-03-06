package com.dayouzc.efk.logcenter.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dayouzc.efk.logcenter.Controller.SearchController;
import com.dayouzc.efk.logcenter.config.ClientPool;
import com.dayouzc.efk.logcenter.constant.APPEnums;
import com.dayouzc.efk.logcenter.constant.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName SearchService.java
 * @Description TODO
 * @createTime 2021???06???09??? 13:35:00
 */
@Service
public class EsSearchService {
    static Logger logger = LoggerFactory.getLogger(EsSearchService.class);

    @Autowired
    private ClientPool clientPool;

    /**
     * ??????????????????
     */
    public JsonObject<JSONArray> searchLog(String indexName, String beginDate,String endDate,String logType,Integer pageNo,Integer pageSize) throws IOException {
        //????????????????????????
        boolean check = checkIndex(indexName);
        if(!check)return new JsonObject(null,500,"???????????????");
        //????????????
        RestHighLevelClient client = clientPool.getConnection();
        SearchRequest searchRequest=new SearchRequest();
        searchRequest.indices(indexName);//?????????
        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        //??????
        /**
         * pageNo*pageSize-pageSize
         */
        builder.from(pageNo*pageSize-pageSize);
        builder.size(pageSize);
        builder.sort("@timestamp", SortOrder.DESC);
        /**
         * ????????????
         */
        String[] excludes={};
        //?????????????????????
        String[] includes={"time","timestamp","message"};
        builder.fetchSource(includes,excludes);
        /**
         * ????????????????????????
         */
        if(beginDate!=null && endDate!=null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("@timestamp");
            rangeQuery.gte(beginDate);
            rangeQuery.lte(endDate);
            builder.query(rangeQuery);
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //????????????????????????
        if(StringUtils.isNoneBlank(logType)){
            boolQueryBuilder.must(QueryBuilders.matchQuery("level",logType));
            builder.query(boolQueryBuilder);
        }

        searchRequest.source(builder);
        SearchResponse response1 = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response1.getHits();
        System.out.println(hits.getTotalHits());//?????????
        System.out.println(response1.getTook());//????????????

        JSONArray jsonArray=new JSONArray();

        for(SearchHit hit:hits){
            String id = hit.getId();
            String index = hit.getIndex();
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("id",id);
            jsonObject.put("index",index);
            JSONObject timeObj = JSONObject.parseObject(hit.getSourceAsString());
            jsonObject.put("source",timeObj.get("time"));
            /**
             * ??????????????????????????????????????????
             * todo ????????????????????????????????????????????????????????????????????????message?????????????????????Exception?????????
             * todo ??????????????????
             */
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Object message = sourceAsMap.get("message");
            if(message!=null) {
                String newMsg = message.toString();
                if (StringUtils.contains(newMsg, "Exception")) {
                    //??????
                    jsonObject.put("status","error");
                } else {
                    //??????
                    jsonObject.put("status","normal");
                }
            }

            jsonArray.add(jsonObject);
        }
        //????????????
        clientPool.releaseConnection(client);
        return new JsonObject<>(jsonArray);
    }

    /**
     * ????????????id?????????id????????????id ????????????????????????????????????
     */
    public JsonObject<String> getLog(String indexName,String docId) throws IOException {
        RestHighLevelClient connection = clientPool.getConnection();
        GetRequest getRequest=new GetRequest();
        getRequest.index(indexName).id(docId);
        GetResponse documentFields = connection.get(getRequest, RequestOptions.DEFAULT);
        String sourceAsString = documentFields.getSourceAsString();
        Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
        String message = sourceAsMap.get("message").toString();
        //????????????
        clientPool.releaseConnection(connection);
        return new JsonObject<>(message);
    }

    /**
     * ?????????????????????????????????
     * todo ?????????rep???1?????????????????????
     */
    public Set<String> getAllIndexs(){
        RestHighLevelClient connection = clientPool.getConnection();
        try {
            GetAliasesRequest request = new GetAliasesRequest();
            request.indicesOptions(IndicesOptions.lenientExpandOpen());

            GetAliasesResponse getAliasesResponse =  connection.indices().getAlias(request,RequestOptions.DEFAULT);
            Map<String, Set<AliasMetadata>> map = getAliasesResponse.getAliases();
            Set<String> indices = map.keySet();
            //??????????????????
            String[] sys_indexs = {".apm-custom-link",".kibana_task_manager_1","kibana_sample_data_ecommerce",
            ".apm-agent-configuration",".async-search",".kibana_1",".kibana-event-log-7.8.1-000001"};
            for(String s:sys_indexs){
                indices.remove(s);
            }
            return indices;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ????????????????????????
     * @throws Exception
     */
    public boolean checkIndex(String indexName){
        RestHighLevelClient connection = clientPool.getConnection();
        try {
            GetIndexRequest request1=new GetIndexRequest(indexName);//???????????????????????????
            GetIndexResponse getIndexResponse = connection.indices().get(request1, RequestOptions.DEFAULT);
            //????????????
            clientPool.releaseConnection(connection);
            return true;
        } catch (ElasticsearchStatusException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
