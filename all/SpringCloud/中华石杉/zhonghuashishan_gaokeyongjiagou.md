# 高可用架构

## 如何设计一个高可用系统

* 高可用系统架构
  * 资源隔离、限流、熔断、降级、运维监控
  * 资源隔离：让你的系统里，某一块东西，在故障的情况下，不会耗尽系统所有的资源，比如线程资源
  * 限流：高并发的流量涌入进来，比如说突然间一秒钟100万QPS，废掉了，10万QPS进入系统，其他90万QPS被拒绝了
  * 熔断：系统后端的一些依赖，出了一些故障，比如说mysql挂掉了，每次请求都是报错的，熔断了，后续的请求过来直接不接收了，拒绝访问，10分钟之后再尝试去看看mysql恢复没有
  * 降级：mysql挂了，系统发现了，自动降级，从内存里存的少量数据中，去提取一些数据出来
  * 运维监控：监控+报警+优化，各种异常的情况，有问题就及时报警，优化一些系统的配置和参数，或者代码

## Hystrix

1. 创建command
2. 执行command
3. request cache
4. 短路器，如果打开了，fallback降级机制，优雅降级
5. circuit breaker：短路器，快速熔断（一旦后端服务故障，立刻熔断，阻止对其的访问）
   * 把一个分布式系统中的某一个服务，打造成一个高可用的服务
   * 资源隔离，优雅降级，熔断
6. 判断，线程池或者信号量的容量是否已满，reject，限流
   * 限流，限制对后端的服务的访问量，比如说你对mysql，redis，zookeeper，各种后端的中间件的资源，访问，其实为了避免过大的流浪打死后端的服务，线程池，信号量，限流
   * 限制服务对后端的资源的访问

* 是什么
  * Hystrix可以在分布式系统中对服务间的调用进行控制，加入一些调用延迟或者依赖故障的容错机制。
  * Hystrix通过将依赖服务进行资源隔离，进而组织某个依赖服务出现故障的时候，这种故障在整个系统所有的依赖服务调用中进行蔓延，同时Hystrix还提供故障时的fallback降级机制
  * 总而言之，Hystrix通过这些方法帮助我们提升分布式系统的可用性和稳定性

* hystrix的设计原则

  1. 对依赖服务调用时出现的调用延迟和调用失败进行控制和容错保护
  2. 在复杂的分布式系统中，阻止某一个依赖服务的故障在整个系统中蔓延，服务A->服务B->服务C，服务C故障了，服务B也故障了，服务A故障了，整套分布式系统全部故障，整体宕机
  3. 提供fail-fast（快速失败）和快速恢复的支持
  4. 提供fallback优雅降级的支持
  5. 支持近实时的监控、报警以及运维操作

  * 调用延迟+失败，提供容错
  * 阻止故障蔓延
  * 快速失败+快速恢复
  * 降级
  * 监控+报警+运维
  * 提供整个分布式系统的高可用的架构

* 要解决的问题是什么？

  * 在复杂的分布式系统架构中，每个服务都有很多的依赖服务，而每个依赖服务都可能会故障
  * 如果服务没有和自己的依赖服务进行隔离，那么可能某一个依赖服务的故障就会拖垮当前这个服务
  * 防止故障蔓延

* 更加细节的设计原则

  1. 阻止任何一个依赖服务耗尽所有的资源，比如tomcat中的所有线程资源
  2. 避免请求排队和积压，采用限流和fail fast来控制故障
  3. 提供fallback降级机制来应对故障
  4. 使用资源隔离技术，比如bulkhead（舱壁隔离技术），swimlane（泳道技术），circuit breaker（短路技术），来限制任何一个依赖服务的故障的影响
  5. 通过近实时的统计/监控/报警功能，来提高故障发现的速度
  6. 通过近实时的属性和配置热修改功能，来提高故障处理和恢复的速度
  7. 保护依赖服务调用的所有故障情况，而不仅仅只是网络故障情况

  * 调用这个依赖服务的时候，client调用包有bug，阻塞，等等，依赖服务的各种各样的调用的故障，都可以处理

