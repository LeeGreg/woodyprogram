* 谈谈对Java的理解

  * 平台无关性
  * GC
  * 语言特性
  * 面向对象
  * 类库
  * 异常处理

* ![image-20190809220026523](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809220026523.png)

* ![image-20190809220121313](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809220121313.png)

* ![image-20190809220541839](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809220541839.png)

* ![image-20190809220658902](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809220658902.png)

* ![image-20190809221127211](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809221127211.png)

* ![image-20190809221309564](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809221309564.png)

* ![image-20190809221403345](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809221403345.png)

* ![image-20190809221457845](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809221457845.png)

* ![image-20190809221813122](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809221813122.png)

* ![image-20190809221859493](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809221859493.png)

* ```java
  public class MyClassLoader extends ClassLoader {
      private String path;
      private String classLoaderName;
  
      public MyClassLoader(String path, String classLoaderName) {
          this.path = path;
          this.classLoaderName = classLoaderName;
      }
  
      //用于寻找类文件
      @Override
      public Class findClass(String name) {
          byte[] b = loadClassData(name);
          return defineClass(name, b, 0, b.length);
      }
  
      //用于加载类文件
      private byte[] loadClassData(String name) {
          name = path + name + ".class";
          InputStream in = null;
          ByteArrayOutputStream out = null;
          try {
              in = new FileInputStream(new File(name));
              out = new ByteArrayOutputStream();
              int i = 0;
              while ((i = in.read()) != -1) {
                  out.write(i);
              }
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              try {
                  out.close();
                  in.close();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          return out.toByteArray();
      }
  }
  ```

  ```java
  public class ClassLoaderChecker {
      public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
          MyClassLoader m = new MyClassLoader("/Users/baidu/Desktop/", "myClassLoader");
          Class c = m.loadClass("Wali");
          System.out.println(c.getClassLoader());
          System.out.println(c.getClassLoader().getParent());
          System.out.println(c.getClassLoader().getParent().getParent());
          System.out.println(c.getClassLoader().getParent().getParent().getParent());
          c.newInstance();
      }
  }
  ```

  ![image-20190809222717454](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809222717454.png)

  ![image-20190809223240117](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809223240117.png)

  ![image-20190809223350864](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809223350864.png)

  ![image-20190809223633975](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809223633975.png)

  ![image-20190809223703393](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809223703393.png)

  ![image-20190810075537996](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810075537996.png)

  ![image-20190809225305787](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809225305787.png)

  ![image-20190809225407191](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809225407191.png)

  ![image-20190809225506177](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809225506177.png)

  * 递归为什么会引发java.lang.StackOverflowError异常
    * 递归过深，栈帧数超出虚拟栈深度
    * 限制递归的次数
* 虚拟机栈过多会引发java.lang.OutOfMemoryError异常
  
![image-20190810080420376](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810080420376.png)
  
![image-20190809230452919](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809230452919.png)
  
  
  
* `javac xxx.xxx.java`

* `javap -verbose xxx.xxx.class`

  ![image-20190810080704715](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810080704715.png)

* ![image-20190810080809878](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810080809878.png)

* ![image-20190810081537498](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810081537498.png)

* JVM三大性能调优参数 -Xms -Xmx -Xss的含义

  * `java -Xms128m -Xmx128m -Xss256k -jar xxx.jar`
  * `-Xss`：规定了每个线程虚拟机栈（堆栈）的大小（会影响并发线程数的大小）
  * `-Xms`：堆的初始值
  * `-Xmx`：堆能达到的最大值

* ![image-20190810082249461](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810082249461.png)

* ![image-20190810082425547](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810082425547.png)

* ![image-20190810082557812](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810082557812.png)

* ![image-20190810082704984](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810082704984.png)

* ![image-20190810082755540](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810082755540.png)

* ![image-20190810082904605](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810082904605.png)

* 