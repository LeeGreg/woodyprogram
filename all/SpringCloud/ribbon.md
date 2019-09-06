# Spring Cloud Ribbon：客户端负载均衡

* ==是一个基于`HTTP`和`TCP`的客户端负载均衡工具，它基于`NetflixRibbon`实现==
  * ==通过`SpringCloud`的封装，可轻松将面向服务的REST模板请求自动转换成客户端负载均衡的服务调用==
  * ==不需要独立部署，几乎存在于每一个`Spring Cloud`构建的微服务和基础设施中==
  * ==微服务间的调用，API 网关的请求转发等内容实际上都是通过`Ribbon`来实现的==

* 负载均衡是对系统的高可用、 缓解网络压力和处理能力扩容的重要手段之一 
  * 服务端负载均衡（硬件或软件）
    * 服务端通过心跳检测来维护可用服务节点列表，根据负载均衡算法将请求转发到某一服务节点
      * 比如线性轮询、 按权重负载、 按流量负载等
  * 客户端负载均衡
    * 与服务注册中心通过心跳去维护服务端清单，从服务注册中心获取自己要访问的服务端清单

## 使用

* 服务提供者
  * 只需启动多个服务实例并注册到一个注册中心或是多个相关联的服务注册中心
* 服务消费者
  * ==直接通过调用被`@LoadBalanced`注解修饰过的`RestTemplate`来实现面向服务的接口调用==
* 这样就可将服务提供者的高可用以及服务消费者的负载均衡调用一起实现了

## RestTemplate

* ==该对象会使用`Ribbon`的自动化配置，同时通过配置`@LoadBalanced` 还能够开启客户端负载均衡==

* Get请求

  * `getForEntity`函数。该方法返回的是`ResponseEntity`, 该对象是`Spring`对`HTTP` 请求响应的封装

    ```java
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<User> responseEntity = restTemplate.getForEntity("http://USER­ SERVICE/user?name= {l}", User.class, "didi");
    User body= responseEntity.getBody();
    ```

  * `getForObject` 函数，可以理解为对 getForEntity 的进一步封装，它通过 `HttpMessageConverterExtractor` 对`HTTP`的请求响应体`body`内容进行对象转换， 实现请求直接返回包装好的对象内容

    ```java
    RestTemplate restTemplate = new RestTemplate();
    User result = restTemplate.getForObject(uri, User.class);
    ```

* Post请求

  * `postForEntity` 函数，同`GET`请求中的`getForEntity`类似， 会在调用后返回`ResponseEntity<T>`对象， 其中 T 为请求响应的`body`类型

  * `postForObject` 函数，简化postForEntity的后续处理，通过直接将请求响应的body内容包装成对象来返回使用 

    ```java
    RestTemplate restTemplate = new RestTemplate();
    User user = new User("didi", 20);
    String postResult = restTemplate.postForObject("http://USER-SERVICE/user", user,
    String.class);
    ```

  * `postForLocation`函数，实现了以`POST`请求提交资源， 并返回新资源的`URI`

    ```java
    User user = new User("didi", 40);
    URI responseURI = restTemplate.postForLocation("http://USER-SERVICE/user", user);
    ```

* Put、Delete...

## 源码分析

* `Spring Cloud Ribbon`中实现客户端负载均衡的基本脉络
  - ==通过`LoadBalancerinterceptor`拦截器对`RestTemplate`的请求进行拦截， 并利用`Spring Cloud`的负载均衡器`LoadBalancerClient`将以逻辑服务名为`host`的`URI`转换成具体的服务实例地址==
  - `LoadBalancerClient`的`Ribbon`实现`RibbonLoadBalancerClient`
    - 在使用`Ribbon`实现负载均衡器的时候， 实际使用的还是`Ribbon`中定义的`ILoadBalancer`接口的实现，自动化配置会采用`ZoneAwareLoadBalancer`的实例来实现客户端负载均衡