* 如何实现它的目标的

  1. 通过HystrixCommand或者HystrixObservableCommand来封装对外部依赖的访问请求，这个访问请求一般会运行在独立的线程中，资源隔离
  2. 对于超出我们设定阈值的服务调用，直接进行超时，不允许其耗费过长时间阻塞住。这个超时时间默认是99.5%的访问时间，但是一般我们可以自己设置一下
  3. 为每一个依赖服务维护一个独立的线程池，或者是semaphore，当线程池已满时，直接拒绝对这个服务的调用
  4. 对依赖服务的调用的成功次数，失败次数，拒绝次数，超时次数，进行统计
  5. 如果对一个依赖服务的调用失败次数超过了一定的阈值，自动进行熔断，在一定时间内对该服务的调用直接降级，一段时间后再自动尝试恢复
  6. 当一个服务调用出现失败，被拒绝，超时，短路等异常情况时，自动调用fallback降级机制
  7. 对属性和配置的修改提供近实时的支持

## Hystrix核心知识

1. hystrix内部工作原理：8大执行步骤和流程
2. 资源隔离：你如果有很多个依赖服务，高可用性，先做资源隔离，任何一个依赖服务的故障不会导致你的服务的资源耗尽，不会崩溃
3. 请求缓存：对于一个request context内的多个相同command，使用request cache，提升性能
4. 熔断：基于短路器，采集各种异常事件，报错，超时，reject，短路，熔断，一定时间范围内就不允许访问了，直接降级，自动恢复的机制
5. 降级：报错，超时，reject，熔断，降级，服务提供容错的机制
6. 限流：在你的服务里面，通过线程池，或者信号量，限制对某个后端的服务或资源的访问量，避免从你的服务这里过去太多的流量，打死某个资源
7. 超时：避免某个依赖服务性能过差，导致大量的线程hang住去调用那个服务，会导致你的服务本身性能也比较差

## hystrix的高阶知识

1. request collapser，请求合并技术
2. fail-fast和fail-slient，高阶容错模式
3. static fallback和stubbed fallback，高阶降级模式
4. 嵌套command实现的发送网络请求的降级模式
5. 基于facade command的多级降级模式
6. request cache的手动清理
7. 生产环境中的线程池大小以及timeout配置优化经验
8. 线程池的自动化动态扩容与缩容技术
9. hystrix的metric高阶配置
10. 基于hystrix dashboard的可视化分布式系统监控
11. 生产环境中的hystrix工程运维经验

## 资源隔离

* 线程池隔离技术与信号量隔离技术的区别
  - 线程池：适合绝大多数的场景，99%的，线程池，对依赖服务的网络请求的调用和访问，timeout这种问题
  - 信号量：适合，你的访问不是对外部依赖的访问，而是对内部的一些比较复杂的业务逻辑的访问，但是像这种访问，系统内部的代码，其实不涉及任何的网络请求，那么只要做信号量的普通限流就可以了，因为不需要去捕获timeout类似的问题，算法+数据结构的效率不是太高，并发量突然太高，因为这里稍微耗时一些，导致很多线程卡在这里的话，不太好，所以进行一个基本的资源隔离和访问，避免内部复杂的低效率的代码，导致大量的线程被hang住

* execution.isolation.strategy
  * 指定了HystrixCommand.run()的资源隔离策略，THREAD或者SEMAPHORE，一种是基于线程池，一种是信号量
  * 线程池机制，每个command运行在一个线程中，限流是通过线程池的大小来控制的
  * 信号量机制，command是运行在调用线程中，但是通过信号量的容量来进行限流
* 如何在线程池和信号量之间做选择？
  * 默认的策略就是线程池
  * 线程池其实最大的好处就是对于网络访问请求，如果有超时的话，可以避免调用线程阻塞住
  * 而使用信号量的场景，通常是针对超大并发量的场景下，每个服务实例每秒都几百的QPS，那么此时你用线程池的话，线程一般不会太多，可能撑不住那么高的并发，如果要撑住，可能要耗费大量的线程资源，那么就是用信号量，来进行限流保护
  * 一般用信号量常见于那种基于纯内存的一些业务逻辑服务，而不涉及到任何网络访问请求
  * netflix有100+的command运行在40+的线程池中，只有少数command是不运行在线程池中的，就是从纯内存中获取一些元数据，或者是对多个command包装起来的facacde command，是用信号量限流的
