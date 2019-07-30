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

## 配置

```java
// 项目 git 地址
git:  https://git.byton.cn/user/login
ext_taow_efast
qwe123
ext_dingyj_efast
dyj123dyj

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
/Users/dingyuanjie/Documents/code/util/redis-5.0.2/src/redis-server ../redis.conf
  
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

// 配置host
47.102.63.28 ec-apollo-dev.byton.cn
47.102.63.28 ec-consul-dev.byton.cn
47.102.63.28 ec-sentinel-dev.byton.cn
47.102.63.28 ec-manage-dev.byton.cn
47.102.63.28 ec-kibana-dev.byton.cn
47.102.63.28 ec-grafana-dev.byton.cn
47.102.63.28 ec-service-dev.byton.cn
47.102.63.28 ec-xxl-job-dev.byton.cn
47.102.63.28  ec-jenkins-dev.byton.cn

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

```sql
select  
DISTINCT
product.id, 
product.uid,
product.type,
product.product_name,
product.series,
product.subtype,
product.down_payment_rate,
product.terms,
product.interest_rate,
product.rate,
product.vehicle_support_type,
product.vehicle_mode,
product.vehicle_edition,
product.product_status,
product.period_of_validity_startdate,
product.period_of_validity_enddate,
product.subsidy_format,
product.discount_interest_rate,
product.discount_rate,
product.product_description,
product.maintain_tag,
	formula.down_payment_amount_cal,
	formula.remaining_money_amount_cal,
	formula.loan_amount_cal,
	formula.subsidy_amount_rate,
	formula.monthly_payment_cal_rate,
	formula.total_fee_cal_rate_bank,
	formula.total_fee_cal_rate_customer,
	formula.subsidy_amount_interest_rate,
	formula.monthly_payment_cal_interest_rate,
	formula.total_fee_cal_interest_rate_bank,
	formula.total_fee_cal_interest_rate_customer,
	formula.rate_cal,
partner.id,
partner.name,
partner.short_name,
partner.code  

from byton_vendor_finance_product product
left join byton_vendor_partner_subsidiary sub on sub.parent_id = product.partner_id
left join byton_vendor_finance_product_formula formula on  formula.id = product.formula_id
left join byton_vendor_partner partner on partner.id = product.partner_id
left join byton_vendor_finance_product_region region on region.finance_product_id = product.id
where product.product_status = 2 and product.vehicle_mode = '' and product.vehicle_edition = ''
and ((product.city_support_type = 1 and sub.city_code = '' ) or (product.city_support_type = 2 and region.city_code = '' ))


select
DISTINCT
product.id, 
product.uid,
product.type,
product.product_name,
product.series,
product.subtype,
product.down_payment_rate,
product.terms,
product.interest_rate,
product.rate,
product.vehicle_support_type,
product.vehicle_mode,
product.vehicle_edition,
product.product_status,
product.period_of_validity_startdate,
product.period_of_validity_enddate,
product.subsidy_format,
product.discount_interest_rate,
product.discount_rate,
product.product_description,
product.maintain_tag,
	formula.down_payment_amount_cal,
	formula.remaining_money_amount_cal,
	formula.loan_amount_cal,
	formula.subsidy_amount_rate,
	formula.monthly_payment_cal_rate,
	formula.total_fee_cal_rate_bank,
	formula.total_fee_cal_rate_customer,
	formula.subsidy_amount_interest_rate,
	formula.monthly_payment_cal_interest_rate,
	formula.total_fee_cal_interest_rate_bank,
	formula.total_fee_cal_interest_rate_customer,
	formula.rate_cal,
partner.id,
partner.name,
partner.short_name,
partner.code  
from byton_vendor_finance_product product
left join byton_vendor_partner_subsidiary sub on sub.parent_id = product.partner_id
left join byton_vendor_finance_product_formula formula on  formula.id = product.formula_id
left join byton_vendor_partner partner on partner.id = product.partner_id
left join byton_vendor_finance_product_region region on region.finance_product_id = product.id
where product.product_status = 2 and product.city_support_type = 1 
and product.vehicle_mode = '' and product.vehicle_edition = ''


select 
distinct 
product.type, product.subtype, product.series, product.maintain_tag, product.subsidy_format, product.partner_id,
product.terms, product.down_payment_rate, product.remaining_money_rate, product.total_amount, product.city_support_type
from byton_vendor_finance_product product 
where product.product_status = 2 
		and product.vehicle_mode = ''
		and product.vehicle_edition = '' 
		and product.id in('')
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





