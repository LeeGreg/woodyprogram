# Byton

## 工具

* kubernetes
  * 是什么
    * 是用于自动部署，扩展和管理容器化应用程序的开源系统，它将组成应用程序的容器组合成逻辑单元，以便于管理和服务发现
  * 能做什么
  * 什么特点
    * 自动放置容器，根据命令或UI或CPU的使用情况自动调整应用程序副本数，自动部署和回滚，存储编排（自动安装所选择的存储系统），自我修复，重新启动失败的容器，服务发现和负载均衡，密钥和配置管理，批处理
* [apollo](https://github.com/ctripcorp/apollo/wiki/Apollo配置中心介绍)
  * [使用指南](https://github.com/ctripcorp/apollo/wiki/Java客户端使用指南)
  * 分布式配置中心
  * 能够集中化管理应用不同环境、不同集群的配置，配置修改后能够实时推送到应用端，并且具备规范的权限、流程治理等特性，适用于微服务配置管理场景
* consul
  * [使用手册](https://blog.csdn.net/liuzhuchen/article/details/81913562)
  * [官网](https://www.consul.io/)
* sentinel
* zipkin
* kibana
* grafana
* jenkins
* rabbitmq
* docker
  * [官网](https://docs.docker.com/)和[docker中文](http://www.docker.org.cn/dockerppt.html)

![image-20190814103622142](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190814103622142.png)

## 配置

```java
// uat 环境jenkins
admin/Byton_12

// 项目 git 地址
git:  https://git.byton.cn/user/login
ext_taow_efast
qwe123
ext_dingyj_efast
dyj123dyj

// gradle 
 gradle clean build --stacktrace --info

// 配置开发环境
1、git clone -b develop https://git.byton.cn/EC-Pre-order/component-java-library.git

2、mkdir byton & cd byton
mkdir biz-cms
cd biz-cms
git clone -b develop https://git.byton.cn/EC-Pre-order/biz-cms.git
ln -s ../../java-library/build.gradle build.gradle
cat biz-cms/settings.gradle.txt  > settings.gradle

3、grade clean build -x test

// 准备
// 1. 配置consul 官网下载安装包、解压、配置  sudo cp consul /usr/local/bin
consul agent -data-dir=/Users/dingyuanjie/Documents/code/work/20190703_byton/consul_agent/data -server -bootstrap -node=Air -bind=127.0.0.1
  
// 2. 启动redis
/Users/dingyuanjie/Documents/code/util/redis-5.0.2/src/redis-server /Users/dingyuanjie/Documents/code/util/redis-5.0.2/redis.conf
  
// 安装rabbitmq standalone版
  	// 安装管理插件
		./rabbitmq-plugins enable rabbitmq_management
		
// 3. 启动rabbitmq 
/Users/dingyuanjie/Documents/code/util/rabbitmq_server-3.7.16/sbin/rabbitmq-server
// ./rabbitmq-server
// http://localhost:15672

 // 4. 启动验证码服务
  java -jar /Users/dingyuanjie/Documents/byton/jar/verificationcode-boot-1.0.0-develop-fc96b72.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.rabbitmq.virtual-host=/ --spring.rabbitmq.port=5672
  
 // 5. 启动adminuser服务
  	java -jar /Users/dingyuanjie/Documents/byton/jar/adminuser-boot-1.0.0-develop-f6bc343.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.rabbitmq.virtual-host=/ --spring.rabbitmq.port=5672
  	
  //6. 启动车型服务
  	java -jar /Users/dingyuanjie/Documents/byton/jar/vehicle-boot-1.0.0-develop-cf24b98.jar
  	
// 前端
npm run dev-serve 

// mac idea中gradle的home环境(command + ,)要选择到.../gradle-5.5/bin
  
// 项目规范
  https://www.processon.com/view/link/5c4ee9d0e4b0fa03cea9b2dc#outline
// 部署工具 
	https://www.processon.com/view/link/5d2438bbe4b05dcb439c1025#outline

//cms,portal,service 三中类型的接口区分在后边的url上,其实域名用的是一个


// 配置host
47.102.63.28 ec-apollo-dev.byton.cn
47.102.63.28 ec-consul-dev.byton.cn
47.102.63.28 ec-sentinel-dev.byton.cn
47.102.63.28 ec-manage-dev.byton.cn
47.102.63.28 ec-kibana-dev.byton.cn
47.102.63.28 ec-grafana-dev.byton.cn
47.102.63.28 ec-service-dev.byton.cn
47.102.63.28 ec-xxl-job-dev.byton.cn
47.102.63.28 ec-jenkins-dev.byton.cn

47.111.39.247 ec-jenkins-test.byton.cn 
47.111.39.247 ec-apollo-test.byton.cn
47.111.39.247 ec-consul-test.byton.cn
47.111.39.247 ec-sentinel-test.byton.cn
47.111.39.247 ec-service-test.byton.cn
47.111.39.247 ec-manage-test.byton.cn
47.111.39.247 ec-zipkin-test.byton.cn
47.111.39.247 ec-xxl-job-test.byton.cn
47.111.39.247 ec-kibana-test.byton.cn

// 部署
https://ec-jenkins-dev.byton.cn/    developer/developer

// 测试环境访问地址、账号
EC - Portal
https://ec-manage-test.byton.cn
登录名：kevin
密码：bytonUser

https://jira.byton.com/secure/Dashboard.jspa
ext_yuanjie.ding
NEEh2wKT7i


//git身份验证失败，清除密码缓存
git config --system --unset credential.helper

// 代码提示 settxxx符号不存在，但是代码没有红色报错
idea编译器设置 command + ,   Annotation Processors 勾选 Enable Annotation Processing



verdor
	model boot
	
cms 
	调用 vendor
	
```

![image-20190709072024823](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190709072024823.png)

```shell
# 查找某个端口的应用，并干掉该应用

netstat -an|grep 8805
kill -9 8805
lsof -i:8805
kill -9 27489
```

## 项目

```java
// 供应商
biz-vendor
// 各服务jar包
component-java-library
// 前端
oms_manage
// 车型
biz_vehicle
// 订单
biz-order
// 支付
biz-payment
// 问卷调查
biz-questionnaire
// 用户
biz-user
// 管理平台用户
biz-adminuser
// 代理
biz_proxy
// 开放给管理后台的接口
biz-cms
// 开放给外网客户端接口,app,浏览器,小程序等
biz-portal
// 开放给外网服务器的接口
biz-service
// 文件上传
biz-upload
// 提醒
biz-notification
// 验证码
component-verificationcode
// 网关
component-zuul
// 字典
biz-dict
// 定时任务
component-appjobs
// 认证
component-oauth2server
// 框架
java-framework
// demo
biz-demo
// 阿波罗配置中心
component-app-config-libs
// 小程序
mini-program-charging
// 文件上传
component-fileupload
// 部署
deploy
```

```java
// 计算机接口
 Map<String, Object> nameMap = (Map<String, Object>) prdMap.get(result.getName());
//                if (nameMap == null) {
//                    nameMap = new HashMap<>();
//                    nameMap.put("productName", result.getName());
//                    nameMap.put("productType", new HashMap<String, Object>());
//                    prdMap.put(result.getName(), nameMap);
//                }
//
//
//                Map<String, Object> typeMap = (Map<String, Object>) nameMap.get(result.getType().toString());
//                if (typeMap == null) {
//                    typeMap = new HashMap<>();
//                    nameMap.put(result.getType().toString(), typeMap);
//                }
//
//                Map<String, Object> subTypeMap = (Map<String, Object>) typeMap.get(result.getSubType().toString());
//                if (subTypeMap == null) {
//                    subTypeMap = new HashMap<>();
//                    typeMap.put(result.getType().toString(), subTypeMap);
//                }
//
//                Map<String, Object> seriesMap = (Map<String, Object>) subTypeMap.get(result.getSeries().toString());
//                if (seriesMap == null) {
//                    seriesMap = new HashMap<>();
//                    subTypeMap.put(result.getSeries().toString(), seriesMap);
//                }
//
//
//                // 合作伙伴
//                if (seriesMap.get("partnerName") == null) {
//                    Set<String> partnerNameList = new HashSet<>();
//                    partnerNameList.add(result.getPartner().getName());
//                    seriesMap.put("partnerName", partnerNameList);
//                } else {
//                    Set<String> partnerNames = (Set) seriesMap.get("partnerName");
//                    partnerNames.add(result.getPartner().getName());
//                }
//                // 首付比例
//                if (seriesMap.get("downPaymentRate") == null) {
//                    Set<BigDecimal> downPaymentRateList = new HashSet<>();
//                    downPaymentRateList.add(result.getDownPaymentRate());
//                    seriesMap.put("downPaymentRate", downPaymentRateList);
//                } else {
//                    Set<BigDecimal> downPaymentRate = (Set) seriesMap.get("downPaymentRate");
//                    downPaymentRate.add(result.getDownPaymentRate());
//                }
//                // 尾款比例
//                if (seriesMap.get("remainingMoneyRate") == null) {
//                    Set<BigDecimal> remainingMoneyRateList = new HashSet<>();
//                    remainingMoneyRateList.add(result.getRemainingMoneyRate());
//                    seriesMap.put("remainingMoneyRate", remainingMoneyRateList);
//                } else {
//                    Set<BigDecimal> remainingMoneyRate = (Set) seriesMap.get("remainingMoneyRate");
//                    remainingMoneyRate.add(result.getRemainingMoneyRate());
//                }
//                // 贷款期限
//                if (seriesMap.get("terms") == null) {
//                    Set<Integer> termsList = new HashSet<>();
//                    termsList.add(result.getTerms().intValue());
//                    seriesMap.put("terms", termsList);
//                } else {
//                    Set<Integer> termsList = (Set) seriesMap.get("terms");
//                    termsList.add(result.getTerms().intValue());
//                }
```





# 环境

## SpringCloud

## 注册中心

* Consul

## 配置中心

* Apollo

## Docker

## Nginx

## Mysql

## K8s





