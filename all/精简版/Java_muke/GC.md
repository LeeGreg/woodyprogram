* 对象被判定为垃圾的标准
  * 没有被其他对象引用
* 判定对象是否为垃圾的算法
  * 引用计数算法
  * 可达性分析算法

![image-20190810084309509](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810084309509.png)

![image-20190810084346553](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810084346553.png)

![image-20190810084620735](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810084620735.png)

![image-20190810084910436](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810084910436.png)

* 垃圾回收算法

  * 标记-清除算法

    * 标记：从根集合进行扫描，对存活的对象进行标记
    * 清除：对堆内存从头到尾进行线性遍历，回收不可达对象内存
    * 缺点：碎片化

  * 复制算法

    * 分为对象面和空闲面，对象在对象面上创建
    * 存活的对象被从对象面复制到空闲面
    * 将对象面的所有对象内存清除
    * 适合对象存活率低的场景，如年轻代（10%的存活率）
    * 优点：解决碎片化问题、顺序分配内存，简单高效

  * 标记-整理算法

    * 标记：从根集合进行扫描，对存活的对象进行标记
    * 清除：移动所有存活的对象，且按照内存地址次序依次排列，然后将末端内存地址以后的内存全部回收
    * 优点：避免内存的不连续性、不用设置两块内存互换、适用于存活率高的场景（老年代）

  * 分代收集算法

    * 垃圾回收算法的组合拳（年轻代存活率低一般采用复制算法，老年代存活率高一般采用标记清除算法或标记整理算法）

    * 按照对象生命周期的不同划分区域以采用不同的垃圾回收算法

    * 目的：提高JVM的回收效率

      ![image-20190810090622547](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810090622547.png)

      ![image-20190810090653295](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810090653295.png)

      * GC的分类

        * Minor GC
        * Full GC

      * 年轻代：尽可能快速的收集掉那些生命周期短的对象

        * Eden区
        * 两个Survivor区
        * ![image-20190810091315200](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810091315200.png)

      * 年轻代垃圾回收过程

        * 对象在Eden区出生并挤满Eden区，触发Minor GC将存活的对象复制到S0区并将年龄设置为1，清除所有的Eden区；然后Eden区再次被填满，触发Minor GC，将Eden和S0区存活的对象复制到S1并将年龄加1，清空Eden区和S0区；Eden区被填满，触发Minor GC，将Enen区和S1区存活的对象拷贝到S0区并且年龄加1，清空Eden和S1

      * 对象如何晋升到老年代

        ![image-20190810095537880](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810095537880.png)

      * ![image-20190810095636107](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810095636107.png)

      * ![image-20190810095719308](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810095719308.png)

      * 老年代

        * Full GC和Minor GC
        * Full GC比Minor GC慢，但执行频率低

      * ![image-20190810100043623](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100043623.png)

      * ![image-20190810100232587](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100232587.png)

      * ![image-20190810100347533](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100347533.png)

* 常见的垃圾收集器

  * JVM的运行模式`java -version`

    * Server
      * 启动慢，运行一段时间稳定后速度较快，重量级虚拟机对程序进行了相应的优化
    * Client
      * 启动较快

  * 垃圾收集器之间的关系

    ![image-20190810100709419](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100709419.png)

  * ![image-20190810100801238](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100801238.png)

  * ![image-20190810100857549](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100857549.png)

  * ![image-20190810100954528](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100954528.png)

  * ![image-20190810101129824](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810101129824.png)

  * ![image-20190810101219849](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810101219849.png)

  * ![image-20190810101329885](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810101329885.png)

  * ![image-20190810101457911](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810101457911.png)

  * ![image-20190810101521933](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810101521933.png)

    * 垃圾回收线程和用户线程几乎可以做到同时工作
      * 初始化标记：会stop-the-world，扫描能和根对象关联的对象做标记，很快完成
      * 并发标记：并发追溯标记，程序不会停顿
      * 并发预清理：查找执行并发标记阶段从年轻代晋升到老年代的对象
      * 重新标记：暂停虚拟机，扫描CMS堆中的剩余对象
      * 并发清理：清理垃圾对象，程序不会停顿
      * 并发重置：重置CMS收集器的数据结构

  * G1收集器（-XX:+UseG1GC，复制 + 标记 - 整理算法）
    * 既能用于年轻代，又能用于老年代
    * 特点
      * 并发与并行
        * 使用多个CPU来缩短stop the world的时间，与用户并发执行
      * 分代收集
        * 独立管理整个堆，但是能够采用不同的方式去处理新创建的对象和已经存活一段时间并且经过多次GC的对象来达到更好的收集效果
      * 空间整合
        * 基于标记-整理算法，解决了内存碎片问题
      * 可预测的停顿
        * 可设置停顿时间
      * 将整个Java堆内存划分为多个大小相等的Region
        * 年轻代和老年代不再物理隔离

![image-20190810103217724](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103217724.png)

![image-20190810103546006](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103546006.png)

![image-20190810103646260](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103646260.png)

![image-20190810103737741](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103737741.png)

![image-20190810103825050](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103825050.png)

![image-20190810103912680](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103912680.png)

![image-20190810104022894](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810104022894.png)

![image-20190810105437339](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810105437339.png)

```java
public class NormalObject {
    public String name;
    public NormalObject(String name){
        this.name = name;
    }

    @Override
    protected void finalize(){
        System.out.println("Finalizing obj " + name);
    }
}
```

```java
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class NormalObjectWeakReference extends WeakReference<NormalObject> {
    public String name;

    public NormalObjectWeakReference(NormalObject normalObject, ReferenceQueue<NormalObject> rq) {
        super(normalObject, rq);
        this.name = normalObject.name;
    }
    @Override
    protected void finalize(){
        System.out.println("Finalizing NormalObjectWeakReference " + name);
    }
}
```

```java
public class ReferenceQueueTest {
    private static ReferenceQueue<NormalObject> rq = new ReferenceQueue<NormalObject>();

    private static void checkQueue(){
        Reference<NormalObject> ref = null;
        while ((ref = (Reference<NormalObject>)rq.poll()) != null){
            if (ref != null){
                System.out.println("In queue: " + ((NormalObjectWeakReference)(ref)).name);
                System.out.println("reference object:" + ref.get());
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<WeakReference<NormalObject>> weakList = new ArrayList<WeakReference<NormalObject>>();
        for (int i =0; i < 3 ; i++){
            weakList.add(new NormalObjectWeakReference(new NormalObject("Weak " + i),rq));
            System.out.println("Created weak:" + weakList.get(i));
        }
        System.out.println("first time");
        checkQueue();
        System.gc();
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("second time");
        checkQueue();
    }
}
```

![image-20190810105650499](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810105650499.png)

* 外部可对ReferenceQueue进行监控，如果有对象即将被回收，相应的Reference对象将被放到该ReferenceQueue，然后就可以对Reference进行操作，如果不带ReferenceQueue，则会不断轮询Reference对象，通过判断get方法是否返回null来确认是否被回收了

