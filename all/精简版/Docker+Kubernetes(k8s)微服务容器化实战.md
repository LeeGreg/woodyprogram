![image-20190626071241022](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071241022.png)

![image-20190626071317059](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071317059.png)

![image-20190626071358571](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071358571.png)

![image-20190626071449133](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071449133.png)

![image-20190626071709469](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071709469.png)

![image-20190626071823234](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071823234.png)

 ![image-20190626071904582](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071904582.png)

![image-20190626071955899](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626071955899.png)

![image-20190626072039080](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626072039080.png)

* 什么是微服务
  * 使用一套小服务来开发单个应用的方式，每个服务运行在独立的进程里，一般采用轻量级的通讯机制互联，并且它们可以通过自动化的方式部署
* 微服务特征
  * 单一职责，如邮件服务、登录服务
  * 轻量级通信，平台无关、语言无关，如http
  * 隔离性，每个服务运行在单独的进程里
  * 有自己的数据
  * 技术多样性，不限开发语言

![image-20190626073609410](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626073609410.png)

![image-20190626074008013](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626074008013.png)

* 微服务优势

  * 独立性、敏捷性、技术栈灵活、高效团队

* 微服务不足

  * 额外的工作：服务拆分
  * 数据一致性
  * 沟通成本：api改变

* 微服务架构引入的问题及解决方案

  * 微服务间如何通讯？

    * 从通讯模式角度考虑

    ![image-20190626074724431](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626074724431.png)

    * 从通讯协议角度考虑

      * REST API

      * RPC：dubbo、Thrift、Motan

        * 如何选择RPC框架
          - I/O、线程调度模型
          - 序列化方式：可读（json、xml）、二进制
          - 多语言支持
          - 服务治理

        ![image-20190626075419812](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626075419812.png)

      * MQ

  * 微服务如何发现彼此？

    * 客户端发现
    * 服务端发现

  * 微服务怎样部署？更新？扩容？

    * 服务编排：Kubernetes

  * SpringBoot与微服务

    * SpringBoot的使命：化繁为简
      * 独立运行：java -jar xxx.jar
      * 内嵌web服务器
      * 简化配置
      * 准生产的应用监控
    * SpringBoot与微服务的关系
      * Java的润滑剂，使服务开发更快、更简便

  * SpringCloud与微服务

    * SpringCloud的使命：简化Java的分布式系统
    * 一些列框架的集合、SpringBoot封装
    * SpringBoot意在简化，是一种开发、配置风格
    * SpringCloud意在简化分布式，是功能的集合，风格的统一

* 微服务开发实战

  * 业务场景
    * 用户可以注册和登录
    * 登录用户可以对课程进行CURD操作

  ![image-20190626230904244](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626230904244.png)

  * 先开发对其他模块依赖较少的模块
* 安装thirft
  
  ![image-20190626211749093](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626211749093.png)

  ![image-20190626211858573](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626211858573.png)

  * 右键执行gen-code.sh

  * 新建实现类实现服务接口message_service.py，右键运行该服务

    * netstat -na|grep 9090，检测端口是否LISTEN

  * docker version检查docker是否启动成功，docker ps

  * docker中安装mysql

    ![image-20190626225211661](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626225211661.png)

    ![image-20190626225050797](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626225050797.png)

    ![image-20190626225306227](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626225306227.png)

    ![image-20190626225404346](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626225404346.png)

  * netstat -na|grep 3306检查mysql是否启动成功

  * pe_user

    ![image-20190626225801355](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626225801355.png)
  
  * 启动user-thrift-service的ServiceApplication
  
    * netstat -na|grep 7911，检查端口是否启动成功
  
  * user-edge-service
  
    * docker启动redis，start.sh
  
      ![image-20190626232157205](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626232157205.png)
  
    * netstat -na|grep 6379
    * telnet localhost 6379
      
      * set a b、get a
  
  
  
  * 先启动user-thrift-service中ServiceApplication
  
  * 启动message-thrift-python-service，右键message_service.py选择run..
  
  * 再启动user-edge-service中ServiceApplication
  
  * postman访问：`http://localhost:8082/login`
  
    ![image-20190627073339284](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627073339284.png)
  
  * ![image-20190627073429109](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627073429109.png)
  
  * ![image-20190627075034586](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627075034586.png)
  
  * ![image-20190627075211112](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627075211112.png)
  
  * user-edge-service-client共其他系统直接调用
  
  * 
  
  ![image-20190627210116093](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627210116093.png)
  
  * 启动docker中zookeeper，`sh start.sh`
  
  ![image-20190627212212755](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627212212755.png)
  
  ![image-20190627230418226](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627230418226.png)
  
  ![image-20190627230436272](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627230436272.png)
  
  ![image-20190627230505009](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627230505009.png)
  
  



