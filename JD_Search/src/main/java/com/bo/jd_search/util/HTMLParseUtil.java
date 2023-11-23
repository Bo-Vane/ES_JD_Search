package com.bo.jd_search.util;

import com.alibaba.fastjson.JSON;
import com.bo.jd_search.pojo.Content;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HTMLParseUtil {

    @Autowired
    RestHighLevelClient client;

    public static void main(String[] args) throws IOException {
        System.out.println(parseJD("java"));
    }

    //将爬取的数据放入索引
    public boolean putIntoIndex(String keyword, String index) throws IOException {

        List<Content> contents = parseJD(keyword);

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");

        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest(index)
                                .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        //add完之后批量上传
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        //查看打入index是否成功
        return !bulkResponse.hasFailures();
    }


    public static List<Content> parseJD(String keyword) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        //解析网页(jsoup 解析返回的对象是浏览器Document对象)
        Document document = Jsoup.parse(new URL(url), 30000);
        // 使用document可以使用在js对document的所有操作
        // 2.获取元素（通过id）
        Element j_goodsList = document.getElementById("J_goodsList");
        // 3.获取J_goodsList ul 每一个 li
        Elements lis = j_goodsList.getElementsByTag("li");

        List<Content> contents = new ArrayList<Content>();

        // 4.获取li下的 img、price、name
        for (Element li : lis) {
            String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");// 获取li下 第0张图片的src
            String name = li.getElementsByClass("p-name").eq(0).text();
            String price = li.getElementsByClass("p-price").eq(0).text();
            Content content = new Content(name, img, price);
            contents.add(content);

        }

        return contents;
    }

}
