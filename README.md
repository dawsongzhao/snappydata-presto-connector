# Snappydata Plugin for presto

该项目为支持presto查询snappydata的插件 

## 前置条件
- presto version:0.217
- snappydata jdbc version:1.1.0

## 编译源码
```
git clone http://gitlab.yingzi.com/zhaodongsheng/snappydata-plugin-for-presto.git
mvn install
```
## 配置presto
### plugin安装
1. 在presto安装目录的plugin目录下新建snappydata文件夹
2. 将编译的jar包`snappydata-1.0-SNAPSHOT.jar`上传至presto安装目录的plugin/snappydata
3. 将plugin/mysql/下的所有jar复制到plugin/snappydata;并删除presto-base-jdbc-0.217.jar
4. 将snappydata-jdbc_2.11-1.1.0.jar上传到plugin/snappydata
```shell
[root@hdfs01-dev snappydata]$ pwd
/data/bigdata/presto/presto-server-0.217/plugin/snappydata
[root@hdfs01-dev snappydata]$ ls
-rw-r--r-- 1 root root    3482 10月 29 20:05 animal-sniffer-annotations-1.14.jar
-rw-r--r-- 1 root root    4467 10月 29 20:05 aopalliance-1.0.jar
-rw-r--r-- 1 root root   20955 10月 29 20:05 bootstrap-0.178.jar
-rw-r--r-- 1 root root   72385 10月 29 20:05 bval-core-1.1.1.jar
-rw-r--r-- 1 root root  373824 10月 29 20:05 bval-jsr-1.1.1.jar
-rw-r--r-- 1 root root  352931 10月 29 20:05 cglib-nodep-3.2.5.jar
-rw-r--r-- 1 root root   31547 10月 29 20:05 checker-compat-qual-2.0.0.jar
-rw-r--r-- 1 root root  206711 10月 29 20:05 commons-beanutils-core-1.8.3.jar
-rw-r--r-- 1 root root  434678 10月 29 20:05 commons-lang3-3.4.jar
-rw-r--r-- 1 root root   31076 10月 29 20:05 concurrent-0.178.jar
-rw-r--r-- 1 root root   75223 10月 29 20:05 configuration-0.178.jar
-rw-r--r-- 1 root root   13704 10月 29 20:05 error_prone_annotations-2.1.3.jar
-rw-r--r-- 1 root root 2738386 10月 29 20:05 guava-24.1-jre.jar
-rw-r--r-- 1 root root  823547 10月 29 20:05 guice-4.2.0.jar
-rw-r--r-- 1 root root    8764 10月 29 20:05 j2objc-annotations-1.1.jar
-rw-r--r-- 1 root root   26529 10月 29 20:05 javax.annotation-api-1.3.1.jar
-rw-r--r-- 1 root root    2497 10月 29 20:05 javax.inject-1.jar
-rw-r--r-- 1 root root   99570 10月 29 20:05 jaxb-api-2.2.6.jar
-rw-r--r-- 1 root root 1112659 10月 29 20:05 jaxb-impl-2.2.6.jar
-rw-r--r-- 1 root root   16515 10月 29 20:05 jcl-over-slf4j-1.7.25.jar
-rw-r--r-- 1 root root 2388002 10月 29 20:05 jmxutils-1.19.jar
-rw-r--r-- 1 root root  640835 10月 29 20:05 joda-time-2.10.jar
-rw-r--r-- 1 root root    5012 10月 29 20:05 joda-to-java-time-bridge-3.jar
-rw-r--r-- 1 root root   19936 10月 29 20:05 jsr305-3.0.2.jar
-rw-r--r-- 1 root root    3918 10月 29 20:05 log-0.178.jar
-rw-r--r-- 1 root root   23645 10月 29 20:05 log4j-over-slf4j-1.7.25.jar
-rw-r--r-- 1 root root  471901 10月 29 20:05 logback-core-1.2.3.jar
-rw-r--r-- 1 root root   19251 10月 29 20:05 log-manager-0.178.jar
-rw-r--r-- 1 root root  999632 10月 29 20:05 mysql-connector-java-5.1.44.jar
-rw-r--r-- 1 root root   71976 10月 29 20:05 presto-base-jdbc-0.217.jar
-rw-r--r-- 1 root root   41203 10月 29 20:05 slf4j-api-1.7.25.jar
-rw-r--r-- 1 root root    8460 10月 29 20:05 slf4j-jdk14-1.7.25.jar
-rw-r--r-- 1 root root   10634 10月 30 11:01 snappydata-1.0-SNAPSHOT.jar
-rw-r--r-- 1 root root 7467704 7月   8 20:59 snappydata-jdbc_2.11-1.1.0.jar
-rw-r--r-- 1 root root   63777 10月 29 20:05 validation-api-1.1.0.Final.jar
```
5. 在presto配置文件etc/catalog/目录下新建snappydata配置文件,注意name配置项必须与plugin目录名snappydata一致；
```shell
[root@hdfs01-dev catalog]$ pwd
/data/bigdata/presto/presto-server-0.217/etc/catalog
[root@hdfs01-dev catalog]$ ll
总用量 12
-rw-r--r-- 1 yzadmin yzadmin 229 3月  20 2019 hive.properties
-rw-rw-r-- 1 yzadmin yzadmin 163 10月 29 11:46 postgresql_ihp.properties
-rw-r--r-- 1 yzadmin yzadmin 147 10月 29 19:56 snappydata.properties
[root@hdfs01-dev catalog]$ cat snappydata.properties
connector.name=snappydata
connection-url=jdbc:snappydata:pool://172.19.101.82:1527
connection-user=test
connection-password=T2Fil91KN4m
```
### 运行及测试
1. 重启presto
```$xslt
[root@hdfs01-dev presto]$ pwd
/data/bigdata/presto
[root@hdfs01-dev presto]$ presto-server-0.217/bin/launcher restart
```
2. tail -100f 查看启动日志
3. 运行presto client
```$xslt
[root@hdfs01-dev presto]$ /data/bigdata/presto/presto --server hdfs01-dev.yingzi.com:3600 --catalog snappydata
presto> show schemas;
         Schema
-------------------------
 app
 bizcenter_anc
 bizcenter_hmc
presto> use app;
USE
presto:app> show tables;
        Table
---------------------
 bio_alarm_recoed
 boolean_test
 ods_pig_fnumbe_test
presto:app> select * from boolean_test;
 id | isok
----+-------
 30 | false
 29 | false
 29 | false
 29 | false
 29 | true
 29 | true
(6 rows)
```
## 遗留问题
1. 如果结果集过大，或出现spark错误
2. 库名大小写影响查询