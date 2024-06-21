package cn.ac.iscas.service.impl;

import cn.ac.iscas.constant.EsIndex;
import cn.ac.iscas.entity.TeleStatus;
import cn.ac.iscas.entity.po.BaseDatabasePO;
import cn.ac.iscas.entity.task.log.JhscLog;
import cn.ac.iscas.entity.vo.chart.Chart;
import cn.ac.iscas.entity.vo.chart.ChartItem;
import cn.ac.iscas.service.ElasticsearchService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.ObjectBuilder;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author WTF
 * @date 2022/8/15 9:03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchServiceImpl<T> implements ElasticsearchService<T> {

    private final ElasticsearchClient elasticsearchClient;

    @SneakyThrows
    @Override
    public boolean insert(String indexName, T tDocument) {

        IndexResponse index = elasticsearchClient.index(i -> i
                .index(indexName)
                .document(tDocument));
        log.info("ES insert,[result={}][index={}][{}]", index.result(), indexName, tDocument);
        return index.result().equals(Result.Created) || index.result().equals(Result.Updated);
    }

    @Override
    public boolean insert(List tDocumentList) {
        return false;
    }

    @Override
    public boolean putMapping() {
        return false;
    }

    @SneakyThrows
    @Override
    public <T extends BaseDatabasePO> T search(String indexName, String id, Class<T> tClass) {
        SearchResponse<T> response = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .size(1)
                        .query(q -> q.ids(i -> i.values(id)))
                , tClass);

        Hit<T> tHit = response.hits().hits().get(0);
        T t = tClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(tHit.source(), t);
        t.setId(tHit.id());
        return t;
    }

    @SneakyThrows
    @Override
    public boolean delete(String indexName, String id) {
        DeleteResponse delete = elasticsearchClient.delete(d -> d.index(indexName).id(id));
        return delete.id().equals(id);
    }

    @Override
    @SneakyThrows
    public boolean insertSync(String indexName, T tDocument) {

        IndexResponse index = elasticsearchClient.index(i -> i
                .index(indexName)
                .document(tDocument).refresh(Refresh.True));
        log.info("ES insert,[result={}][index={}]", index.result(), indexName);
        return index.result().equals(Result.Created) || index.result().equals(Result.Updated);
    }

    @Override
    @SneakyThrows
    public boolean insertSync(String indexName, List<T> tDocuments) {

        if (CollectionUtils.isEmpty(tDocuments)) {
            return true;
        }

        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        tDocuments.forEach(x -> bulkRequest.operations(op -> op
                .index(i -> i
                        .index(indexName)
                        .document(x)
                )).refresh(Refresh.True));

        BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
        return !response.errors();
    }

    @Override
    @SneakyThrows
    public boolean insert(String indexName, List<T> tDocuments) {

        if (CollectionUtils.isEmpty(tDocuments)) {
            return true;
        }

        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        tDocuments.forEach(x -> bulkRequest.operations(op -> op
                .index(i -> i
                        .index(indexName)
                        .document(x)
                )).refresh(Refresh.False));

        BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
        return !response.errors();
    }

    @Override
    public boolean delete(String indexName) {
        try {
            elasticsearchClient.indices().delete(d -> d.index(indexName));
        } catch (Exception e) {

        }
        return true;
    }

    @SneakyThrows
    @Override
    public <T extends BaseDatabasePO> List<T> search(Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> fn, Class<T> tClass) {
        SearchResponse<T> response = elasticsearchClient.search(fn
                , tClass);

        List<T> list = new ArrayList<>();
        if (null == response || null == response.hits()) {
            return list;
        }

        List<Hit<T>> hits = response.hits().hits();
        for (Hit<T> hit : hits) {
            T t = tClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(hit.source(), t);
            list.add(t);
            t.setId(hit.id());
        }
        return list;
    }

    @Override
    public long size(String indexName) {
        try {
            return elasticsearchClient.count(c -> c.index(indexName)).count();
        } catch (IOException e) {
            log.error("ES get size error,[index={}],{}", indexName, e);
            return 0L;
        }
    }

    @Override
    public long size(String indexName, LocalDateTime beginTime, LocalDateTime endTime) {
        try {
            return elasticsearchClient.count(c -> c
                    .index(indexName)
                    .query(q -> q
                            .bool(b -> {
                                if (!ObjectUtils.isEmpty(beginTime) || !ObjectUtils.isEmpty(endTime)) {
                                    b.must(m -> m
                                            .range(r -> {
                                                r.field("createTime");
                                                if (!ObjectUtils.isEmpty(beginTime)) {
                                                    r.gte(JsonData.of(beginTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()));
                                                }
                                                if (!ObjectUtils.isEmpty(endTime)) {
                                                    r.lte(JsonData.of(endTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()));
                                                }
                                                return r;
                                            })
                                    );
                                }
                                return b;
                            })
                    )
            ).count();
        } catch (IOException e) {
            log.error("ES get size error,[index={}],{}", indexName, e);
            return 0L;
        }
    }

    @Override
    public long statusSize(String indexName, String status, LocalDateTime beginTime, LocalDateTime endTime) {
        try {
            return elasticsearchClient.count(c -> c
                    .index(indexName)
                    .query(q -> q
                            .bool(b -> {
                                if (!ObjectUtils.isEmpty(beginTime) || !ObjectUtils.isEmpty(endTime)) {
                                    b.must(m -> m
                                            .range(r -> {
                                                r.field("createTime");
                                                if (!ObjectUtils.isEmpty(beginTime)) {
                                                    r.gte(JsonData.of(beginTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()));
                                                }
                                                if (!ObjectUtils.isEmpty(endTime)) {
                                                    r.lte(JsonData.of(endTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()));
                                                }
                                                return r;
                                            })
                                    );
                                }

                                if (!StringUtils.isEmpty(status)) {
                                    b.must(m -> m
                                            .term(t -> t
                                                    .field("status")
                                                    .value(status)
                                            )
                                    );
                                }
                                return b;
                            })
                    )
            ).count();
        } catch (IOException e) {
            log.error("ES get size error,[index={}],{}", indexName, e);
            return 0L;
        }
    }

    @Override
    public long statusesSize(String indexName, List<String> statuses, LocalDateTime beginTime, LocalDateTime endTime) {

        List<FieldValue> statusFields = Optional.ofNullable(statuses)
                .orElse(Collections.emptyList()).stream().map(FieldValue::of).collect(Collectors.toList());

        try {
            return elasticsearchClient.count(c -> c
                    .index(indexName)
                    .query(q -> q
                            .bool(b -> {
                                if (!ObjectUtils.isEmpty(beginTime) || !ObjectUtils.isEmpty(endTime)) {
                                    b.must(m -> m
                                            .range(r -> {
                                                r.field("createTime");
                                                if (!ObjectUtils.isEmpty(beginTime)) {
                                                    r.gte(JsonData.of(beginTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()));
                                                }
                                                if (!ObjectUtils.isEmpty(endTime)) {
                                                    r.lte(JsonData.of(endTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()));
                                                }
                                                return r;
                                            })
                                    );
                                }

                                if (!CollectionUtils.isEmpty(statusFields)) {
                                    b.must(m -> m
                                            .terms(t -> t
                                                    .field("status").terms(tt -> tt.value(statusFields))
                                            )
                                    );
                                }
                                return b;
                            })
                    )
            ).count();
        } catch (IOException e) {
            log.error("ES get size error,[index={}],{}", indexName, e);
            return 0L;
        }
    }

    /**
     * 图表展示
     * 两层聚合
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    @SneakyThrows
    @Override
    public Chart chart(String index, LocalDateTime beginTime, LocalDateTime endTime, String dateField1, String aggfield1) {

        SearchResponse<Object> search = elasticsearchClient.search(s -> s
                        .index(index)
                        .query(q -> q
                                .bool(b -> {
                                            if (!org.apache.commons.lang3.ObjectUtils.isEmpty(beginTime)) {
                                                b.must(m -> m.range(t -> t.field("createTime").gte(JsonData.of(beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))));
                                            }
                                            if (!org.apache.commons.lang3.ObjectUtils.isEmpty(endTime)) {
                                                b.must(m -> m.range(t -> t.field("createTime").lte(JsonData.of(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))));
                                            }
                                            return b;
                                        }
                                )
                        )
                        .size(0)
                        .aggregations("timeAgg", a -> a
                                .dateHistogram(d -> d
                                        .field(dateField1)
                                        .calendarInterval(CalendarInterval.Month)
                                        .format("YYYY-MM")
                                        .minDocCount(1)
                                )
                                .aggregations("agg", aa -> aa.terms(t -> t.field(aggfield1).size(Integer.MAX_VALUE)))
                        )
                , Object.class);
        List<DateHistogramBucket> timeAgg = search.aggregations().get("timeAgg").dateHistogram().buckets().array();
//        List<Map<String, Map<String, Integer>>> list = new ArrayList<>();

        Chart chart = new Chart();
        List<String> labels = new ArrayList<>();
        chart.setLabels(labels);
        List<String> values = new ArrayList<>();
        ChartItem chartItem = new ChartItem();
        chart.setData(Lists.newArrayList(chartItem));
        chartItem.setChartName("total");
        chartItem.setValues(values);

        for (DateHistogramBucket bucket : timeAgg) {

            labels.add(bucket.keyAsString());

//            Map<String, Map<String, Integer>> timeMap = new HashMap<>();

            if (bucket.aggregations().get("agg").isLterms()) {
                List<LongTermsBucket> agg2 = bucket.aggregations().get("agg").lterms().buckets().array();
                values.add(agg2.size() + "");
            } else if (bucket.aggregations().get("agg").isSterms()) {
                values.add(bucket.docCount() + "");
            }

//            Map<String, Integer> map = new HashMap<>();
//            map.put("total", agg2.size());
//            timeMap.put(String.valueOf(bucket.keyAsString()), map);
//            list.add(timeMap);
        }

        return chart;
    }

    @SneakyThrows
    @Override
    public boolean exist(String indexName) {
        BooleanResponse exists = elasticsearchClient.indices().exists(e -> e.index(indexName));
        return exists.value();
    }

}
