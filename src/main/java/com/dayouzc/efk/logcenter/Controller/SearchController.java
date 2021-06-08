package com.dayouzc.efk.logcenter.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dayouzc.efk.logcenter.config.ClientPool;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName SearchController.java
 * @Description TODO 检索日志
 * @createTime 2021年06月08日 15:31:00
 */
@RestController
@RequestMapping("/search")
public class SearchController {
    static Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    private ClientPool clientPool;

    /**
     * 查询全部日志
     */
    @PostMapping("/searchAll")
    public JSONArray searchLog(String indexName) throws IOException {
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
        //只查询时间戳和日志字段
        String[] includes={"@timestamp","message"};
        builder.fetchSource(includes,excludes);
        searchRequest.source(builder);
        SearchResponse response1 = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response1.getHits();
        System.out.println(hits.getTotalHits());//总条数
        System.out.println(response1.getTook());//查询时间

        JSONArray jsonArray=new JSONArray();

        for(SearchHit hit:hits){
            JSONObject jsonObject = JSONObject.parseObject(hit.getSourceAsString());
            jsonArray.add(jsonObject);
        }
        return jsonArray;

    }
}