* command名称和command组
  * 线程池隔离，依赖服务->接口->线程池，如何来划分
    * 你的每个command，都可以设置一个自己的名称，同时可以设置一个自己的组
    * command group，是一个非常重要的概念，默认情况下，因为就是通过command group来定义一个线程池的，而且还会通过command group来聚合一些监控和报警信息
    * 同一个command group中的请求，都会进入同一个线程池中
* command线程池
  * threadpool key代表了一个HystrixThreadPool，用来进行统一监控，统计，缓存
  * 默认的threadpool key就是command group名称
  * 每个command都会跟它的threadpool key对应的thread pool绑定在一起
  * 如果不想直接用command group，也可以手动设置thread pool name
  * command threadpool -> command group -> command key
  * command key，代表了一类command，一般来说，代表了底层的依赖服务的一个接口
  * command group，代表了某一个底层的依赖服务，合理，一个依赖服务可能会暴露出来多个接口，每个接口就是一个command key
  * command group，在逻辑上去组织起来一堆command key的调用，统计信息，成功次数，timeout超时次数，失败次数，可以看到某一个服务整体的一些访问情况
  * command group，一般来说，推荐是根据一个服务去划分出一个线程池，command key默认都是属于同一个线程池的
  * 比如说你以一个服务为粒度，估算出来这个服务每秒的所有接口加起来的整体QPS在100左右
  * 你调用那个服务的当前服务，部署了10个服务实例，每个服务实例上，其实用这个command group对应这个服务，给一个线程池，量大概在10个左右，就可以了，你对整个服务的整体的访问QPS大概在每秒100左右
  * 一般来说，command group是用来在逻辑上组合一堆command的
  * 举个例子，对于一个服务中的某个功能模块来说，希望将这个功能模块内的所有command放在一个group中，那么在监控和报警的时候可以放一起看
  * command group，对应了一个服务，但是这个服务暴露出来的几个接口，访问量很不一样，差异非常之大
  * 你可能就希望在这个服务command group内部，包含的对应多个接口的command key，做一些细粒度的资源隔离
  * 对同一个服务的不同接口，都使用不同的线程池
  * command key -> command group
  * command key -> 自己的threadpool key
  * 逻辑上来说，多个command key属于一个command group，在做统计的时候，会放在一起统计
  * 每个command key有自己的线程池，每个接口有自己的线程池，去做资源隔离和限流
  * 但是对于thread pool资源隔离来说，可能是希望能够拆分的更加一致一些，比如在一个功能模块内，对不同的请求可以使用不同的thread pool
  * command group一般来说，可以是对应一个服务，多个command key对应这个服务的多个接口，多个接口的调用共享同一个线程池
  * 如果说你的command key，要用自己的线程池，可以定义自己的threadpool key，就ok了
* coreSize
  * 设置线程池的大小，默认是10，一般来说，用这个默认的10个线程大小就够了
* queueSizeRejectionThreshold
  * 控制queue满后reject的threshold，因为maxQueueSize不允许热修改，因此提供这个参数可以热修改，控制队列的最大大小
  * HystrixCommand在提交到线程池之前，其实会先进入一个队列中，这个队列满了之后，才会reject
  * 默认值是5
* execution.isolation.semaphore.maxConcurrentRequests
  * 设置使用SEMAPHORE隔离策略的时候，允许访问的最大并发量，超过这个最大并发量，请求直接被reject
  * 这个并发量的设置，跟线程池大小的设置，应该是类似的，但是基于信号量的话，性能会好很多，而且hystrix框架本身的开销会小很多
  * 默认值是10，设置的小一些，否则因为信号量是基于调用线程去执行command的，而且不能从timeout中抽离，因此一旦设置的太大，而且有延时发生，可能瞬间导致tomcat本身的线程资源本占满

## hystrix执行时的8大流程以及内部原理

* 创建command，执行这个command，配置这个command对应的group和线程池，以及线程池/信号量的容量和大小
* 开始执行这个command，调用了这个command的execute()方法以后，hystrix内部的底层的执行流程和步骤以及原理是什么呢？

1. 构建一个HystrixCommand或者HystrixObservableCommand
   * 一个HystrixCommand（主要用于仅仅会返回一个结果的调用）或一个HystrixObservableCommand对象（主要用于可能会返回多条结果的调用），代表了对某个依赖服务发起的一次请求或者调用
   * 构造的时候，可以在构造函数中传入任何需要的参数
