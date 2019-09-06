# Readme

```java
// 分页  byton 请求（page，pageSize）、返回（总数和当前页面数据量）
// 业务
  商品，有些字段不由前端传过来，而是再次从数据库查询，然后更新，如商品单价、库存等
  判断状态（特定状态下才能改状态），再更改状态
  限制只能自己查自己的订单
// if(CollectionUtils.isEmpty(orderDTO.getOrderDetailList()){...}  
// 计算总价
BigDecimal orderAmount = new BigDecimal(BigInteger.ZERO);
for(...) {
  orderAmount = productInfo.getProductPrice()
		.multiply(new BigDecimal(orderDetail.getProductQuantity()))
		.add(orderAmount);
}
// 尽量提取出公共逻辑
// 操作订单详情时：
	//请求参数为orderId和openId，根据orderId查出订单后，还要判断订单的openId是否等于传入的openId
	// 然后再进行订单的其他操作，如返回订单详情、取消订单（还要先判断订单状态）等
//String fileName = String.format("%s.%s", UUID.randomUUID().toString(), multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1));
// redisTemplate.opsForValue().set(String.format("token_%s", token), openid, expire, TimeUnit.SECONDS);
// 比较两个金额是否相等：比较差值的绝对值是否小于0.01。Math.abs(d1 - d2);
```

## git

```shell
# 在git上创建一个新仓库 https://github.com/xiaojieWoody/jvm_queue.git
# 将本地代码推送到新仓库
echo "# jvm_queue" >> README.md
git init
git add .
git commit -m "init"
git remote add origin https://github.com/xiaojieWoody/jvm_queue.git
git push -u origin master
```

## RPC与REST

* 耦合性
  * RPC：强耦合，服务端与客户端必须以特定的格式
  * REST：松散耦合
* 消息协议
  * RPC：二进制，thrift、protobuf
  * REST：文本，XML、JSON
* 通信协议
  * RPC：TCP
  * REST：HTTP
* 性能
  * RPC：高
  * REST：一般低于RPC
* 接口契约IDL
  * RPC：thrift、protobuf IDL
  * REST：Swagger
* 客户端
  * RPC：强类型客户端，一般自动生成、多语言
  * REST：一般HTTP客户端可访问，可自动生成强类型客户端、多语言
* 案例
  * RPC：Dubbo、motan、tars、grpc、thrift
  * REST：SpringMVC、SpringBoot
* 开发者友好
  * RPC：客户端比较方便，但二进制消息不可读
  * REST：文本消费开发者可读，浏览器就可访问
* 对外开放
  * RPC：对外一般需要转换成REST/文本协议
  * REST：直接可以对外开放

## 分布式事务

* 两阶段提交：询问和提交
  * 事务管理器负责协调多个数据库的事务，先询问每个数据库是否准备好，如果每个数据库都回复ok，那么就正式提交事务，如果有一个没回复ok则回滚事务
  * 适用场景：单块应用里，跨多个数据库的分布式事务，而且严重依赖于数据库层面来搞定复杂的事务，效率低，不适合高并发的场景
  * 基于Sping+JTA
  * 分布式系统规范和规定：要求每个服务只能操作自己对应的一个数据库，不允许直接连接别的服务的数据库，否则没法管理，如果需要操作其他服务的数据库，必须调用相应服务的接口来实现

* TCC方案：Try、Confirm、Cancel
  * Try阶段：对各个服务的资源做检测以及对资源进行锁定或者预留
  * Confirm阶段：在各个服务中执行实际的操作
  * Cancel阶段：如果任何一个服务的业务方法执行出错，那么这里就需要进行补偿，就是执行已经执行成功的业务逻辑的回滚操作
  * 事务回滚实际上是严重依赖于自己写代码来回滚和补偿了，会造成补偿代码巨大，非常之繁琐，业务代码很难维护
  * 一般来说跟钱相关的，跟钱打交道的，支付、交易相关的场景，会用TCC，最好是各个业务执行的时间都比较短

* 本地消息表
  * 国外的ebay
  * A系统在自己本地一个事务里操作同时，插入一条数据到消息表
  * 接着A系统将这个消息发送到MQ中去
  * B系统接收到消息之后，在一个事务里，往自己本地消息表里插入一条数据，同时执行其他的业务操作，如果这个消息已经被处理过了，那么此时这个事务会回滚，这样保证不会重复处理消息
  * B系统执行成功之后，就会更新自己本地消息表的状态以及A系统消息表的状态（可通过zk）
  * 如果B系统处理失败了，那么就不会更新消息表状态，那么此时A系统会定时扫描自己的消息表，如果有没处理的消息，会再次发送到MQ中去，让B再次处理
  * 这个方案保证了最终一致性，哪怕B事务失败了，但是A会不断重发消息，直到B那边成功为止
  * 最大的问题就在于严重依赖于数据库的消息表来管理事务，高并发场景很难扩展
* 可靠消息最终一致性方案
  * 不要用本地的消息表了，直接基于MQ来实现事务。比如阿里的RocketMQ就支持消息事务
  * A系统先发送一个prepared消息到mq，如果这个prepared消息发送失败那么就直接取消操作别执行了
  * 如果这个消息发送成功过了，那么接着执行本地事务，如果成功就告诉mq发送确认消息，如果失败就告诉mq回滚消息
  * 如果发送了确认消息，那么此时B系统会接收到确认消息，然后执行本地的事务
  * mq会自动定时轮询所有prepared消息回调你的接口，问你，这个消息是不是本地事务处理失败了，所有没发送确认消息？那是继续重试还是回滚？一般来说这里你就可以查下数据库看之前本地事务是否执行，如果回滚了，那么这里也回滚吧。这个就是避免可能本地事务执行成功了，别确认消息发送失败了
  * 这个方案里，要是系统B的事务失败了咋办？重试咯，自动不断重试直到成功，如果实在是不行，要么就是针对重要的资金类业务进行回滚，比如B系统本地回滚后，想办法通知系统A也回滚；或者是发送报警由人工来手工回滚和补偿
  * 目前国内互联网公司大都是这么玩儿的，要不举用RocketMQ支持的，要不就自己基于类似ActiveMQ？RabbitMQ？自己封装一套类似的逻辑出来，总之思路就是这样子的
  * ![06_可靠消息最终一致性方案](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/06_可靠消息最终一致性方案.png)
* 最大努力通知方案
  * 系统A本地事务执行完之后，发送个消息到MQ
    * 这里会有个专门消费MQ的最大努力通知服务，这个服务会消费MQ然后写入数据库中记录下来，或者是放入个内存队列也可以，接着调用系统B的接口
  * 要是系统B执行成功就ok了；要是系统B执行失败了，那么最大努力通知服务就定时尝试重新调用系统B，反复N次，最后还是不行就放弃
* byton做法

  * 利用redis
* 特别严格的场景，用的是TCC来保证强一致性；然后其他的一些场景基于了阿里的RocketMQ来实现了分布式事务
  * 严格资金要求绝对不能错的场景，可以说是用的TCC方案；如果是一般的分布式事务场景，订单插入之后要调用库存服务更新库存，库存数据没有资金那么的敏感，可以用可靠消息最终一致性方案
  * rocketmq 3.2.6之前的版本，是可以按照上面的思路来的，但是之后接口做了一些改变
