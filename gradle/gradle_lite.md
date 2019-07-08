## 使用

```java
// build.gradle
apply plugin:'java'
apply plugin:'eclipse'
// 使jar包能够运行 ./gradle run
// jar tvf xxx.jar 查看jar包内容
apply plugin:'application'
mainClassName='hello.HelloWorld`

repositories {
    mavenCentral()
}

jar{
    baseName:'gs-gradle'
    version:'1.0.0'
}

sourceCompatibility=1.8
targetCompatibility=1.8

dependencies {
    // providedCompile运行时编译
    compile:"joda-time:joda-time:2.2"
    testCompile:"junit:junit:4.12"
}
```

## 简介

* Gradle

  * 新建工程（build.gradle）-  编码（依赖管理） - 测试（自动执行）- 打包（插件） - 发布（插件）

* Gradle是什么

  * 开源的项目自动化构建工具
  * 建立在Apache Ant和Apache Maven概念的基础上
  * 并引入了基于Groovy的特定领域语言（DSL），而不再使用XML形式管理构建脚本

* Groovy

  * 用于Java虚拟机的一种敏捷的动态语言，它是一种成熟的面向对象编程语言，既可以用于面向对象编程，又可以用作纯粹的脚本语言，使用这种语言不必编写过多的代码，同时又具有闭包和动态语言中的其他特性

  * 与Java比较

    * Groovy完全兼容Java的语法
    * 分号是可选的
    * 类、方法默认是public的
    * 编译器给属性自动添加getter/setter方法
    * 属性可以直接用点号获取
    * 最后一个表达式的值会被作为返回值
    * ==等同于equals()，不会有NullPointerExecptions

  * 高效的Groovy特性

    * assert语句
    * 可选类型定义
    * 可选的括号
    * 字符串
    * 集合API
    * 闭包

    ```java
    //1. 可选的类型定义
    def version = 1
    //2. assert
    assert version == 1
    //3. 括号是可选的
    println version
    //4. 字符串
    def s1 = 'immoc'
    def s2 = "gradle version is ${version}"
    def s3 = '''my 
    name 
    is 
    imooc'''
    println s1
    println s2
    println s3
    //5. 集合api
    //list
    def buildTools=['ant', 'maven']
    buildTools << 'gradle'
    assert buildTools.getClass() == ArrayList
    assert buildTools.size() == 3
    //map
    def buildYears = ['ant':2000, 'maven':2004]
    buildYears.gradle=2009
    println buildYears.ant
    println buildYears['gradle']
    println buildYears.getClass()
    //6. 闭包
    def c1 = {
        v ->
            println v
    }
    def c2 = {
        println 'hello'
    }
    def method1(Closure closure) {
        closure('param')
    }
    def method2(Closure closure) {
        closure()
    }
    method1(c1);   // param
    method2(c2);   // hello
    ```

## 构建脚本

* 构建块

  * Gradle构建中的两个基本概念是项目（project）和任务（task），每个构建至少包含一个项目，项目中包含一个或多个任务。在多项目构建中，一个项目可以依赖于其他项目；类似的，任务可以形成一个依赖关系图来确保他们的执行顺序

  * Project1 依赖于 Project2

    * Project1 中，TaskA 依赖于 TaskB、TaskC
    * Project2 中，TaskF 依赖于 TaskE 依赖于TaskD

  * 项目(Project)

    * 一个项目代表一个正在构建的组件（比如一个jar文件），当构建启动后，Gradle会基于build.gradle实例化一个org.gradle.api.Project类，并且能够通过projcet变量使其隐式可用
    * group、name、version
    * apply、dependencies、repositories、task
    * 属性的其他配置方式：ext、gradle.properties

  * 任务（task）

    * 任务对应org.gradle.api.Task

    * 主要包括任务动作和任务依赖

    * 任务动作定义了一个最小的工作单元

    * 可以定义依赖于其他任务、动作序列和执行条件

    * dependsOn

    * doFirst、doLast<<

      ```java
      // build.gradle
      // 自定义任务创建项目目录
      //闭包
      def createDir = {
          path ->
              File dir = new File(path);
              if(!dir.exists()) {
                  dir.mkdirs();
              }
      }
      task makeJavaDir() {
          def paths = ['src/main/java', 'src/main/resources', 'src/test/java', 'src/test/resources']
          // 动作
          doFirst{
              paths.forEach(createDir);
          }
      }
      // Gradle projects目录下 Tasks/other栏中会增加makeJavaDir的task，直接运行
      task makeWebDir() {
          dependsOn 'makeJavaDir'
          def paths = ['src/main/webapp', 'src/test/webapp'];
          doLast {
              paths.forEach(createDir)
          }
      }
      // Gradle projects目录下 Tasks/other栏中会增加makeWebDir的task，直接运行
      ```

  * 构建生命周期：初始化-配置-执行

    ```java
    //配置
    task loadVersionConf{
        project.version='1.0'
    }
    task loadVersion <<{
        print 'success'
    }
    ```

  * 依赖管理

    * 概述

      * 自动化的依赖管理可以明确依赖的版本，可以解决因传递性依赖带来的版本冲突

    * 工件坐标

      * group、name、version

    * 常用仓库

      * mavenLocal/mavenCentral/jcenter
      * 自定义maven仓库
      * 文件仓库

    * 依赖的传递性

      * B 依赖 A，如果C依赖B，那么C依赖A

    * 自动化依赖管理

    * 依赖阶段配置

      * complie、runtime
      * testCompile、testRuntime

    * 依赖阶段关系

      * testRuntime -extends-> testCompile -extends-> compile
      * testRuntime -extends-> runtime -extends-> compile

    * 解决冲突

      * 查看依赖报告，gradle默认使用最高版本jar包

        * Tasks-help-dependencies

      * 排除传递性依赖

      * 强制一个版本

      * 修改默认解决策略

        ```java
        //修改默认解决策略
        configurations.all {
            resolutionStrategy {
                failOnVersionConflict()
                // 解决版本冲突方法二：强制指定一个版本
                force 'org.slf4j:slf4j-api:1.7.24'
            }
        }
        repositories {
            maven{
                url ''
            }
            mavenLocal()
            mavenCentral()
        }
        // 依赖
        dependencies {
            //compile 'ch.qos.logback:logback-classic:1.3.0-alpha4'
            implementation ('ch.qos.logback:logback-classic:1.3.0-alpha4'){
                // 解决版本冲突方法一：排除依赖
                exclude group:"org.slf4j", module:"slf4j-api"
            }
            testCompile group:'junit', name:'junit', version:'4.12'
        }
        ```

## 多项目构建

* TODO模块依赖关系
  * Web -depends on-> Model
  * Web -depends on-> Repository->depends on-> Model
* 配置子项目
  * 配置要求
    * 所有项目应用java插件
    * web子项目打包成WAR
    * 所有项目添加logback日志功能
    * 统一配置公共属性
  * settings.gradle用来构建多项目
  * gradle.properties统一管理版本

```java
/*  * TODO 所有项目中应用Java插件
    * TODO web子项目打包成WAR
    * TODO 所有项目都添加logback日志功能
    * TODO 统一配置group和version
*/
//gralde.properties
project.group 'com.fine'
version '1.0-SNAPSHOT'
//settings.gradle
rootProject.name = 'woody'
include 'web'
include 'model'
include 'repository'
// build.gradle
allprojects {
    apply plugin: 'java'
    sourceCompatibility = 1.8
}