2. 调用command的执行方法
   * 执行Command就可以发起一次对依赖服务的调用
   * 要执行Command，需要在4个方法中选择其中的一个：execute()，queue()，observe()，toObservable()
   * 其中execute()和queue()仅仅对HystrixCommand适用
   * K execute()：调用后直接block住，属于同步调用，直到依赖服务返回单条结果，或者抛出异常
     * 获取一个Future.get()，然后拿到单个结果
     * execute()实际上会调用queue().get().queue()，接着会调用toObservable().toBlocking().toFuture()
     * 也就是说，无论是哪种执行command的方式，最终都是依赖toObservable()去执行的
   * Future<K> queue()：返回一个Future，属于异步调用，后面可以通过Future获取单条结果
     * 返回一个Future
   * Observable<K> observe()：订阅一个Observable对象，Observable代表的是依赖服务返回的结果，获取到一个那个代表结果的Observable对象的拷贝对象
     * 立即订阅Observable，然后启动8大执行步骤，返回一个拷贝的Observable，订阅时理解回调给你结果
   * Observable<K> toObservable()：返回一个Observable对象，如果我们订阅这个对象，就会执行command并且获取返回结果
     * 返回一个原始的Observable，必须手动订阅才会去执行8大步骤
3. 检查是否开启缓存
   * 如果这个command开启了请求缓存，request cache，而且这个调用的结果在缓存中存在，那么直接从缓存中返回结果
4. 检查是否开启了短路器
   * 检查这个command对应的依赖服务是否开启了短路器
   * 如果断路器被打开了，那么hystrix就不会执行这个command，而是直接去执行fallback降级机制
5. 检查线程池/队列/semaphore是否已经满了
   * 如果command对应的线程池/队列/semaphore已经满了，那么也不会执行command，而是直接去调用fallback降级机制
6. 执行command
   * 调用HystrixObservableCommand.construct()或HystrixCommand.run()来实际执行这个command
   * HystrixCommand.run()是返回一个单条结果，或者抛出一个异常
   * HystrixObservableCommand.construct()是返回一个Observable对象，可以获取多条结果
   * 如果HystrixCommand.run()或HystrixObservableCommand.construct()的执行，超过了timeout时长的话，那么command所在的线程就会抛出一个TimeoutException
   * 如果timeout了，也会去执行fallback降级机制，而且就不会管run()或construct()返回的值了
   * 这里要注意的一点是，我们是不可能终止掉一个调用严重延迟的依赖服务的线程的，只能说给你抛出来一个TimeoutException，但是还是可能会因为严重延迟的调用线程占满整个线程池的
   * 即使这个时候新来的流量都被限流了。。。
   * 如果没有timeout的话，那么就会拿到一些调用依赖服务获取到的结果，然后hystrix会做一些logging记录和metric统计
7. 短路健康检查
   * Hystrix会将每一个依赖服务的调用成功，失败，拒绝，超时，等事件，都会发送给circuit breaker断路器
   * 短路器就会对调用成功/失败/拒绝/超时等事件的次数进行统计
   * 短路器会根据这些统计次数来决定，是否要进行短路，如果打开了短路器，那么在一段时间内就会直接短路，然后如果在之后第一次检查发现调用成功了，就关闭断路器
8. 调用fallback降级机制
   * 在以下几种情况中，hystrix会调用fallback降级机制：run()或construct()抛出一个异常，短路器打开，线程池/队列/semaphore满了，command执行超时了
   * 一般在降级机制中，都建议给出一些默认的返回值，比如静态的一些代码逻辑，或者从内存中的缓存中提取一些数据，尽量在这里不要再进行网络请求了
   * 即使在降级中，一定要进行网络调用，也应该将那个调用放在一个HystrixCommand中，进行隔离
   * 在HystrixCommand中，上线getFallback()方法，可以提供降级机制
   * 在HystirxObservableCommand中，实现一个resumeWithFallback()方法，返回一个Observable对象，可以提供降级结果
   * 如果fallback返回了结果，那么hystrix就会返回这个结果
   * 对于HystrixCommand，会返回一个Observable对象，其中会发返回对应的结果
   * 对于HystrixObservableCommand，会返回一个原始的Observable对象
   * 如果没有实现fallback，或者是fallback抛出了异常，Hystrix会返回一个Observable，但是不会返回任何数据
   * 不同的command执行方式，其fallback为空或者异常时的返回结果不同
     * 对于execute()，直接抛出异常
     * 对于queue()，返回一个Future，调用get()时抛出异常
     * 对于observe()，返回一个Observable对象，但是调用subscribe()方法订阅它时，理解抛出调用者的onError方法
     * 对于toObservable()，返回一个Observable对象，但是调用subscribe()方法订阅它时，理解抛出调用者的onError方法

