package com.dayouzc.efk.logcenter.config;

import com.dayouzc.efk.logcenter.Controller.SearchController;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName ClientPool.java
 * @Description TODO es连接池
 * @createTime 2021年06月08日 17:47:00
 */
public class ClientPool {

     Logger logger = LoggerFactory.getLogger(ClientPool.class);


    @Value("${es.ip}")
    private String ip;
    @Value("${es.port}")
    private Integer port;
    @Value("${es.type}")
    private String type;

    /**
     * 空闲连接池
     */
    private LinkedBlockingQueue<RestHighLevelClient> idleConnectPool=new LinkedBlockingQueue<>();

    /**
     * 活跃连接池
     */
    private LinkedBlockingQueue<RestHighLevelClient> busyConnectPool=new LinkedBlockingQueue<>();

    /**
     * 当前正在被使用的连接数
     */
    private AtomicInteger activeSize = new AtomicInteger(0);

    /**
     * 最大连接数
     */
     private static int maxSize = 10;


    /**
     * 获取连接
     */
    public RestHighLevelClient getConnection(){
        //从idle池取出一个连接
        RestHighLevelClient poll = idleConnectPool.poll();
        if(poll!=null){
            //如果有连接，则放入busy池中
            busyConnectPool.offer(poll);
            return poll;
        }
        //如果未达到最大连接数
        if(activeSize.get()<maxSize){
            if(activeSize.incrementAndGet()<=maxSize){
                //创建一个新的连接
                RestHighLevelClient client=new RestHighLevelClient(RestClient.builder(new HttpHost(ip,port,type)));
                busyConnectPool.offer(client);
                return client;
            }
        }

        // 如果空闲池中连接数达到maxSize， 则阻塞等待归还连接
        try {
            logger.info("排队等待连接");
            poll = idleConnectPool.poll(10000, TimeUnit.MILLISECONDS);// 阻塞获取连接，如果10秒内有其他连接释放，
            if (poll == null) {
                logger.info("等待超时");
                throw new RuntimeException("等待连接超时");
            }
            logger.info("等待到了一个连接");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return poll;

    }

    /**
     * 释放一个连接
     */
    public void releaseConnection(RestHighLevelClient client){
        if(client!=null){
            busyConnectPool.remove(client);
            idleConnectPool.offer(client);
        }
    }

    /**
     * 定时对连接进行健康检查
     * 注意：只能对idle连接池中的连接进行健康检查，
     * 不可以对busyConnectPool连接池中的连接进行健康检查，因为它正在被客户端使用;
     */
    @Scheduled(cron = "0/60 * * * * *") //每一分钟执行一次
    public void check() throws IOException {
        logger.info("例行健康检查");
        for (int i = 0; i < activeSize.get(); i++) {
            RestHighLevelClient connection = idleConnectPool.poll();
                boolean valid=isOk(connection);//校验连接是否可用，这里暂时写死为true
                if (!valid) {
                    // 如果连接不可用，则创建一个新的连接
                     connection=new RestHighLevelClient(RestClient.builder(new HttpHost(ip,port,type)));
                }
                idleConnectPool.offer(connection);// 放进一个可用的连接
        }
    }

    /**
     * 检查某个连接是否可用
     */
    public boolean isOk(RestHighLevelClient client) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest();
        try {
            GetAliasesResponse alias = client.indices().getAlias(request, RequestOptions.DEFAULT);
            return true;
        } catch (RuntimeException e) {
            return false;
        }

    }


}