* 其实用任何一个分布式事务的这么一个方案，都会导致那块儿代码会复杂10倍。很多情况下，系统A调用系统B、系统C、系统D，可能根本就不做分布式事务。如果调用报错会打印异常日志
* 每个月也就那么几个bug，很多bug是功能性的，体验性的，真的是涉及到数据层面的一些bug，一个月就几个，两三个？如果为了确保系统自动保证数据100%不能错，上了几十个分布式事务，代码太复杂；性能太差，系统吞吐量、性能大幅度下跌
* 99%的分布式接口调用，不要做分布式事务，直接就是监控（发邮件、发短信）、记录日志（一旦出错，完整的日志）、事后快速的定位、排查和出解决方案、修复数据
* 每个月，每隔几个月，都会对少量的因为代码bug，导致出错的数据，进行人工的修复数据，自己临时动手写个程序，可能要补一些数据，可能要删除一些数据，可能要修改一些字段的值
* 比做50个分布式事务，成本要来的低上百倍，低几十倍
* trade off，权衡，要用分布式事务的时候，一定是有成本，代码会很复杂，开发很长时间，性能和吞吐量下跌，系统更加复杂更加脆弱反而更加容易出bug；好处，如果做好了，TCC、可靠消息最终一致性方案，一定可以100%保证你那快数据不会出错
* 1%，0.1%，0.01%的业务，资金、交易、订单，会用分布式事务方案来保证，会员积分、优惠券、商品信息，其实不要这么搞了

## 数据库和缓存双写一致性

1. 读的时候，先读缓存，缓存没有的话，那么就读数据库，然后取出数据后放入缓存，同时返回响应

2. 更新的时候，先删除缓存，然后再更新数据库

* 为什么是删除缓存，而不是更新缓存呢？
  * 原因很简单，很多时候，复杂点的缓存的场景，因为缓存有的时候，不简单是数据库中直接取出来的值
    * 商品详情页的系统，修改库存，只是修改了某个表的某些字段，但是要真正把这个影响的最终的库存计算出来，可能还需要从其他表查询一些数据，然后进行一些复杂的运算，才能最终计算出
    * 现在最新的库存是多少，然后才能将库存更新到缓存中去
    * 比如可能更新了某个表的一个字段，然后其对应的缓存，是需要查询另外两个表的数据，并进行运算，才能计算出缓存最新的值的
  * 更新缓存的代价是很高的
    * 是不是说，每次修改数据库的时候，都一定要将其对应的缓存去更新一份？也许有的场景是这样的，但是对于比较复杂的缓存数据计算的场景，就不是这样了
    * 如果频繁修改一个缓存涉及的多个表，那么这个缓存会被频繁的更新，频繁的更新缓存
    * 但是问题在于，这个缓存到底会不会被频繁访问到？？？
    * 举个例子，一个缓存涉及的表的字段，在1分钟内就修改了20次，或者是100次，那么缓存跟新20次，100次; 但是这个缓存在1分钟内就被读取了1次，有大量的冷数据
    * 28法则，黄金法则，20%的数据，占用了80%的访问量
    * 实际上，如果你只是删除缓存的话，那么1分钟内，这个缓存不过就重新计算一次而已，开销大幅度降低
    * 每次数据过来，就只是删除缓存，然后修改数据库，如果这个缓存，在1分钟内只是被访问了1次，那么只有那1次，缓存是要被重新计算的，用缓存才去算缓存
    * 其实删除缓存，而不是更新缓存，就是一个lazy计算的思想，不要每次都重新做复杂的计算，不管它会不会用到，而是让它到需要被使用的时候再重新计算
    * 查询一个部门，部门带了一个员工的list，没有必要说每次查询部门，都里面的1000个员工的数据也同时查出来啊
    * 80%的情况，查这个部门，就只是要访问这个部门的信息就可以了
    * 先查部门，同时要访问里面的员工，那么这个时候只有在你要访问里面的员工的时候，才会去数据库里面查询1000个员工
* 高并发场景下的缓存+数据库双写不一致问题分析与解决方案设计
  1. 最初级的缓存不一致问题以及解决方案
     * 问题：先修改数据库，再删除缓存，如果删除缓存失败了，那么会导致数据库中是新数据，缓存中是旧数据，数据出现不一致
       * 解决思路：先删除缓存，再修改数据库，如果删除缓存成功了，如果修改数据库失败了，那么数据库中是旧数据，缓存中是空的，那么数据不会不一致。因为读的时候缓存没有，则读数据库中旧数据，然后更新到缓存中
  2. 比较复杂的数据不一致问题分析
     * 数据发生了变更，先删除了缓存，然后要去修改数据库，此时还没修改，一个请求过来，去读缓存，发现缓存空了，去查询数据库，查到了修改前的旧数据，放到了缓存中，数据变更的程序完成了数据库的修改，数据库和缓存中的数据不一样了
     * 只有在对一个数据在并发的进行读写的时候，才可能会出现这种问题
       * 其实如果并发量很低的话，特别是读并发很低，每天访问量就1万次，那么很少的情况下，会出现刚才描述的那种不一致的场景
       * 但是问题是，如果每天的是上亿的流量，每秒并发读是几万，每秒只要有数据更新的请求，就可能会出现上述的数据库+缓存不一致的情况
     * 数据库与缓存更新与读取操作进行异步串行化
       * 更新数据的时候，根据数据的唯一标识，将操作路由之后，发送到一个jvm内部的队列中
       * 一个队列对应一个工作线程，每个工作线程串行拿到对应的操作，然后一条一条的执行，这样的话，一个数据变更的操作，先执行，删除缓存，然后再去更新数据库，但是还没完成更新
       * 此时如果一个读请求过来，读到了空的缓存，那么可以先将缓存更新的请求发送到队列中，此时会在队列中积压，然后同步等待缓存更新完成
       * 这里有一个优化点，一个队列中，其实多个更新缓存请求串在一起是没意义的，因此可以做过滤，如果发现队列中已经有一个更新缓存的请求了，那么就不用再放个更新请求操作进去了，直接等待前面的更新操作请求完成即可
       * 待那个队列对应的工作线程完成了上一个操作的数据库的修改之后，才会去执行下一个操作，也就是缓存更新的操作，此时会从数据库中读取最新的值，然后写入缓存中
       * 如果请求还在等待时间范围内，不断轮询发现可以取到值了，那么就直接返回; 如果请求等待的时间超过一定时长，那么这一次直接从数据库中读取当前的旧值
     * 高并发的场景下，该解决方案要注意的问题
       1. 读请求长时阻塞
          * 由于读请求进行了非常轻度的异步化，所以一定要注意读超时的问题，每个读请求必须在超时时间范围内返回
          * 该解决方案，最大的风险点在于说，可能数据更新很频繁，导致队列中积压了大量更新操作在里面，然后读请求会发生大量的超时，最后导致大量的请求直接走数据库，务必通过一些模拟真实的测试，看看更新数据的频繁是怎样的
          * 另外一点，因为一个队列中，可能会积压针对多个数据项的更新操作，因此需要根据自己的业务情况进行测试，可能需要部署多个服务，每个服务分摊一些数据的更新操作
          * 如果一个内存队列里居然会挤压100个商品的库存修改操作，每隔库存修改操作要耗费10ms区完成，那么最后一个商品的读请求，可能等待10 * 100 = 1000ms = 1s后，才能得到数据，这个时候就导致读请求的长时阻塞
          * 一定要做根据实际业务系统的运行情况，去进行一些压力测试，和模拟线上环境，去看看最繁忙的时候，内存队列可能会挤压多少更新操作，可能会导致最后一个更新操作对应的读请求，会hang多少时间，如果读请求在200ms返回，如果你计算过后，哪怕是最繁忙的时候，积压10个更新操作，最多等待200ms，那还可以的
          * 如果一个内存队列可能积压的更新操作特别多，那么你就要加机器，让每个机器上部署的服务实例处理更少的数据，那么每个内存队列中积压的更新操作就会越少
          * 其实根据之前的项目经验，一般来说数据的写频率是很低的，因此实际上正常来说，在队列中积压的更新操作应该是很少的
          * 针对读高并发，读缓存架构的项目，一般写请求相对读来说，是非常非常少的，每秒的QPS能到几百就不错了
          * 一秒，500的写操作，5份，每200ms，就100个写操作
          * 单机器，20个内存队列，每个内存队列，可能就积压5个写操作，每个写操作性能测试后，一般在20ms左右就完成
          * 那么针对每个内存队列中的数据的读请求，也就最多hang一会儿，200ms以内肯定能返回了
          * 写QPS扩大10倍，但是经过刚才的测算，就知道，单机支撑写QPS几百没问题，那么就扩容机器，扩容10倍的机器，10台机器，每个机器20个队列，200个队列
          * 大部分的情况下，应该是这样的，大量的读请求过来，都是直接走缓存取到数据的
          * 少量情况下，可能遇到读跟数据更新冲突的情况，如上所述，那么此时更新操作如果先入队列，之后可能会瞬间来了对这个数据大量的读请求，但是因为做了去重的优化，所以也就一个更新缓存的操作跟在它后面
          * 等数据更新完了，读请求触发的缓存更新操作也完成，然后临时等待的读请求全部可以读到缓存中的数据
       2. 读请求并发量过高
          * 这里还必须做好压力测试，确保恰巧碰上上述情况的时候，还有一个风险，就是突然间大量读请求会在几十毫秒的延时hang在服务上，看服务能不能抗的住，需要多少机器才能抗住最大的极限情况的峰值
          * 但是因为并不是所有的数据都在同一时间更新，缓存也不会同一时间失效，所以每次可能也就是少数数据的缓存失效了，然后那些数据对应的读请求过来，并发量应该也不会特别大
          * 按1:99的比例计算读和写的请求，每秒5万的读QPS，可能只有500次更新操作
          * 如果一秒有500的写QPS，那么要测算好，可能写操作影响的数据有500条，这500条数据在缓存中失效后，可能导致多少读请求，发送读请求到库存服务来，要求更新缓存
          * 一般来说，1:1，1:2，1:3，每秒钟有1000个读请求，会hang在库存服务上，每个读请求最多hang多少时间，200ms就会返回
          * 在同一时间最多hang住的可能也就是单机200个读请求，同时hang住
          * 单机hang200个读请求，还是ok的
          * 1:20，每秒更新500条数据，这500秒数据对应的读请求，会有20 * 500 = 1万
          * 1万个读请求全部hang在库存服务上，就死定了
       3. 多服务实例部署的请求路由
          * 可能这个服务部署了多个实例，那么必须保证说，执行数据更新操作，以及执行缓存更新操作的请求，都通过nginx服务器路由到相同的服务实例上
       4. 热点商品的路由问题，导致请求的倾斜
          * 万一某个商品的读写请求特别高，全部打到相同的机器的相同的队列里面去了，可能造成某台机器的压力过大
          * 就是说，因为只有在商品数据更新的时候才会清空缓存，然后才会导致读写并发，所以更新频率不是太高的话，这个问题的影响并不是特别大
          * 但是的确可能某些机器的负载会高一些

