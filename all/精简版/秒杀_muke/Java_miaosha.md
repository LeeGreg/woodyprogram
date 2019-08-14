# Readme

```java
// 多看别人写的优秀代码，比如幕客网上找优秀的视频教程
// 项目源码git地址：https://github.com/xiaojieWoody/miaosha.git
// 这个项目的代码多看几遍，编码风格、思想要学会
// SpringBoot引入第三方框架：1.添加相关依赖 2.配置文件中配置相关属性（查看SpringBoot官网配置页） 3. 使用
// JMeter压测时，一般取第二次的压测结果，第一次就当热身
// 第7章多看几遍，重点！
// 查看SpringBoot各版本配置文件内容
https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/
搜索：spring.resources.、spring.rabbitmq.
// 可写个Controller方法参数拦截器校验数据是否为空
自定义注解（方法上）
新增拦截器并注册
```

# 技术点

* 前端
  * Thymeleaf、Bootstrap、JQuery
* 后端
  * SpringBoot、JSR303、MyBatis
* 中间件
  * RabbitMQ、Redis、Druid
* 秒杀
  * 分布式会话
  * 商品列表页
  * 商品详情页
  * 订单详情页
  * 系统压测
  * 缓存优化
  * 消息队列
  * 接口安全
* 目标
  * 如何利用缓存
  * 如何使用异步
  * 如何编写优雅的代码

# 1. 项目框架搭建

## 数据库

```sql
CREATE TABLE `miaosha_user` {
	`id` bigint(20) NOT NULL COMENT '用户ID，手机号码',
	`nickname` varchar(255) NOT NULL,
	`password` varchar(32) DEFAULT NULL COMENT 'MD5(MD5(pass明文+固定salt)+salt)',
	`salt` varchar(10) DEFAULT NULL,
	`head` varchar(120) DEFAULT NULL COMENT '头像，云存储的ID',
	`register_date` datetime DEFAULT NULL COMENT '注册时间',
	`last_login_date` datetime DEFAULT NULL COMENT '上次登录时间',
	`login_count` int(11) DEFAULT '0' COMENT '登录次数',
	PRIMARY KEY('id')
} ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

## 内容

* SpringBoot环境搭建
* 集成Thymeleaf， Result结果封装
* 集成MyBatis + Druid
* 集成Jedis + Redis安装 + 通用缓存Key封装

> 集成mybatis、linux下源码安装redis、集成redis

## 总结

```java
//事务控制：Service的方法上添加 @Transactional
// redis
// 做成一个系统服务
	// redis/utils/install_server.sh
	// utils]# ./install_server.sh
		// 配置端口、配置文件位置、log文件位置、数据目录
	// 检查安装的ridis系统服务
	// chkconfig --list | grep redis
	// systemctl status redis_6379
  // systemctl stop redis_6379
	// systemctl start redis_6379
  // 如果要修改redis服务属性
	// vi /etc/init.d/redis_6379
	// 客户端密码登录 redis-cli   auth 123456
