package cn.ac.iscas.service;

import cn.ac.iscas.entity.po.BaseDatabasePO;
import cn.ac.iscas.entity.vo.chart.Chart;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.util.ObjectBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author WTF
 * @date 2022/8/15 9:03
 */
public interface ElasticsearchService<T> {

    boolean insert(String indexName, T tDocument);

    boolean insert(List<T> tDocumentList);

    boolean putMapping();

    <T extends BaseDatabasePO> T search(String indexName, String id, Class<T> tClass);

    boolean delete(String indexName,String id);

    boolean insertSync(String indexName, T tDocument);

    boolean insertSync(String indexName, List<T> tDocuments);

    boolean insert(String indexName, List<T> tDocuments);

    boolean delete(String indexName);

    <T extends BaseDatabasePO> List<T> search(Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> fn, Class<T> tClass);

    long size(String indexName);

    long size(String indexName, LocalDateTime beginTime, LocalDateTime endTime);

    long statusSize(String indexName, String status, LocalDateTime beginTime, LocalDateTime endTime);

    long statusesSize(String indexName, List<String> statuses, LocalDateTime beginTime, LocalDateTime endTime);

    Chart chart(String index, LocalDateTime beginTime, LocalDateTime endTime, String dateField1, String field2);

    boolean exist(String indexName);
}