![image-20190818153531661](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190818153531661.png)

* 更新数据库的请求和读数据库来更新缓存的请求都放到内存队列中，具有先后执行顺序
* 面试题
  * 如何保证缓存与数据库的双写一致性
    * 一般来说，如果系统不是严格要求缓存+数据库必须一致性的话，缓存可以稍微的根数据库偶尔有不一致的情况，最好不要做这个方案，读请求和写请求串行化，串到一个内存队列里去，这样就可以保证一定不会出现不一致的的情况
    * 串行化之后，就会导致系统的吞吐量会大幅度的降低，用比正常情况下多几倍的机器去支撑线上的一个请求

* 实现缓存与数据库双写一致性保障方案

  ```java
  // Application#servletListenerRegistrationBean
  // InitListener
  // RequestProcessorThread
  // RequestProcessorThreadPool
  // Request
  // RequestQueue
  ```
  
  ```java
  更新数据的时候，根据数据的唯一标识，将操作路由之后，发送到一个jvm内部的队列中
  
  读取数据的时候，如果发现数据不在缓存中，那么将重新读取数据+更新缓存的操作，根据唯一标识路由之后，也发送同一个jvm内部的队列中
  
  一个队列对应一个工作线程
  
  每个工作线程串行拿到对应的操作，然后一条一条的执行
  
  这样的话，一个数据变更的操作，先执行，删除缓存，然后再去更新数据库，但是还没完成更新
  
  此时如果一个读请求过来，读到了空的缓存，那么可以先将缓存更新的请求发送到队列中，此时会在队列中积压，然后同步等待缓存更新完成
  
  这里有一个优化点，一个队列中，其实多个更新缓存请求串在一起是没意义的，因此可以做过滤，如果发现队列中已经有一个更新缓存的请求了，那么就不用再放个更新请求操作进去了，直接等待前面的更新操作请求完成即可
  
  待那个队列对应的工作线程完成了上一个操作的数据库的修改之后，才会去执行下一个操作，也就是缓存更新的操作，此时会从数据库中读取最新的值，然后写入缓存中
  
  如果请求还在等待时间范围内，不断轮询发现可以取到值了，那么就直接返回; 如果请求等待的时间超过一定时长，那么这一次直接从数据库中读取当前的旧值
  
  int h;
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  
  (queueNum - 1) & hash
  
  1、线程池+内存队列初始化
  
  java web应用，做系统的初始化，一般在哪里做呢？
  
  ServletContextListener里面做，listener，会跟着整个web应用的启动，就初始化，类似于线程池初始化的构建
  
  spring boot应用，Application，搞一个listener的注册
  
  2、两种请求对象封装
  
  3、请求异步执行Service封装
  
  4、请求处理的工作线程封装
  
  5、两种请求Controller接口封装
  
  6、读请求去重优化
  
  如果一个读请求过来，发现前面已经有一个写请求和一个读请求了，那么这个读请求就不需要压入队列中了
  
  因为那个写请求肯定会更新数据库，然后那个读请求肯定会从数据库中读取最新数据，然后刷新到缓存中，自己只要hang一会儿就可以从缓存中读到数据了
  
  7、空数据读请求过滤优化
  
  可能某个数据，在数据库里面压根儿就没有，那么那个读请求是不需要放入内存队列的，而且读请求在controller那一层，直接就可以返回了，不需要等待
  
  如果数据库里都没有，就说明，内存队列里面如果没有数据库更新的请求的话，一个读请求过来了，就可以认为是数据库里就压根儿没有数据吧
  
  如果缓存里没数据，就两个情况，第一个是数据库里就没数据，缓存肯定也没数据; 第二个是数据库更新操作过来了，先删除了缓存，此时缓存是空的，但是数据库里是有的
  
  但是的话呢，我们做了之前的读请求去重优化，用了一个flag map，只要前面有数据库更新操作，flag就肯定是存在的，你只不过可以根据true或false，判断你前面执行的是写请求还是读请求
  
  但是如果flag压根儿就没有呢，就说明这个数据，无论是写请求，还是读请求，都没有过
  
  那这个时候过来的读请求，发现flag是null，就可以认为数据库里肯定也是空的，那就不会去读取了
  
  或者说，我们也可以认为每个商品有一个最最初始的库存，但是因为最初始的库存肯定会同步到缓存中去的，有一种特殊的情况，就是说，商品库存本来在redis中是有缓存的
  
  但是因为redis内存满了，就给干掉了，但是此时数据库中是有值得
  
  那么在这种情况下，可能就是之前没有任何的写请求和读请求的flag的值，此时还是需要从数据库中重新加载一次数据到缓存中的
  
  8、深入的去思考优化代码的漏洞
  
  我的一些思考，如果大家发现了其他的漏洞，随时+我Q跟我交流一下
  
  一个读请求过来，将数据库中的数刷新到了缓存中，flag是false，然后过了一会儿，redis内存满了，自动删除了这个额缓存
  
  下一次读请求再过来，发现flag是false，就不会去执行刷新缓存的操作了
  
  而是hang在哪里，反复循环，等一会儿，发现在缓存中始终查询不到数据，然后就去数据库里查询，就直接返回了
  
  这种代码，就有可能会导致，缓存永远变成null的情况
  
  最简单的一种，就是在controller这一块，如果在数据库中查询到了，就刷新到缓存里面去，以后的读请求就又可以从缓存里面读了
  
  队列
  
  对一个商品的库存的数据库更新操作已经在内存队列中了
  
  然后对这个商品的库存的读取操作，要求读取数据库的库存数据，然后更新到缓存中，多个读
  
  这多个读，其实只要有一个读请求操作压到队列里就可以了
  
  其他的读操作，全部都wait那个读请求的操作，刷新缓存，就可以读到缓存中的最新数据了
  
  如果读请求发现redis缓存中没有数据，就会发送读请求给库存服务，但是此时缓存中为空，可能是因为写请求先删除了缓存，也可能是数据库里压根儿没这条数据
  
  如果是数据库中压根儿没这条数据的场景，那么就不应该将读请求操作给压入队列中，而是直接返回空就可以了
  
  都是为了减少内存队列中的请求积压，内存队列中积压的请求越多，就可能导致每个读请求hang住的时间越长，也可能导致多个读请求被hang住
  ```
