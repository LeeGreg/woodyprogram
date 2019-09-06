# WebService

==http + xml==

使用HTTP方式，接收和响应外部系统的某种请求，从而实现远程调用.

* Socket访问： **Socket属于传输层，它是对Tcp/ip协议的实现，包含TCP/UDP,它是所有通信协议的基础，Http协议需要Socket支持，以Socket作为基础**

  > 1. 开启端口，该通信是长连接的通信 ，很容易被防火墙拦截，可以通过心跳机制来实现 ，开发难度大
  > 2. 传输的数据一般是字符串 ，可读性不强
  > 3. socket端口不便于推广
  > 4. 性能相对于其他的通信协议是最优的

* Http协议访问 ：属于应用层的协议，对Socket进行了封装

  > 1. 跨平台
  >
  > 2. 传数据不够友好
  >
  > 3. 对第三方应用提供的服务，希望对外暴露服务接口
  >
  >    数据封装不够友好 ：可以用xml封装数据 
  >
  >    希望给第三方应用提供web方式的服务  （http + xml） = web  Service

内容：

* XML扩展性标记语言，用于传输格式化的数据，是Web服务的基础

  > namespace-命名空间
  >
  > xmlns=“http://itcast.cn” 使用默认命名空间
  >
  > xmlns:itcast=“http://itcast.cn”使用指定名称的命名空间

* WSDL Web服务描述语言

  > 通过XML形式说明服务在什么地方－地址
  >
  > 通过XML形式说明服务提供什么样的方法 – 如何调用

* SOAP简单对象访问协议

  > 作为一个基于XML语言的协议用于有网上传输数据
  >
  > SOAP = 在HTTP的基础上+XML数据
  >
  > 基于HTTP
  >
  > 组成：
  >
  > 1. Envelope – 必须的部分。以XML的根元素出现
  > 2. Headers – 可选的
  > 3. Body – 必须的。在body部分，包含要执行的服务器的方法。和发送到服务器的数据

* http-get方式访问webservice

  ```java
  public void get(String mobileCode ,String userID ) throws Exception{
      URL url=new URL("http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx/getMobileCodeInfo?mobileCode="+mobileCode+
                      "&userID="+userID);
      HttpURLConnection conn=(HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setRequestMethod("GET");
      if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){ //结果码=200
          InputStream is=conn.getInputStream();
          //内存流 ，
          ByteArrayOutputStream boas=new ByteArrayOutputStream();
          byte[] buffer=new byte[1024];
          int len=-1;
          while((len=is.read(buffer))!=-1){
              boas.write(buffer, 0, len);
          }
          System.out.println("GET请求获取的数据:"+boas.toString());
          boas.close();
          is.close();
      }
  }
  ```

* Http-Client 框架POST请求 

  原生态的Socket基于传输层,现在要访问的WebService是基于HTTP的属于应用层,所以Socket通信要借助HttpClient发HTTP请求,这样**格式才能匹配**

  > 1. 创建 HttpClient 的实例
  > 2. 创建某种连接方法的实例，在这里是 GetMethod。在 GetMethod 的构造函数中传入待连接的地址
  > 3. 配置要传输的参数，和消息头信息
  > 4. 调用第一步中创建好的实例的 execute 方法来执行第二步中创建好的 method 实例
  > 5. 通过response读取字符串
  > 6. 释放连接。无论执行方法是否成功，都必须释放连接

  ```java
  //2.Post请求 ：通过Http-Client 框架来模拟实现 Http请求
      public static void post(String mobileCode, String userID) throws Exception {
  
          /**HttpClient访问网络的实现步骤：
           *  1. 准备一个请求客户端:浏览器
           *  2. 准备请求方式： GET 、POST
           *  3. 设置要传递的参数
           *  4.执行请求
           *  5. 获取结果
           */
          HttpClient client = new HttpClient();
          PostMethod postMethod = new PostMethod("http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx/getMobileCodeInfo");
          //3.设置请求参数
          postMethod.setParameter("mobileCode", mobileCode);
          postMethod.setParameter("userID", userID);
          //4.执行请求 ,结果码
          int code = client.executeMethod(postMethod);
          //5. 获取结果
          String result = postMethod.getResponseBodyAsString();
          System.out.println("Post请求的结果：" + result);
      }
  
      //2.Post请求 ：通过Http-Client 框架来模拟实现 Http请求
      public void soap() throws Exception{
          HttpClient client=new HttpClient();
          PostMethod postMethod=new PostMethod("http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx");
          //3.设置请求参数
          postMethod.setRequestBody(new FileInputStream("c:/soap.xml"));
          //修改请求的头部
          postMethod.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
          //4.执行请求 ,结果码
          int code=client.executeMethod(postMethod);
          System.out.println("结果码:"+code);
          //5. 获取结果
          String result=postMethod.getResponseBodyAsString();
          System.out.println("Post请求的结果："+result);
      }
  ```

  GET方式或者使用Http-Client框架来调用webservice的服务：

  1. 传递参数麻烦【get方式都写在请求地址上、post方式要一个一个封装】
  2. 解析结果麻烦【根据返回的XML来解析字符串】

  把webservice服务搞成是Java类让我们自己调用其实就是**Java帮我们生成本地代理，再通过本地代理来访问webservice**

