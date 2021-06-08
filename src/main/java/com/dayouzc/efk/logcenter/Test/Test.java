package com.dayouzc.efk.logcenter.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName Test.java
 * @Description TODO es操作测试
 * @createTime 2021年06月08日 09:52:00
 */
public class Test {
    public static void main(String[] args) throws IOException {
        //############################### 创建es客户端 #############################################
        RestHighLevelClient esClient=new RestHighLevelClient(
                RestClient.builder(new HttpHost("172.16.2.209",9200,"http"))
        );

        //############################### 创建索引(名为user) ########################################
        CreateIndexRequest request=new CreateIndexRequest("user");
        CreateIndexResponse response = esClient.indices().create(request, RequestOptions.DEFAULT);
        //响应结果
        boolean result = response.isAcknowledged();//true或false

        //################################# 查询索引 ###########################################
        GetIndexRequest request1=new GetIndexRequest("user");//查询指定索引的信息
        GetIndexResponse getIndexResponse = esClient.indices().get(request1, RequestOptions.DEFAULT);
        //响应状态
        System.out.println(getIndexResponse.getAliases());
        System.out.println(getIndexResponse.getMappings());
        System.out.println(getIndexResponse.getSettings());

        //################################# 删除索引 ###########################################
        DeleteIndexRequest deleteIndexRequest=new DeleteIndexRequest("user");
        AcknowledgedResponse delete = esClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        //响应状态
        boolean acknowledged = delete.isAcknowledged();//true或false

        //################################# 创建文档（新增数据） ###########################################
        IndexRequest indexRequest=new IndexRequest();
        indexRequest.index("user").id("1001");
        User user=new User("test1","test2","test3");
        //像es插入数据，必须将数据转换成json格式
        ObjectMapper mapper=new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        indexRequest.source(userJson, XContentType.JSON);
        IndexResponse index = esClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index.getResult());

        //################################# 更新文档（修改数据） ###########################################
        UpdateRequest updateRequest=new UpdateRequest();
        updateRequest.index("user").id("1001");
        updateRequest.doc(XContentType.JSON,"sex","女");
        UpdateResponse update = esClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update.getResult());

        //################################# 查询文档（根据文档id查询数据） ###########################################
        GetRequest getRequest=new GetRequest();
        getRequest.index("user").id("1001");
        GetResponse documentFields = esClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(documentFields.getSourceAsString());
        System.out.println(documentFields.getField("message"));

        //################################# 删除文档（根据文档id删除数据） ###########################################
        DeleteRequest deleteRequest=new DeleteRequest();
        deleteRequest.index("user").id("1001");
        DeleteResponse delete1 = esClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(delete1.getResult());

        //################################# 批量创建文档 ###########################################
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.add(new IndexRequest().index("user").id("1001").source(XContentType.JSON,"name","zhangsan"));
        bulkRequest.add(new IndexRequest().index("user").id("1002").source(XContentType.JSON,"name","lisi"));
        bulkRequest.add(new IndexRequest().index("user").id("1003").source(XContentType.JSON,"name","wangwu"));
        BulkResponse bulk = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.getTook());
        System.out.println(bulk.getItems());

        //################################# 批量删除文档 ###########################################
        BulkRequest deleteRequests=new BulkRequest();
        deleteRequests.add(new DeleteRequest().index("user").id("1001"));
        deleteRequests.add(new DeleteRequest().index("user").id("1002"));
        deleteRequests.add(new DeleteRequest().index("user").id("1003"));
        BulkResponse bulk1 = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk1.getTook());
        System.out.println(bulk1.getItems());

        //################################# 查询索引中全部的数据 ###########################################
        SearchRequest searchRequest=new SearchRequest();
        searchRequest.indices("user");//索引名
        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
        /**
         * 查询分页
         */
        builder.from(0);
        builder.size(10);
        /**
         * 排序
         */
        builder.sort("age", SortOrder.DESC);
        /**
         * 过滤字段
         */
        String[] excludes={};
        String[] includes={"name"};
        builder.fetchSource(includes,excludes);

        searchRequest.source(builder);
        SearchResponse response1 = esClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = response1.getHits();
        System.out.println(hits.getTotalHits());//总条数
        System.out.println(response1.getTook());//查询时间
        for(SearchHit hit:hits){
            System.out.println(hit.getSourceAsString());
        }

        //################################# 条件查询 ###########################################
        //条件查询（精确条件）
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("age","30")));


        //#################################### 多条件查询 #######################################
        /**
         * ........
         * SearchSourceBuilder builders = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
         *         BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
         *         boolQueryBuilder.must(QueryBuilders.matchQuery("age",30));
         *         boolQueryBuilder.mustNot(QueryBuilders.matchQuery("age",40));
         *         boolQueryBuilder.should(QueryBuilders.matchQuery("sex","男"));
         *         builder.query(boolQueryBuilder);
         *
         * ........
         */
        //#################################### 范围查询 #######################################
        /**
         * .......
         * SearchSourceBuilder builders = new SearchSourceBuilder();
         *         RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age");
         *         rangeQuery.gte(30);//大于等于
         *         rangeQuery.lte(50);//小于等于
         *         builders.query(rangeQuery);
         * .......
         */

        //#################################### 模糊查询 #######################################
        /**
         *  SearchSourceBuilder builders = new SearchSourceBuilder();
         *         builders.query(QueryBuilders.fuzzyQuery("name","wangwu").fuzziness(Fuzziness.ONE));
         *
         */

        //#################################### 查询结果高亮显示 #######################################
        SearchSourceBuilder builders = new SearchSourceBuilder();
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("name", "wangwu");
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.field("name");
        builders.highlighter(highlightBuilder);
        builders.query(termsQueryBuilder);

        //#################################### 关闭es客户端 #######################################
        esClient.close();
    }
}
