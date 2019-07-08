# Byton

## 配置

```java
// 项目 git 地址
git:  https://git.byton.cn/user/login
ext_taow_efast
qwe123

// 配置开发环境
1、git clone -b develop https://git.byton.cn/EC-Pre-order/component-java-library.git

2、mkdir byton & cd byton
mkdir biz-cms
cd biz-cms
git clone -b develop https://git.byton.cn/EC-Pre-order/biz-cms.git
ln -s ../../java-library/build.gradle build.gradle
cat biz-cms/settings.gradle.txt  > settings.gradle

3、grade clean build -x test

// 配置consul
consul agent -data-dir=/Users/ccciyo/workspace/efastserv/byton/git/consul-agent/data -server -bootstrap -node=Air -bind=127.0.0.1

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

// 部署
https://ec-jenkins-dev.byton.cn/    developer/developer

// 测试环境访问地址、账号
EC - Portal
https://ec-manage-test.byton.cn
登录名：kevin
密码：bytonUser
```

![image-20190709072024823](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190709072024823.png)



## 金融产品主数据结构

```mysql
# FINANCE_PRODUCT 金融产品主数据表
product_series_number				金融产品序列号（创建未发布）
product_unique_id						金融产品唯一识别码（发布后）
product_name								金融产品名称：贷款产品名：分享、易享、畅想  租赁产品名：乐享、畅想
product_type								金融产品类型：贷款 / 租赁
subtype											金融产品子类型：贷款子类型：标准、气球、定额 租赁子类型：标准、定额
financial_vendor						金融产品供应商名称 读取合作伙伴主数据 – 银行或租赁公司的公司名称
product_series							金融产品系列：	
															贷款类型包括的产品系列：分期贷、5050、343 
															租赁类型包括的产品：分期回租、分期直租
terms												金融产品贷款期数：12、24、36、48、60
down_payment_rate						首付比例
remaining_money_rate				尾款比例
total_amount								贷款额度，仅当金融产品子类型为定额贷时才进行维护
area												地区
Interest_rate								利率值
rate												费率值
subsidy_format							贴息方式：0无贴息、1按利率贴息、2按费率贴息、3按固定金额贴息
discount_interest_rate			按利率贴息率
discount_rate								按费率贴息率
discount_amount							按固定金额贴息值
period_of_validity_startdate金融产品有效期开始日期
period_of_validity_enddate	金融产品有效期结束日期
product_status							金融产品状态：0未发布、1待激活、2已激活、3已下架
product_description					金融产品简述
vehicle_mode								车款：null全部车款、model某车款
vehicle_edition							车型：null全部车型、edition某车型
# FINANCE_PRODUCT_CONFIG 金融产品主数据配置项表
configuration_id						金融产品主数据配置项唯一识别码
configuration_name					金融产品主数据配置项名称：净车价极小值金额/直租绑定保险金额
configuration_value					金融产品主数据配置项值
# FINANCE_PRODUCT_FORMULA 金融产品主数据计算公式表
formula_id									金融产品计算公式唯一识别码
formula_name								金融产品计算公式名称
formula_description					金融产品计算公式描述
product_type								金融产品类型：贷款 / 租赁
subtype											金融产品子类型
															贷款子类型：标准贷、气球贷、定额贷
															租赁子类型：标准贷、定额贷
product_series							金融产品系列：贷款：分期贷、5050、343	租赁：分期回租、分期直租
subsidy_format							贴息方式：0无贴息、1按利率贴息、2按费率贴息、3按固定金额贴息
down_payment_amount_cal			首付金额计算公式
remaining_money_amount_cal	尾款金额计算公式
loan_amount_cal							贷款额度计算公式
monthly_payment_cal					月供金额计算公式
total_fee_cal								分期总利息计算公式
rate_cal										利率转费率计算公式
# FINANCE_PRODUCT_BATCH_PUBLISH_LOG 金融产品批量发布日志表
log_id											金融产品批量发布日志唯一识别码
publish_datetime						批量发布日期及时间
log_content									批量发布日志内容
publish_user								批量发布用户
publish_result							批量发布结果：0成功，1失败
# PARTNER_PRIMARY_INFO 合作伙伴主数据表
partner_id									合作伙伴唯一识别码
partnership_type						合作伙伴类型：0经销商集团、1经销商公司、2银行、3租赁公司、4保险公司
company_name、company_address、company_city、primary_contact_name、email_address、phone_number、phone_number_1、position

# Feature&Option
model、edition
# City
```

