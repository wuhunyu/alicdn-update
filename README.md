### 阿里云 CDN https 证书自动更新

#### 1. 功能

自动更新**阿里云 CDN https 证书**配置

#### 2. 证书更新方式

1.1 程序启动时主动更新一次 CDN https 证书
1.2 主动监听证书公私钥文件的变化，当文件发生变化时，主动更新 CDN https 证书
1.3 定时更新 CDN https 证书

#### 3. 依赖环境

项目使用 docker 容器化部署，docker 容器依赖了 `openjdk:21-jdk-slim` 镜像

#### 4. 配置

##### 4.1 配置

项目中存在两个配置文件，分别是 `Dockerfile` 和 `docker-compose.yml`，其中需要用户修改的配置文件是 `docker-compose.yml`
`docker-compose.yml` 是一个 demo 配置，需要用户根据自己的域名情况进行修改

##### 4.2 概念

###### 4.2.1 证书所在目录 & 项目日志

| 名称         | 含义                                                         | 宿主机配置                           | 容器配置     | 参考值                                                |
| ------------ | ------------------------------------------------------------ | ------------------------------------ | ------------ | ----------------------------------------------------- |
| 证书所在目录 | 用户的域名证书所在的目录，需要用户的公私钥在同一个目录下     | 用户宿主机证书的目录，结尾不要有 `/` | /app/sslPath | /root/.acme.sh/static.wuhunyu.top_ecc:/app/sslPath:ro |
| 项目日志     | 此程序在运行过程中，会产生运行日志，这部分日志需要挂载在宿主机器上，以便容器出现问题时方便排查 | 用户宿主机用于保存项目日志的目录     | /var/logs    | /var/alicdn-update/logs:/var/logs                     |

###### 4.2.2 用户自定义参数

| 名称                   | 含义                                               | 默认值                     | 参考值                 | 备注                                                         |
| ---------------------- | -------------------------------------------------- | -------------------------- | ---------------------- | ------------------------------------------------------------ |
| accessKeyId            | 阿里云访问key                                      | 无                         | -                      | 需要开通 [AliyunCDNFullAccess](https://ram.console.aliyun.com/users) 权限 |
| accessKeySecret        | 阿里云访问密钥                                     | 无                         | -                      | 需要开通 [AliyunCDNFullAccess](https://ram.console.aliyun.com/users) 权限 |
| domain                 | 需要修改 https 证书的域名                          | 无                         | static.wuhunyu.top     | 注意不要有传输协议http/https                                 |
| sslPath                | 容器内 https 证书存放的目录                        | /app/sslPath               | /app/sslPath           | 保持默认即可，不推荐修改                                     |
| pub                    | 公钥证书名称                                       | 无                         | static.wuhunyu.top.cer | 需要公钥文件中只包含公钥信息                                 |
| pri                    | 私钥证书名称                                       | 无                         | static.wuhunyu.top.key | 需要私钥文件中只包含私钥信息                                 |
| scheduledCron          | 定时更新证书的 cron 表达式                         | 0 0 1 * * ?(每天凌晨 1 点) | 0 0 1 * * ?            | 一般保持默认，可根据实际情况修改 cron 表达式                 |
| fileModifyListenDelay  | 监听到证书修改时，延迟多少毫秒后执行更新证书任务   | 1000                       | 1000                   | 保持默认即可，不推荐修改                                     |
| retryTimeWhenException | 如果更新证书任务执行失败，则重试，重试次数可自定义 | 3                          | 3                      | 保持默认即可，不推荐修改                                     |

##### 4.3 修改 `docker-compose.yml`

请参照 **4.2 概念** 中的备注信息进行修改

#### 5. 部署

将已经修改完毕的 `docker-compose.yml` 和 `Dockerfile` 上传到服务器同一个目录

执行 `docker-compose up -d` 等待拉取镜像和启动容器即可

可通过 `docker logs -f -n 100 alicdn-update` 观察程序的日志输出情况，不出意外的话，可观察到类似于这样的日志信息

```shell
2024-06-24 18:23:33.974 [main] INFO  top.wuhunyu.alicdn.AliCdnUpdateApplication - 阿里云cdn更新程序启动中 
2024-06-24 18:23:34.004 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 读取配置信息如下： 
2024-06-24 18:23:34.005 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 阿里云访问key：XXXXXXXXXXXXXXXX 
2024-06-24 18:23:34.006 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 阿里云域名：static.wuhunyu.top 
2024-06-24 18:23:34.006 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 保存 ssl 证书的目录：/app/sslPath 
2024-06-24 18:23:34.007 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 公钥的名称：static.wuhunyu.top.cer 
2024-06-24 18:23:34.007 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 私钥的名称：static.wuhunyu.top.key 
2024-06-24 18:23:34.007 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 更新的时间表达式：0 0 1 * * ? 
2024-06-24 18:23:34.007 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 文件监听延迟时间：1000 
2024-06-24 18:23:34.007 [main] INFO  top.wuhunyu.alicdn.properties.AliCdnProperties - 异常时重试的次数：3 
2024-06-24 18:23:34.646 [main] INFO  t.w.alicdn.handler.SetCdnDomainSSLCertificate - => 修改阿里云 CDN https 证书请求发起成功 
2024-06-24 18:23:36.407 [httpclient-dispatch-1] INFO  t.w.alicdn.handler.SetCdnDomainSSLCertificate - <= 修改阿里云 CDN https 证书成功
```

#### 6. 其他

##### 6.1 异常

通过观察日志应该可以大体判断出问题所在

##### 6.2 源码

源码基于 jdk21 进行编写，如果有自定义的需求，期望的 jdk 版本也是 21