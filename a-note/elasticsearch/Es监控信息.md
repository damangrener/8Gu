```java
    private NodesStatsResponse nodesStatsResponse() {
        try {
            return client.nodes().stats();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<IndicesRecord> indicesRecords() {
        try {
            return client.cat().indices(i -> i
                    .bytes(Bytes.Bytes)
            ).valueBody();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ClusterStatsResponse clusterStatsResponse() {
        try {
            return client.cluster().stats();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HealthResponse clusterHealthResponse() {
        try {
            return client.cluster().health();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
```
```java

@Override
    public Map<String, Object> getState(List<String> ids) throws BaseException {
        if (CommonUtils.isCollectionEmpty(ids)) {

            try {
                Map<String, Object> result = new HashMap<>(initStateMap().size());
                stateMap.forEach((k, v) -> result.put(k, v.get()));
                return result;
            } catch (IOException e) {
                throw new BaseException(Status.SERVER_ERROR, String.format("init elasticsearch state error %s", e));
            }
        }

        Map<String, Object> result = new HashMap<>(ids.size());

        ids.forEach(id -> result.put(id, stateMap.get(id).get()));
        return result;
    }

    /**
     * 索引状态
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cat-indices.html
     */
    private Object innerGetClusterIndicesState(List<IndicesRecord> indicesRecords) {
        List<Object> result = new ArrayList<>();
        List<Object> header = new ArrayList<>();
        //健康状态 green,yellow,red
        header.add("health");
        //索引元数据状态 OPEN,CLOSE
//        header.add("status");
        //索引名
        header.add("index");
        //主分片
        header.add("pri");
        //复制分片
        header.add("rep");
        //文档数量
        header.add("docs.count");
        //文档删除数量
        header.add("docs.deleted");
        //存储大小
        header.add("store.size");
        //主分片容量
        header.add("pri.store.size");

        result.add(header);

        indicesRecords.forEach(i -> {
            List<Object> item = new ArrayList<>();

            //status
//            item.add(i.status());
            switch (Objects.requireNonNull(i.health())) {
                case "green":
                    item.add("<span style=\"color:green\">" + i.health() + "</span>");
                    break;
                case "yellow":
                    item.add("<span style=\"color:yellow\">" + i.health() + "</span>");
                    break;
                case "red":
                    item.add("<span style=\"color:red\">" + i.health() + "</span>");
                    break;
                default:
                    break;
            }

            //index
            item.add(i.index());
            //pri
            item.add(i.pri());
            //rep
            item.add(i.rep());
            //docs.count
            item.add(i.docsCount());
            //docs.deleted
            item.add(i.docsDeleted());
            //store.size
            item.add(CommonUtils.byteFormat(i.storeSize()));
            // pri.store.size
            item.add(CommonUtils.byteFormat(i.priStoreSize()));
            result.add(item);
        });
        return result;
    }

    /**
     * 集群基础状态
     */
    private Object innerGetClusterBaseState(ClusterStatsResponse clusterStatsResponse, HealthResponse clusterHealthResponse) {
        StringBuilder sbBasicInfo = new StringBuilder();

        sbBasicInfo.append("集群名称：");
        sbBasicInfo.append(clusterStatsResponse.clusterName()).append(System.lineSeparator());

        sbBasicInfo.append("集群状态：");
        String shardInfo = String.format(" (%,d of %s) ", clusterHealthResponse.activeShards(), clusterStatsResponse.indices().shards().total());
        HealthStatus clusterHealthStatus = clusterHealthResponse.status();
        switch (clusterHealthStatus) {
            case Green:
                sbBasicInfo.append("<span style=\"color:green\">").append(clusterHealthStatus.name()).append(shardInfo).append("</span>").append(System.lineSeparator());
                break;
            case Yellow:
                sbBasicInfo.append("<span style=\"color:yellow\">").append(clusterHealthStatus.name()).append(shardInfo).append("</span>").append(System.lineSeparator());
                break;
            case Red:
                sbBasicInfo.append("<span style=\"color:red\">").append(clusterHealthStatus.name()).append(shardInfo).append("</span>").append(System.lineSeparator());
                break;
            default:
        }

        sbBasicInfo.append("节点数量：");
        sbBasicInfo.append(clusterHealthResponse.numberOfNodes()).append(System.lineSeparator());

        sbBasicInfo.append("版本号：");
        sbBasicInfo.append(clusterStatsResponse.nodes().versions().iterator().next()).append(System.lineSeparator());


        sbBasicInfo.append("数据总量：");
        sbBasicInfo.append(String.format("%,d条(%s)", clusterStatsResponse.indices().docs().count(), CommonUtils.getSizeWithUnit(clusterStatsResponse.indices().store().sizeInBytes()))).append(System.lineSeparator());

        sbBasicInfo.append("索引总数：");
        sbBasicInfo.append(clusterStatsResponse.indices().count()).append(System.lineSeparator());

        return sbBasicInfo.toString();
    }

    /**
     * 集群内存信息
     * 包含有关所选节点使用的操作系统的统计信息。
     * nodes.os.mem
     * total_in_bytes （整数）所有选定节点上物理内存的总量（以字节为单位）
     * used_in_bytes （整数）所有选定节点上使用的物理内存量（以字节为单位）
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-stats.html
     */
    private Object innerGetClusterMemState(ClusterStatsResponse statsResponse) {
        Map<String, Double> legendValHeap = new HashMap<>(2);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TOTAL_LEGEND,
                ((double) (((statsResponse.nodes().os().mem().totalInBytes() / 1024 / 1024 / 1024) * 1000)) / 1000));
        legendValHeap.put(ElasticSearchConstant.MONITOR_USED_LEGEND,
                ((double) (((statsResponse.nodes().os().mem().usedInBytes() / 1024 / 1024 / 1024) * 1000)) / 1000));

        return legendValHeap;
    }


    /**
     * 包含有关所选节点使用的 Java 虚拟机 （JVM） 的统计信息。
     * nodes.jvm.mem
     * heap_max_in_bytes （整数）堆在所有选定节点上可用的最大内存量（以字节为单位）
     * heap_used_in_bytes （整数）堆当前在所有选定节点上使用的内存（以字节为单位）
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-stats.html
     */
    private Object innerGetClusterJvmState(ClusterStatsResponse statsResponse) {
        Map<String, Double> legendValHeap = new HashMap<>(2);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TOTAL_LEGEND,
                ((double) (((statsResponse.nodes().jvm().mem().heapMaxInBytes() / 1024 / 1024 / 1024) * 1000)) / 1000));
        legendValHeap.put(ElasticSearchConstant.MONITOR_USED_LEGEND,
                ((double) (((statsResponse.nodes().jvm().mem().heapUsedInBytes() / 1024 / 1024 / 1024) * 1000)) / 1000));

        return legendValHeap;
    }

    /**
     * 包含有关所选节点的文件存储的统计信息。
     * nodes.fs
     * total_in_bytes （整数）所有选定节点上所有文件存储的总大小（以字节为单位）。
     * free_in_bytes （整数）跨所有选定节点的文件存储中未分配字节的总数。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-stats.html
     */
    private Object innerGetClusterFsState(ClusterStatsResponse statsResponse) {
        Map<String, Double> legendValHeap = new HashMap<>(2);
        try {
            legendValHeap.put(ElasticSearchConstant.MONITOR_TOTAL_LEGEND,
                    ((double) (((statsResponse.nodes().fs().totalInBytes() / 1024 / 1024 / 1024) * 1000)) / 1000));
            legendValHeap.put(ElasticSearchConstant.MONITOR_FREE_LEGEND,
                    ((double) (((statsResponse.nodes().fs().freeInBytes() / 1024 / 1024 / 1024) * 1000)) / 1000));
        } catch (Exception e) {
            return legendValHeap;
        }

        return legendValHeap;
    }

    /**
     * 包含有关节点的索引操作的统计信息。
     * nodes.indices.indexing
     * index_total （整数）索引操作的总数。
     * index_time_in_millis （整数）执行索引操作所花费的总时间（以毫秒为单位）。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-nodes-stats.html
     */
    private Object innerGetClusterIndexState(NodesStatsResponse nodesStatsResponse) {

        long total = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().indexing())
                .mapToLong(n -> n.indices().indexing().indexTotal()).sum();

        long index_time_in_millis = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().indexing())
                .mapToLong(n -> n.indices().indexing().indexTimeInMillis()).sum();

        Map<String, Long> legendValHeap = new HashMap<>(1);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TIME_LEGEND, total > 0 ? (index_time_in_millis / total) : 0);

        return legendValHeap;
    }

    /**
     * （对象）包含有关节点搜索操作的统计信息。
     * nodes.indices.search
     * query_total （整数）查询操作的总数。
     * query_time_in_millis （整数）执行查询操作所花费的时间（以毫秒为单位）。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-nodes-stats.html
     */
    private Object innerGetClusterSearchState(NodesStatsResponse nodesStatsResponse) {

        long total = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().search())
                .mapToLong(n -> n.indices().search().queryTotal()).sum();

        long index_time_in_millis = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().search())
                .mapToLong(n -> n.indices().search().queryTimeInMillis()).sum();

        Map<String, Long> legendValHeap = new HashMap<>(1);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TIME_LEGEND, total > 0 ? (index_time_in_millis / total) : 0);

        return legendValHeap;
    }

    /**
     * （对象）包含有关节点的获取操作的统计信息。
     * nodes.indices.get
     * total （整数）获取操作的总数。
     * time_in_millis （整数）执行 get 操作所花费的时间（以毫秒为单位）。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-nodes-stats.html
     */
    private Object innerGetClusterGetState(NodesStatsResponse nodesStatsResponse) {

        long total = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().get())
                .mapToLong(n -> n.indices().get().total()).sum();

        long index_time_in_millis = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().get())
                .mapToLong(n -> n.indices().get().timeInMillis()).sum();

        Map<String, Long> legendValHeap = new HashMap<>(1);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TIME_LEGEND, total > 0 ? (index_time_in_millis / total) : 0);

        return legendValHeap;
    }

    /**
     * （对象）包含有关节点刷新操作的统计信息。
     * nodes.indices.refresh
     * total （整数）刷新操作的总数。
     * total_time_in_millis （整数）执行刷新操作所花费的总时间（以毫秒为单位）。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-nodes-stats.html
     */
    private Object innerGetClusterRefreshState(NodesStatsResponse nodesStatsResponse) {

        long total = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().refresh())
                .mapToLong(n -> n.indices().refresh().total()).sum();

        long index_time_in_millis = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().refresh())
                .mapToLong(n -> n.indices().refresh().totalTimeInMillis()).sum();

        Map<String, Long> legendValHeap = new HashMap<>(1);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TIME_LEGEND, total > 0 ? (index_time_in_millis / total) : 0);

        return legendValHeap;
    }

    /**
     * （对象）包含有关节点合并操作的统计信息。
     * nodes.indices.merges
     * total （整数）合并操作的总数。
     * total_time_in_millis （整数）执行合并操作所花费的总时间（以毫秒为单位）。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-nodes-stats.html
     */
    private Object innerGetClusterMergeState(NodesStatsResponse nodesStatsResponse) {

        long total = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().merges())
                .mapToLong(n -> n.indices().merges().total()).sum();

        long index_time_in_millis = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().merges())
                .mapToLong(n -> n.indices().merges().totalTimeInMillis()).sum();

        Map<String, Long> legendValHeap = new HashMap<>(1);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TIME_LEGEND, total > 0 ? (index_time_in_millis / total) : 0);

        return legendValHeap;
    }

    /**
     * 包含有关节点的索引操作的统计信息。
     * nodes.indices.indexing
     * delete_total （整数）删除操作的总数。
     * delete_time_in_millis （整数）执行删除操作所花费的时间（以毫秒为单位）。
     *
     * @resource https://www.elastic.co/guide/en/elasticsearch/reference/8.1/cluster-nodes-stats.html
     */
    private Object innerGetClusterDeleteState(NodesStatsResponse nodesStatsResponse) {

        long total = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().indexing())
                .mapToLong(n -> n.indices().indexing().deleteTotal()).sum();

        long index_time_in_millis = nodesStatsResponse.nodes().values().stream()
                .filter(n -> null != n.indices() && null != n.indices().indexing())
                .mapToLong(n -> n.indices().indexing().deleteTimeInMillis()).sum();

        Map<String, Long> legendValHeap = new HashMap<>(1);
        legendValHeap.put(ElasticSearchConstant.MONITOR_TIME_LEGEND, total > 0 ? (index_time_in_millis / total) : 0);

        return legendValHeap;
    }
```