* `LoadBalancerClient`接口
  * 根据传入的服务名`serviceld`，从负载均衡器中挑选一个对应服务的实例来执行请求内容 
  * 为系统构建一个合适的`host:port`形式的URI
    * 在分布式系统中， 使用逻辑上的服务名称作为host来构建URI（替代服务实例的host:port形式)进行请求， 比如`http://myservice/path/to/service` 。
    *  在该操作的定义中，前者 ServiceInstance对象是带有host和port的具体服务实例 ， 而后者URI对象则是使用逻辑服务名定义为host的URI , 而返回的URI内容则是通过ServiceInstance的服务实例详情拼接出的具体host:post形式的请求地址  
* 负载均衡器
  * 虽然`Spring Cloud`中定义了`LoadBalancerClient`为负载均衡器的通用接口， 并且针对`Ribbon`实现了`RibbonLoadBalancerClient`， 但是它在具体实现客户端负载均衡时，是通过`Ribbon`的`ILoadBalancer`接口实现的
* ==负载均衡策略==
  * `Random Rule`：从服务实例清单中随机选择一个服务实例的功能
  * `RoundRobinRule`：实现了按照线性轮询的方式依次选择每个服务实例的功能
  * `RetryRule`：实现了一个具备重试机制的实例选择功能
  * `Weighted ResponseTimeRule`：根据实例的运行情况来计算权重， 并根据权重来挑选实例

## 配置详解

* 由于Ribbon中定义的每一个接口都有多种不同的策略实现，同时这些接口之间又有一定的依赖关系，如何选择具体的实现策略以及如何组织它们的关系 ——Spring CloudRibbon中的自动化配置
* 在引入Spring Cloud Ribbon的 依赖之后， 就能够自动化构建下面这些接口的实现。
  * IClientConfig: Ribbon的客户端配置 ， 默认采用DefaultClientConfigImpl实现 
  * IRule: Ribbon 的负载均衡策略 ， 默认采用ZoneAvoidanceRule实现，该策略能够在多区域环境下选出最佳区域的实例进行访问 
  * IPing：Ribbon的实例检查策略，默认采用.NoOpPinng 实现， 该检查策略是一个特殊的实现，实际上它并不会检查实例是否可用，而是始终返回true, 默认认为所有服务实例都是可用的  
  * ServerList<Server>：服务实例清单的维护机制， 默认采用ConfigurationBasedServerList实现 
  * ServerListFilter<Server>：服务实例清单过滤机制 ，默认采用ZonePreferenceServerListFilter实现， 该策略能够优先过滤出与请求调用方处于同区域的服务实例 
  * ILoadBalancer：负载均衡器，默认采用ZoneAwareLoadBalancer实现， 它具备了区域感知的能力 
* 上面这些自动化配置内容仅在没有引入Spring Cloud Eureka等服务治理框架时如此， 在同时引入Eureka和Ribbon依赖时，自动化配置会一些不同 
* 通过自动化配置的实现，可以轻松地实现客户端负载均衡 。 同时，针对一些个性化需求，也可以方便地替换上面的这些默认实现。 只需在SpringBoot应用中创建对应的实现实例就能覆盖这些默认的配置实现 
* 另外， 也可以通过使用`@RibbonClient`注解来实现更细粒度的客户端配置
* Spring Cloud和Ribbon对 RibbonClient 定义个性化配置的方法做了进一步优化
  *  可以直接通过`<clientName>.ribbon.<key>=<value>`的形式进行配置
  * 将 hello-service 服务客户端的IPing 接口实现替换为 PingUrl, 只需在 application.properties 配置中增加下面的内容即可`hello-service.ribbon.NFLoadBalancerPingClassName= com.netflix.loadbalancer.PingUrl`
  * 其中 hello-service 为服务名， NFLoadBalancerPingClassName 参数用来指定具体 的 IPing 接口实现类
* 在`Camden`版本中可通过配置的方式，更加方便地为`RibbonClient`指定`ILoadBalancer`、`IPing`、`IRule`、`ServerList`和`ServerListFilter`的定制化实现 
* 参数配置
  * 全局配置
    * 只需使用`ribbon.<key>=<value>`格式进行配置即可
  *  指定客户端的配置方式
    * 采用`<client>.ribbon.<key>=<value>`的格式
    * `<client>`代表了客户端 的名称， 如上文中在`@RibbonClient`中指定的名称， 也可以将它理解为是一个服务名 

## 与Eureka结合