## JVM内存队列

```java
// Application#servletListenerRegistrationBean
// InitListener
// RequestProcessorThread
// RequestProcessorThreadPool
// Request
// RequestQueue
```

## Java8

* lambda表达式

```java
	// 将List对象中的某个属性取出到另一个List里
  List<Integer> categoryTypeList = productInfoList.stream()
    // .map(e -> e.getCategoryType())
    .map(ProductInfo::getCategoryType)
    .collect(Collectors.toList());
  // 提取某List对象中的几个几个属性去组成另一个List对象
  List<DecreaseStockInput> decreaseStockInputList = orderDTO.getOrderDetailList().stream()
  	.map(e -> new DecreaseStockInput(e.getProductId(), e.getProductQuantity()))
  	.collect(Collectors.toList());
  
  @Override
  public Page<ProductInfo> findAll(Pageable pageable) {
    Page<ProductInfo> productInfoPage = repository.findAll(pageable);
    productInfoPage.getContent().stream()
      .forEach(e -> e.addImageHost(upYunConfig.getImageHost()));
    return productInfoPage;
  }

// List转String(元素间,分隔)
String.join(",", roleNames);
roleNames.stream().collect(Collectors.joining(","))
```

## JSON

```java
// 序列化时，将Date转为Long类型
/** 创建时间. */
@JsonSerialize(using = Date2LongSerializer.class)
private Date createTime;
/** 更新时间. */
@JsonSerialize(using = Date2LongSerializer.class)
private Date updateTime;

// Date 转 Long  （毫秒转为秒）
public class Date2LongSerializer extends JsonSerializer<Date> {
  @Override
  public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
    jsonGenerator.writeNumber(date.getTime() / 1000);
  }
}
```

```java
// 将对象格式化成字符串json格式显示
public class JsonUtil {
    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }
}
```

```java
// 字段值为null，则不返回该字段
//@JsonInclude(JsonInclude.Include.NON_NULL)
// 配置文件中配置
spring: 
	jackson: 
		default-property-inclusion: non_null
```

```java
// 设置json序列化属性名
@JsonProperty("id")
private String productId;
```

## 请求规定

```java
// 接口指定一种请求方式，如Get或POST或DELETE等
@RequestParam：获取请求参数的值，不管使用什么方式都能接收到（url或form或json）
@PathVariable：从url中获取参数{id}
Get请求参数放到url后
Post请求参数放在form中
```

## 返回及统一异常处理

```java
// RunTimeException才事务回滚，Exception不事务回滚
// controller层：方法 throws Exception
// service层：方法 throws Exception
						 // throw new GirlException(ResultEnum.PRIMARY_SCHOOL);

/**  http请求返回的最外层对象 */
@Data
public class Result<T> {
    /** 错误码. */
    private Integer code;
    /** 提示信息. */
    private String msg;
    /** 具体的内容. */
    private T data;
}
```

```java
@Getter
public enum ResultEnum {
    UNKONW_ERROR(-1, "未知错误"),
    SUCCESS(0, "成功"),
    ;

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
```

```java
// 异常类
@Getter
public class SellException extends RuntimeException{
    private Integer code;
    public SellException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());

        this.code = resultEnum.getCode();
    }
    public SellException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

@Data
public class GirlException extends RuntimeException{
    private Integer code;
    public GirlException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }
}

public class SellerAuthorizeException extends RuntimeException {
}
```

```java
@ControllerAdvice
public class ExceptionHandle {
    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);
    //@ExceptionHandler(value = Exception.class)
    //@ResponseBody
    //public Result handle(Exception e) {
    //    if (e instanceof GirlException) {
    //        GirlException girlException = (GirlException) e;
    //        return ResultUtil.error(girlException.getCode(), girlException.getMessage());
    //    }else {
    //        logger.error("【系统异常】{}", e);
    //        return ResultUtil.error(-1, "未知错误");
    //    }
    //}
  
  	@Autowired
    private ProjectUrlConfig projectUrlConfig;

    //拦截登录异常
    //http://sell.natapp4.cc/sell/wechat/qrAuthorize?returnUrl=http://sell.natapp4.cc/sell/seller/login
    @ExceptionHandler(value = SellerAuthorizeException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handlerAuthorizeException() {
        return new ModelAndView("redirect:"
        .concat(projectUrlConfig.getWechatOpenAuthorize())
        .concat("/sell/wechat/qrAuthorize")
        .concat("?returnUrl=")
        .concat(projectUrlConfig.getSell())
        .concat("/sell/seller/login"));
    }

    @ExceptionHandler(value = SellException.class)
    @ResponseBody
    public ResultVO handlerSellerException(SellException e) {
        return ResultVOUtil.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = ResponseBankException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleResponseBankException() {

    }
}
```

```java
// 工具
public class ResultUtil {
  public static Result success(Object object) {
    Result result = new Result();
    result.setCode(0);
    result.setMsg("成功");
    result.setData(object);
    return result;
  }
  
  public static Result success() {
        return success(null);
  }

  public static Result error(Integer code, String msg) {
    Result result = new Result();
    result.setCode(code);
    result.setMsg(msg);
    return result;
  }
}
```

## 配置

* 配置文件参数映射成Bean

```java
// application-dev.properties或application-dev.yaml(冒号后加空格)文件中
// 参数值 映射 成Bean
limit: 
  minMoney: 0.01
  maxMoney: 9999
	description: 最少要发${limit.minMoney}元, 最多${limit.maxMoney}元
	
// 多个属性	
@Data
@Component
@ConfigurationProperties(prefix = "limit")
public class LimitConfig {
	private BigDecimal minMoney;
	private BigDecimal maxMoney;
  private String description;
}

// 单个属性
@Value("${limit.minMoney}")
private BigDecimal minMoney;
```

## 返回

```java
// 返回 json 指定名字
@Data
public class Product {
  @JsonProperty("name")
  private String categoryName;
  //...
}
```

## JWT

* `Json Web Token`
* 服务器不保存 session 数据，所有数据都保存在客户端，每次请求都发回服务器，服务器变成无状态了，从而比较容易实现扩展

