package org.apache.dolphinscheduler.zt.service.es;

import java.util.List;

/**
 * @author WTF
 * @date 2022/8/15 9:03
 */
public interface ElasticsearchService<T> {

    boolean insert(String indexName, T tDocument);

    boolean insertSync(String indexName, T tDocument);

    boolean insertSync(String indexName, List<T> tDocuments);

    boolean insert(String indexName, List<T> tDocuments);

    boolean insert(String indexName,String id, T tDocument);

    boolean insert(List<T> tDocumentList);

    boolean putMapping();

    Long updateStatusByEsIds(String indexName,List<String> ids,int newStatus);

    Long updateStatusNextTaskIdByEsIds(String indexName,List<String> ids,String newStatus,String nextTakId);

    boolean exist(String indexName);

//    <T extends BaseDatabasePO> List<T> listByStatus(String index, String status, int size, Class<T> tClass);
//
//    List<SingleObjectData> listStructByStatus(String index, String status, int size, Class<SingleObjectData> tClass);


    ;
}
