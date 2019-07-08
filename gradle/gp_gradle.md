## 简介

* gradle是什么

  * 使用编程语言构建项目(gradle特色)
    * ANT & MAVEN 使用xml构建
  * DSL：领域特定语言:html，UML
  * 粒度在task (ant、maven源代码和构建脚本分开)
  * 兼容ant|maven

* gradle组成

  * groovy核心语法
    * gradle字符串用法
    * gradle数据结构用法
    * gradle面向对象
    * gradle闭包用法
  * build script block
  * gradle api

* groovy

  * 下载配置sdk bundle版本
  * 兼具脚本语言与静态语言
  * jvm虚拟机上
  * 完全兼容java语法

* groovy核心语法

  ```java
//函数中超过两个参数以上，并且最后一个参数是闭包，可以把闭包放在{} 
  Project project(String var1, Closure var2); 
  project('myproj-common'){Project project ->
      group 'com.test'
      version '2.0.0'
      dependencies {
  }
  ```
  
  * 所有类型都是对象
  * 字符串
  * 逻辑控制，重点：switch，for each
  * 闭包：所有的闭包都有返回值:没有return语句;返回null，反之返回具体的值
  * 字符串与闭包
  * 列表list、字典map、Range
  
* gradle执行过程

```groovy
//int x = 10
//
//println x.class
//
//double y = 3.14
//println y.class

def x_1 = 11

println x_1.class

x_1 = '11'

println x_1.class

def color = [red:"ff0000",green:"00ff00",blue:"0000ff"]
color.yellow = 'ffff00'
println color['red']
color.(pink) = 'ff00ff'
//println color.getClass()
println color.toMapString()
```

```groovy
//String str = "123"
//def str = 'a singe string'

def str = '''\
1
2
3
'''
def name = "咕泡"
def str2 = "Hello: ${name}"
def str3 = "1 add 2 equals ${1 + 2}"

//println str3

def result = hello(str3)
String hello(String message){
    return message
}

//println result

def str4 = "groovy hello"
println str4.center(8,"1")  //填充后str4为8字节大小
println str4.padLeft(8,'1')
def str5 = "hello"
println str4 - str5   //为groovy
println str5[0..1]   // gr
println str5.reverse()
println str5.capitalize()  //Hello
```

```groovy
names = ["1","2","3"]
//for (String name : names){
//    println name
//}
//
//for ( name in names){
//    println name
//}

/*for (i in 1..10){
    println i
}*/

//1.upto(3){
//    println it
//}
//
//
//3.downto(1){
//    println it
//}
//
//1.step(9,2){
//    println it   // 1 3 5 7
//}


String s = "1232"
n = new Random().nextInt(101)
println n
switch (n) {
    case [98,99,100]:
        println "good"
        break
    case 90 .. 97:
        println "nice"
        break
    case {it >0 & it <60 }:
        println "too bad"
        break

}
```