![hystrix执行时的8大流程以及内部原理](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/hystrix执行时的8大流程以及内部原理-6653865.png)

## 缓存

* HystrixCommand和HystrixObservableCommand都

* 以指定一个缓存key，然后hystrix会自动进行缓存，接着在同一个request context内，再次访问的时候，就会直接取用缓存

* 用请求缓存，可以避免重复执行网络请求

* 多次调用一个command，那么只会执行一次，后面都是直接取缓存

* 对于请求缓存（request caching），请求合并（request collapsing），请求日志（request log），等等技术，都必须自己管理HystrixReuqestContext的声明周期

  ```java
  // 一般来说，在java web来的应用中，都是通过filter过滤器来实现的
  public class HystrixRequestContextServletFilter implements Filter {
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
       throws IOException, ServletException {
       		// 在一个请求执行之前，都必须先初始化一个request context
          HystrixRequestContext context = HystrixRequestContext.initializeContext();
          try {
              chain.doFilter(request, response);
          } finally {
          	  // 然后在请求结束之后，需要关闭request context
              context.shutdown();
          }
      }
  }
  
  @Bean
  public FilterRegistrationBean indexFilterRegistration() {
      FilterRegistrationBean registration = new FilterRegistrationBean(new IndexFilter());
      registration.addUrlPatterns("/");
      return registration;
  }
  ```

  ```java
  // 对批量查询商品数据的接口，可以用request cache做一个优化，就是说一次请求，就是一次request context，对相同的商品查询只能执行一次，其余的都走request cache
  public class CommandUsingRequestCache extends HystrixCommand<Boolean> {
      private final int value;
  
      protected CommandUsingRequestCache(int value) {
          super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
          this.value = value;
      }
  
      @Override
      protected Boolean run() {
          return value == 0 || value % 2 == 0;
      }
  
      @Override
      protected String getCacheKey() {
          return String.valueOf(value);
      }
  
  }
  
  @Test
  public void testWithCacheHits() {
      HystrixRequestContext context = HystrixRequestContext.initializeContext();
      try {
          CommandUsingRequestCache command2a = new CommandUsingRequestCache(2);
          CommandUsingRequestCache command2b = new CommandUsingRequestCache(2);
  
          assertTrue(command2a.execute());
          // this is the first time we've executed this command with
          // the value of "2" so it should not be from cache
          assertFalse(command2a.isResponseFromCache());
  
          assertTrue(command2b.execute());
          // this is the second time we've executed this command with
          // the same value so it should return from cache
          assertTrue(command2b.isResponseFromCache());
      } finally {
          context.shutdown();
      }
  
      // start a new request context
      context = HystrixRequestContext.initializeContext();
      try {
          CommandUsingRequestCache command3b = new CommandUsingRequestCache(2);
          assertTrue(command3b.execute());
          // this is a new request context so this 
          // should not come from cache
          assertFalse(command3b.isResponseFromCache());
      } finally {
          context.shutdown();
      }
  }
  
  //缓存的手动清理
  public static class GetterCommand extends HystrixCommand<String> {
      private static final HystrixCommandKey GETTER_KEY = HystrixCommandKey.Factory.asKey("GetterCommand");
      private final int id;
  
      public GetterCommand(int id) {
          super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetSetGet"))
                  .andCommandKey(GETTER_KEY));
          this.id = id;
      }
  
      @Override
      protected String run() {
          return prefixStoredOnRemoteDataStore + id;
      }
  
      @Override
      protected String getCacheKey() {
          return String.valueOf(id);
      }
  
      /**
       * Allow the cache to be flushed for this object.
       * 
       * @param id
       *            argument that would normally be passed to the command
       */
      public static void flushCache(int id) {
          HystrixRequestCache.getInstance(GETTER_KEY,
                  HystrixConcurrencyStrategyDefault.getInstance()).clear(String.valueOf(id));
      }
  
  }
  
  public static class SetterCommand extends HystrixCommand<Void> {
  
      private final int id;
      private final String prefix;
  
      public SetterCommand(int id, String prefix) {
          super(HystrixCommandGroupKey.Factory.asKey("GetSetGet"));
          this.id = id;
          this.prefix = prefix;
      }
  
      @Override
      protected Void run() {
          // persist the value against the datastore
          prefixStoredOnRemoteDataStore = prefix;
          // flush the cache
          GetterCommand.flushCache(id);
          // no return value
          return null;
      }
  }
  ```

  

