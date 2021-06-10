package com.dayouzc.efk.logcenter.Controller;

import com.alibaba.fastjson.JSONArray;

import com.alibaba.fastjson.JSONObject;
import com.dayouzc.efk.logcenter.constant.APPEnums;
import com.dayouzc.efk.logcenter.constant.JsonObject;
import com.dayouzc.efk.logcenter.service.EsSearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;


/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName SearchController.java
 * @Description TODO 检索日志接口
 * @createTime 2021年06月08日 15:31:00
 */
@RestController
@RequestMapping("/search")
public class SearchController {
    static Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    EsSearchService searchService;

    public String transfterDate(String time){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String format="";
        try {
            Date parse = sdf.parse(time);
             format = sdf.format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return format;
    }

    /**
     * 查询全部日志
     * 根据索引、开始时间、结束时间查询
     */
    @PostMapping("/searchAll")
    public JsonObject<JSONArray> searchLog(String indexName,String beginTime,String endTime) throws IOException {
        Assert.notNull(indexName,"索引不可为空");
        if(beginTime!=null && !"".equals(beginTime)){
            return searchService.searchLog(indexName,transfterDate(beginTime),transfterDate(endTime));
        }
        return searchService.searchLog(indexName,null,null);
    }

    /**
     * 根据日志id（文档id）和索引id 查询日志详情（文档详情）
     */
    @GetMapping("/getLog")
    public JsonObject<String> getLog(String indexName,String docId) throws IOException {
        Assert.notNull(indexName,"索引不可为空");
        Assert.notNull(docId,"文档id不可为空");
        return searchService.getLog(indexName,docId);
    }

    /**
     * 查询存在有效的索引列表
     */
    @GetMapping("/getAllIndexs")
    public JsonObject<Set<String>> getAllIndexs(){
        Set<String> allIndexs = searchService.getAllIndexs();
        if(allIndexs!=null){
            return new JsonObject(allIndexs);
        }else{
            return JsonObject.Error();
        }
    }



}