```groovy
//闭包  所有的闭包都有返回值:没有return语句;返回null，反之返回具体的值
package com.gupaoedu.vip.groovy.variable

//闭包
//    def clouser  = {
//        return  "Hello"
//
//    }
//
//    println clouser.call()  // 所有的闭包都有返回值

// it闭包默认变量
//def clouser2  = { return  "Hello ${it}"}
//print(clouser2.call())  // 没有传参  Hello null


//def clouser3 = {println item ++}
//clouser3.call()

//c = {it}
//println c('run')  //run

//def clouser3  = { name -> return  "Hello ${name}"}
//def result = clouser3('groovy!')
//println result
//

int x = 10
//用来求指定number的阶乘
int fab(int number){
    int result = 1
    //result = result * num
    1.upto(number,{num -> result *= num})
    return result
}

//println fab(5)  //120
//
//int fab2(int number){
//    int result = 1
//    1.downto(1){
//        num -> result *= num
//    }
//
//    return result
//}
//

3.times{
//    println it   //0 1 2
}
//for (int i = 0; i <3 ; i++) {
//    println i
//}

int cal(int number){
    int result = 0
    number.times{
        num -> result  += num
    }
    return result

}
//println cal(5)

/**
 *
 * 字符串与闭包
 */

 String str = 'abcd'
 str.each {
//     String temp -> println(temp)   // a b c d
 }

println str.find{
//    String s -> s.isNumber()  //打印字符串中数字，没有则打印出null
}
//
def scriptClouser = {
//

}
//scriptClouser.call()
//
//class Person {
//    def static classClouser = {
//        println "scriptClouser this:"+this    //代表闭包定义处的类  Person
//        println "scriptClouser this:"+owner  //代表闭包定义处的类或对象  Person
//        println "scriptClouser this:"+delegate //代表任意对象，默认与owner一样  Person
//    }
//
//    def static say() {
//        def classClouser = {
//            println "scriptClouser this:"+this    //代表闭包定义处的类
//            println "scriptClouser this:"+owner  //代表闭包定义处的类或对象
//            println "scriptClouser this:"+delegate //代表任意对象，默认与owner一样
//        }
//        classClouser.call()
//    }
//
//
//}
//Person.classClouser.call()
//Person.say()
//scriptClouser.call()
//
////闭包中定义闭包
//def nestClouser = {
//    def innerClouser = {
//        println "innserClouser this:"+this    //代表闭包定义处的类 最外部的类 Clouser
//        println "innserClouser this:"+owner  //代表闭包定义处的类或对象 Clouser$_run_clouser5..
//        println "innserClouser this:"+delegate //代表任意对象，默认与owner一样  Clouser$_run_clouser5..
//    }
//    innerClouser.call()
//
//    println "nestClouser this:"+this    //代表闭包定义处的类  
//    println "nestClouser this:"+owner  //代表闭包定义处的类或对象
//    println "nestClouser this:"+delegate //代表任意对象，默认与owner一样
//}
//nestClouser.call()

//字符串调与闭包的结合使用
String str2 = 'the 2 and 5 is 7'
//each
//str2.each {
//    String temp -> println temp
//}

//find来查找符合条件的第一个
println str2.find {
    String s -> s.isNumber()
}

def list = str2.findAll { String s -> s.isNumber()}
//    println list.toListString()

//def result = str2.any {
//    String s -> s.isNumber()
//}
//
//println result

def result = str2.every {
    String s -> s.isNumber()
}
//println result


def list2 = str2.collect{
    it.toUpperCase()
}

println list2.toListString()
```

```groovy
闭包使用
所有的闭包都有返回值:没有return语句;返回null，反之返回具体的值 函数中超过两个参数以上，并且最后一个参数是闭包，可以把闭包放在{} Project project(String var1, Closure var2); project('myproj-common'){Project project ->
    group 'com.test'
    version '2.0.0'
    dependencies {
    }
}
字符串与闭包 
列表:list 
字典:map 
Rang
```

## 构建项目

* build.gradle，构建逻辑，gradle工具通过build.gradle完成构建，build.gradle :task

* settings.gradle，配置项目基本信息，标记根项目和子项目
* gradlew，对gradle可执行命令的包装，屏蔽不同版本的兼容
* project + task 组成了gradle基础骨架

* gradle组成部分

  * 依赖管理

    * implementation (gradle4.x)
      * 对于使用了该命令编译的依赖，对该项目有依赖的项目将无法访问到使用该命令编译的依赖中的
        任何程序，也就是将该依赖隐藏在内部，而不对外部公开，api 完全等同于compile指令
      * 推荐使用，没有传递依赖
    * compile (gradle3.x)，使用该方式依赖的库将会参与编译和打包
    * testCompile，只在单元测试代码的编译以及最终打包测试apk时有效

  * 解决冲突

    * 默认情况下使用最高版本

    * 查看依赖报告

    * 排查传递性依赖

      ```java
      compile('org.xxx:yyy:1.0'){
           exclude group:"org.slf4j",module:"slf4j-api"
           transitive = false
      }
      ```

    * 强制一个版本

      ```java
      configurations.all {
      	resolutionStrategy{
          //默认冲突策略
          //failOnVersionConflict
      		force 'org.slf4j:slf4j-api:1.7.25'
      	} 
      }
      ```

  * 单项目配置

    ```java
    project ('springboot-application') {
        dependencies {
            compile project (':springboot-data')
            compile project (':springboot-web')
            compile project (':springboot-shiro')
            compile project (':springboot-base') 
        }
    }
    ```

  * 扩展属性配置

    ```java
    1.ext { springBootVersion = '2.0.1.RELEASE' } 
    2.通过gradle.properties配置
    ```

  * 发布

    ```java
    apply plugin: 'maven-publish'
    publishing {
      publications {
          mypublish(MavenPublication){
              groupId 'com.gupao.edu.vip.plugin'
              artifactId 'myplugin'
    					version '1.0.0'
    					from components.java
    			} 
      }
    }
    publishing {
        repositories {
            maven {
                url uri('')
    				} 
        }
    }
    ```

  * source sets

    * 通过修改SourceSets中的属性，可以指定哪些源文件(或文件夹下的源文件)要被编译，哪些源文件要被排除
    * Gradle就是通过它实现Java项目的布局定义

    ```java
    sourceSets { 
      main { 
        java { 
          srcDir 'src/java' // 指定源码目录 
        } 
        resources { 
          srcDir 'src/resources' //资源目录 
        } 
      }
    }
    ```

