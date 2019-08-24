项目演示：`微信公众号：师兄干货`

Web后端：`http://sell.springboot.cn/sell/seller/order/list`

版本：`SpringBoot1.5.2`、`Linux centos7.3`、`Maven 3.3.9`、`MySQL 5.7.17`、`Nginx 1.11.7`、`Redis 3.2.8`

* 角色划分
  * 买家（手机端）
  * 卖家（PC端）

![image-20190802165211895](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165211895.png)

![image-20190802165254507](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165254507.png)

![image-20190802165355202](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165355202.png)

![image-20190802165608466](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165608466.png)

```sql
-- 类目
create table `product_category` (
    `category_id` int not null auto_increment,
    `category_name` varchar(64) not null comment '类目名字',
    `category_type` int not null comment '类目编号',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`category_id`),
    unique key `uqe_category_type` (`category_type`)
);
INSERT INTO `product_category` (`category_id`, `category_name`, `category_type`, `create_time`, `update_time`)
VALUES
	(1,'热榜',11,'2017-03-28 16:40:22','2017-11-26 23:39:36'),
	(2,'好吃的',22,'2017-03-14 17:38:46','2017-11-26 23:39:40');

-- 商品
create table `product_info` (
    `product_id` varchar(32) not null,
    `product_name` varchar(64) not null comment '商品名称',
    `product_price` decimal(8,2) not null comment '单价',
    `product_stock` int not null comment '库存',
    `product_description` varchar(64) comment '描述',
    `product_icon` varchar(512) comment '小图',
    `product_status` tinyint(3) DEFAULT '0' COMMENT '商品状态,0正常1下架',
    `category_type` int not null comment '类目编号',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`product_id`)
);
INSERT INTO `product_info` (`product_id`, `product_name`, `product_price`, `product_stock`, `product_description`, `product_icon`, `product_status`, `category_type`, `create_time`, `update_time`)
VALUES
	('157875196366160022','皮蛋粥',0.01,39,'好吃的皮蛋粥','//fuss10.elemecdn.com/0/49/65d10ef215d3c770ebb2b5ea962a7jpeg.jpeg',0,1,'2017-03-28 19:39:15','2017-07-02 11:45:44'),
	('157875227953464068','慕斯蛋糕',10.90,200,'美味爽口','//fuss10.elemecdn.com/9/93/91994e8456818dfe7b0bd95f10a50jpeg.jpeg',1,1,'2017-03-28 19:35:54','2017-04-21 10:05:57'),
	('164103465734242707','蜜汁鸡翅',0.02,982,'好吃','//fuss10.elemecdn.com/7/4a/f307f56216b03f067155aec8b124ejpeg.jpeg',0,1,'2017-03-30 17:11:56','2017-06-24 19:20:54');

-- 订单
create table `order_master` (
    `order_id` varchar(32) not null,
    `buyer_name` varchar(32) not null comment '买家名字',
    `buyer_phone` varchar(32) not null comment '买家电话',
    `buyer_address` varchar(128) not null comment '买家地址',
    `buyer_openid` varchar(64) not null comment '买家微信openid',
    `order_amount` decimal(8,2) not null comment '订单总金额',
    `order_status` tinyint(3) not null default '0' comment '订单状态, 默认为新下单',
    `pay_status` tinyint(3) not null default '0' comment '支付状态, 默认未支付',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`order_id`),
    key `idx_buyer_openid` (`buyer_openid`)
);

-- 订单商品
create table `order_detail` (
    `detail_id` varchar(32) not null,
    `order_id` varchar(32) not null,
    `product_id` varchar(32) not null,
    `product_name` varchar(64) not null comment '商品名称',
    `product_price` decimal(8,2) not null comment '当前价格,单位分',
    `product_quantity` int not null comment '数量',
    `product_icon` varchar(512) comment '小图',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`detail_id`),
    key `idx_order_id` (`order_id`)
);

-- 用户
CREATE TABLE `user_info` (
  `id` varchar(32) NOT NULL,
  `username` varchar(32) DEFAULT '',
  `password` varchar(32) DEFAULT '',
  `openid` varchar(64) DEFAULT '' COMMENT '微信openid',
  `role` tinyint(1) NOT NULL COMMENT '1买家2卖家',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP on update current_timestamp COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
);
```

