### 创建索引
```java
/**
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/indices-create-index.html
     */
    @Override
    public boolean createTable(String tableName, List<Column> columnList) throws Exception {
        if (StringUtils.isEmpty(tableName) || columnList == null
                || columnList.isEmpty()) {
            return false;
        }

        //1. 索引名
        String myTableName = ...;

        //2. 基础配置
        IndexSettings indexSettings =
                IndexSettings.of(
                        indexSetting -> indexSetting
                                .numberOfShards(config.getShardCount())
                                .numberOfReplicas(config.getReplicasCount())
                                .refreshInterval(interval -> interval.time(config.getRefreshInterval()))
                                .maxResultWindow(Integer.MAX_VALUE)
                                .translog(tl -> tl
                                        .durability(config.getTranslogDurability())
                                        .flushThresholdSize(config.getTranslogFlushThresholdSize())
                                )
                        //额外配置,暂时用不到
                                .withJson(config.getIndexExtraSettings())
                );

        try {
            //3. 创建索引
            CreateIndexRequest createIndexRequest =
                    CreateIndexRequest.of(c -> c
                            .index(myTableName)
                            .settings(indexSettings)
                    );

            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
            if (Boolean.FALSE.equals(createIndexResponse.acknowledged())) {
                throw new ;
            }


            try {
                //3. 更新mapping
                PutMappingRequest putMappingRequest = columns2Properties(myTableName, columnList);
                PutMappingResponse putMappingResponse = client.indices().putMapping(putMappingRequest);
                return putMappingResponse.acknowledged();
            } catch (Exception e) {
                client.indices().delete(d -> d.index(myTableName));
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
```

### 新增字段
```java
    @Override
    public boolean addColumn(String tableName, List<Column> columnList) throws Exception {
        //1. 索引名
        String myTableName = ...;
        if (this.isTableExist(myTableName)) {
            return false;
        }

        //2. 更新mapping，column转成PutMappingRequest
        PutMappingRequest putMappingRequest = columns2Properties(myTableName, columnList);

        try {
            PutMappingResponse putMappingResponse = client.indices().putMapping(putMappingRequest);
            return putMappingResponse.acknowledged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
```
### 表存在
```java
    /**
     * 校验表是否存在
     *
     * @param myTableName 表名
     * @return isTableExist
     */
    private boolean isTableExist(String myTableName) {
        try {
            BooleanResponse booleanResponse = client.existsSource(e -> e.index(myTableName));
            return booleanResponse.value();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
```
### 删除表
```java
    /**
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/indices-delete-index.html
     */
    @Override
    public boolean deleteTable(String tableName) throws Exception {
        String myTableName = ...;
        try {
            return client.indices().delete(d -> d.index(myTableName)).acknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
```

### 获取表的所有字段类型
```java
    /**
     * 获取表的所有字段类型
     */
    public Map<String, Property> getTableProperties(String tableName) throws Exception {

        String myTableName = ...;

        try {
            GetMappingResponse mapping = client.indices().getMapping(g -> g.index(myTableName));

            return mapping.get(myTableName).mappings().properties();
        } catch (IOException e) {
            throw new ;
        }

    }
```

### 
```java
    /**
     * 将字段参数转为es字段参数
     *
     * @param myTableName 表名
     * @param columnList  字段
     * @return PutMappingRequest
     */
    private PutMappingRequest columns2Properties(String myTableName, List<Column> columnList) throws Exception {
        Map<String, Property> map = new HashMap<>(20);

        columnList.removeIf(x -> x.getName().equals(ElasticSearchConstant.ELASTICSEARCH_ID));
        for (Column column : columnList) {
            String fieldName = StringUtils.isEmpty(column.getGroup()) ?
                    column.getName() :
                    column.getGroup() + Constant.FieldGroupSeparator + column.getName();
            map.put(fieldName, AllColumnType.getEs8PropertyBy((FulltextColumn) column));
        }

        return PutMappingRequest.of(mapping -> mapping
                .properties(map)
                .index(myTableName)
        );
    }
```
