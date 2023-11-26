package com.bo.jd_search.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ContentService {
    //获取ES中的数据实现搜索功能
    List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException;

    //将解析数据结果放入ES
    boolean parseContent(String keyword) throws Exception;

    //高亮结果字段
    List<Map<String, Object>> searchPageHighlight(String keyword, int pageNo, int pageSize) throws IOException;

}