## 环境

* 虚拟机的使用
  * 虚拟机与本机互通(ping)
    * virtual虚拟机
* jdk
* maven

  * 联调

  ```java
  // 开启virtual虚拟机
  192.168.1.105/#/order
  //Chrome - F12 - Application
  // console中设置 document.cookie='openid=abc123'
  // Application 中 Cookies：openid abc123
  // 再访问 192.168.1.105 就会有页面
  // 进入虚拟机，设置nginx代理 (虚拟机能够访问本地)
  vim /usr/local/nginx/conf/nginx.conf
  	// 微信需要使用域名
  	server_name 改为 sell.com
  	location /sell/ {
  		proxy_pass http://本地ip:8080/sell/; 
  	}
  nginx -s reload
  
  //修改本机host
  vim etc/hosts
  // 虚拟机ip
  192.168.1.102 sell.com
  
  //访问 sell.com
  ```

## 微信授权

### 微信授权

* 获取OpenID
  * 手动方式
    * 设置域名、获取code、换取access_token
  * 利用第三方SDK

```properties r
# 微信网页授权  以微信文档为准
官方文档=https://mp.weixin.qq.com/wiki
调试=https://natapp.cn
第三方SDK=https://github.com/Wechat-Group/weixin-java-tools
```

```java
// 微信访问到自己开发的电脑
https://natapp.cn/
复制 authtoken ，下载客户端并解压，进入目录
ll
chmod 777 natapp
./natapp -authtoken=68a5946a2edc73fa

// 启动本地项目
http://127.0.0.1:8080/sell/buyer/product/list
// 开启 natapp
cd /Users/dingyuanjie/video/code
./natapp -authtoken=68a5946a2edc73fa  // 68a5946a2edc73fa为natapp上获取authtoken
// 返回 Forwarding              http://2dk2sp.natappfree.cc -> 127.0.0.1:8080
// 因为是免费的，所以是随机的，每次都变2dk2sp.natappfree.cc
// 将 其他电脑通过 http://2dk2sp.natappfree.cc 可以访问自己电脑的8080端口
// 可用于微信访问本级端口进行调试
http://2dk2sp.natappfree.cc/sell/buyer/product/list
```

```java
// 微信公众号【服务号-有微信支付】
// 网页授权域名（域名需备案）
【公众号设置】- 【功能设置】- 【网页授权域名】- 【设置】- 填写 natapp的域名 - 根据提示下载文件并放到项目的resources/static目录下（MP_xxx.txt）
通过 http://2dk2sp.natappfree.cc/MP_xxx.txt访问
如sell.nataapp4.cc

https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140842
// 按照步骤依次操作，url中的参数按照说明依次填写，其中redirect_uri为自己开发认证登录接口
https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
// 将上述url发送到微信并点击访问，就会调用redirect_uri接口生成code（开发时可断点调试）
```

```java
@RestController
@RequestMapping("/weixin")
@Slf4j
public class WeixinController {
    @GetMapping("/auth")
    public void auth(@RequestParam("code") String code) {
        log.info("进入auth方法。。。");
        log.info("code={}", code);

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxd898fcb01713c658&secret=29d8a650db31472aa87800e3b0d739f2&code=" + code + "&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        log.info("response={}", response);
    }
}
```

![image-20190803184008443](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190803184008443.png)