## 限流

- 如何限流？在工作中是怎么做的？说一下具体的实现？

## 熔断

- 如何进行熔断？熔断框架都有哪些？具体实现原理知道吗？

## 降级

- 如何进行降级？（fallback降级机制）

  - hystrix调用各种接口，或者访问外部依赖，mysql，redis，zookeeper，kafka，等等，如果出现了任何异常的情况
  - 比如说报错了，访问mysql报错，redis报错，zookeeper报错，kafka报错，error
  - 对每个外部依赖，无论是服务接口，中间件，资源隔离，对外部依赖只能用一定量的资源去访问，线程池/信号量，如果资源池已满，reject
  - 访问外部依赖的时候，访问时间过长，可能就会导致超时，报一个TimeoutException异常，timeout
  - 上述三种情况，都是我们说的异常情况，对外部依赖的东西访问的时候出现了异常，发送异常事件到短路器中去进行统计
  - 如果短路器发现异常事件的占比达到了一定的比例，直接开启短路，circuit breaker
  - 上述四种情况，都会去调用fallback降级机制
  - fallback，降级机制，你之前都是必须去调用外部的依赖接口，或者从mysql中去查询数据的，但是为了避免说可能外部依赖会有故障
  - 比如，你可以再内存中维护一个ehcache，作为一个纯内存的基于LRU自动清理的缓存，数据也可以放入缓存内
  - 如果说外部依赖有异常，fallback这里，直接尝试从ehcache中获取数据
  - 比如说，本来你是从mysql，redis，或者其他任何地方去获取数据的，获取调用其他服务的接口的，结果人家故障了，人家挂了，fallback，可以返回一个默认值
  - 两种最经典的降级机制：纯内存数据，默认值
  - run()抛出异常，超时，线程池或信号量满了，或短路了，都会调用fallback机制
  - 给大家举个例子，比如说我们现在有个商品数据，brandId，品牌，一般来说，假设，正常的逻辑，拿到了一个商品数据以后，用brandId再调用一次请求，到其他的服务去获取品牌的最新名称
  - 假如说，那个品牌服务挂掉了，那么我们可以尝试本地内存中，会保留一份时间比较过期的一份品牌数据，有些品牌没有，有些品牌的名称过期了，Nike++，Nike
  - 调用品牌服务失败了，fallback降级就从本地内存中获取一份过期的数据，先凑合着用着

  ```java
  public class CommandHelloFailure extends HystrixCommand<String> {
      private final String name;
      public CommandHelloFailure(String name) {
          super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
          this.name = name;
      }
  
      @Override
      protected String run() {
          throw new RuntimeException("this command always fails");
      }
  
      @Override
      protected String getFallback() {
          return "Hello Failure " + name + "!";
      }
  }
  
  @Test
  public void testSynchronous() {
      assertEquals("Hello Failure World!", new CommandHelloFailure("World").execute());
  }
  ```
  - HystrixObservableCommand，是实现resumeWithFallback方法

* fallback.isolation.semaphore.maxConcurrentRequests
  * 这个参数设置了HystrixCommand.getFallback()最大允许的并发请求数量，默认值是10，也是通过semaphore信号量的机制去限流
  * 如果超出了这个最大值，那么直接被reject

## 短路器深入的工作原理

1. 如果经过短路器的流量超过了一定的阈值，HystrixCommandProperties.circuitBreakerRequestVolumeThreshold()
   * 举个例子，可能看起来是这样子的，要求在10s内，经过短路器的流量必须达到20个；在10s内，经过短路器的流量才10个，那么根本不会去判断要不要短路
2. 如果断路器统计到的异常调用的占比超过了一定的阈值，HystrixCommandProperties.circuitBreakerErrorThresholdPercentage()
   * 如果达到了上面的要求，比如说在10s内，经过短路器的流量（你，只要执行一个command，这个请求就一定会经过短路器），达到了30个；同时其中异常的访问数量，占到了一定的比例，比如说60%的请求都是异常（报错，timeout，reject），会开启短路
