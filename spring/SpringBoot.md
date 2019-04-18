# SpringBoot

[SpringBoot运行](https://zhuanlan.zhihu.com/p/54146400)

* 一个单体应用程序把它==所有的功能放在一个单一进程中，并且通过在多个服务器上复制这个单体进行扩展==
* 一个微服务架构把==每个功能元素放进一个独立的服务中，并且通过跨服务器分发这些服务进行扩展，只在需要时才复制==
* 面向服务架构（SOA）：==面向服务（Service-Oriented）、松耦合（Loose-Coupling）、模块化（Modular）、分布式计算（Distributed Computing）、平台无关性（Independent Platform）==
* 微服务面临的挑战
  * 注册与发现（Registry and Discovery）、
  * 路由（Routing）：负载均衡、版本
  * 可靠性（ Reliability ）：进程服务调用
  * 延迟（Latency ）：多进程、网络开销、序列化反序列化
  * 热点（Hotspot）：负载均衡解决热点问题
  * 短路（ Circuit Break ）：保障主要功能可用
  * 伸缩（Scale）：容器、部署
  * 异步（Async）
  * 监控（Monitoring）
  * 配置（Configuration）
  * 数据同步（Data Sync）
  * 安全（Security）
* 动态Web内容与静态 Web 内容不同，请求内容通过服务器计算而来

## 问题

* ==Spring Boot是如何基于Spring Framework逐步走向自动装配？==
* ==SpringApplication是怎样掌控Spring应用生命周期？==
* ==Spring Boot外部化配置与Spring Environment抽象之间是什么关系？==
* ==Spring Web MVC向Spring Reactive WebFlux过渡的真实价值和意义？==

## 特点

- 组件自动装配：规约大于配置，专注核心业务
  - 模式注解、@Enable模块、条件装配、加载机制
- 外部化配置：一次构建、按需调配，到处运行
  - Environment抽象、生命周期、破坏性变更
- 嵌入式容器：内置容器、无需部署、独立运行
  - Servlet Web容器、Reactive Web容器
- Spring Boot Starter：简化依赖、按需装配、自我包含
  - 依赖管理、装配条件、装配顺序
- Production-Ready：一站式运维、生态无缝整合
  - 健康检查、数据指标、@Endpoint管控

## 与JavaEE规范

* Web：Servlet（JSR-315、JSR-340）
* SQL：JDBC（JSR-221）
* 数据校验：Bean Validation（JSR303、JSR-349）
* 缓存：Java Caching API（JSR-107）
* WebSockets：Java API for WebSocket（JSR-356）
* Web Services：JAX-WS（JSR-224）
* Java管理：JMX（JSR3）
* 消息：JMS（JSR-914）

## 三大特性

### 组件自动装配

* Web MVC、Web Flux、JDBC等
* 实现
  * 激活: @EnableAutoConfiguration
  * 配置: /META-INF/spring.factories
  * 实现: XXXAutoConfiguration
* 依赖
  * `spring-boot-starter-web`

### 嵌入式Web容器

* Web Servlet 容器
  * Tomcat、Jetty和Undertow
  * 嵌入式servlet容器工厂类：TomcatEmbeddedServletContainerFactory
* Web Reactive 容器
  * Netty Web Server

### 生产准备特性

* 指标(Metrics)
  * /actuator/metrics
  * CPU、磁盘、内存等利用率的暴露
* 健康检查(Health Check)
  * /actuator/health
  * 磁盘、DB等健康检查

* 外部化配置(Externalized Configuration)
  * /actuator/configprops
  * 通过调整配置来修改应用的行为

## Web应用

* Spring Boot 2.0是基于Spring Framework 5.0（java 8），而Spring Framework 5.0最大的特性是Web引入了Reactive Web开发，即Web Flux，是对Spring MVC或者Servlet的一种补充，SpringMVC是构建在Servlet基础之上的
* 使用可执行 Tomcat Maven 插件
  * 把war包打包成可执行jar包，实现本地嵌入式，不需要在外部配置tomcat并把war包导入进去
* Web MVC注解驱动
  * 基本配置步骤
    - 注解配置: @Configuration ( Spring 范式注解 ) 
    - 组件激活: @EnableWebMvc (Spring 模块装配) 
    - 自定义组件 : 实现WebMvcConfigurer (Spring Bean) 
* 跨域访问
  * CORS（SpringFramework提供）
  * 请求并没有被阻塞，而是返回头里是否存在Access-Control-Allow-origin
  * 注解驱动 `@CrossOrigin`：请求Controller上标注@CrossOrigin("*")

## 自动装配

### Spring 模式注解装配

* ==模式注解是一种用于声明在应用中扮演“组件”角色的注解==，如举例：@Component、@Service、@Controller、@Configuration等

* 装配方式：

  1. `<context:component-scan>` 方式

     ```xml
     <!-- 激活注解驱动特性 -->
         <context:annotation-config />
     <!--找寻被 @Component 或者其派生 Annotation 标记的类(Class)，将它们注册为 Spring Bean --> 
         <context:component-scan base-package="com.imooc.dive.in.spring.boot" />
     </beans>
     ```

  2. `@ComponentScan` 方式

     ```java
     @ComponentScan(basePackages = "com.imooc.dive.in.spring.boot")
     public class SpringConfiguration {
     ...
     }
     ```

* 自定义模式注解

  1. `@Component` “派生性”

     ```java
     @Target({ElementType.TYPE})
     @Retention(RetentionPolicy.RUNTIME)
     @Documented
     @Repository
     public @interface FirstLevelRepository {
         String value() default "";
     }
     ```

  2. `@Component` “层次性”

     ```java
     @Target({ElementType.TYPE})
     @Retention(RetentionPolicy.RUNTIME)
     @Documented
     @FirstLevelRepository
     public @interface SecondLevelRepository {
         //签名要保持一致
         String value() default "";
     }
     ```

### Spring @Enable 模块装配

* ==所谓“模块”是指具备相同领域的功能组件集合， 组合所形成一个独立的单元==。比如 Web MVC 模块、AspectJ代理模块、Caching(缓存)模块、JMX(Java 管理扩展)模块、Async(异步处理)模块等

* 如@EnableWebMvc、@EnableAutoConfiguration

* 实现方式：

  * 注解驱动方式

    ```java
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    @Import(DelegatingWebMvcConfiguration.class)
    public @interface EnableWebMvc {
    }
    
    @Configuration
    public class DelegatingWebMvcConfiguration extends
    WebMvcConfigurationSupport {
    ... 
    }
    ```

  * 接口编程方式

    ```java
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Import(CachingConfigurationSelector.class)
    public @interface EnableCaching {
    ... 
    }
    ```

    ```java
    public class CachingConfigurationSelector extends AdviceModeImportSelector<EnableCaching> {
        public String[] selectImports(AdviceMode adviceMode) {
            switch (adviceMode) {
                case PROXY:
                    return new String[] {
    AutoProxyRegistrar.class.getName(),ProxyCachingConfiguration.class.getName() };
            	case ASPECTJ:
                	return new String[] {
                    	AnnotationConfigUtils.CACHE_ASPECT_CONFIGURATION_CLASS_NAME };
            	default:
                    return null;
    	}
    }
    ```

* 自定义@Enable 模块

### 条件装配

* 从 Spring Framework 3.1 开始，允许==在 Bean 装配时增加前置条件判断==
* 条件注解举例
  * `@Profile`：配置化条件装配，3.1
  * `@Conditional`：编程条件装配，4.0

```java
 public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CalculateServiceBootstrap.class)
                .web(WebApplicationType.NONE)
                .profiles("Java8")   //.profile("Java8);
                .run(args);
        // CalculateService Bean 是否存在
        CalculateService calculateService = context.getBean(CalculateService.class);
     
     // 关闭上下文
        context.close();
 }
```

### SpringBoot自动装配

* 基于Spring Framework手动装配
* 定义：在 Spring Boot 场景下，基于==约定大于配置的原则==，实现 Spring 组件自动装配的目的。其中使用了底层装配技术：
  - Spring 模式注解装配 
  - Spring @Enable 模块装配 
  - Spring 条件装配
  - Spring 工厂加载机制 
    - 实现类: SpringFactoriesLoader 
    - 配置资源: META-INF/spring.factories 
* 实现方法
  1. 激活自动装配 - @EnableAutoConfiguration（@SpringBootApplication里包含了这部分内容）
  2. 实现自动装配 - XXXAutoConfiguration
  3. 配置自动装配实现 - META-INF/spring.factories
* SpringBoot自动化装配并不是SpringBoot的特性，而是基于SpringFramework的实现，增加了META-INF/spring.factories

## SpringApplication

```java
//运行
//方式1
SpringApplication.run(DiveInSpringBootApplication.class, args)
//方式2
new SpringApplicationBuilder(DiveInSpringBootApplication.class)
    .bannerMode(Banner.Mode.CONSOLE)
    .web(WebApplicationType.NONE)
    .profiles("prod")
    .headless(true)
    .run(args);    
```

### 推断引导类(Main Class)

* 根据 Main 线程执行堆栈判断实际的引导类