### 1. 创建一个名为`elasticsearch.service`的文件，通常放在`/etc/systemd/system/`目录下
```shell
sudo vi /etc/systemd/system/elasticsearch.service
```
```shell
[Unit]
Description=Elasticsearch
After=network.target

[Service]
Type=simple
User=es
ExecStartPre=/bin/bash -c 'ulimit -n 65536'
LimitNOFILE=65536
LimitNPROC=65536
ExecStart=/home/es/es_833/bin/elasticsearch
Restart=always

[Install]
WantedBy=multi-user.target
```
### 2. 设置自启
```shell
sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
```

### 出现的问题  
#### Q1. 设置完成后，启动失败，查看log，出现眼熟的65536报错  
使用 ExecStartPre 选项来执行 ulimit 命令，以设置文件描述符和进程数的限制。
```shell
ExecStartPre=/bin/bash -c 'ulimit -n 65536'
LimitNOFILE=65536
LimitNPROC=65536
```
在系统中设置了 Elasticsearch 所需的文件描述符和进程数限制，但在自启动时仍然出现了引导检查失败的问题，可能还有其他一些因素导致了这个问题。
  
在某些情况下，Systemd 可能无法正确读取 /etc/security/limits.conf 文件中的限制设置。这可能是因为 Systemd 在启动服务时不会读取该文件，而是读取其它位置的配置文件。
  
您可以尝试将限制设置直接添加到 Systemd 服务文件中。