3. 然后断路器从close状态转换到open状态
4. 断路器打开的时候，所有经过该断路器的请求全部被短路，不调用后端服务，直接走fallback降级
5. 经过了一段时间之后，HystrixCommandProperties.circuitBreakerSleepWindowInMilliseconds()，会half-open，让一条请求经过短路器，看能不能正常调用。如果调用成功了，那么就自动恢复，转到close状态
   * 短路器，会自动恢复的，half-open，半开状态
6. circuit breaker短路器的配置
   1. circuitBreaker.enabled
      * 控制短路器是否允许工作，包括跟踪依赖服务调用的健康状况，以及对异常情况过多时是否允许触发短路，默认是true
   2. circuitBreaker.requestVolumeThreshold
      * 设置一个rolling window，滑动窗口中，最少要有多少个请求时，才触发开启短路
      * 举例来说，如果设置为20（默认值），那么在一个10秒的滑动窗口内，如果只有19个请求，即使这19个请求都是异常的，也是不会触发开启短路器的
   3. circuitBreaker.sleepWindowInMilliseconds
      * 设置在短路之后，需要在多长时间内直接reject请求，然后在这段时间之后，再重新导holf-open状态，尝试允许请求通过以及自动恢复，默认值是5000毫秒
   4. circuitBreaker.errorThresholdPercentage
      * 设置异常请求量的百分比，当异常请求达到这个百分比时，就触发打开短路器，默认是50，也就是50%
   5. circuitBreaker.forceOpen
      * 如果设置为true的话，直接强迫打开短路器，相当于是手动短路了，手动降级，默认false
   6. circuitBreaker.forceClosed
      * 如果设置为ture的话，直接强迫关闭短路器，相当于是手动停止短路了，手动升级，默认false
   7. 实战演练
      * 配置一个断路器，流量要求是20，异常比例是50%，短路时间是5s
      * 在command内加入一个判断，如果是productId=-1，那么就直接报错，触发异常执行
      * 写一个client测试程序，写入50个请求，前20个是正常的，但是后30个是productId=-1，然后继续请求，会发现

## 线程池隔离技术的设计原则

* Hystrix采取了bulkhead舱壁隔离技术，来将外部依赖进行资源隔离，进而避免任何外部依赖的故障导致本服务崩溃
* 线程池隔离，学术名称：bulkhead，舱壁隔离
* 外部依赖的调用在单独的线程中执行，这样就能跟调用线程隔离开来，避免外部依赖调用timeout耗时过长，导致调用线程被卡死
* Hystrix对每个外部依赖用一个单独的线程池，这样的话，如果对那个外部依赖调用延迟很严重，最多就是耗尽那个依赖自己的线程池而已，不会影响其他的依赖调用
* Hystrix选择用线程池机制来进行资源隔离，要面对的场景如下：
  1. 每个服务都会调用几十个后端依赖服务，那些后端依赖服务通常是由很多不同的团队开发的
  2. 每个后端依赖服务都会提供它自己的client调用库，比如说用thrift的话，就会提供对应的thrift依赖
  3. client调用库随时会变更
  4. client调用库随时可能会增加新的网络请求的逻辑
  5. client调用库可能会包含诸如自动重试，数据解析，内存中缓存等逻辑
  6. client调用库一般都对调用者来说是个黑盒，包括实现细节，网络访问，默认配置，等等
  7. 在真实的生产环境中，经常会出现调用者，突然间惊讶的发现，client调用库发生了某些变化
  8. 即使client调用库没有改变，依赖服务本身可能有会发生逻辑上的变化
  9. 有些依赖的client调用库可能还会拉取其他的依赖库，而且可能那些依赖库配置的不正确
  10. 大多数网络请求都是同步调用的
  11. 调用失败和延迟，也有可能会发生在client调用库本身的代码中，不一定就是发生在网络请求中