```

* 自定义redis工具类（不使用Spring提供的RedisTemplate）
  * Jedis依赖、Fastjson依赖
  * 根据Class自动化前缀
  * 序列化与反序列化
* 封装工具相关类
  * `RedisPoolFactory.java、RedisConfig.java、KeyPrefix.java、BasePrefix.java、UserKey.java、RedisService.java`

# 2. 实现登录

## 内容

* 数据库设计
* 明文密码两次MD5处理
  * 客户端：PASS = MD5（明文 + 固定Salt）
  * 服务端：PASS=MD5（用户输入 + 随机Salt）
* JSR303参数校验 + 全局异常处理器
* 分布式Session

> 明文密码两次md5入库、登录功能实现、集成Jsr303参数校验框架、系统通用异常处理、分布式session

## 总结

* 前台 MD5 加密 login.html
* 后台 MD5 加密 MD5Util.java

* JSP303参数检验
  - Controller方法参数前加上@Valid注解
  - Bean属性上添加相应注解，如@NotNull等
  - 自定义校验注解`IsMobile.java`
    - 照葫芦画瓢参考已有的注解，如@NotNull
* 全局异常处理器
  - `GlobalException extend RuntimeException`
  - `GlobalExceptionHandler`上添加`@ControllerAdvice`、`@ResponseBody`，类中方法上添加`@ExceptionHandler(value=Exception.class)`
* 分布式Session
* 登录统一校验
  - 登录访问`/login/to_login`接口
* `public class WebConfig extends WebMvcConfigurerAdapter`

# 3. 实现秒杀

## 内容

* 数据库设计

  * 商品表、订单表、秒杀商品表、秒杀订单表

  * ```sql
    CREATE TABLE `goods` {
      	`id` bigint(20) NOT NULL AUTO_INCREMENT COMENT '商品ID',
      	`goods_name` varchar(16) DEFAULT NULL COMENT '商品名称',
      	`goods_title` varchar(64) DEFAULT NULL COMENT '商品标题',
      	`goods_img` varchar(64) DEFAULT NULL COMENT '商品的图片',
      	`goods_detail` longtext COMENT '商品的详情介绍',
      	`goods_price` decimal(10,2) DEFAULT '0.00' COMENT '商品单价',
      	`goods_stock` int(11) DEFAULT '0' COMENT '商品库存，-1表示没有限制',
      	PRIMARY KEY('id')
      } ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
      
      INSERT INTO `goods` VALUES (1, 'iphoneX', 'Apple iPhone X(1865) 64GB 银色 移动联通电信4G手机','/img/iphonex.png','最新款iPhone',8765.00, 12)
    ```

    ```sql
     CREATE TABLE `miaosha_goods` {
      	`id` bigint(20) NOT NULL AUTO_INCREMENT COMENT '秒杀的商品表',
      	`goods_id` bigint(20) DEFAULT NULL COMENT '商品id',
      	`miaosha_price` decimal(10,2) DEFAULT '0.00' COMENT '秒杀价',
      	`stock_count` int(11) DEFAULT NULL COMENT '库存数量',
      	`start_date` datetime DEFAULT NULL COMMENT '秒杀开始时间',
      	`start_end` datetime DEFAULT NULL COMMENT '秒杀结束时间',
      	PRIMARY KEY('id')
      } ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
      
      INSERT INTO `miaosha_goods` VALUES(1,1,0.01,4,'2017-11-05 15:18:00','2017-11-13 14:00:18'),(2,2,0.01,9,'2017-11-12 14:00:14','2017-11-13 14:00:24');
    ```

    ```sql
    CREATE TABLE `order_info` {
      	`id` bigint(20) NOT NULL AUTO_INCREMENT ,
      	`user_id` bigint(20) DEFAULT NULL COMENT '用户ID',
      	`goods_id` bigint(20) DEFAULT NULL COMENT '商品ID',
      	`delivery_addr_id` bigint(20) DEFAULT NULL COMENT '收获地址ID',
      	`goods_name` varchar(16) DEFAULT NULL COMENT '冗余过来的商品名称',
      	`goods_count` int(11) DEFAULT '0' COMENT '商品数量',
      	`goods_price` decimal(10,2) DEFAULT '0.00' COMENT '商品单价',
      	`goods_channel` tinyint(4) DEFAULT '0' COMENT '1pc,2android,3ios',
      	`status` tinyint(4) DEFAULT '0' COMENT '订单状态,0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成',
      	`create_date` datetime DEFAULT NULL COMMENT '订单的创建时间',
      	`pay_date` datetime DEFAULT NULL COMMENT '支付时间',
      	PRIMARY KEY('id')
      } ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;
    ```

    ```sql
    CREATE TABLE `miaosha_order` {
      	`id` bigint(20) NOT NULL AUTO_INCREMENT,
      	`user_id` bigint(20) DEFAULT NULL COMENT '用户id',
      	`order_id` bigint(20) DEFAULT NULL COMENT '订单ID',
      	`goods_id` bigint(20) DEFAULT NULL COMENT '商品ID',
      	PRIMARY KEY('id')
      } ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
    ```

* 页面设计

  * 商品列表页
    * `/to_list`
  * 商品详情页
    * `/to_detail/{goodsId}`
  * 订单详情页

> 商品列表页、商品详情页、秒杀功能实现、订单详情页

* 秒杀
  * `/do_miaosha`

## 总结

# 4. JMeter压测

## 内容

* JMeter入门
  * 添加线程组
  * 添加监听器 -> 聚合报告
  * 线程组右键 -> 添加Sampler -> HTTP请求
* 自定义变量模拟多用户
  * 测试计划-添加配置元件-CSV Data Set Config
  * 引用变量{}
* JMeter命令行使用
  * 录好jmx
    * 列表页压测：记录QPS
    * 秒杀压测，记录QPS，模拟多用户卖超，改SQL，同一个用户卖超加主键
    * 前提：服务器上启动应用
      * `nohup java -jar -server -Xmx2048m -Xms2048m miaosha.jar &`
  * 命令行：`sh jmeter.sh -n -t XXX.jmx -l result.jtl`
  * 把`result.jtl`倒入JMeter
* Redis压测工具redis-benchmark
  * `redis-benchmark -h 127.0.0.1 -p 6379 -c 100 -n 100000` 100个并发连接，100000个请求
  * `redis-benchmark -h 127.0.0.1 -p 6379 -q -d 100` 存取大小为100字节的数据包
  * `redis-benchmark -t set,lpush -q -n 100000` 只测某些操作的性能
  * `redis-benchmark -n 100000 -q script load "redis.call('set','foo','bar')"`
  * ![image-20190811102808544](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811102808544.png)
* SpringBoot打war包
  * 添加`spring-boot-starter-tomcat`的`provided`依赖
    * provided：编译时通过，运行时不依赖
  * 添加`maven-war-plugin`插件

> jmeter快速入门、自定义变量模拟多用户、命令行压测

## 总结

* JMeter
  * 添加线程组
    * 配置：线程数、循环请求次数
  * ![image-20190811100604347](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811100604347.png)
    * 配置通用请求设置：协议-http、host-localhost、port-8080
  * ![image-20190811100651367](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811100651367.png)
    * 填写需要压测的接口路径
  * ![image-20190811100708922](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811100708922.png)
  * 自定义变量
  * ![image-20190811101923780](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811101923780.png)
  * ![image-20190811101849469](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811101849469.png)
  * ![image-20190811102012728](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811102012728.png)
  * ![image-20190811102029358](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811102029358.png)
  * 服务器上启动应用并查看启动日志
  * ![image-20190811103832727](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811103832727.png)
  * 服务器上开启压测
  * ![image-20190811104042936](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811104042936.png)
  * 查看压测占用系统资源情况
  * ![image-20190811104435143](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811104435143.png)
  * 压测完成后，导出结果result.jtl
  * ![image-20190811104236989](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811104236989.png)
  * Jmeter中倒入result.jtl进行查看聚合报告-QPS
  * `UserUtil.java`，`自动生成5000个token到tokens.txt中`
  * 自定义变量：用生成的5000个token模拟5000个用户并发请求
  * ![image-20190811104959594](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811104959594.png)
  * ![image-20190811105037651](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811105037651.png)
  * 将配置好的jmx文件上传到服务器上，准备压测
  * ![image-20190811105206236](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811105206236.png)
  * 修改自定义变量文件路径
  * ![image-20190811105328903](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811105328903.png)
  * 开始压测
  * ![image-20190811105417061](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811105417061.png)
  * 查看压测时占用资源情况
  * ![image-20190811105506323](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811105506323.png)
  * ![image-20190811105615774](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811105615774.png)

# 5. 页面优化技术

## 内容

* 页面缓存 + URL缓存 + 对象缓存
* 页面静态化，前后端分离
* 静态资源优化
* CDN优化

> 商品列表页页面缓存实现、热点数据对象缓存、商品详情静态化、秒杀接口前后端分离、订单详情静态化，解决卖超问题、静态资源优化

## 总结

* 页面缓存
  * `@RequestMapping(value="/to_list", produces="text/html")`
  * 取缓存
    * `String html = redisService.get(GoodsKey.getGoodsList, "", String.class);`
  * 手动渲染模板
    * `SpringWebContext`
    * `html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);`
    * `redisService.set(GoodsKey.getGoodsList, "", html);`
  * 结果输出
* URL缓存及对象缓存
  * 更细粒度的缓存
  * `MiaoshaUserService.java`
  * 60秒访问一次数据库
  * ![image-20190811114010698](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811114010698.png)
* 页面静态化
  * 常用技术：AngularJS、Vue.js
  * 优点：利用浏览器的缓存
  * 本质：`html + ajax`
  * [高性能网站设计之缓存更新的套路](https://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323)
  * [静态资源配置](https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/)
  * `@RequestMapping(value="/detail/{goodsId}")`
  * `GoodsDetailVo.java`
  * `goods_detail.htm`
    * `http://localhost:8080/goods_detail.htm?goodsId=1`
  * `/do_miaosha`
  * `order_detail.htm`
  * 状态为304
    * ![image-20190811120611291](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811120611291.png)
  * 页面缓存（配置文件中先配置，如缓存过期时间3600，spring.resources.cache-period= 3600）
    * ![image-20190811121342381](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811121342381.png)
  * 订单详情页面静态化
    * `OrderController.java`、`order_detail.htm`
  * 解决超卖问题：sql设置 where ... and stock > 0
    * ![image-20190811123525966](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811123525966.png)
  * 一个用户只能秒杀一个同种商品
    * 利用数据库的唯一索引
      * ![image-20190811122826535](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811122826535.png)
    * 优化，存入redis，从redis中查询，避免访问数据库
      * `getMiaoshaOrderByUserIdGoodsId()`
    * 打包部署到服务器，然后重新压测，检测是否有卖超