* 原理：服务器认证以后，生成一个 JSON 对象，发回给用户，以后，用户与服务端通信的时候，都要发回这个 JSON 对象。服务器完全只靠这个对象认定用户身份。为了防止用户篡改数据，服务器在生成这个对象的时候，会加上签名
* 组成：
  * Header（头部）
  * Payload（负载）
  * Signature（签名）：是对前两部分的签名，防止数据篡改
    * 首先，需要指定一个密钥（secret）。这个密钥只有服务器才知道，不能泄露给用户。然后，使用 Header 里面指定的签名算法（默认是 HMAC SHA256），按照公式产生签名，算出签名以后，把 Header、Payload、Signature 三个部分拼成一个字符串，每个部分之间用"点"（`.`）分隔，就可以返回给用户
* 使用：
  * 客户端每次与服务器通信，都要带上这个 JWT。可以把它放在 Cookie 里面自动发送，但是这样不能跨域，所以更好的做法是放在 HTTP 请求的头信息`Authorization`字段里面
* 特点：
  * 默认是不加密，但也是可以加密的。生成原始 Token 以后，可以用密钥再加密一次
  * 不加密的情况下，不能将秘密数据写入 JWT
  * 不仅可以用于认证，也可以用于交换信息。有效使用 JWT，可以降低服务器查询数据库的次数
  * 最大缺点是一旦 JWT 签发了，在到期之前就会始终有效，除非服务器部署额外的逻辑
  * JWT 本身包含了认证信息，一旦泄露，任何人都可以获得该令牌的所有权限。为了减少盗用，JWT 的有效期应该设置得比较短。对于一些比较重要的权限，使用时应该再次对用户进行认证
  * 为了减少盗用，JWT 不应该使用 HTTP 协议明码传输，要使用 HTTPS 协议传输

## JPA

* 定义了一系列对象持久化的标准，有hibernate

```yaml
spring:
		jpa:
    	hibernate:
      	ddl-auto: update
    	show-sql: true
```

```java
// entity 类
  @Entity
  @Data
  @DynamicUpdate  // 自动更新mysql的设置UPDATE CURRENT_TIMESTAMP的update_time字段
  // create_time   规则：_改为驼峰形式 
  private Date createTime;
  
  @Id
  @GeneratedValue
  
  // jpa interface，使用时，直接@Autowired，自带默认方法
  // 实体，主键类型
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
  // 定义符合规则的方法名findByCategoryTypeIn，不用实现，直接使用
  // 数据库表中有category_type字段
  List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypeList);
}
// 数据库中有product_status字段
public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {
  List<ProductInfo> findByProductStatus(Integer productStatus);
}
// 数据库中有buyer_openid字段
public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
  Page<OrderMaster> findByBuyerOpenid(String buyerOpenid, Pageable pageable);
}
```

```java
// 数据库实体,主键类型
public interface LuckmoneyRepository extends JpaRepository<Luckymoney, Integer> {
}

// repository.findById(id).orElse(null);
// 更新数据：先查询，再更新
Optional<Luckymoney> optional = repository.findById(id);
if (optional.isPresent()) {
  Luckymoney luckymoney = optional.get();
  luckymoney.setConsumer(consumer);
  return repository.save(luckymoney);
}

@Data
@Entity
public class Luckymoney {

	@Id
	@GeneratedValue
	private Integer id;
  
	private BigDecimal money;
  /** 发送方 */
	private String producer;
	/**  接收方 */
	private String consumer;
	public Luckymoney() {
	}
}
```

```java
 public ProductInfo findOne(String productId) {
  		Optional<ProductInfo> productInfoOptional = repository.findById(productId);
	//        if (productInfoOptional.isPresent()) {
  //            return productInfoOptional.get().addImageHost(upYunConfig.getImageHost());
  //        }
  //        return null;
  		productInfoOptional.ifPresent(e -> e.addImageHost(upYunConfig.getImageHost()));
  		return productInfoOptional.orElse(null);
  }
```

```java
// 分页
//订单列表
@GetMapping("/list")
public ResultVO<List<OrderDTO>> list(@RequestParam("openid") String openid,
		@RequestParam(value = "page", defaultValue = "0") Integer page,
		@RequestParam(value = "size", defaultValue = "10") Integer size) {
  if (StringUtils.isEmpty(openid)) {
    log.error("【查询订单列表】openid为空");
    throw new SellException(ResultEnum.PARAM_ERROR);
  }

  PageRequest request = PageRequest.of(page, size);
  Page<OrderDTO> orderDTOPage = orderService.findList(openid, request);
  return ResultVOUtil.success(orderDTOPage.getContent());
}

public Page<OrderDTO> findList(String buyerOpenid, Pageable pageable) {
		Page<OrderMaster> orderMasterPage = orderMasterRepository.findByBuyerOpenid(buyerOpenid, pageable);
		List<OrderDTO> orderDTOList = OrderMaster2OrderDTOConverter.convert(orderMasterPage.getContent());
		return new PageImpl<OrderDTO>(orderDTOList, pageable, orderMasterPage.getTotalElements());
}

public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
    Page<OrderMaster> findByBuyerOpenid(String buyerOpenid, Pageable pageable);
}

public class OrderMaster2OrderDTOConverter {
    public static OrderDTO convert(OrderMaster orderMaster) {
        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(orderMaster, orderDTO);
        return orderDTO;
    }

    public static List<OrderDTO> convert(List<OrderMaster> orderMasterList) {
        return orderMasterList.stream().map(e ->
                convert(e)
        ).collect(Collectors.toList());
    }
}

public class ResultVOUtil {
    public static ResultVO success(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setData(object);
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        return resultVO;
    }

    public static ResultVO success() {
        return success(null);
    }

    public static ResultVO error(Integer code, String msg) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        return resultVO;
    }
}
```

## 事务

```java
// inodb
// Service层方法上加 @Transactional
```

## @Valid表单验证

```java
// Controller 方法中对象参数@Valid Girl girl  , 验证结果 BindingResult
@PostMapping(value = "/girls")
public Result<Girl> girlAdd(@Valid Girl girl, BindingResult bindingResult) {
  // 验证结果
  if (bindingResult.hasErrors()) {
    return ResultUtil.error(1, bindingResult.getFieldError().getDefaultMessage());
  }
  girl.setCupSize(girl.getCupSize());
  girl.setAge(girl.getAge());
  return ResultUtil.success(girlRepository.save(girl));
}

// 对象类
@Entity
public class Girl {
  @Min(value = 18, message = "xxxx")
  private Integer age;
  @NotNull(message = "金额必传")
  private Double money;
  @NotBlank(message = "这个字段必传")
  private String cupSize;
}
```

## 使用AOP 处理请求

* 面向切面编程
* Spring AOP就是负责实施切面的框架, 它将切面所定义的横切逻辑织入到切面所指定的连接点中
  * Spring AOP 默认使用标准的 JDK 动态代理(dynamic proxy)技术来实现 AOP 代理, 通过它, 可以为任意的接口实现代理
  * 当一个业务逻辑对象没有实现接口时, 那么Spring AOP 就默认使用 CGLIB 来作为 AOP 代理了
* aspect-切面
  * 可以简单地认为, 使用 @Aspect 注解的类就是切面
  * `aspect` 由 `pointcount` 和 `advice` 组成, 它既包含了横切逻辑的定义, 也包括了连接点的定义.
* advice-增强
  * 由 aspect 添加到特定的 join point(即满足 point cut 规则的 join point) 的一段代码
  * Spring AOP, 会将 advice 模拟为一个拦截器(interceptor), 并且在 join point 上维护多个 advice, 进行层层拦截
* join point-连接点
  * 在 Spring AOP 中, join point 总是方法的执行点, 即只有方法连接点
* point cut-切点
  * Advice 是和特定的 point cut 关联的, 并且在 point cut 相匹配的 join point 中执行
  * 在 Spring 中, 所有的方法都可以认为是 joinpoint, 但是并不希望在所有的方法上都添加 Advice, 而 pointcut 的作用就是提供一组规则来匹配joinpoint, 给满足规则的 joinpoint 添加 Advice.
