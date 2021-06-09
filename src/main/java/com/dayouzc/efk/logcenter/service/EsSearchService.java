package com.dayouzc.efk.logcenter.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dayouzc.efk.logcenter.Controller.SearchController;
import com.dayouzc.efk.logcenter.config.ClientPool;
import com.dayouzc.efk.logcenter.constant.APPEnums;
import com.dayouzc.efk.logcenter.constant.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName SearchService.java
 * @Description TODO
 * @createTime 2021年06月09日 13:35:00
 */
@Service
public class EsSearchService {
    static Logger logger = LoggerFactory.getLogger(EsSearchService.class);

    @Autowired
    private ClientPool clientPool;
    /**
     * 查询全部日志
     */
    public JsonObject<JSONArray> searchLog(String indexName, String beginDate,String endDate) throws IOException {
        //检查索引是否有效
        boolean check = checkIndex(indexName);
        if(!check)return new JsonObject(null,500,"索引不存在");
        //得到连接
        RestHighLevelClient client = clientPool.getConnection();
        SearchRequest searchRequest=new SearchRequest();
        searchRequest.indices(indexName);//索引名
        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        /**
         * 查询分页
         */
        builder.from(0);
        builder.size(10);
        /**
         * 过滤字段
         */
        String[] excludes={};
        //只查询时间字段
        String[] includes={"time"};
        builder.fetchSource(includes,excludes);
        /**
         * 根据时间范围查询
         */
        if(!StringUtils.isBlank(beginDate) && !StringUtils.isBlank(endDate)){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("@timestamp");
            rangeQuery.gte(beginDate);
            rangeQuery.lte(endDate);
            builder.query(rangeQuery);
        }

        searchRequest.source(builder);
        SearchResponse response1 = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response1.getHits();
        System.out.println(hits.getTotalHits());//总条数
        System.out.println(response1.getTook());//查询时间

        JSONArray jsonArray=new JSONArray();

        for(SearchHit hit:hits){
            String id = hit.getId();
            String index = hit.getIndex();
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("id",id);
            jsonObject.put("index",index);
            JSONObject timeObj = JSONObject.parseObject(hit.getSourceAsString());
            jsonObject.put("source",timeObj.get("time"));

            jsonArray.add(jsonObject);
        }
        //释放连接
        clientPool.releaseConnection(client);
        return new JsonObject<>(jsonArray);
    }

    /**
     * 根据日志id（文档id）和索引id 查询日志详情（文档详情）
     */
    public JsonObject<String> getLog(String indexName,String docId) throws IOException {
        RestHighLevelClient connection = clientPool.getConnection();
        GetRequest getRequest=new GetRequest();
        getRequest.index(indexName).id(docId);
        GetResponse documentFields = connection.get(getRequest, RequestOptions.DEFAULT);
        String sourceAsString = documentFields.getSourceAsString();
        Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
        String message = sourceAsMap.get("message").toString();
        //释放连接
        clientPool.releaseConnection(connection);
        return new JsonObject<>(message);
    }


    /**
     * 检测索引是否存在
     * @throws Exception
     */
    public boolean checkIndex(String indexName){
        RestHighLevelClient connection = clientPool.getConnection();
        try {
            GetIndexRequest request1=new GetIndexRequest(indexName);//查询指定索引的信息
            GetIndexResponse getIndexResponse = connection.indices().get(request1, RequestOptions.DEFAULT);
            //释放连接
            clientPool.releaseConnection(connection);
            return true;
        } catch (ElasticsearchStatusException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