* 简单来说，就是你必须默认client调用库就很不靠谱，而且随时可能各种变化，所以就要用强制隔离的方式来确保任何服务的故障不能影响当前服务
* 线程池机制的优点如下：
  1. 任何一个依赖服务都可以被隔离在自己的线程池内，即使自己的线程池资源填满了，也不会影响任何其他的服务调用
  2. 服务可以随时引入一个新的依赖服务，因为即使这个新的依赖服务有问题，也不会影响其他任何服务的调用
  3. 当一个故障的依赖服务重新变好的时候，可以通过清理掉线程池，瞬间恢复该服务的调用，而如果是tomcat线程池被占满，再恢复就很麻烦
  4. 如果一个client调用库配置有问题，线程池的健康状况随时会报告，比如成功/失败/拒绝/超时的次数统计，然后可以近实时热修改依赖服务的调用配置，而不用停机
  5. 如果一个服务本身发生了修改，需要重新调整配置，此时线程池的健康状况也可以随时发现，比如成功/失败/拒绝/超时的次数统计，然后可以近实时热修改依赖服务的调用配置，而不用停机
  6. 基于线程池的异步本质，可以在同步的调用之上，构建一层异步调用层
     * 简单来说，最大的好处，就是资源隔离，确保说，任何一个依赖服务故障，不会拖垮当前的这个服务
* 线程池机制的缺点：
  1. 线程池机制最大的缺点就是增加了cpu的开销，除了tomcat本身的调用线程之外，还有hystrix自己管理的线程池
  2. 每个command的执行都依托一个独立的线程，会进行排队，调度，还有上下文切换
  3. Hystrix官方自己做了一个多线程异步带来的额外开销，通过对比多线程异步调用+同步调用得出，Netflix API每天通过hystrix执行10亿次调用，每个服务实例有40个以上的线程池，每个线程池有10个左右的线程
  4. 最后发现说，用hystrix的额外开销，就是给请求带来了3ms左右的延时，最多延时在10ms以内，相比于可用性和稳定性的提升，这是可以接受的
* 可以用hystrix semaphore技术来实现对某个依赖服务的并发访问量的限制，而不是通过线程池/队列的大小来限制流量
  * sempahore技术可以用来限流和削峰，但是不能用来对调研延迟的服务进行timeout和隔离
  * execution.isolation.strategy，设置为SEMAPHORE，那么hystrix就会用semaphore机制来替代线程池机制，来对依赖服务的访问进行限流
  * 如果通过semaphore调用的时候，底层的网络调用延迟很严重，那么是无法timeout的，只能一直block住
  * 一旦请求数量超过了semephore限定的数量之后，就会立即开启限流

* 接口限流实验
  * 假设，一个线程池，大小是15个，队列大小是10个，timeout时长设置的长一些，5s
  * 模拟发送请求，然后写死代码，在command内部做一个sleep，比如每次sleep 1s，10个请求发送过去以后，直接被hang死，线程池占满
  * 再发送请求，就会堵塞在缓冲队列，queue，10个，20个，10个，后10个应该就直接reject，fallback逻辑
  * 15 + 10 = 25个请求，15在执行，10个缓冲在队列里了，剩下的流量全部被reject，限流，降级
  * withCoreSize：设置你的线程池的大小
  * withMaxQueueSize：设置的是你的等待队列，缓冲队列的大小
  * withQueueSizeRejectionThreshold：如果withMaxQueueSize<withQueueSizeRejectionThreshold，那么取的是withMaxQueueSize，反之，取得是withQueueSizeRejectionThreshold
  * 线程池本身的大小，如果你不设置另外两个queue相关的参数，等待队列是关闭的
  * queue大小，等待队列的大小，timeout时长
  * 先进去线程池的是10个请求，然后有8个请求进入等待队列，线程池里有空闲，等待队列中的请求如果还没有timeout，那么就进去线程池去执行
  * 10 + 8 = 18个请求之外，7个请求，直接会被reject掉，限流，fallback
  * withExecutionTimeoutInMilliseconds(20000)：timeout也设置大一些，否则如果请求放等待队列中时间太长了，直接就会timeout，等不到去线程池里执行了
  * withFallbackIsolationSemaphoreMaxConcurrentRequests(30)：fallback，sempahore限流，30个，避免太多的请求同时调用fallback被拒绝访问

## 超时

* execution.isolation.thread.timeoutInMilliseconds
  * 手动设置timeout时长，一个command运行超出这个时间，就被认为是timeout，然后将hystrix command标识为timeout，同时执行fallback降级逻辑
  * 默认是1000，也就是1000毫秒
* execution.timeout.enabled
  * 控制是否要打开timeout机制，默认是true