* join point和point cut的区别
  * 在 Spring AOP 中, 所有的方法执行都是 join point. 而 point cut 是一个描述信息, 它修饰的是 join point, 通过 point cut, 就可以确定哪些 join point 可以被织入 Advice. 因此 join point 和 point cut 本质上就是两个不同纬度上的东西
  * advice 是在 join point 上执行的, 而 point cut 规定了哪些 join point 可以执行哪些 advice

```java
//统一处理日志请求
@Aspect
@Component
public class HttpAspect {
    private final static Logger logger = LoggerFactory.getLogger(HttpAspect.class);

    @Pointcut("execution(public * com.imooc.controller.GirlController.*(..))")
    public void log() {
    }

    @Before("log()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        //url
        logger.info("url={}", request.getRequestURL());

        //method
        logger.info("method={}", request.getMethod());

        //ip
        logger.info("ip={}", request.getRemoteAddr());

        //类方法
        logger.info("class_method={}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());

        //参数
        logger.info("args={}", joinPoint.getArgs());
    }

    @After("log()")
    public void doAfter() {
        logger.info("222222222222");
    }
  
    // 获取返回参数
    @AfterReturning(returning = "object", pointcut = "log()")
    public void doAfterReturning(Object object) {
        logger.info("response={}", object.toString());
    }
}
```

* HTTP接口鉴权

  ```java
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME) //方法注解，注解 RequestMapping 方法
  public @interface AuthChecker {
  }
  ```

  ```java
  @Component
  @Aspect
  public class HttpAopAdviseDefine {
  		//当被 AuthChecker 注解所标注的方法调用前, 会执行这个 advice
      // 定义一个 Pointcut, 使用 切点表达式函数 来描述对哪些 Join point 使用 advise.
      @Pointcut("@annotation(com.xys.demo1.AuthChecker)")
      public void pointcut() {
      }
  
      // 定义 advise
      @Around("pointcut()")
      public Object checkAuth(ProceedingJoinPoint joinPoint) throws Throwable {
          HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                  .getRequest();
          // 检查用户所传递的 token 是否合法
          String token = getUserToken(request);
          if (!token.equalsIgnoreCase("123456")) {
              return "错误, 权限不合法!";
          }
          // 如果是记录操作日志，则先执行业务joinPoint.proceed()，然后再记录日志
          return joinPoint.proceed();
      }
  
      private String getUserToken(HttpServletRequest request) {
          Cookie[] cookies = request.getCookies();
          if (cookies == null) {
              return "";
          }
          for (Cookie cookie : cookies) {
              if (cookie.getName().equalsIgnoreCase("user_token")) {
                  return cookie.getValue();
              }
          }
          return "";
      }
  }
```
  
  ```java
  @RestController
  public class DemoController {
      @RequestMapping("/aop/http/alive")
      public String alive() {
          return "服务一切正常";
      }
  
      @AuthChecker
      @RequestMapping("/aop/http/user_info")
      public String callSomeInterface() {
          return "调用了 user_info 接口.";
      }
  }
```
  
  

## 单元测试

* ` 测试类  右键类名 - GOTO - Test`

```java
// 打包自动测试
mvn clean package
// 打包时跳过测试
mvn clean package -Dmaven.test.skip=true

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GirlControllerTest {
  
  @Autowired
  private GirlService girlService;

  @Autowired
  private MockMvc mvc;

  @Test
  //@Transactional //不向数据库中插入数据
  public void girlList() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/girls"))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().string("abc"));
  }

  @Test
  public void findOneTest() {
    Girl girl = girlService.findOne(73);
    //    Assert.assertNotNull(girl);
    //    Assert.assertNotEquals(null, result);
    Assert.assertEquals(new Integer(13), girl.getAge());
  }
  
  @Test
  public void findAll() throws Exception {
    // 第几页，该页有多少数据
    PageRequest request = PageRequest.of(0, 2);
    Page<ProductInfo> productInfoPage = productService.findAll(request);
    //        System.out.println(productInfoPage.getTotalElements());
    Assert.assertNotEquals(0, productInfoPage.getTotalElements());
  }
}
```

## 日志

* 日志门面SLF4j - 日志实现Logback
* logback-spring.xml
  - 区分info和error日志
  - 每天产生一个日志文件

* `logback-spring.xml`

  ```xml
  <?xml version="1.0" encoding="UTF-8" ?>
  <configuration>
      <appender name="consoleLog" class="ch.qos.logback.core.ConsoleAppender">
          <layout class="ch.qos.logback.classic.PatternLayout">
              <pattern>
                  %d - %msg%n
              </pattern>
          </layout>
      </appender>
  
      <!--配置成 INFO，不打印ERROR信息（ERROR级别比INFO高，所以级别为INFO时会打印ERROR，要避免打印ERROR）-->
      <appender name="fileInfoLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <filter class="ch.qos.logback.classic.filter.LevelFilter">
              <level>ERROR</level>
              <!--如果匹配到ERROR，则不打印-->
              <onMatch>DENY</onMatch>
              <!--匹配到非ERROR则打印-->
              <onMismatch>ACCEPT</onMismatch>
          </filter>
          <encoder>
              <pattern>
                  %msg%n
              </pattern>
          </encoder>
          <!--滚动策略-->
          <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
              <!--路径-->
              <fileNamePattern>/Users/dingyuanjie/video/SpringBoot企业微信点餐系统/资料/coding-117/log/tomcat/sell/info.%d.log</fileNamePattern>
              <!-- <fileNamePattern>/var/log/tomcat/sell/info.%d.log</fileNamePattern>-->
          </rollingPolicy>
      </appender>
  
      <appender name="fileErrorLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
              <level>ERROR</level>
          </filter>
          <encoder>
              <pattern>
                  %msg%n
              </pattern>
          </encoder>
          <!--滚动策略-->
          <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
              <!--路径-->
  						<!--<fileNamePattern>/var/log/tomcat/sell/error.%d.log</fileNamePattern>-->
              <fileNamePattern>/Users/dingyuanjie/video/SpringBoot企业微信点餐系统/资料/coding-117/log/tomcat/sell/error.%d.log</fileNamePattern>
          </rollingPolicy>
      </appender>
  
      <root level="info">
          <appender-ref ref="consoleLog" />
          <appender-ref ref="fileInfoLog" />
          <appender-ref ref="fileErrorLog" />
      </root>
  </configuration>
  ```

  ```yaml
  # 设置日志级别：打印sql
  logging:
    level:
      com.imooc.dataobject.mapper: trace
  ```

## WebSocket

