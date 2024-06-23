FROM openjdk:21-jdk-slim

# 工作目录
WORKDIR /app

VOLUME /app
# 证书存放目录
VOLUME /app/sslPath
# 日志文件目录
VOLUME /var/logs

# 配置变量
ENV accessKeyId=""
ENV accessKeySecret=""
ENV domain=""
ENV sslPath=""
ENV pub=""
ENV pri=""
ENV scheduledCron=""
ENV fileModifyListenDelay=""
ENV retryTimeWhenException=""

# jvm 调优参数
ENV JAVA_OPTS=""

# 启动程序
COPY ./alicdn-update.jar /app/alicdn-update.jar

# 运行程序
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/alicdn-update.jar"]