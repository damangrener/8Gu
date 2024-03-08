<!--markdown--># 0、集群需要在每个节点都安装ik（重复如下步骤）

# 1、官网下载ik

[下载地址](https://github.com/medcl/elasticsearch-analysis-ik/releases)

![image.png](https://mubei.icu/usr/uploads/2022/07/2445692249.png)

下载**zip**文件，下载tar.gz启动会报错，`java.nio.file.NoSuchFileException: /opt/es_8.1.3/1_es/plugins/ik/plugin-descriptor.properties`

# 2、解压

1. 切换用户，进入es安装目录，在plugins创建ik文件夹

   `cd plugins`

   `mkdir ik`
2. 下载的zip放在ik下并解压

   `unzip elasticsearch-analysis-ik-8.1.3.zip`

# 3、重新启动

1. 关闭es

`ps -ef|grep elast`

`kill -9 000`

2. 后台启动es

`./bin/elasticsearch -d`
