# 课程导学

![image-20190807101644769](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807101644769.png)

# 计算机网络面试核心

* TCP的三次握手
  * 传输控制协议TCP	
    * 面向连接的、可靠的、基于字节流的传输层通信协议
    * 将应用层的数据流分割成报文段并发送给目标节点的TCP层

![image-20190807103246875](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807103246875.png)

![image-20190807103731218](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807103731218.png)

![image-20190807104050540](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807104050540.png)

![image-20190807104448786](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807104448786.png)

![image-20190807104804128](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807104804128.png)

![image-20190807105314799](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807105314799.png)

![image-20190807105144723](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807105144723.png)

![image-20190807105251729](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807105251729.png)

![image-20190807105614722](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807105614722.png)

* UDP
  * 面向非连接
  * 不维护连接状态，支持同时向多个客户端传输相同的信息
  * 数据包报头只有8个字节，额外开销较小
  * 吞吐量只受限于数据生成速率、传输速率以及及其性能
  * 尽最大努力交付，不保证可靠交付，不需要维持复杂的链接状态表
  * 面向报文，不对应用程序提交的报文信息进行拆分或者合并

* TCP和UDP区别
  * 是否面向连接
  * 可靠性
  * 有序性
  * 速度
  * 量级（TCP头部20字节，UDP8字节）

* TCP的滑动窗口

  * RTT：发送一个数据包到接收到对应的ACK，所花费的时间
  * RTO：重传时间间隔
  * 做流量控制和乱序排序
    * 保证TCP的可靠性
    * 保证TCP的流控特性

* HTTP

  * 超文本传输协议
    * 支持客户/服务器模式、简单快速、灵活、无连接、无状态

  ![image-20190807111633030](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807111633030.png)

  ![image-20190807111702215](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807111702215.png)

  ![image-20190807111930851](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807111930851.png)

  ![image-20190807112042645](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807112042645.png)

  ![image-20190807112251820](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807112251820.png)

  ![image-20190807112401031](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807112401031.png)

  ![image-20190807113032967](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113032967.png)

  ![image-20190807113207357](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113207357.png)

  ![image-20190807113300049](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113300049.png)

  ![image-20190807113435445](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113435445.png)

  ![image-20190807113540706](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113540706.png)

  ![image-20190807113629166](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113629166.png)

  ![image-20190807113740965](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113740965.png)

  ![image-20190807113855164](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807113855164.png)

  ![image-20190807114056806](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114056806.png)

  ![image-20190807114203533](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114203533.png)

  ![image-20190807114304328](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114304328.png)

  ![image-20190807114426992](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114426992.png)

  ![image-20190807114545320](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114545320.png)

  

  ![image-20190807114627079](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114627079.png)

  

  ![image-20190807114722327](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114722327.png)

  ![image-20190807114833859](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114833859.png)

  ![image-20190807114859069](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807114859069.png)

  ![image-20190807115112427](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807115112427.png)

  ![image-20190807115242773](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807115242773.png)

# 数据库

## 架构

* 如何设计一个关系型数据库

  ![image-20190807120115310](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807120115310.png)

## 索引

* 为什么要使用索引

  * 快速查询数据，避免全表扫描

* 什么样的信息能够成为索引

  * 主键、唯一键以及普通键

* 索引的数据结构

  * 生成索引，建立二叉查找树进行二分查找

    * 每个节点最多有两个节点
    * 左子节点小于父节点，右子节点大于父节点
    * 平衡二叉树
    * 缺点：O(logn) - 会变成 - O(n)、树的深度会很深导致IO次数变多

  * 生成索引，建立B-Tree结构进行查找

    * B-Tree

      * 根节点至少包括两个孩子
      * 树中每个节点最多含有m个孩子(m >= 2)
      * 除根节点和叶节点外，其他每个节点至少有ceil(m/2)个孩子（ceil-取上）
      * 所有的叶子节点都位于同一层

      ![image-20190807135334991](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807135334991.png)

      ![image-20190807135404619](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807135404619.png)

  * 生成索引，建立B+-Tree结构进行查找

    * B+树是B树的变体，其定义基本与B树相同，除了：

      * 非叶子节点的子树指针与关键字个数相同
      * 非叶子节点的子树指针P[i]，指向关键字值[K[i], K[i+1])的子树
      * 非叶子节点仅用来索引，数据都保存在叶子节点中
      * 所有叶子节点均有一个链指针指向下一个叶子节点

      ![image-20190807135854117](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807135854117.png)

      * B+Tree更适合用来做存储索引
        * B+树的磁盘读写代价更低
        * B+树的查询效率更加稳定
        * B+树更有利于对数据库的扫描

  * 生成索引，建立Hash结构进行查找

    * 仅能满足“=”，“IN”，不能使用范围查询
    * 无法被用来避免数据的排序操作
    * 不能利用部分索引键查询
    * 不能避免表扫描
    * 遇到大量Hash值相等的情况后性能并不一定就会比B-Tree索引高

* 密集索引和稀疏索引的区别

  * ![image-20190807222151039](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807222151039.png)
  * ![image-20190807222327301](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807222327301.png)
  * innodb的索引和数据是存在一起的；MyISAM的索引和数据是分开存储的；

* 如何定位并优化慢查询SQL

  * 具体场景具体分析，大致思路
    * 根据慢日志定位慢查询SQL

* 联合索引的最左匹配原则的成因

* 索引是建立越多越好吗

## 锁

## 语法

## 理论范式

# Redis

# Linux

# JVM

# GC

# Java多线程与并发

# Java常用类库与技巧

# Spring

# 总结

