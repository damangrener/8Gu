<!--markdown--># 0 下载Elasticsearch8.1.3Linux版

[下载地址]([Elasticsearch 8.1.3 | Elastic](https://www.elastic.co/cn/downloads/past-releases/elasticsearch-8-1-3))

# 1 安装

## 1.1 新建用户

`useradd es`

`passwd es`

## 1.2 解压

root新建文件夹：`mkdir /opt/es_8.1.3`

文件夹权限赋给 `es`用户：`chown -R es /opt/es_8.1.3`

`su es`cd bi    

将安装包放到 `/opt/es_8.1.3/`下

解压：`tar -zxvf elasticsearch-8.1.3-linux-x86_64.tar.gz`

重命名：`mv elasticsearch-8.1.3 1_es`

## 1.3 默认安装

### 1.1.1 直接启动

直接 `./bin/elasticsearch`启动，8.0之后，首次启动ES，默认情况下会启用并配置安全功能，将自动进行一下安全配置：

* 启用身份验证和授权，并未内置超级用户 `elastic`生成密码
* 为传输层和HTTP层生成TLS的证书和密钥，并启用TLS，并使用这些密钥和证书进行配置
* 为kibana生成注册令牌，有效期为30分钟

  自动配置在`elasticsearch.yml`中具体体现在启动后比直接解压的yml中多了一些配置，如 `xpack.security.enabled: true`

  出现 `[ERROR][o.e.i.g.GeoIpDownloader  ] [localhost.localdomain] error downloading geoip database [GeoLite2-Country.mmdb]`这个报错不影响启动，可以去 `elasticsearch.yml`中加上

  ```yml
  # ip地理位置信息下载，设置为false，不然内网环境会有些报错
  ingest.geoip.downloader.enabled: false
  ```

  出现下图代表启动成功了

  ![image.png](https://mubei.icu/usr/uploads/2022/06/1993192880.png)

  接下来具体解释下这张图，就给大家翻译下吧

  ```
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ✅ Elasticsearch security features have been automatically configured!
      Elasticsearch安全特性已经自动配置! 
  ✅ Authentication is enabled and cluster connections are encrypted.
      已经启用身份验证并加密集群连接 

  ℹ️  Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
    DPZR=F*dJx_k4u2b*XsR
      超级用户elastic的密码是DPZR=F*dJx_k4u2b*XsR，
      可以用`bin/elasticsearch-reset-password -u elastic`这个命令重置

  ℹ️  HTTP CA certificate SHA-256 fingerprint:
    0f99b445d71f696cf60041edc6efbaa03dd54abd9147b35a5bbba749d8eeca59

  ℹ️  Configure Kibana to use this cluster:
  • Run Kibana and click the configuration link in the terminal when Kibana starts.
  • Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
    eyJ2ZXIiOiI4LjEuMyIsImFkciI6WyIxNzIuMTYuMTAuMTMwOjkyMDAiXSwiZmdyIjoiMGY5OWI0NDVkNzFmNjk2Y2Y2MDA0MWVkYzZlZmJhYTAzZGQ1NGFiZDkxNDdiMzVhNWJiYmE3NDlkOGVlY2E1OSIsImtleSI6IkEyQVFSNEVCa05CZ0otUWNrSWdTOnM1bkl1VU9zUmxDa052aHplY1pwNFEifQ==

  ℹ️  Configure other nodes to join this cluster:
  • On this node:
    ⁃ Create an enrollment token with `bin/elasticsearch-create-enrollment-token -s node`.
    ⁃ Uncomment the transport.host setting at the end of config/elasticsearch.yml.
    ⁃ Restart Elasticsearch.
  • On other nodes:
    ⁃ Start Elasticsearch with `bin/elasticsearch --enrollment-token <token>`, using the enrollment token that you generated.
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ```

### 1.1.2 重置超级用户elastic的密码

新开一个窗口，切换到es用户执行

`bin/elasticsearch-reset-password -u elastic -i`

* `-u`表示需要修改的用户名
* `-i` 表示交互式，可以自己指定密码，默认的是系统自动分配。

  按照提示修改密码，比如我现在更改为了`123456`

  ![image.png](https://mubei.icu/usr/uploads/2022/06/3893193625.png)

**更改密码报错**：ERROR: Failed to determine the health of the cluster.

去掉`elasticsearch.yml`中自动生成的`cluster.initial_master_nodes: ["localhost.localdomain"]`然后再启动，再修改密码。

如果忘记了超级管理员的密码，可以进行重置：[[弹性搜索重置密码|弹性搜索指南 [8.2] |弹性的 (elastic.co)](https://www.elastic.co/guide/en/elasticsearch/reference/current/reset-password.html)]

### 1.1.3 验证启动成功

因为默认是https访问，命令行输入`curl --cacert /opt/es_8.1.3/1_es/config/certs/http_ca.crt -u elastic https://localhost:9200`，把自己的安装目录替换下，我的是`/opt/es_8.1.3/1_es`,

`--cacert`为 HTTP 层生成的证书的路径。

输入之前设置的密码，看到熟悉的 You Know, for Search

```
[es@localhost root]$ curl --cacert /opt/es_8.1.3/1_es/config/certs/http_ca.crt -u elastic https://localhost:9200
Enter host password for user 'elastic':
{
  "name" : "localhost.localdomain",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "uElLJbu3T26mT818VLKBFw",
  "version" : {
    "number" : "8.1.3",
    "build_flavor" : "default",
    "build_type" : "tar",
    "build_hash" : "39afaa3c0fe7db4869a161985e240bd7182d7a07",
    "build_date" : "2022-04-19T08:13:25.444693396Z",
    "build_snapshot" : false,
    "lucene_version" : "9.0.0",
    "minimum_wire_compatibility_version" : "7.17.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "You Know, for Search"
}

```

## 1.3 更改配置

8.0之后默认将传输层和HTTP层都启用加密访问，我目前的需求是用HTTP+超级用户登录访问。

将`xpack.security.http.ssl.enabled: false`设置为false

```
xpack.security.http.ssl:
  #enabled: true
  enabled: false
  keystore.path: certs/http.p12
```

配置集群名称，节点名称等

(可以不用配置，head插件也可以正常访问)配置跨域`http.cors.enabled: true ``http.cors.allow-origin: "*"`

如下图，已正常启动

![image.png](http://mubei.icu/usr/uploads/2022/06/3889947295.png)

# 1.4 同服务器下新节点

1. 将`1_es`整个目录拷贝，重命名`2_es`

   `cp -r 1_es/ 2_es`

2. 删除2_es下data目录`rm -rf 2_es/data`
3. 修改elasticsearch.yml中的集群名，节点名，因为目前是单台服务器三节点，所以加入`discovery.seed_hosts: ["127.0.0.1"]`
4. 启动，如图所示已经是http请求

   ![image.png](https://mubei.icu/usr/uploads/2022/06/4099404119.png)

# 1.5 不同服务器下新节点

需在1.3中额外配置`network.host: 172.16.10.130`，host为当前服务器ip。每个节点都需配置，在新节点中配置`discovery.seed_hosts: ["172.16.10.130","172.16.10.131"]`

建议在一开始就配置`network.host`，不然需要删除data才能重新设置。

![image.png](https://mubei.icu/usr/uploads/2022/06/2516463301.png)

# 安装问题总结

1. [2022-06-08T17:27:54,672][WARN ][o.e.b.BootstrapChecks] [node-1-win] Transport SSL must be enabled if security is enabled. Please set [xpack.security.transport.ssl.enabled] to [true] or disable security by setting [xpack.security.enabled] to [false]
   配置如下：
   `xpack.security.enabled: true`
   `xpack.security.transport.ssl: enabled: false`
   解决：警告中很明显了，Transport SSL must be enabled if security is enabled.
2. [2022-06-08T05:30:43,051][WARN ][o.e.b.BootstrapChecks    ] [node-2-linux] max file descriptors [4096] for elasticsearch process is too low, increase to at least [65535]
   [2022-06-08T05:30:43,052][WARN ][o.e.b.BootstrapChecks    ] [node-2-linux] max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
3. 更改密码报错：ERROR: Failed to determine the health of the cluster.去掉`elasticsearch.yml`中自动生成的`cluster.initial_master_nodes: ["localhost.localdomain"]`然后再启动，再修改密码

# 配置文件

elasticsearch.yml

[tabs]
[tab name="node-1-130" active="true"]

```yml
cluster.name: es8
node.name: node-1-130
network.host: 172.16.10.130
http.port: 9200
#----------------------- BEGIN SECURITY AUTO CONFIGURATION -----------------------
#
# The following settings, TLS certificates, and keys have been automatically  
# generated to configure Elasticsearch security features on 13-06-2022 01:30:01
#
# --------------------------------------------------------------------------------

# Enable security features
xpack.security.enabled: true

xpack.security.enrollment.enabled: true

# Enable encryption for HTTP API client connections, such as Kibana, Logstash, and Agents
xpack.security.http.ssl:
  enabled: false
  keystore.path: certs/http.p12

# Enable encryption and mutual authentication between cluster nodes
xpack.security.transport.ssl:
  enabled: true
  verification_mode: certificate
  keystore.path: certs/transport.p12
  truststore.path: certs/transport.p12
# Create a new cluster with the current node only
# Additional nodes can still join the cluster later
cluster.initial_master_nodes: ["node-1-130"]

# Allow HTTP API connections from localhost and local networks
# Connections are encrypted and require user authentication
http.host: [_local_, _site_]

# Allow other nodes to join the cluster from localhost and local networks
# Connections are encrypted and mutually authenticated
#transport.host: [_local_, _site_]

#----------------------- END SECURITY AUTO CONFIGURATION -------------------------
```

[/tab]
[tab name="node-2-130"]

```yml
cluster.name: es8
node.name: node-2-130
#http.port: 9200

network.host: 172.16.10.130
discovery.seed_hosts: ["172.16.10.130"]

#----------------------- BEGIN SECURITY AUTO CONFIGURATION -----------------------
#
# The following settings, TLS certificates, and keys have been automatically  
# generated to configure Elasticsearch security features on 13-06-2022 01:30:01
#
# --------------------------------------------------------------------------------

# Enable security features
xpack.security.enabled: true

xpack.security.enrollment.enabled: true

# Enable encryption for HTTP API client connections, such as Kibana, Logstash, and Agents
xpack.security.http.ssl:
  enabled: false
  keystore.path: certs/http.p12

# Enable encryption and mutual authentication between cluster nodes
xpack.security.transport.ssl:
  enabled: true
  verification_mode: certificate
  keystore.path: certs/transport.p12
  truststore.path: certs/transport.p12
# Create a new cluster with the current node only
# Additional nodes can still join the cluster later
#cluster.initial_master_nodes: ["localhost.localdomain"]

# Allow HTTP API connections from localhost and local networks
# Connections are encrypted and require user authentication
http.host: [_local_, _site_]

# Allow other nodes to join the cluster from localhost and local networks
# Connections are encrypted and mutually authenticated
#transport.host: [_local_, _site_]

#----------------------- END SECURITY AUTO CONFIGURATION -------------------------
```

[/tab]

[tab name="node-2-131"]

```yml
cluster.name: es8
node.name: node-2-131

network.host: 172.16.10.131
#http.port: 9200
discovery.seed_hosts: ["172.16.10.130","172.16.10.131"]

#----------------------- BEGIN SECURITY AUTO CONFIGURATION -----------------------
#
# The following settings, TLS certificates, and keys have been automatically  
# generated to configure Elasticsearch security features on 13-06-2022 01:30:01
#
# --------------------------------------------------------------------------------

# Enable security features
xpack.security.enabled: true

xpack.security.enrollment.enabled: true

# Enable encryption for HTTP API client connections, such as Kibana, Logstash, and Agents
xpack.security.http.ssl:
  enabled: false
  keystore.path: certs/http.p12

# Enable encryption and mutual authentication between cluster nodes
xpack.security.transport.ssl:
  enabled: true
  verification_mode: certificate
  keystore.path: certs/transport.p12
  truststore.path: certs/transport.p12
# Create a new cluster with the current node only
# Additional nodes can still join the cluster later
#cluster.initial_master_nodes: ["node-1-130","node-2-130","node-2-131]

# Allow HTTP API connections from localhost and local networks
# Connections are encrypted and require user authentication
http.host: [_local_, _site_]

# Allow other nodes to join the cluster from localhost and local networks
# Connections are encrypted and mutually authenticated
#transport.host: [_local_, _site_]

#----------------------- END SECURITY AUTO CONFIGURATION -------------------------
```

[/tab]
[/tabs]
