package org.apache.dolphinscheduler.zt.service.es.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.zt.service.es.ElasticsearchService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author WTF
 * @date 2022/8/15 9:03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchServiceImpl<T> implements ElasticsearchService<T> {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    @SneakyThrows
    public boolean insert(String indexName, T tDocument) {

        if (ObjectUtils.isEmpty(tDocument)) {
            return true;
        }

        IndexResponse index = elasticsearchClient.index(i -> i
                .index(indexName)
                .document(tDocument));
        log.debug("ES insert,[result={}][index={}][{}]", index.result(), indexName);
        return index.result().equals(Result.Created) || index.result().equals(Result.Updated);
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
                )));

        BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
        return !response.errors();
    }

    @Override
    @SneakyThrows
    public boolean insert(String indexName, String id, T tDocument) {
        IndexResponse index = elasticsearchClient.index(i -> i
                .index(indexName)
                .id(id)
                .document(tDocument));
        log.debug("ES insert,[result={}][index={}][{}]", index.result(), indexName, tDocument);
        return index.result().equals(Result.Created) || index.result().equals(Result.Updated);
    }

    @Override
    public boolean insert(List tDocumentList) {
        return false;
    }

    @Override
    @SneakyThrows
    public boolean insertSync(String indexName, T tDocument) {

        IndexResponse index = elasticsearchClient.index(i -> i
                .index(indexName)
                .document(tDocument).refresh(Refresh.True));
        log.debug("ES insert,[result={}][index={}][{}]", index.result(), indexName);
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
    public boolean putMapping() {
        return false;
    }

    @SneakyThrows
    @Override
    public Long updateStatusByEsIds(String indexName, List<String> ids, int newStatus) {

        String scripts = "ctx._source['status']={newStatus}";
        final String finalScripts = scripts.replace("{newStatus}", String.valueOf(newStatus));

        //failed to create query: maxClauseCount is set to 1024
        List<List<String>> partition = Lists.partition(ids, 1024);

        long result = 0;

        for (List<String> list : partition) {

            UpdateByQueryResponse response = elasticsearchClient.updateByQuery(u -> u
                    .index(indexName)
                    .query(q -> q
                            .ids(i -> i.values(list)))
                    .script(Script.of(s -> s.inline(i -> i.lang(ScriptLanguage.Painless).source(finalScripts))))
            );
            result += response.updated();
        }

        return result;
    }

    @Override
    public Long updateStatusNextTaskIdByEsIds(String indexName, List<String> ids, String newStatus, String nextTakId) {

        String scripts = "ctx._source['status']='{newStatus}';ctx._source['nextTaskId']='{nextTaskId}'";
        final String finalScripts = scripts.replace("{newStatus}", String.valueOf(newStatus)).replace("{nextTaskId}", nextTakId);

        //failed to create query: maxClauseCount is set to 1024
        List<List<String>> partition = Lists.partition(ids, 1024);

        long result = 0;

        for (List<String> list : partition) {
            result += updateByQuery(indexName, list, finalScripts);
        }

        return result;
    }

    @SneakyThrows
    @Override
    public boolean exist(String indexName) {
        BooleanResponse exists = elasticsearchClient.indices().exists(e -> e.index(indexName));
        return exists.value();
    }

    @SneakyThrows
    private Long updateByQuery(String indexName, List<String> ids, String scripts) {

        UpdateByQueryResponse response = elasticsearchClient.updateByQuery(u -> u
                .conflicts(Conflicts.Proceed).refresh(true)
                .index(indexName)
                .query(q -> q
                        .ids(i -> i.values(ids)))
                .script(Script.of(s -> s.inline(i -> i.lang(ScriptLanguage.Painless).source(scripts))))
        );
        return response.updated();
    }

//    @Override
//    public <T extends BaseDatabasePO> List<T> listByStatus(String index, String status, int size, Class<T> tClass) {
//
//        try {
//            SearchResponse<T> searchResponse = elasticsearchClient.search(s -> s
//                            .index(index)
//                            .size(size)
//                            .query(q -> q
//                                    .bool(b -> b
//                                            .filter(f -> f
//                                                    .terms(t -> t
//                                                            .field("status")
//                                                            .terms(tt -> tt.value(List.of(FieldValue.of(status))))))))
//                    , tClass);
//            List<Hit<T>> hits = searchResponse.hits().hits();
//            if (null == searchResponse.hits() || CollectionUtils.isEmpty(searchResponse.hits().hits())) {
//                return new ArrayList<>(0);
//            }
//            List<T> result = new ArrayList<>(searchResponse.hits().hits().size());
//            for (Hit<T> hit : hits) {
//                T t = hit.source();
//                t.setId(hit.id());
//                result.add(t);
//            }
//            return result;
//        } catch (IOException e) {
//            log.error("ES search error,{}", e);
//        }
//        return new ArrayList<>(0);
//    }
//
//    @Override
//    public List<SingleObjectData> listStructByStatus(String index, String status, int size, Class<SingleObjectData> tClass) {
//
//        try {
//            SearchResponse<SingleObjectData> searchResponse = elasticsearchClient.search(s -> s
//                            .index(index)
//                            .size(size)
//                            .query(q -> q
//                                    .bool(b -> b
//                                            .filter(f -> f
//                                                    .terms(t -> t
//                                                            .field("status")
//                                                            .terms(tt -> tt.value(List.of(FieldValue.of(status))))))))
//                    , SingleObjectData.class);
//            List<Hit<SingleObjectData>> hits = searchResponse.hits().hits();
//            if (null == searchResponse.hits() || CollectionUtils.isEmpty(searchResponse.hits().hits())) {
//                return new ArrayList<>(0);
//            }
//            List<SingleObjectData> result = new ArrayList<>(searchResponse.hits().hits().size());
//            for (Hit<SingleObjectData> hit : hits) {
//                SingleObjectData t = hit.source();
//                t.setesId(hit.id());
//                result.add(t);
//            }
//            return result;
//        } catch (IOException e) {
//            log.error("ES search error,{}", e);
//        }
//        return new ArrayList<>(0);
//    }





}