# 微服务部署

## 服务Docker化

* 准备环境

![image-20190627231111669](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627231111669.png)

### user-thrift-service

* 配置不要写死，maven ackage成可执行jar

![image-20190627231324357](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627231324357.png)

```properties
# Dockerfile
FROM hub.mooc.com:8080/micro-service/openjdk:7-jre
MAINTAINER xxx xxx@imooc.com

COPY target/user-thrift-service-1.0-SNAPSHOT.jar /user-service.jar

ENTRYPOINT ["java", "-jar", "/user-service.jar"]
```

![image-20190627231732613](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627231732613.png)

![image-20190627231918181](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627231918181.png)

```bash
# build.sh
#!/usr/bin/env bash

mvn clean package

docker build -t hub.mooc.com:8080/micro-service/user-service:latest .
docker push hub.mooc.com:8080/micro-service/user-service:latest
```

![image-20190627232204989](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627232204989.png)

### message-thrift-python-service

![image-20190627232458420](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627232458420.png)

`pip install thrift`

```python
# 基础镜像
# Dockerfile.base
FROM hub.mooc.com:8080/micro-service/python:3.6
MAINTAINER xxx xxx@imooc.com

RUN pip install thrift
```

```bash
# build_base.sh  右键执行run
#!/usr/bin/env bash

docker build -t python-base:latest -f Dockerfile.base .
```

![image-20190627232713619](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627232713619.png)

```properties
# Dockerfile  以Dockerfile.base 为基础
FROM hub.mooc.com:8080/micro-service/python-base:latest
MAINTAINER xxx xxx@imooc.com

ENV PYTHONPATH /
COPY message /message

ENTRYPOINT ["python", "/message/message_service.py"]
```

```bash
# build.sh 右键run
#!/usr/bin/env bash
docker build -t hub.mooc.com:8080/micro-service/message-service:latest .
docker push hub.mooc.com:8080/micro-service/message-service:latest
```

![image-20190627233113959](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627233113959.png)

### user-edge-service

![image-20190627233335680](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627233335680.png)

* package成可执行jar

```properties
# Dockerfile
FROM hub.mooc.com:8080/micro-service/openjdk:7-jre
MAINTAINER xxx xxx@imooc.com

COPY target/user-edge-service-1.0-SNAPSHOT.jar /user-edge-service.jar

ENTRYPOINT ["java", "-jar", "/user-edge-service.jar"]
```

```bash
# build.sh  右键run
#!/usr/bin/env bash
mvn clean package

docker build -t hub.mooc.com:8080/micro-service/user-edge-service:latest .
docker push hub.mooc.com:8080/micro-service/user-edge-service:latest
```

![image-20190627233836472](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627233836472.png)

### course-dubbo-service

![image-20190627234337481](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627234337481.png)

```properties
# Dockerfile
FROM hub.mooc.com:8080/micro-service/openjdk:7-jre
MAINTAINER xxx xxx@imooc.com

COPY target/course-dubbo-service-1.0-SNAPSHOT.jar /course-service.jar

ENTRYPOINT ["java", "-jar", "/course-service.jar"]

```

```bash
# build.sh  右键run
#!/usr/bin/env bash

mvn clean package

docker build -t hub.mooc.com:8080/micro-service/course-service:latest .
docker push hub.mooc.com:8080/micro-service/course-service:latest
```

![image-20190627234432164](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627234432164.png)

### course-edge-service

* maven package可执行jar

![image-20190627234730147](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627234730147.png)

```properties
# Dockerfile
FROM hub.mooc.com:8080/micro-service/openjdk:7-jre
MAINTAINER xxx xxx@imooc.com

COPY target/course-edge-service-1.0-SNAPSHOT.jar /course-edge-service.jar

ENTRYPOINT ["java", "-jar", "/course-edge-service.jar"]

```

```bash
# build.sh 右键run 
#!/usr/bin/env bash

mvn clean package

docker build -t hub.mooc.com:8080/micro-service/course-edge-service:latest .
docker push hub.mooc.com:8080/micro-service/course-edge-service:latest
```

![image-20190627234839792](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627234839792.png)

### api-gateway-zuul

* maven package 成可执行jar

```properties
# Dockerfile
FROM hub.mooc.com:8080/micro-service/openjdk:7-jre
MAINTAINER xxx xxx@imooc.com

COPY target/api-gateway-zuul-1.0-SNAPSHOT.jar /api-gateway-zuul.jar

ENTRYPOINT ["java", "-jar", "/api-gateway-zuul.jar"]

```