```java
groovy核心语法 
build script block
	buildScript{ 
		依赖配置:gradle自身对外部插件的依赖
	} 
	外部(allprojects{}){
  	依赖配置:项目本身对外部库的依赖
	}
gradle api
```

```groovy
// build.gradle
	// buildscript:gradle自身依赖的插件获取
	buildscript {
    ext {
        springBootVersion = '2.1.1.RELEASE'
    }
    repositories {
        maven {url "http://maven.aliyun.com/nexus/content/groups/public"}
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
	}
	// 插件配置
	 plugins {
  		id 'java'
	 }
	 apply plugin: 'java'
	// jar插件
	 jar {
    	baseName = 'springboot-application'
    	version = '0.0.1-SNAPSHOT'
	 }
	// 仓库配置
	  repositories {
			//mavenCentral()
    	maven{ url 'http://maven.aliyun.com/nexus/content/groups/public/'}
		}
	// 依赖
	dependencies {
    compile 'org.springframework.boot:spring-boot-starter-web'
		// 添加该依赖主要是解析yaml
		compile 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.5'
		// 添加该依赖主要解析json // 
    compile 'com.fasterxml.jackson.core:jackson-databind:2.9.5'
		testCompile 'org.springframework.boot:spring-boot-starter-test'
  }
```

### gradle执行过程

* Initialization初始化阶段
  * 解析整个工程中所有Project，构建所有的Project对应的的project对象
* Configuration配置阶段
  * 解析所有的projects对象中的task，构建好所有task的拓扑图（有向无环图）
* Execution执行阶段
  * 执行具体的task及其依赖task

### Gradle生命周期

* 初始化-配置-执行

### Project API组成

* project相关API

* task相关api
* 属性相关api
* file相关api

```java
//Project核心API讲解
AllProjects
subProjects 
parentProject 
rootProject
配置project group version
allprojects 配置
subproject 配置
配置project属性，ext，apply from this.file('common.gradle') 
文件属性相关
	rootdir，builddir,projectdir 
获取文件内容
copy
文件遍历
依赖管理，buildScript
```

* 反例

![image-20190706175355855](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706175355855.png)

![image-20190706175636559](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706175636559.png)

* 当一个方法中有两个参数时，后面的闭包参数可以用{}

  ```java
  Project project2(String name){
    
  }
  ```

  * 获取一个项目

  ![image-20190706180108591](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706180108591.png)

  ![image-20190706180613262](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706180613262.png)

  ![image-20190706180759023](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706180759023.png)

  ![image-20190706182135630](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706182135630.png)

  ![image-20190706182306127](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706182306127.png)

  ```java
   build.gradle 是什么，有什么用 构建逻辑，gradle工具通过build.gradle完成
  构建
  settings.gradle 是什么，有什么用
  标记根项目和子项目 gradlew 是什么，有什么用
  对gradle可执行命令的包装，屏蔽不同版本的兼 容
  Gradle 生命周期 初始化->配置->执行
  ```




![image-20190706231029539](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190706231029539.png)

```groovy
// build.gradle
def createDir = {
    path ->
      File dir = new File(path)
      if(!dir.exists()){
          dir.mkdirs()
      }
}

task createJavaDir() {
    def paths = ['src/main/java','src/main/resources',
                 'src/test/java',
             'src/test/resources']
    //在任务执行之前运行
    doFirst{
        paths.forEach(createDir)
    }
}

task createWebDir(){
    def paths = ['src/main/webapp','src/test/webapp']
    dependsOn 'createJavaDir'
    doLast{
         paths.forEach(createDir)
    }
}
```





