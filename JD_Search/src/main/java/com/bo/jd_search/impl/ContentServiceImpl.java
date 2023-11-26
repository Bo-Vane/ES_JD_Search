package com.bo.jd_search.impl;


import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.bo.jd_search.service.ContentService;
import com.bo.jd_search.util.HTMLParseUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class ContentServiceImpl implements ContentService {

    private final HTMLParseUtil htmlParseUtil;

    private final RestHighLevelClient client;

    public ContentServiceImpl(HTMLParseUtil htmlParseUtil, RestHighLevelClient client) {
        this.htmlParseUtil = htmlParseUtil;
        this.client = client;
    }


    @Override
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo <= 1){
            pageNo = 1;
        }

        //条件搜索
        SearchRequest request = new SearchRequest("jd_goods");
        // 创建搜索源建造者对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        // 条件采用：精确查询 通过keyword查字段name,注意这个QueryBuilders的类型，不能用spring的那个
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", keyword);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));// 60s

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.requireFieldMatch(false);//关闭多个高亮，只高亮一个关键词就行了
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        //解析结果,都在hits里
        for (SearchHit hit : response.getHits().getHits()) {
            list.add(hit.getSourceAsMap());
        }

        return list;
    }

    @Override
    public boolean parseContent(String keyword) throws Exception {
        return htmlParseUtil.putIntoIndex(keyword,"jd_goods");
    }


    public List<Map<String, Object>> searchPageHighlight(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo <= 1){
            pageNo = 1;
        }

        //条件搜索
        SearchRequest request = new SearchRequest("jd_goods");
        // 创建搜索源建造者对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        // 条件采用：精确查询 通过keyword查字段name,注意这个QueryBuilders的类型，不能用spring的那个
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", keyword);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));// 60s

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.requireFieldMatch(false);//关闭多个高亮，只高亮一个关键词就行了
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        request.source(searchSourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        //解析结果,都在hits里
        for (SearchHit hit : response.getHits().getHits()) {
            //解析高亮的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();//这是原来的结果我们要把它置换成高亮的
            if (name != null){
                String highLightName = "";
                for (Text fragment : name.fragments()) {
                    highLightName += fragment;//取出高亮的字段赋值到临时变量highLightName
                }
                sourceAsMap.put("name", highLightName);//替换原来的字段
            }
            list.add(sourceAsMap);
        }

        return list;
    }




}