```bash
# build.sh 右键run
#!/usr/bin/env bash

mvn package

docker build -t hub.mooc.com:8080/micro-service/api-gateway-zuul:latest .

docker push hub.mooc.com:8080/micro-service/api-gateway-zuul:latest
```

![image-20190627235058245](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190627235058245.png)

### Docker下的服务通讯

* IP和端口：docker每次启动后IP都会变
* 服务把端口映射出去，把容器的端口转换为主机的端口，依赖容器去访问主机的IP端口
  * 依赖的基础环境，如redis、zookeeper、mysql使用的方式
* 
  * 开发的微服务使用的方式

```dockerfile
# docker-compose.yml
version: '3'

networks:
  default:
    external:
      name: imooc-network

services:
  message-service:
    image: message-service:latest

  user-service:
    image: user-service:latest
    command:
    - "--mysql.address=172.19.0.2"

  user-edge-service:
    image: user-edge-service:latest
    links:
    - user-service
    - message-service
    command:
    - "--redis.address=172.19.0.3"

  course-service:
    image: course-service:latest
    links:
    - user-service
    command:
    - "--mysql.address=172.19.0.2"
    - "--zookeeper.address=172.19.0.4"

  course-edge-service:
    image: course-edge-service:latest
    links:
    - user-edge-service
    command:
    - "--zookeeper.address=172.19.0.4"

  api-gateway-zuul:
    image: api-gateway-zuul:latest
    links:
    - course-edge-service
    - user-edge-service
    ports:
    - 8080:8080
```

![image-20190628063208803](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063208803.png)

![image-20190628063344320](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063344320.png)

![image-20190628063510978](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063510978.png)

![image-20190628063637698](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063637698.png)

![image-20190628063736248](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063736248.png)

![image-20190628063749862](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063749862.png)

![image-20190628063932307](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628063932307.png)

![image-20190628064229864](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628064229864.png)

![image-20190628064732334](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628064732334.png)

![image-20190628064819777](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628064819777.png)

* `apt-get update`
* `apt-get install net-tools`

* 查看下9090端口：`netstat -na|grep 9090`

![image-20190628065145952](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628065145952.png)

![image-20190628065251161](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628065251161.png)

![image-20190628065520500](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628065520500.png)

![image-20190628065601997](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628065601997.png)

## Docker仓库

* 共有仓库

  * `docker images`

  ![image-20190628070139617](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628070139617.png)

  ![image-20190628070240382](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628070240382.png)

* 建立私有仓库

  ![image-20190628070406422](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628070406422.png)

  ![image-20190628070513880](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628070513880.png)

  ![image-20190628070538726](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628070538726.png)

  ![image-20190628070848387](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628070848387.png)

  * `harbor/vi harbor.cfg`， `hostname = hub.mooc.com:8080`

  * 非mac系统，`/harbor$ ./install.sh`

  * mac系统需要更改挂载，然后`./install.sh`

    ![image-20190628071351828](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628071351828.png)

    ![image-20190628072125445](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072125445.png)

    `/volumes`

    ![ddimage-20190628071836850](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628071836850.png)

    ![image-20190628071906677](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628071906677.png)

    ![image-20190628071946129](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628071946129.png)

    * 前面加个.，放在当前目录下

    ![image-20190628072015950](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072015950.png)

    ![image-20190628072337522](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072337522.png)

    ![image-20190628072423856](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072423856.png)

    ![image-20190628072451057](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072451057.png)

    ![image-20190628072529597](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072529597.png)

    `./install.sh`

    ![image-20190628072611062](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072611062.png)

    ![image-20190628072714601](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072714601.png)

    ![image-20190628072807633](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628072807633.png)

    * 创建成员

    * `docker ps`

    * `docker images`

      ![image-20190628073315711](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628073315711.png)

      ![image-20190628073359820](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628073359820.png)

      ![image-20190628073515002](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628073515002.png)

    ![image-20190628073645477](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628073645477.png)

    ![image-20190628073725813](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628073725813.png)

    ![image-20190628073805845](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628073805845.png)

    ![image-20190628074251424](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628074251424.png)

    * 右键run

    ![image-20190628074357802](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628074357802.png)

## 服务编排

### Mesos

![image-20190628075021007](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075021007.png)

![image-20190628075202477](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075202477.png)

![image-20190628075221726](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075221726.png)

![image-20190628075431780](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075431780.png)

![image-20190628075514708](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075514708.png)

![image-20190628075824578](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075824578.png)

![image-20190628075944853](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190628075944853.png)



### Swarm

### Kubernetes

* 是什么

![image-20190629092613260](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629092613260.png)

![image-20190629092810938](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629092810938.png)