* wsimport

  Java自带的一个命令

  **wsimport命令后面跟着的是WSDL的url路径** 语法 `wsimport [opations] <wsdl_uri>`

  > 1. wsdl_uri:wsdl 的统一资源标识符
  > 2. -keep：是否生成java源文件
  > 3. -d：指定.class文件的输出目录
  > 4. d  ：指定要输出的文件的位置
  > 5. -s  ：表示要解析java的源码 ，默认解析出的是class字节码 
  > 6. -p：定义生成类的包名，不定义的话有默认包名
  > 7. -verbose：在控制台显示输出信息
  > 8. -b：指定jaxws/jaxb绑定文件或额外的schemas
  > 9. -extension：使用扩展来支持SOAP1.2

  idea控制台：

  ```shell
  wsimport -keep -s wstest -p com.woody.framework.ws.iws.wsimport -verbose http://localhost:9999/ws/jobservice\?WSDL 
  ```

  将生成的java文件拷贝到项目中

  ![image-20181120225033995](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/1.基础/java/image-20181120225033995-2725434.png)

   ```java
  MobileCodeWS mobileCodeWS = new MobileCodeWS();
  MobileCodeWSSoap mobileCodeWSSoap = mobileCodeWS.getMobileCodeWSSoap();
  String mobileCodeInfo = mobileCodeWSSoap.getMobileCodeInfo("18721581173", "");
  System.out.println(mobileCodeInfo);
   ```

  本地代理仅仅是有其方法，类，并不能解析出具体的实现的。具体的操作其实还是webservice去完成的

## 自定义webservice服务

在jdk 1.6 版本以后 ，**通过jax-ws 包提供对webservice的支持 **

- **该方式通过注解的方式来声明webservice**
- **通过 jdk EndPoint.publish()发布webserive服务**

```java
//实体类
public class Phone {
    private String name;//操作系统名
    private String owner;//拥有者
    private int total;//市场占有率
    ...
}    
```