* 第三方sdk（推荐方式）

  ```java
  // https://github.com/Wechat-Group/weixin-java-tools
  https://github.com/Wechat-Group/WxJava/wiki
  网页授权
  https://github.com/Wechat-Group/WxJava/wiki/MP_OAuth2%E7%BD%91%E9%A1%B5%E6%8E%88%E6%9D%83
  // WechatController.java
  // 微信上访问
  http://微信可以访问的本地内网域名/sell/wechat/authorize?returnUrl=http://www.imooc.com
  // 微信上会跳转到 http://www.imooc.com
  ```

  ```java
  // 虚拟机上配置前端
  cd /opt/code/sell_fe_buyer
  cd config
  vim index.js
  sellUrl:'http://sell.com',
  openidUrl:'http://sell.natapp4.cc/sell/wechat/authorize',
  wechatPayUrl:'http://sell.natapp4.cc/sell/pay/create'
  // 回到前端项目根目录 /opt/code/sell_fe_buyer
  // 重新构建
  npm run build
  // 构建好所在目录
  ls -al dist/
  // 把该目录下文件都拷贝到网站根目录
  cp -r dist/* /opt/data/wwwroot/sell/  
  // 重新访问
  sell.com
  
  // 抓包工具 charles
  // 手机ip 192.168.30.143
  // 电脑ip 192.168.30.112
  电脑上ping下手机ip
  手机上-无线局域网-HTTP代理-手动-服务器-填写电脑ip，端口8888
  ```

### 微信支付

* 第三方sdk,`https://github.com/Pay-Group/best-pay-sdk/`

  ![image-20190803194002712](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190803194002712.png)

```java
// https://pay.weixin.qq.com/wiki/doc/api/index.html
// PayController.java
// create.ftl
http://localhost:8080/sell/pay/create?orderId=233r&returnUrl=http://www.baidu.com
// 前端配置 index.js
wechatPayUrl:'http://sell.natapp4.cc/sell/pay/create'
```

### 微信退款

```java
// 【微信支付-商户平台】- 【账户中心】 - 【API安全】- 【下载证书】
配置文件中配置证书路径keyPath
```

## 支付流程

* 支付需要openid（是微信用户在公众号appid下的唯一用户标识，appid不同，则获取到的openid不同）

  * JSAPI支付

* 微信授权

  * 获取openid
    * 微信公众平台-微信网页授权

  ```java
  https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
  // REDIRECT_URI 需要到 natapp.cn购买域名，例如 http://lsx888.natapp1.cc
  // lsx888.natapp1.cc 需要到 测试号管理-网页帐号中配置
  // 使用urlEncoder对链接进行处理
  // scope=snsapi_base
  
  浏览器访问
  http://localhost:8080/sell/wechat/userInfo?code=abc&state=edc
  {
  "code": 20,
  "msg": "invalid code, hints: [ req_id: RHcDlfyFe-Cymf9a ]"
  }
  // 将 抓包获取的 code 填写到url参数里
  http://localhost:8080/sell/wechat/userInfo?code=抓包获取的code&state=edc
  ```

```java
// 第一步开发：授权
	// 【开始开发】- 【接口测试号申请】
https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421137522
		appID(mpAppId)：wx83b775319ba15501
		appsecret(mpAppSecret)：53bb9a26149bb9f1451f1cb9e8427930
扫描关注【测试号二维码】
【网页帐号】 - 【网页授权获取用户基本信息】 - 【修改】配置自己的外网地址，如【sell.natapp4.cc】
测试：方法中断点
1. 先本地：http://localhost:8080/sell/wechat/authorize?returnUrl=http://www.imooc.com
2. 再域名：http://2dk2sp.natappfree.cc/sell/wechat/authorize?returnUrl=http://www.imooc.com
3. 域名再发送到手机微信中去访问：http://2dk2sp.natappfree.cc/sell/wechat/authorize?returnUrl=http://sell.com/#/

// 第二步开发：支付
// 第三步开发：授权 + 支付
```

## 管理平台