![image-20190629092902900](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629092902900.png)

 ![image-20190629093017176](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093017176.png)

![image-20190629093104974](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093104974.png)

![image-20190629093304183](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093304183.png)

* Pod（实际存在）里可运行多个容器（Docker容器）
* Service（逻辑概念）里可包含多个Pod
* Deployment可以部署Pod或Service

![image-20190629093539876](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093539876.png)

* 扩容，Service可提供负载均衡



![image-20190629093655554](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093655554.png)

* 滚动更新，Service的IP是不变的

![image-20190629093824702](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093824702.png)

![image-20190629093901307](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093901307.png)

![image-20190629093958253](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629093958253.png)

* 整体架构

![image-20190629094542622](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629094542622.png)

* ControllerManager负责维护集群状态，如故障检测、扩缩容、滚动更新
* Scheudler资源调度，按照一定的策略把Pod调度到相应的Node节点上
* ETCD，保存Pod、Service、集群状态等信息，所有需要持久化的数据都会存在里面
* Kubelet，负责维护当前Node节点上容器的生命周期、Volume、网络等
* kube-proxy，提供当前Node内部的服务发现、负载均衡，相当于为Service这个逻辑概念提供了落地的方法
* kube-dns，负责整个集群的dns服务，可选
* dashboard，提供整个集群数据的展示、GUI界面
* 首先执行kubectl向API Server发起一个命令请求认证，然后经过Scheduler的各种策略计算得到一个Node并告诉APIServer去请求相关Node的Kubelet，通过Kubelet把Pod运行起来，APIServer还会将Pod信息保存到ETCD，Pod运行后，ControllerManager就会管理Pod，此时Pod有个独立的IP地址，在整理集群内可以访问，不过该IP地址在服务重启、服务升级时易变。kube-proxy完成Service模块的具体功能、给Service分配一个IP，然后去访问Pod。集群外访问集群内服务，把服务端口直接暴露在Node上，然后外部直接访问Node上的端口就可以访问到Service了。容器里可以通过名字直接访问其他容器，kube-dns可使每个Pod通过名字去访问对方

* 设计理念
  * API设计原则，声明式
  * 控制机设计原则

* 网络，以插件的形式提供

  * 实现CNI接口，Container、NetWork、Interface，专门用来处理容器间通讯
  * Flannel、Calico、Weave
  * Pod网路，Docker网路只能访问当前Node上的容器，没办法跨主机访问其他Node上的容器。集群内的每个Pod都可以互联互通。

* scheduler

  * 决定每个Pod应该调度到哪个节点上
  * scheduler-preselect，预选规则
    * NodiskConflict，不会让挂载冲突发生（Node上已被其他Pod挂载）
    * CheckNodeMemoryPressure，检查当前节点内存压力
    * NodeSelector，可以选择注定hostname或具有某些标签的节点
    * FitResource，Node要满足Pod的CPU、memory、GPU等要求
    * Affinity，要满足许多需求，如一个Pod要和另一个Pod连在一起
  * scheduler-optimize-select，优选规则，对通过预选规则选出来的一系列Node进行打分，通过优先级函数处理Node，返回的0-10分数、每个函数对应一个权重，最终选择分数最高的主机部署
    * SelectorSpreadPriority，对于同一个Service或Controller的Pod会分布在不同的机器上
    * LeastRequestedPriority，如果新的Pod要分配节点，这个节点的优先级就由节点空闲部分的容量的比值来决定，（节点Pod容量-新Pod容量）/ 总容量
    * AffinityPriority

* Pod通讯

  * Pod的内部通讯

    ![image-20190629105222717](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629105222717.png)

    * 同一个Pod中容器之间相互通信，localhost加上端口就能访问

  * Pod间通讯——同一个Node上的不同Pod之间通讯

    ![image-20190629105146349](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629105146349.png)

    * 通过Pod的IP直接访问

  * Pod间通讯-不同Node之间通讯

    ![image-20190629105358079](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190629105358079.png)

    * Pod的IP不能相同；Pod的IP和所在的Node的IP关联起来

* 服务发现
  * Kube-proxy(ClusterIP)
    * 给所有相关的Pod做一个虚拟IP，直接把虚拟IP的流量重定向到后端集合，虚拟IP只能在集群内部访问，并且是固定的，只要Service不删除，这个IP是不变的
  * Kube-proxy(NodePort)
    * 在每个Node上启一个监听端口，将服务暴露在节点上，集群外部的服务可以通过Node的IP（NodePort）访问到集群内部的服务
  * Kube-DNS
    * 集群之间可以通过名字进行访问









































![image-20190626075753360](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190626075753360.png)