```java
@WebService(serviceName = "PhoneManager",
        targetNamespace = "http://dd.ws.it.cn") //修改命名空间 ，默认包名，取反
//声明该业务类对外提供webservice服务,默认只是对public修饰的方法对外以webservice形式发布
//类添加上@WebService注解后，类中所有的非静态方法都将会对外公布，必须至少有一个可以公开的方法，否则将会启动失败。protected、private、final、static方法不能对外公开
public class PhoneService {
    public static void main(String[] args) {
        String address1 = "http://127.0.0.1:8888/ws/phoneService";
//		String address2="http://127.0.0.1:8888/ws/phoneManager";
        /**
         * 发布webservice服务
         * 1.address：服务的地址
         * 2：implementor 服务的实现对象
         */

        Endpoint.publish(address1, new PhoneService());
//		Endpoint.publish(address2, new PhoneService());
        System.out.println("wsdl地址 :" + address1 + "?WSDL");
    }

    /**
     * @WebMethod(operationName="getMObileInfo"): 修改方法名
     * @WebResult(name="phone")：修改返回参数名
     * @WebParam(name="osName")：修改输入参数名
     */
    @WebMethod(operationName = "getMObileInfo")
    public @WebResult(name = "phone")Phone getPhoneInfo(@WebParam(name = "osName") String osName) {
        Phone phone = new Phone();
        if (osName.endsWith("android")) {
            phone.setName("android");
            phone.setOwner("google");
            phone.setTotal(80);
        } else if (osName.endsWith("ios")) {
            phone.setName("ios");
            phone.setOwner("apple");
            phone.setTotal(15);
        } else {
            phone.setName("windows phone");
            phone.setOwner("microsoft");
            phone.setTotal(5);
        }
        return phone;
    }

    @WebMethod(exclude = true)//把该方法排除在外，不对外公开
    public void sayHello(String city) {
        System.out.println("你好：" + city);
    }

    private void sayLuck(String city) {
        System.out.println("好友：" + city);
    }

    void sayGoodBye(String city) {
        System.out.println("拜拜:" + city);
    }

    protected void saySayalala(String city) {
        System.out.println("再见！" + city);
    }
}
```

```shell
wsimport -keep -s wstest -p com.woody.framework.ws.myws.test -verbose http://127.0.0.1:8888/ws/phoneService\?WSDL
```

## SOAP协议

WebService只采用HTTP POST方式传输数据，不使用GET方式

普通http post的contentType为：application/X-www-form-urlencoded

WebService的contentType为：在Http的基础上发SOAP协议，

​	soap1.1：text/xml 

​	soap1.2：application/soap+xml

​	命名空间不一样；SOAP1.2没有SOAPAction的请求头

WebService数据格式主要采用SOAP协议，实际上就是一种基于XML编码规范的文本协议

SOAP（Simple Object Access Protocol）简单对象访问协议，是运行在HTTP协议基础之上的协议，其实就是在HTTP协议上传输XML文件，就变成了SOAP协议

```java
@WebService
public interface JobService {
    public String getJob();
}
```

```java
//设置服务端点接口 ，指定对外提供服务的接口
@WebService(endpointInterface="com.woody.framework.ws.iws.JobService")
public class JobServiceImpl implements JobService{
    @Override
    public String getJob() {
        return "JEE研发工程师|Android研发工程师|数据库工程师|前端工程师|测试工程师|运维工程师";
    }
    public void say(){
        System.out.println("早上好!");
    }
}
```

```java
public static void main(String[] args){
    JobService jobService=new JobServiceImpl();
    String address="http://localhost:9999/ws/jobservice";
    Endpoint.publish(address, jobService);
    System.out.println("wsdl地址:"+address+"?WSDL");
}
```

```shell
#如果要在服务器上使用生成的java文件，那么生成的Java文件的包路径和服务器上的包路径要不一样， -p
wsimport -keep -s wstest -p com.woody.framework.ws.iws.wsimport -verbose http://localhost:9999/ws/jobservice\?WSDL
```

## SOA

Soa（**Service-Oriented Architecture）** ：**面向服务的架构**，即插即用

企业服务总线 （EnterPrise Service Bus ：ESB）

## uddi

uddi （Universal Description, Discovery and Integration）**统一描述、发现、集成**

它是目录服务，通过该服务可以注册和发布webservcie，以便第三方的调用者统一调用

## CXF

开源的 Services 框架

是 Celtrix （ESB框架）和 XFire（webserivice） 合并而成

核心是org.apache.cxf.Bus(总线)，类似于Spring的 ApplicationContext

默认是依赖于Spring的， 内置了Jetty服务器 

> - 与Spring、Servlet做了无缝对接，cxf框架里面集成了Servlet容器Jetty
> - 支持注解的方式来发布webservice
> - 能够显示一个webservice的服务列表
> - 能够添加拦截器：输入拦截器、输出拦截器 
> - 输入日志信息拦截器、输出日志拦截器、用户权限认证的拦截器

## Spring + cxf

