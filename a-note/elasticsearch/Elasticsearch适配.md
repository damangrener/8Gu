# 1. 适配其他平台作为数据库
```java
    @SneakyThrows
private SearchResponse<Object> searchDataRequest2ES(String tableName, SearchDataRequest request) {

        ElasticsearchClient esClient = ESUtils.getClient("esClient");

        SearchRequest.Builder searchRequest = new SearchRequest.Builder();

        searchRequest.index(tableName);

        //分页
        searchRequest
        .from(request.getStart())
        .size(request.getSize());

        //需要返回的列
        if (!CollectionUtil.isEmpty(request.getColumns())) {
        SourceConfig.Builder sourceBuilder = new SourceConfig.Builder();
        sourceBuilder.filter(f -> f.includes(new ArrayList<>(request.getColumns())));
        searchRequest.source(sourceBuilder.build());
        }

        ISearchCondition searchCondition = request.getSearch();

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        this.ds2ES(searchCondition, boolBuilder, null);

        searchRequest.query(q -> q.bool(boolBuilder.build()));

        SearchResponse<Object> response = esClient.search(searchRequest.build(), Object.class);
        return response;

        }
/**
     * 请求转ES查询
     *
     * @param searchCondition
     * @param boolBuilder
     * @param esBool
     */
    private void ds2ES(ISearchCondition searchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {
        if (searchCondition instanceof BoolSearchCondition) {
            List<ISearchCondition> mustConditions = ((BoolSearchCondition) searchCondition).getMust();
            for (ISearchCondition mustCondition : mustConditions) {
                this.ds2ES(mustCondition, boolBuilder, EsBool.MUST);
            }
            List<ISearchCondition> mustNotConditions = ((BoolSearchCondition) searchCondition).getMust_not();
            for (ISearchCondition mustNotCondition : mustNotConditions) {
                this.ds2ES(mustNotCondition, boolBuilder, EsBool.MUST_NOT);
            }
            List<ISearchCondition> shouldConditions = ((BoolSearchCondition) searchCondition).getShould();
            for (ISearchCondition shouldCondition : shouldConditions) {
                this.ds2ES(shouldCondition, boolBuilder, EsBool.SHOULD);
            }

        } else if (searchCondition instanceof TermSearchCondition) {
            this.analyzeTermSearchCondition((TermSearchCondition) searchCondition, boolBuilder, esBool);
        } else if (searchCondition instanceof RangeSearchCondition) {
            this.analyzeRangeSearchCondition((RangeSearchCondition) searchCondition, boolBuilder, esBool);
        } else if (searchCondition instanceof WildcardSearchCondition) {
            this.analyzeWildcardSearchCondition((WildcardSearchCondition) searchCondition, boolBuilder, esBool);
        } else if (searchCondition instanceof GeoDistanceSearchCondition) {
            this.analyzeGeoDistanceSearchCondition((GeoDistanceSearchCondition) searchCondition, boolBuilder, esBool);
        } else if (searchCondition instanceof GeoGridSearchCondition) {

        } else if (searchCondition instanceof GeoSearchCondition) {

        } else if (searchCondition instanceof PrefixSearchCondition) {
            this.analyzePrefixSearchCondition((PrefixSearchCondition) searchCondition, boolBuilder, esBool);
        } else if (searchCondition instanceof RegexpSearchCondition) {
            this.analyzeRegexpSearchCondition((RegexpSearchCondition) searchCondition, boolBuilder, esBool);
        }
    }

    /**
     * 组装精确查询条件
     *
     * @param termSearchCondition
     * @param boolBuilder
     */
    private void analyzeTermSearchCondition(TermSearchCondition termSearchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {

        if (StringUtils.isEmpty(termSearchCondition.getColumn())) {
            return;
        }

        Query query =
                Query.of(q -> q.
                        term(t -> t
                                .field(termSearchCondition.getColumn())
                                .value(v -> v
                                        .stringValue(termSearchCondition.getValue().toString()
                                        )
                                ).caseInsensitive(true)
                        )
                );
        EsBool.bool(esBool, query, boolBuilder);
    }

    /**
     * 组装范围查询条件
     *
     * @param rangeSearchCondition
     * @param boolBuilder
     */
    private void analyzeRangeSearchCondition(RangeSearchCondition rangeSearchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {

        if (StringUtils.isEmpty(rangeSearchCondition.getColumn())) {
            return;
        }

        Query query =
                Query.of(q -> q
                                .range(r -> {
                                            r.field(rangeSearchCondition.getColumn());
                                            if (null != rangeSearchCondition.getFrom()) {
                                                r.from(rangeSearchCondition.getFrom().toString());
                                            }
                                            if (null != rangeSearchCondition.getTo()) {
                                                r.to(rangeSearchCondition.getTo().toString());
                                            }
                                            return r;
                                        }
                                )
                );
        EsBool.bool(esBool, query, boolBuilder);
    }

    /**
     * 组装模糊（通配符）查询条件
     *
     * @param wildcardSearchCondition
     * @param boolBuilder
     */
    private void analyzeWildcardSearchCondition(WildcardSearchCondition wildcardSearchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {

        if (StringUtils.isEmpty(wildcardSearchCondition.getColumn())) {
            return;
        }

        Query query =
                Query.of(q -> q
                        .wildcard(t -> t
                                .field(wildcardSearchCondition.getColumn())
                                .value(wildcardSearchCondition.getValue())
                        )
                );
        EsBool.bool(esBool, query, boolBuilder);
    }

    /**
     * 组装前缀匹配查询条件
     *
     * @param prefixSearchCondition
     * @param boolBuilder
     */
    private void analyzePrefixSearchCondition(PrefixSearchCondition prefixSearchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {

        if (StringUtils.isEmpty(prefixSearchCondition.getColumn())) {
            return;
        }

        Query query =
                Query.of(q -> q.
                        prefix(t -> t
                                .field(prefixSearchCondition.getColumn())
                                .value(prefixSearchCondition.getValue())
                        )
                );
        EsBool.bool(esBool, query, boolBuilder);
    }

    /**
     * 组装正则匹配查询条件
     *
     * @param regexpSearchCondition
     * @param boolBuilder
     */
    private void analyzeRegexpSearchCondition(RegexpSearchCondition regexpSearchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {

        if (StringUtils.isEmpty(regexpSearchCondition.getColumn())) {
            return;
        }

        Query query =
                Query.of(q -> q.
                        regexp(t -> t
                                .field(regexpSearchCondition.getColumn())
                                .value(regexpSearchCondition.getValue())
                        )
                );
        EsBool.bool(esBool, query, boolBuilder);
    }

    /**
     * 组装正则匹配查询条件
     *
     * @param geoDistanceSearchCondition
     * @param boolBuilder
     */
    private void analyzeGeoDistanceSearchCondition(GeoDistanceSearchCondition geoDistanceSearchCondition, BoolQuery.Builder boolBuilder, EsBool esBool) {

        if (StringUtils.isEmpty(geoDistanceSearchCondition.getColumn())) {
            return;
        }

        Query query =
                Query.of(q -> q.
                        geoDistance(t -> t

                                .location(l -> l
                                        .latlon(ll -> ll
                                                .lat(geoDistanceSearchCondition.getLat())
                                                .lon(geoDistanceSearchCondition.getLon())
                                        )
                                )
                                .distance(String.valueOf(geoDistanceSearchCondition.getDistance()))
                        )
                );
        EsBool.bool(esBool, query, boolBuilder);
    }
```