* 静态资源优化
  * JS/CSS压缩，减少流量
  * 多个JS/组合，减少连接数
  * CDN（内容分发网络）就近访问
  * ![image-20190811124006197](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811124006197.png)

# 6. 接口优化

## 内容

* Redis预减库存减少数据库访问
* 内存标记减少Redis访问
* RabbitMQ队列缓存，异步下单，增强用户体验
* RabbitMQ安装与SpringBoot集成
* 访问Nginx水平扩展
* 压测

> 接口优化的思路概述、linux下源码安装rabbitmq、集成rabbitmq（4种交换机模式）、redis预减库存，rabbitmq异步下单、使用本地标识减少redis请求、压力测试对比

## 总结

* 解决超卖
  * 数据库加唯一索引：防止用户重复购买
  * SQL加库存数量判断：方式库存数量变为负数
* 秒杀接口优化
  * 思路：减少数据库访问
    1. 系统初始化，把商品库存数量加载到Redis
       1. `public class MiaoshaController implements InitializingBean`
       2. `afterPropertiesSet() 系统启动时就将秒杀商品信息加载到redis`
    2. 收到请求，Redis预减库存，库存不足，直接返回，否则进入3
    3. 请求入队，立即返回排队中
    4. 请求出队，生成订单，减少库存
    5. 客户端轮询，是否秒杀成功