```java
服务端
1. web.xml中配置webservice服务配置、加载Spring容器、CXFServlet
2. 服务接口、实现——@WebService
3. webservice服务配置中暴露服务地址

客户端
1. 利用idea，通过wsdl文件生成客户端文件
2. 服务在application-cxf-client.xml中配置成Bean，使用时直接@AutoWired
```

```xml
<!--web.xml-->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <!--
		application-context.xml中
		<import resource="classpath:/spring/application-cxf-server.xml"/>
	-->
    <param-value>classpath*:/spring/application-context.xml</param-value>
</context-param>

<!-- 使用一个监听器加载spring容器，保证web应用启动时加载spring -->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
<servlet>
    <servlet-name>cxf</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>cxf</servlet-name>
    <url-pattern>/myService/*</url-pattern>
</servlet-mapping>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jaxws="http://cxf.apache.org/jaxws"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
           http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
           http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd
           http://cxf.apache.org/jaxws
           http://cxf.apache.org/schemas/jaxws.xsd">
    <!-- web应用的类加载路径有两类：
    1.WEB-INF/classes目录。
    2.WEB-INF/lib目录下，两者的唯一区别是前者是散的class文件，后者是打成jar包的class文件
     -->
    <import resource="classpath:META-INF/cxf/cxf.xml"/>

    <!--所有服务 http://localhost:8080/woody-ding/myService -->
    <bean id="userService" class="com.woody.framework.ws.cxfspring.server.autowired.UserServiceImpl" />
    <bean id="helloWorldWs" class="com.woody.framework.ws.cxfspring.server.HelloWebServiceImpl">
        <property name="userService" ref="userService"></property>
    </bean>
    <jaxws:endpoint
            implementor="#helloWorldWs"
        <!--暴露的服务 http://localhost:8080/woody-ding/myService/fkjava?WSDL-->
            address="/fkjava">
        <!-- 如果要添加out拦截器，则使用 jaxws:outInterceptors标签定义 -->
        <jaxws:inInterceptors>

            <!--<jaxws:inInterceptors>-->
            <!--<ref bean="loggingin"/>-->
            <!--<ref bean="authInInterceptor"/>-->
            <!--</jaxws:inInterceptors>-->
            <!--<jaxws:outInterceptors>-->
            <!--<ref bean="loggingout"/>-->
            <!--</jaxws:outInterceptors> -->
            
            <!--这里是临时定义的一个嵌套bean，这是cxf提供的拦截器-->
            <bean class="org.apache.cxf.interceptor.LoggingInInterceptor"/>
            <!--这里是引用容器中已有的一个bean-->
            <!--<ref bean="anotherInterceptor"/>-->
            <!--这里将自己的权限控制器引进来
                extents AbstractPhaseInterceptor<SoapMessage> 空构造器 -->
            <bean class="com.woody.framework.ws.cxfspring.server.AuthInterceptor"/>
        </jaxws:inInterceptors>
    </jaxws:endpoint>
</beans>
```

```java
//接口
@WebService
public interface HelloWebService {
    String sayHello(String name);
}

//接口实现类
@WebService(endpointInterface = "com.woody.framework.ws.cxfspring.server.HelloWebService",
        serviceName = "HelloWorldWs")
public class HelloWebServiceImpl implements HelloWebService {
	//注入依赖
    private UserService userService;
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String sayHello(String name) {
        return "Hello" + name + "123" + userService.getUserName("woodyfine");
    }
}
```

```xml
客户端
1. 通过idea（右键项目——WebService，Web Service Platform - Glassfish...），用wsdl文件生成客户端访问文件
2. application-cxf-client.xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jaxws="http://cxf.apache.org/jaxws" xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://cxf.apache.org/jaxws http://cxf.apache.org/schemas/jaxws.xsd">

    <jaxws:client id="helloWebService"
                  serviceClass="com.woody.framework.ws.cxfspring.client.HelloWebService"
                  <!--去掉?wsdl-->
                  address="http://localhost:8080/woody-ding/myService/fkjava"/>
</beans>
3. 
@Autowired
private HelloWebService helloWebService;
```