```java
// 卖家端订单
// SellerOrderController.java
//list.ftl  freemarker  修改前端代码后不用重启  build - build project
<h1>orderDTOPage.totalPages</h1>
<h1>orderDTOPage.getTotalPages()</h1>
// 前端页面 www.ibootstrap.cn
// 拖拽控件、下载选择控件的code

// 卖家端通用功能和上下架

// 卖家端新增商品和类目

// 买家和卖家端联通

// 项目优化

// 项目部署

```

```java
// 枚举  根据 code 获取 message
public interface CodeEnum {
    Integer getCode();
}

public class EnumUtil {
    public static <T extends CodeEnum> T getByCode(Integer code, Class<T> enumClass) {
        for (T each: enumClass.getEnumConstants()) {
            if (code.equals(each.getCode())) {
                return each;
            }
        }
        return null;
    }
}

EnumUtil.getByCode(payStatus, PayStatusEnum.class);

@Getter
public enum PayStatusEnum implements CodeEnum {

    WAIT(0, "等待支付"),
    SUCCESS(1, "支付成功"),
    ;

    private Integer code;
    private String message;

    PayStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

## 分布式Session

* 登录
  
  * 验证身份，存储信息
* 登出
  
  * 失效浏览状态
* 分布式系统
  
  * 旨在支持应用程序和服务的开发，可以利用物理架构由多个自治的处理元素，不共享主内存，但通过网络发送消息合作
* 多节点、消息通信、不共享内存
* 分布式系统、集群、分布式计算

* 微信扫码登录

  ```java
  //微信开放平台 - 资源中心 - 网站应用 - 网站应用微信登录开发指南
  // 公司资质 - 公众平台
  openAppId
  openAppSecret
  1. 请求code
  2. 根据code获取access_token
  
  // 获取code、openid
  	// WechatController.java
  // 登录、登出
  	// SellerUserController.java
  	// 登录二维码测试：sell.natapp4.cc/sell/wechat/qrAuthorize?returnUrl=www.imooc.com
  	// 登录：sell.natapp4.cc/sell/wechat/qrAuthorize?returnUrl=http://sell.natapp4.cc/sell/seller/login
  String.format("token_%s", token);
  // 跳转时用绝对地址，不要用相对地址
  return new ModelAndView("redirect:" + projectUrlConfig.getSell() + "/sell/seller/order/list");
  
  // aop访问登录验证
  // SellerAuthorizeAspect.java
  // 拦截登录异常
  // SellExceptionHandler.java
  "112".concat("sf");
  
  // 消息
  // 微信模版消息、WebSocket
  【微信公众平台】 - 【消息管理】 - 【发送消息-模版消息接口】- 选择模版
  // PushMessageImpl.java
  ```


## 总结

* 项目分析设计
* 微信特性
* 微信支付与退款
* Token认证
* WebSocket消息
* Redis缓存 + 分布式锁



## 抓包

```shell
路由器ip  192.168.31.1
手机ip 192.168.31.18
电脑ip 192.168.31.32
电脑上ping手机ip
手机-无线局域网-配置HTTP代理：手动-服务器（电脑ip），端口8888
```

![image-20190804230743636](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190804230743636.png)

![image-20190804231451984](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190804231451984.png)

```java
302 GET sell.springboot.cn 
	访问：http://sell.springboot.cn/sell/wechat/authorize?returnUrl=http://sell.springboot.cn/openid.html
	重定向，看Headers，跳转到微信授权地址：https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd898xxx&redirect_uri=http://sell.springboot.cn/sell/wechat/userInfo&response_type=code&scope=snsapi_base&state=http://sell.springboot.cn/openid.html#wechat_redirect
302 GET sell.springboot.cn
	访问：http://sell.springboot.cn/sell/wechat/userInfo?code=02123w3dxxx&state=http://sell.springboot.cn/openid.html
	//拿到code，请求微信接口获取openid
	重定向到：http://sell.springboot.cn/openid.html?openid=xcsfsfsxxxx
```

![image-20190805155513786](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190805155513786.png)

* 把连接发送到微信里访问，再次调试

![image-20190805165757041](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190805165757041.png)