* SpringBoot集成RabbitMQ
  * 添加`spring-boot-starter-amqp`
  * 创建消息接收者
  * 创建消息发送者
* 同步下单改为异步下单
  * 同步
    * 1. 判断用户是否登录
      2. 判断库存
      3. 判断是否已经秒杀到
      4. 减库存 下订单 写入秒杀订单
  * 异步
    * `miaosha_6 `
    * `MiaoshaController.java`
    * `goods_detail.htm`
* ![image-20190811161737032](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811161737032.png)
* nginx配置
  * ![image-20190811162244131](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811162244131.png)
  * ![image-20190811161915390](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190811161915390.png)

# 7. 安全优化

## 内容

* 秒杀接口地址隐藏
  * 秒杀开始之前，先去请求接口获取秒杀地址
    1. 接口改造，带上@PathVariable参数
    2. 添加生成地址的接口
       * 获取秒杀接口，`/path`
    3. 秒杀收到请求，先验证`PathVariable`
* 数学公式验证码
  * 点击秒杀之前，先输入验证码，分散用户的请求
    1. 添加生成验证码的接口
    2. 在获取秒杀路径的时候，验证验证码
    3. ScriptEngine使用，计算js公式
  * `goods_detail.htm`
  * `/verifyCode`
* 接口限流防刷
  * 对接口做限流
    * 可用拦截器减少对业务入侵
    * 自定义注解`@AccessLimit(seconds=5, maxCount=5, needLogin=true)`
    * 拦截器``AccessInterceptor.java``
    * 注册拦截器`WebConfig.java`

> 隐藏秒杀地址、图形验证码、接口限流防刷

## 总结

* `goods_detail.htm`
* `/{path}/do_miaosha`
* `/verifyCode`

# 8. Tomcat服务端优化

* ##### Tomcat/Ngnix/LVS/Keepalived

## 内容

* tomcat配置优化
* tomcat使用apr连接器
* nginx配置并发、长连接、压缩、状态监控
* nginx配置缓存、请求统计
* LVS四层负载均衡
*  keepalive负载均衡与高可用

## 总结