* 前后端通信

  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
  </dependency>
  ```

  ```java
  import javax.websocket.OnClose;
  import javax.websocket.OnMessage;
  import javax.websocket.OnOpen;
  import javax.websocket.Session;
  import javax.websocket.server.ServerEndpoint;
  import java.util.concurrent.CopyOnWriteArraySet;
  
  @Component
  @ServerEndpoint("/webSocket")
  @Slf4j
  public class WebSocket {
    private Session session;
    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();
  
    @OnOpen
    public void onOpen(Session session) {
      this.session = session;
      webSocketSet.add(this);
      log.info("【websocket消息】有新的连接, 总数:{}", webSocketSet.size());
    }
  
    @OnClose
    public void onClose() {
      webSocketSet.remove(this);
      log.info("【websocket消息】连接断开, 总数:{}", webSocketSet.size());
    }
  
    @OnMessage
    public void onMessage(String message) {
      log.info("【websocket消息】收到客户端发来的消息:{}", message);
    }
  
    public void sendMessage(String message) {
      for (WebSocket webSocket: webSocketSet) {
        log.info("【websocket消息】广播消息, message={}", message);
        try {
          webSocket.session.getBasicRemote().sendText(message);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
  ```

  ```java
  @Autowired
  private WebSocket webSocket;
  
  //发送websocket消息
  webSocket.sendMessage(orderDTO.getOrderId());
  ```

  ```js
  <#--弹窗-->
  <div class="modal fade" id="myModal" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
      <div class="modal-dialog">
          <div class="modal-content">
              <div class="modal-header">
                  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                  <h4 class="modal-title" id="myModalLabel">
                      提醒
                  </h4>
              </div>
              <div class="modal-body">
                  你有新的订单
              </div>
              <div class="modal-footer">
                  <button onclick="javascript:document.getElementById('notice').pause()" type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                  <button onclick="location.reload()" type="button" class="btn btn-primary">查看新的订单</button>
              </div>
          </div>
      </div>
  </div>
  
  <#--播放音乐-->
  <audio id="notice" loop="loop">
      <source src="/sell/mp3/song.mp3" type="audio/mpeg" />
  </audio>
  
  <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
  <script src="https://cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  <script>
      var websocket = null;
      if('WebSocket' in window) {
          websocket = new WebSocket('ws://sell.natapp4.cc/sell/webSocket');
      }else {
          alert('该浏览器不支持websocket!');
      }
  
      websocket.onopen = function (event) {
          console.log('建立连接');
      }
  
      websocket.onclose = function (event) {
          console.log('连接关闭');
      }
  
      websocket.onmessage = function (event) {
          console.log('收到消息:' + event.data)
          //弹窗提醒, 播放音乐
          $('#myModal').modal('show');
          document.getElementById('notice').play();
      }
  
      websocket.onerror = function () {
          alert('websocket通信发生错误！');
      }
  
      window.onbeforeunload = function () {
          websocket.close();
      }
  </script>
  ```

## Mybatis

```yaml
// 引入依赖
// 引导类上扫描Mapper接口文件
@MapperScan(basePackages = "com.imooc.dataobject.mapper")
// 配置文件中配置mapper的xml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  
# 注解方式 或 xml方式

建表用sql，不用JPA建表
慎用@ManyToOne 和 @OneToMany
```

## Apache ab 压测

```java
// 使用简易工具  Apache ab
// n 100个请求、c 100个并发，相当于100个人同时访问
// ab -n 100 -c 100 http://www.baidu.com
// t 60 秒
// ab -t 60 -c 100 http://www.baidu.com

// http://localhost:8080/sell/skill/order/123456 多刷新几次
// mac电脑
sudo apachectl -v
sudo apachectl start    // 浏览器访问 http://localhost/   显示 it works
ab -n 100 -c 10 http://localhost:8080/sell/skill/order/123456
ab -n 500 -c 100 http://localhost:8080/sell/skill/order/123456
浏览器访问 http://localhost:8080/sell/skill/order/123456  数据对应不上
// orderProductMockDiffUser 方法加上synchronized关键字 重启应用，再压测
ab -n 500 -c 100 http://localhost:8080/sell/skill/order/123456
synchronized 修饰方法
	数据对应上了，是一种解决办法，但无法做到细粒度控制，只适合单点的情况
redis分布式锁 redis.cn
	setnx(设置成功返回1，失败返回0)、getset(设置新值返回旧值)
  //http://localhost:8080/sell/skill/order/123456
  ab -n 500 -c 100 http://localhost:8080/sell/skill/order/123456
  // 速度快，数目能够对应上，虽然只有部分用户能够拿到
	// 支持分布式，可以更细粒度的控制，多台机器上多个进程对一个数据进行操作的互斥
```

## 模拟秒杀

```java
@RestController
@RequestMapping("/skill")
@Slf4j
public class SecKillController {

  @Autowired
  private SecKillService secKillService;

  // 查询秒杀活动特价商品的信息
  @GetMapping("/query/{productId}")
  public String query(@PathVariable String productId)throws Exception {
    return secKillService.querySecKillProductInfo(productId);
  }

  // 秒杀，没有抢到获得"哎呦喂,xxxxx",抢到了会返回剩余的库存量
  @GetMapping("/order/{productId}")
  public String skill(@PathVariable String productId)throws Exception
  {
    log.info("@skill request, productId:" + productId);
    secKillService.orderProductMockDiffUser(productId);
    return secKillService.querySecKillProductInfo(productId);
  }
}
```

```java
@Service
public class SecKillServiceImpl implements SecKillService {
  private static final int TIMEOUT = 10 * 1000; //超时时间 10s

  @Autowired
  private RedisLock redisLock;

  // 国庆活动，皮蛋粥特价，限量100000份
  static Map<String,Integer> products;
  static Map<String,Integer> stock;
  static Map<String,String> orders;
  static
  {
    // 模拟多个表，商品信息表，库存表，秒杀成功订单表
    products = new HashMap<>();
    stock = new HashMap<>();
    orders = new HashMap<>();
    products.put("123456", 100000);
    stock.put("123456", 100000);
  }

  private String queryMap(String productId)
  {
    return "国庆活动，皮蛋粥特价，限量份"
      + products.get(productId)
      +" 还剩：" + stock.get(productId)+" 份"
      +" 该商品成功下单用户数目："
      +  orders.size() +" 人" ;
  }

  @Override
  public String querySecKillProductInfo(String productId) {
    return this.queryMap(productId);
  }

  @Override
  public void orderProductMockDiffUser(String productId) {
    //加锁
  	long time = System.currentTimeMillis() + TIMEOUT;
  	if(!redisLock.lock(productId, String.valueOf(time))) {
    	throw new SellException(101, "1111111");
  	}

    //1.查询该商品库存，为0则活动结束。
    int stockNum = stock.get(productId);
    if(stockNum == 0) {
      throw new SellException(100,"活动结束");
    }else {
      //2.下单(模拟不同用户openid不同)
      orders.put(KeyUtil.genUniqueKey(),productId);
      //3.减库存
      stockNum =stockNum-1;
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      stock.put(productId,stockNum);
    }
    
    //解锁
  	redisLock.unlock(productId, String.valueOf(time));
  }
}
```

## Redis分布式锁

```java
@Component
@Slf4j
public class RedisLock {
	@Autowired
	private StringRedisTemplate redisTemplate;

  /**
	* 加锁
	* @param key
  * @param value 当前时间+超时时间
  * @return
  */
  public boolean lock(String key, String value) {
    if(redisTemplate.opsForValue().setIfAbsent(key, value)) {
      return true;
    }
    // 下面代码防止加锁后，后面的代码出错导致锁无法释放
    //currentValue=A   这两个线程的value都是B  其中一个线程拿到锁
    String currentValue = redisTemplate.opsForValue().get(key);
    //如果锁过期
    if (!StringUtils.isEmpty(currentValue)
        && Long.parseLong(currentValue) < System.currentTimeMillis()) {
      //获取上一个锁的时间
      String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
      if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
        return true;
      }
    }
    return false;
  }

  /**
	* 解锁
	* @param key
	* @param value
	*/
  public void unlock(String key, String value) {
    try {
      String currentValue = redisTemplate.opsForValue().get(key);
      if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
        redisTemplate.opsForValue().getOperations().delete(key);
      }
    }catch (Exception e) {
      log.error("【redis分布式锁】解锁异常, {}", e);
    }
  }
}
```

## Redis缓存

```java
//命中、失效、更细

// 启动类上加上
@EnableCaching

要实现序列化
```

```java
//controller中的方法上
// 查询方法 和 保存方法 都要加缓存注解，否则不会更新缓存
//list，第一次访问时返回的ResultVO对象会缓存到redis，后面访问直接从redis中返回，不会再执行代码
// 动态key：sellerId是方法的参数  condition条件成立时才缓存 unless依照结果判断是否缓存（result为返回对象-固定名称）
@Cacheable(cacheNames = "product", key = "#sellerId", condition = "#sellerId.length() > 3", unless = "#result.getCode() != 0")
// save， 会把内容更新到redis中
@Cacheable(cacheNames = "product", key = "123")   // 但是返回的ModelAndView无法序列化，也不是ResuleVO
//需使用 @CacheEvict，使缓存失效，下次list查询时再缓存进redis
@CacheEvict(cacheNames = "product", allEntries = true, beforeInvocation = true)

// 上述返回的不是同一个对象
// 返回是同一个对象   key要相同，否则是方法的参数值
@Cacheable(cacheName = "product" key = "123")
public ProductInfo findOne(String productId) {...}
@CachePut(cacheName = "product" key = "123")
public ProductInfo save(ProductInfo productInfo) {...}
// 可以直接在类上加
@CacheConfig(cacheNames = "product")
// 方法上的@Cache相关的注解就不用加 cacheNames参数了
```

## 部署

```shell
mvn clean package -Dmaven.test.skip=true
  
<build>
	<!--最终打包名称-->
	<finalName>sell</finalName>
    ..
  
连接虚拟机，将jar包放到 /opt/javaapps目录
scp target/sell.jar root@ip:/opt/javaapps
java -jar sell.jar
//访问 虚拟机ip:8080/sell/buyer/product/list

java -jar -Dserver.port=8090 -Dspring.profiles.actives=product sell.jar

// 后台启动，返回启动进程号
vim start.sh
#!/bin/sh
nohup java -jar sell.jar > /dev/null 2>&1 &
// 执行
bash start.sh
ps -ef|grep sell

cd /etc/systemd/system/
ll
vim sell.service
```

```shell
# 做成系统服务
cd /etc/systemd/system/
ll
vim sell.service
```

```shell
[Unit]
Description=sell
After=syslog.target network.target

[Service]
Type=simple

ExecStart=/usr/bin/java -jar /opt/javaapps/sell.jar
ExecStop=/bin/kill -15 $MAINPID

User=root
Group=root

[Install]
WantedBy=multi-user.target
```

![image-20190804225131840](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190804225131840.png)

```shell
systemctl start sell
ps -ef|grep sell
systemctl stop sell
ps -ef|grep sell
# 开机启动
systemctl enable sell
# 禁止开机启动
systemctl disable sell
```

## 设计一个高并发的系统架构

* 2000万个用户，高峰期2000QPS，线上机器内存开始吃紧，内存使用率开始上升，高峰期CPU转的很快，CPU负载开始增强，甚至CPU负载开始到60%、80%，数据库已经快要扛不住了，磁盘IO效率开始降低，很多SQL开始跑的很慢
* ![image-20190818142911959](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190818142911959.png)
* ![image-20190818143003001](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190818143003001.png)
* ![image-20190818143424709](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190818143424709.png)

## Zookeeper分布式锁

```java
public class ZooKeeperSession {
	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	private ZooKeeper zookeeper;

	public ZooKeeperSession() {
		// 去连接zookeeper server，创建会话的时候，是异步去进行的
		// 所以要给一个监听器，说告诉我们什么时候才是真正完成了跟zk server的连接
		try {
			this.zookeeper = new ZooKeeper(
					"192.168.31.187:2181,192.168.31.19:2181,192.168.31.227:2181", 
					50000, 
					new ZooKeeperWatcher());
			// 给一个状态CONNECTING，连接中
			System.out.println(zookeeper.getState());
			
			try {
				// CountDownLatch
				// java多线程并发同步的一个工具类
				// 会传递进去一些数字，比如说1,2 ，3 都可以
				// 然后await()，如果数字不是0，那么久卡住，等待
				
				// 其他的线程可以调用coutnDown()，减1
				// 如果数字减到0，那么之前所有在await的线程，都会逃出阻塞的状态
				// 继续向下运行
				
				connectedSemaphore.await();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("ZooKeeper session established......");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取分布式锁
	 */
	public void acquireDistributedLock(Long productId) {
		String path = "/product-lock-" + productId;
	
		try {
			zookeeper.create(path, "".getBytes(), 
					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			System.out.println("success to acquire lock for product[id=" + productId + "]");  
		} catch (Exception e) {
			// 如果那个商品对应的锁的node，已经存在了，就是已经被别人加锁了，那么就这里就会报错
			// NodeExistsException
			int count = 0;
			while(true) {
				try {
					Thread.sleep(1000); 
					zookeeper.create(path, "".getBytes(), 
							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				} catch (Exception e2) {
					count++;
					System.out.println("the " + count + " times try to acquire lock for product[id=" + productId + "]......");
					continue;
				}
				System.out.println("success to acquire lock for product[id=" + productId + "] after " + count + " times try......");
				break;
			}
		}
	}
	
	/**
	 * 释放掉一个分布式锁
	 */
	public void releaseDistributedLock(Long productId) {
		String path = "/product-lock-" + productId;
		try {
			zookeeper.delete(path, -1); 
			System.out.println("release the lock for product[id=" + productId + "]......");  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 建立zk session的watcher
	 */
	private class ZooKeeperWatcher implements Watcher {
		public void process(WatchedEvent event) {
			System.out.println("Receive watched event: " + event.getState());
			if(KeeperState.SyncConnected == event.getState()) {
				connectedSemaphore.countDown();
			} 
		}
	}
	
	/**
	 * 封装单例的静态内部类
	 */
	private static class Singleton {
		
		private static ZooKeeperSession instance;
		static {
			instance = new ZooKeeperSession();
		}
		
		public static ZooKeeperSession getInstance() {
			return instance;
		}
	}
	
	/**
	 * 获取单例
	 */
	public static ZooKeeperSession getInstance() {
		return Singleton.getInstance();
	}
	
	/**
	 * 初始化单例的便捷方法
	 */
	public static void init() {
		getInstance();
	}
}
```

## 缓存雪崩

* 事前
  * redis本身的高可用性，复制，主从架构，操作主节点，读写，数据同步到从节点，一旦主节点挂掉，从节点跟上
  * 双机房部署，一套redis cluster，部分机器在一个机房，另一部分机器在另外一个机房
  * 还有一种部署方式，两套redis cluster，两套redis cluster之间做一个数据的同步，redis集群是可以搭建成树状的结构的
  * 一旦说单个机房出了故障，至少说另外一个机房还能有些redis实例提供服务
* 事中
  * redis cluster已经彻底崩溃了，已经开始大量的访问无法访问到redis了
    1. ehcache本地缓存
       * 所做的多级缓存架构的作用上了，ehcache的缓存，应对零散的redis中数据被清除掉的现象，另外一个主要是预防redis彻底崩溃
       * 多台机器上部署的缓存服务实例的内存中，还有一套ehcache的缓存
       * ehcache的缓存还能支撑一阵
    2. 对redis访问的资源隔离
    3. 对源服务访问的限流以及资源隔离
* 事后
  1. redis数据可以恢复，做了备份，redis数据备份和恢复，redis重新启动起来
  2. redis数据彻底丢失了，或者数据过旧，快速缓存预热，redis重新启动起来
* redis对外提供服务
  * 缓存服务里，熔断策略，自动可以恢复，half-open，发现redis可以访问了，自动恢复了，自动就继续去访问redis了
  * 基于hystrix的高可用服务这块技术之后，先讲解缓存服务如何设计成高可用的架构
  * 缓存架构应对高并发下的缓存雪崩的解决方案，基于hystrix去做缓存服务的保护
  * 事中，ehcache本身也做好了
  * 基于hystrix对redis的访问进行保护，对源服务的访问进行保护，讲解hystrix的时候，也说过对源服务的访问怎么怎么进行这种高可用的保护
  * 但是站的角度不同，源服务如果自己本身不知道什么原因出了故障，我们怎么去保护，调用商品服务的接口大量的报错、超时、限流，资源隔离，降级