* ==当在`Spring Cloud`的应用中同时引入`Spring Cloud Ribbon`和`Spring Cloud Eureka`依赖时，会触发`Eureka对Ribbon`的自动化配置==

  * ==将服务清单列表交给`Eureka`的服务治理机制来维护 ，而不是`Ribbon`的`ServerList`的维护机制来维护==
  * ==实例检查的任务（`IPing`的实现）交给了服务治理框架来进行维护==

* 在与`SpringCloud Eureka`结合使用的时候， 配置将会变得更加简单

  * 不再需要通过类似`hello-service.ribbon.listOfServers`的参数来指定具体的服务实例清单，因为`Eureka`将维护所有服务的实例清单

* 对于`Ribbon`的参数配置

  *   依然可以采用之前的两种配置方式来实现
  * 而指定客户端的配置方式可直接使用`Eureka `中的服务名作为`<client>`来针对各个微服务的个性化配置  

* `Spring Cloud Ribbon`默认实现了区域亲和策略

  * 可通过`Eureka`实例的元数据配置来实现区域化的实例配置方案
  * 比如，可将处于不同机房的实例配置成不同的区域值，以作为跨区域的容错机制实现
  * 而实现的方式非常简单， 只需在服务实例的元数据中增加zone参数来指定自己所在的区域 `eureka.instance.metadataMap.zone=shanghai`

* 可通过`ribbon.eureka.enabled=false`禁用`Eureka`对`Ribbon`服务实例的维护实现

  * 服务实例的维护回到使用` <client>.ribbon.listOfServers`参数配置的方式来实现

* ==重试机制==

  * `SpringCloud Eureka`实现的服务治理机制强调了CAP原理中的AP，与`ZooKeeper`这类强调CP的服务治理框架最大的区别就是

    * `Eureka`为了实现更高的服务可用性， 牺牲了一定的一致性， 在极端情况下它宁愿接受故障实例也不要丢掉 “ 健康 ” 实例 
  
  * 比如，当服务注册中心的网络发生故障断开时， 由于所有的服务实例无法维持续约心跳，在强调AP的服务治理中将会把所有服务实例都剔除掉
  
  * 而`Eureka`则会因为超过85%的实例丢失心跳而会触发保护机制，注册中心将会保留此时的所有节点， 以实现服务间依然可以进行互相调用的场景， 即使其中有部分故障节点， 但这样做可以继续保障大多数的服务正常消费
    
  * 由于`SpringCloud Eureka`在可用性与一致性上的取舍， 不论是由于触发了保护机制还是服务剔除的延迟， 引起服务调用到故障实例的时候， 还是希望能够增强对这类问题的==容错==。 所以，在实现服务调用的时候通常会加入一些重试机制
  
    * 开启重试机制，默认关闭
    * ==断路器的超时时间需要大于Ribbon的超时时间， 不然不会触发重试==
    * 请求连接的超时时间
    * 请求处理的超时时间
    * 对所有操作请求都 进行重试
    * ==切换实例的重试次数==
    * ==对当前实例的重试次数==
    
    ```properties
    # 开启重试机制，默认关闭
    spring.cloud.loadbalancer.retry.enabled=true
    # 断路器的超时时间需要大于Ribbon的超时时间， 不然不会触发重试
    hystrix.command.default.execution.isolation.thread.timeoutinMilliseconds=lOOOO
    # 请求连接的超时时间
    hello-service.ribbon.ConnectTimeout= 250 
    # 请求处理的超时时间。
    hello-service.ribbon.ReadTimeout= lOOO 
    # 对所有操作请求都 进行重试。
    hello-service.ribbon.OkToRetryOnAllOperations=true 
    #切换实例的重试次数
    hello-service.ribbon.MaxAutoRetriesNextServer=2 
    # 对当前实例的重试次数
    hello-service.ribbon.MaxAutoRetries=l
    ```
    
  *  根据如上配置，当访问到故障请求的时候，它会再尝试访问一次当前实例(次数由 `MaxAutoRetries`配置)， 如果不行，就换一个实例进行访问，如果还是不行，再换一次实例访问(更换次数由`MaxAutoRetriesNextServer`配置)， 如果依然不行， 返回失败信息
  
  