subprojects {
    repositories {
        mavenCentral()
    }
    dependencies {
        //添加依赖
        //compile 'ch.qos.logback:logback-classic:1.2.1'
        implementation 'ch.qos.logback:logback-classic:1.3.0-alpha4'
        testCompile group: 'junit', name: 'junit', version: '4.12'
    }
}

// web模块 build.gradle
apply plugin: 'war'

repositories {
    mavenCentral()
}

dependencies {
    compile project(":repository")
}
// repository模块 build.gradle
repositories {
    mavenCentral()
}

dependencies {
    compile project(":model")
}
// model模块 build.gradle为空
```

* gradle测试

  * 自动化测试

  * build

    - classes
    - reports
    - test-results

  * 测试发现

    * 任何继承自junit.framework.TestCase或groovy.util.GroovyTestCase的类
    * 任何被@RunWith注解的类
    * 任何至少包含一个被@Test注解的类

    ```java
    //测试配置
    dependencies {
        testCompile 'junit:junit:4.11'
    }
    // 测试
    @Test
    public void testSave(){
        TodoItem item = new TodoItem("todoItem");
        repository.save(item);
        Assert.assertNotNull((repository.query(item)))
    }
    ```

* 发布

  * 发布到本地和远程仓库

    ```java
    // 如果发布到私服，url填写私服地址，然后publish
    apply plugin:'maven-publish'
    publishing {
        publications {
            myPublish(MavenPublication) {
                from components.java
            }
        }
        repositories {
            maven {
                name "myRepo"
                url ""
            }
        }
    }
    ```

```java
// 构建脚本中默认都是有个Project实例
apply plugin 'java'
apply plugin 'war'
version '1.0'
repositories {
    mavenCentral()
}
dependencies {
    compile 'commons-codec:commons-codec:1.6'
}

// 写个Main方法
public class App{
    public static void main(String[] args){
        int i = 0;
        Scanner scanner = new Scanner(System.in);
        while(++i > 0) {
            System.out.println("请输入：");
            System.out.println("结果：" + scanner.nextLine());
        }
    }
}
// 运行jar插件打包成jar包
java -classpath build/libs/xxx.jar yyy.App
// 运行war插件打包成war包，放入tomcat的webapp目录下s
```

