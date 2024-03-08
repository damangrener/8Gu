# 0、前置条件

## 0.1、ES查询结果转换
```java
    /**
     * 查询结果转换
     *
     * @param searchResponse 返回结果
     * @return List<Object>
     */
    public static List<Map<String, Object>> searchResponse2List(SearchResponse<ObjectNode> searchResponse) {

        if (searchResponse == null) {return new ArrayList<>(0);}
        if (searchResponse.hits() == null) {return new ArrayList<>(0);}
        if (CommonUtils.isCollectionEmpty(searchResponse.hits().hits())) {return new ArrayList<>(0);}

        List<Hit<ObjectNode>> hits = searchResponse.hits().hits();

        List<Map<String, Object>> list = new ArrayList<>(hits.size());
        for (Hit<ObjectNode> hit : hits) {
            ObjectNode node = hit.source();
            Map<String, Object> map = this.objectNode2Map(node);
            list.add(map);
        }
        return list;
    }

    /**
     * objectNode转Map
     *
     * @return Map<String, Object>
     */
    public static Map<String, Object> objectNode2Map(ObjectNode objectNode) {
        if (null == objectNode) {return new HashMap<>(0);}
        if (objectNode.isEmpty()) {return new HashMap<>(0);}
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(objectNode, new TypeReference<Map<String, Object>>() {
        });
    }
```
## 0.3、查询请求
```java
SearchRequest.Builder searchRequest = new SearchRequest.Builder();
searchRequest.query(q -> q.bool(b -> b.must(${query})));

SearchResponse<ObjectNode> response;
response = client.search(searchRequest.trackTotalHits(t->t.enabled(true)).build(), ObjectNode.class);
```


# 1、类型

## 1.1、 [matchAll](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-match-all-query.html)

**Java Client**
```java
 searchRequest.query(q -> q.matchAll(m -> m));
```
 **Rest Api**
```json
{
 "query": {
  "match_all": {}
 }
}   
```

## 1.2、 [match](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-match-query.html)
**Java Client**
```java
Query query =
        Query.of(q -> q.
                match(m -> m
                        .field(${field})
                        .query(FieldValue.of(${value}))
                )
        );
```
**Rest Api**
```json
{
 "query": {
  "match": {
   "message": {
    "query": "this is a test"
   }
  }
 }
}
```  

## 1.2、 [terms](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-terms-query.html)  词查询,多个值
**Java Client**
```java
List<FieldValue> terms = new ArrayList<>();
terms.add(FieldValue.of(${value1}));
terms.add(FieldValue.of(${value2}));

Query query =
        Query.of(q -> q.
                terms(t -> t
                        .field(${field})
                        .terms(tt -> tt.value(terms))
                )
        );
```
**Rest Api**
```json
{
  "query": {
    "terms": {
      "${field}": [ "${value1}", "${value2}" ],
      "boost": 1.0
    }
  }
}
```  
## 1.3、 [range](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-range-query.html)     范围
**Java Client**
```java
Query query =
        Query.of(q -> q
                .range(r -> {
                            r.field(${field});
                            if (${true}) {
                                r.gte(${value1});
                            } else {
                                r.gt(${value1});
                            }
                            if (${true}) {
                                r.lte(${value2});
                            } else {
                                r.lt(${value2});
                            }
                            return r;
                        }
                )
        );
```
**Rest Api**
```json
{
  "query": {
    "range": {
      "age": {
        "gte": 10,
        "lte": 20,
        "boost": 2.0
      }
    }
  }
}
```  
## 1.4、 [wildcard](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-wildcard-query.html)  通配符
+ `?`用来匹配任意字符，
+ `*`用来匹配零个或者多个字符。  
**Java Client**
```java
//?用来匹配任意字符，*用来匹配零个或者多个字符。
String value = condition.getValue().replace("_", "?");
value = value.replace("%", "*");

String finalValue = value;
Query query =
        Query.of(q -> q
                .wildcard(t -> t
                        .field(${field})
                        .value(${finalValue})
                )
        );
```
**Rest Api**
```json
{
  "query": {
    "wildcard": {
      "${field}": {
        "value": "${value}",
        "boost": 1.0,
        "rewrite": "constant_score"
      }
    }
  }
}
```  
## 1.5、 [prefix](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-prefix-query.html)    前缀
**Java Client**
```java
Query query =
        Query.of(q -> q
                .prefix(t -> t
                        .field(${field})
                        .value(${value})
                )
        );
```
**Rest Api**
```json
{
  "query": {
    "prefix": {
      "${field}": {
        "value": "${value}"
      }
    }
  }
}
```  
## 1.6、 [geo_distance](https://www.elastic.co/guide/en/elasticsearch/reference/8.1/query-dsl-geo-distance-query.html)    地理距离查询
**Java Client**
```java
        Query query =
                Query.of(q -> q
                        .geoDistance(t -> t
                                .field(${field})
                                .location(l -> l
                                        .latlon(ll -> ll
                                                .lat(${lat})
                                                .lon(${lon})
                                        )
                                )
                                .distance(String.valueOf(${distance}))
                        )
                );
```

```json
{
  "query": {
    "bool": {
      "must": {
        "match_all": {}
      },
      "filter": {
        "geo_distance": {
          "distance": "200km",
          "pin.location": {
            "lat": 40,
            "lon": -70
          }
        }
      }
    }
  }
}
```

## 1.5、 regexp    正则
**Java Client**
```java
Query query =
        Query.of(q -> q
                .regexp(t -> t
                        .field(${field})
                        .value(${value})
                )
        );
```
**Rest Api**
```json
{
  "query": {
    "regexp": {
      "user.id": {
        "value": "k.*y",
        "flags": "ALL",
        "case_insensitive": true,
        "max_determinized_states": 10000,
        "rewrite": "constant_score"
      }
    }
  }
}
```  

