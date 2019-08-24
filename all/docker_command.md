# Readme

```java
// 主要看常用命令这块
```

# 概念

* 简单的说，容器是独立运行的一个或一组应用，以及它们的运行态环境。对应的，虚拟机可以理解为模拟运行的一整套操作系统（提供了运行态环境和其他系统环境）和跑在上面的应用

* 每个容器里封装的是一个或者多个应用，并且将依赖库、可执行文件都封装到容器里面，运行时能够达到很好的一致性
* 镜像，用于创建容器的只读模版，通过Docker build命令创建，存储于Docker Registry
* 容器，镜像的运行实例，一个镜像可创建多个容器，应用运行环境的封装
* Registry，存储Docker镜像的地方，在Registry中，镜像存储在Repository
* Repository，具有相同名字，不同标签的Docker镜像的集合
* `netstat -an|grep 6379`

# 常用命令

```shell
docker 命令 --help
--rm 																# 容器退出后，自动删除
-ti  																# 交互式，容器的shell映射到当前的shell
-d   																# 运行后台
-p 8080:80 													# HostPort:ContainerPort 访问8080会转到80
--restart=always　　						 		 # 随着docker容器的启动而启动
docker exec -ti 容器id bash          # 进入到容器中            
docker exec -ti 容器id sh
docker run --rm imageID pwd         # 启动容器，并显示目录
docker tag 5089850c0cde wget:0.2    # 打标签
docker run --rm -ti wget:0.1 bash
docker rm -v container1              # 删除容器时顺便删除volume
# 容器中目录(datavol)和主机目录(Users)互通   
			# 设置-file sharing
			docker run -it --name container1 -v /Users:/datavol busybox
			cd datavol
			ls
#从容器中拷贝文件到宿主机 不需要容器启动
docker cp containerName:/etc/mysql/my.cnf  /home/xxx/my.cnf 
# 将宿主机的文件拷贝容器里面的目录下 会覆盖老的文件
docker cp /home/xxx/my.cnf   containerName:/etc/mysql/	
docker exec -ti aic-mysql bash
# 主机（本地）连接docker中的mysql
docker exec -ti aic-mysql mysql -uroot -p
mysql -uroot -p
Aic_woody
	select version();
#
docker pull redis
# 本机连接docker中的redis
docker exec -ti redis redis-cli -a qwer6379
docker run -itd -p 6379:6379 redis --requirepass qwer6379
telnet localhost 6379
get 20190630
# 获取容器的输出信息 
docker container logs
# 清理所有处于终止状态的容器
docker container prune

# 开启redis
-it参数：容器的shell映射到当前的shell，然后在本机窗口输入的命令，就会传入容器
-d：容器启动后，在后台运行
	# 方式一
		docker run --name myredis -itd -p 6379:6379 redis --requirepass yuYU6U7J
	# 方式二	
		docker run --rm -itd -p 6379:6379 redis --requirepass yuYU6U7J
	# 方式三	
    	docker run -itd -p 6379:6379 redis --requirepass qwer6379
    	
# 连接到redis客户端
	# 方式一
		docker exec -it 4e2841e0c672 redis-cli
	# 方式二
    	docker exec -it myredis redis-cli

docker version
docker info
docker attach containerid
docker port containerName						# 2181/tcp -> 0.0.0.0:2181  docker端口 -> 外部端口
docker ps [-a]											# 查看容器运行情况
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' # 查看container的ip
docker inspect <container id> | grep "IPAddress"
docker system df										# 查看占用空间
docker image ls                     # 列出顶层镜像
docker image ls -a 									# 列出中间层镜像（其他镜像的依赖，只会存在一遍）
docker image ls -f since=ubuntu:16.06  # f为fliter，过滤
docker image ls -q									# 只列出id
# --filter结合-q产生出指定范围内的ID列表，然后送给另一个docker命令作为参数，然后批量操作
docker image ls --format "{{.ID}}: {{.Repository}}" # 定制返回列
docker image ls --format "table {{.ID}}\t {{.Repository}}\t{{.Tag}}"
docker image rm [选项] 镜像1 ..2      # （ID、镜像名、摘要）删除镜像（ID的前三个以上字符就行）
docker image ls --digests           # 列出镜像摘要
docker image rm $(docker image ls -q redis)  # 删除所有镜像名为redis的镜像
docker image rm $(docker image ls -q -f before=mongo:3.2)  # 删除所有在3.2之前的镜像
docker search mysql
docker stop  容器id		 					     # 停止容器
docker start 容器id		 					     # 启动容器
docker rm 容器id/容器名字             # 删除容器，容器无状态，退出后，数据会删除
docker rm -f 容器id/容器名字          # 强制删除
docker inspect --help		            # 查看容器运行信息				
docker inspect 容器id
docker history [image]							# Display the history of a particular image
docker kill [container]							# Kill a particular container
docker kill $(docker ps -q)         # Kill all containers that are currently running

# 创建镜像
	# 方式一  docker commit    # docker commit [ContainerID] [Repository[:Tag]
		docker ps -a
		docker commit --help
		docker commit 容器ID wget:0.1
		docker images
		docker run --rm -ti wget:0.1 bash
		docker commit mycontainer1 rominirani/ubuntu-git  #my Docker Hub user name is rominirani 
		docker run -it --name c1 <yourusername>/ubuntu-git
		docker push <yourusername>/ubuntu-git
		docker search ubuntu-git
	# 方式二 Dockerfile，包含用户创建Docker镜像所有指令的文本文件，指令指定在创建Docker镜像时做什么操作
		# Docker读取Dockerfile中的指令创建Docker镜像，每个指令将创建新的Docker镜像层
		# Docker build上下文
			# 1. Docker客户端以当前目录为build上下文
			# 2. 默认读取当前目录的Dockerfile进行build
			# 3. Docker客户端开始build后会将build上下文目录的文件打包成tar包并上传给Docker守护进程
			mkdir demo
			cd demo
			vi Dockerfile
			#Dockerfile内容
			docker build .      #创建  docker build -t wget:0.3 .
			docker images
			docker run --rm -ti 5089850c0cde bash
			
# 创建私有仓库
			docker pull registry
			docker run -d -p 5000:5000 --name localregistry registry
			docker ps
			docker pull busybox
			docker pull alpine
			docker pull localhost:5000/alpine
			docker tag alpine:latest localhost:5000/alpine:latest
			docker images           #alpine and localhost:5000/alpine
			docker push localhost:5000/alpine:latest # 推送标签过镜像或容器到本地仓库
			
# 挂载磁盘
	# 1. Data volumes
		# 容器中特殊的目录，停止容器后不被删除，能在容器间共享
		docker pull busybox
		docker run -it -v /data --name container1 busybox   # /data是容器中目录
		cd data
		touch file1.txt
		exit;
		docker ps -a
		docker inspect container1
		docker restart container1
		docker attach container1                 #之前file1.txt文件还在/Data目录中
		docker rm container1
		docker volume ls                         # 之前volume还在
		docker rm --help
		docker rm -v container1                  # 删除容器时顺便删除volume
		# 容器中目录(datavol)和主机目录(Users)互通   
			# 设置-file sharing
			docker run -it --name container1 -v /Users:/datavol busybox
			cd datavol
			ls
	# 2. Data volume containers
		# 容器间共享数据 --volumes-from
		docker run -it -v /data --name container1 busybox
		cd data
		touch file1.txt
		touch file2.txt
		docker ps
		docker exec container1 ls /data
		docker run -it --volumes-from container1 --name container2 busybox
		
# 容器间通讯
	# --link sourcecontainername:containeraliasname
		docker pull redis
		docker run -d --name redis1 redis
		docker ps
		docker run -it --link redis1:redis --name redisclient1 busybox
		cat /etc/hosts
		ping redis
		set
		docker run -it --link redis1:redis --name client1 redis sh
		ping redis
		redis-cli -h redis [-a qwer6379]
		PING
		set myvar DOCKER
		get myvar
		docker run -it --link redis1:redis --name client2 redis sh
		redis-cli -h redis [-a qwer6379]
		get myvar
	# docker compose
		
docker pull openjdk:8-jre          
docker images|grep jdk
docker run -it --entrypoint bash openjdk:8-jre

docker pull mysql
sh start.sh
docker logs [-f] aic-mysql

# 解决 MySQL 8.0 - Client does not support authentication protocol requested by server; consider upgrading MySQL client
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'Aic_woody';
flush privileges;
# root的plugin为mysql_native_password才正常
select Host,User,plugin from mysql.user;
		%         | root             | mysql_native_password

docker pull zookeeper 
docker run -it --rm --link aic-zookeeper:zookeeper zookeeper zkCli.sh -server zookeeper
# zookeeper客户端，执行成功后，会弹出java ui client
# java -jar zookeeper-dev-ZooInspector.jar  
java -jar /Users/dingyuanjie/Documents/study/docker/zookeeper/ZooInspector/build/zookeeper-dev-ZooInspector.jar
```

## commit

```shell
# commit不要使用，会导致臃肿
docker run --name webserver -d -p 80:80 nginx
docker exec -it webserver bash
echo '<h1>Hello Docker</h1>'> /usr/share/nginx/html/index.html
docker diff    								# 查看具体改动
# 定制镜像：在原镜像的基础上叠加存储层，保存为一个新的镜像
docker commit \
--author "woodyfine <woodyfine@qq.com>" \
--message "修改了默认网页" \
webserver \
nginx:v2
# 运行新的镜像  http://localhost:81/
docker run --name webserver3 -d -p 81:80 nginx:v2
```

# Dockerfile

```shell
mkdir mynginx
cd mynginx
touch Dockerfile
	# 每个指令都会建立一层
	FROM nginx                     # 指定基础镜像， FROM scratch 空白镜像
	# RUN 执行命令行命令的，
		# shell 格式：RUN <命令>
		# exec 格式：RUN ["可执行文件","参数1","参数2"]
	RUN echo '<h1>Hello Docker</h1>' > /usr/share/nginx/html/index.html
# 避免每一个命令都构建一层造成臃肿浪费，命令之间用  &&  连接
# 如 RUN buildDeps='gcc libc6-dev make' \
#        && apt-get update \
#        && ..
#        #记得清理之前下载的文件，否则会很臃肿
#        && rm -rf .*..

# 在Dockerfile所在目录执行  docker build [选项] <上下文路径/URL/->
# docker build 命令并非在本地构建镜像，而是在服务端（即Docker引擎中构建）
# . 代表上下文路径：服务端获取本地文件来获取构建所需的一切文件
# .dockerignore 文件，剔除不需要作为上下文传递给Docker引擎构建
docker build -t nginx:v3 .
```

```shell
# 其他 docker build用法
# 1. 直接用git repo构建
docker build https://github.com/twang2218/gitlab-ce-zh.git#:8.14    # 构建目录为 /8.14/
# 2. 用给定的 tar 压缩包构建
docker build http://server/context.tar.gz
# 3. 从标准输入中读取 Dockerfile 进行构建
docker build - < Dockerfile  或  cat Dockerfile | docker build -
# 4. 从标准输入中读取上下文压缩包进行构建
docker build - < context.tar.gz
```

```shell
# Dockerfile
# 基础镜像
FROM centos        
 #标签
LABEL maintainer "Clare Yang(zhangsan@qq.com)" 
# 后面路径覆盖前面路径
WORKDIR /apps  # 相对路径
WORKDIR nginx/logs # 相对路径会附加上上一个绝对路径上，/apps/nginx/logs

COPY test.txt .
COPY test1.txt .    #正则匹配 COPY *.txt /apps  

CMD curl
# docker build -t cmd:0.1 .
# docker run --rm cmd:0.1 curl -s https://httpbin.org/uuid
#或者
ENTRYPOINT ["curl","-s"]
CMD ["https://httpbin.org/uuid"]
# docker build -t cmd:0.2 .
# docker run --rm cmd:0.2
# docker run --rm cmd:0.2 http://www.baidu.com

RUN yum update -y && yum install -y wget  # 指令
```

- FROM指令

  Dockerfile必须以FROM开头

  ```shell
  FROM <image>[AS <name>]
  FROM <image>[:<tag>][AS <name>]
  ```

- WORKDIR指令

  > WORKDIR指令设置RUN、CMD、ENTRYPOINT、COPY和ADD的工作目录
  >
  > 语法：
  >
  > ```shell
  > WORKDIR  /path/to/directory	
  > ```
  >
  > 默认WORKDIR是根目录/，任何通过WORKDIR指令设置的WORKDIR都相对于根目录/
  >
  > 在同一Dockerfile中可设置多个WORKDIR指令，如果设置WORKDIR为绝对路径，后续WORKDIR指令设置的相对路径被附加到初始绝对路径

- COPY和ADD指令

  > COPY和ADD指令实现从主机到容器的文件传输功能
  >
  > COPY支持简单的文件服务
  >
  > ADD除了COPY支持的功能外还支持其他功能，如tar包自动解压、远程URL等
  >
  > 语法：
  >
  > ```shell
  > ADD <source> <destination>
  > COPY <source> <destination>
  > ADD --chown=<user>:<group><source><destination>
  > COPY --chown=<user>:<group><source><destination>
  > ```
  >
  > 如果destination不存在，则自动创建
  >
  > 所有新创建的文件或者文件夹UID和GID为0，即root用户，通过--chown选项可修改
  >
  > 如果destination未以/结尾，则认为destination为文件，source的内容将写入该文件
  >
  > 如果source包含有通配符，则destination必须为目录，且以/结尾，否则build失败
  >
  > source必须在build上下文存在，不能在build上下文之外
  >
  > 针对ADD指令，如果source是URL，而destination不是目录且不以/结尾，则从URL下载的文件写入destination；如果descination是目录且以/结尾，则URL对应的文件被下载到destination所在目录，如<destination>/<filename>;如果source是本地tar包，tar包被自动解压为目录，而远端tar包暂时不支持

- RUN指令

  > 语法
  >
  > ```shell
  > RUN <command>
  > RUN ["executable","parameter1","parameter2"]
  > ```
  >
  > RUN指令在容器的可写入层执行命令，并commit容器为新的镜像
  >
  > 上一步RUN指令生成的镜像被接下来RUN使用，每次RUN指令生成一个新的镜像
  >
  > Dockerfile中最好链式输入命令以减少创建镜像层数量，从而减少镜像大小

- CMD/ENTRYPOINT指令

  > 如何制定容器启动时执行什么命令
  >
  > 语法
  >
  > ```shell
  > CMD ["executable","param1","param2"](exec形式)
  > CMD ["param1","param2"](作为ENTRYPOINT的默认参数)
  > CMD command param1 param2(shell形式)
  > ENTRYPOINT ["executable","param1","param2"](exec形式)
  > ENTRYPOINT command param1 param2(shell形式)
  > ```
  >
  > 如果在Dockerfile中不指定CMD/ENTRYPOINT指令，Docker将使用基础镜像提供的默认命令
  >
  > CMD/ENTYRPOINT指令在创建Docker镜像时不执行，只有在容器启动时才执行
  >
  > 既可以exec形式，也可以shell形式指定要执行的指令

  ![image-20181203115230408](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/11.docker/image-20181203115230408-3809150.png)

  > ENTRYPOINT指令最好以exec形式执行，如果以shell形式，则一些参数不能正确传入或不能正常工作
  >
  > shell形式整个命令作为参数传入，可执行子命令，管道等
  >
  > exec形式不会调用shell命令，这意味着shell的一些特性如变量替换，管道等不能正常工作
  >
  > shell形式下将以/bin/sh -c 调用可执行程序，这意味着可执行程序没有PID与之对应，将不能接收UNIX信号

- VOLUME指令

  > VOLUME指令在Docker主机上创建目录并挂载到容器中，通常在Docker的根目录
  >
  > 语法
  >
  > ```shell
  > VOLUME <dir>
  > ```

- EXPOSE指令

  > EXPOSE指令告知Docker容器将监听在指定的端口
  >
  > 语法
  >
  > ```shell
  > EXPOSE <port> [<port>/<protocol>...]
  > ```

- Dockerfile Best Practice

  > 确保build上下文尽可能的小，以此减少镜像大小，.dockerignore可用于忽略不想被包含到Docker镜像的文件
  >
  > 尽可能使用multi-stage创建镜像
  >
  > 避免安装不必要的包
  >
  > 尽可能减少镜像的layer数量，以此减少镜像大小（Docker 1.10及以上版本只有RUN，COPY和ADD创建新的layer）

# Docker指令

* COPY 复制文件

  ```shell
  # 将从构建上下文目录中 <源路径> 的文件/目录复制到新的一层的镜像内的 <目标路径> 位置
  COPY [--chown=<user>:<group>] <源路径>... <目标路径>
  COPY [--chown=<user>:<group>] ["<源路径1>",... "<目标路径>"]
  #  --chown=<user>:<group> 选项来改变文件的所属用户及所属组
  # 例子
  COPY package.json /usr/src/app/
  COPY hom* /mydir/
  COPY hom?.txt /mydir/
  COPY --chown=55:mygroup files* /mydir/
  COPY --chown=bin files* /mydir/
  ```

* ADD

  ```shell
  # 在 COPY 基础上增加了一些功能，自动解压缩
  # 所有的文件复制均使用 COPY 指令，仅在需要自动解压缩的场合使用 ADD
  ```

* CMD

  ```shell
  # Docker 不是虚拟机，容器就是进程。既然是进程，那么在启动容器的时候，需要指定所运行的程序及参数。CMD 指令就是用于指定默认的容器主进程的启动命令的
  # CMD，容器启动命令
  	# shell 格式：CMD <命令>
  	# exec 格式：CMD ["可执行文件", "参数1", "参数2"...]
  	# 参数列表格式：CMD ["参数1", "参数2"...]。在指定了 ENTRYPOINT 指令后，用 CMD 指定具体的参数
  # Docker 不是虚拟机，容器中的应用都应该以前台执行，而不是像虚拟机、物理机里面那样，用 systemd 去启动后台服务，容器内没有后台服务的概念
  # 对于容器而言，其启动程序就是容器应用进程，容器就是为了主进程而存在的，主进程退出，容器就失去了存在的意义，从而退出，其它辅助进程不是它需要关心的东西
  # 而使用 service nginx start 命令，则是希望 upstart 来以后台守护进程形式启动 nginx 服务。而刚才说了 CMD service nginx start 会被理解为 CMD [ "sh", "-c", "service nginx start"]，因此主进程实际上是 sh。那么当 service nginx start 命令结束后，sh 也就结束了，sh 作为主进程退出了，自然就会令容器退出
  # 正确的做法是直接执行 nginx 可执行文件，并且要求以前台形式运行。比如
  CMD ["nginx", "-g", "daemon off;"]
  ```

* ENTRYPOINT

  ```shell
  # ENTRYPOINT 入口点，在指定容器启动程序及参数，需要通过 docker run 的参数 --entrypoint 来指定
  # 当指定了 ENTRYPOINT 后，CMD 的含义就发生了改变，不再是直接的运行其命令，而是将 CMD 的内容作为参数传给 ENTRYPOINT 指令，<ENTRYPOINT> "<CMD>"
  # 	场景一：让镜像变成像命令一样使用
  FROM ubuntu:18.04
  RUN apt-get update \
      && apt-get install -y curl \
      && rm -rf /var/lib/apt/lists/*
  CMD [ "curl", "-s", "https://ip.cn" ]
  # 使用docker build -t myip . 来构建镜像，需要查询当前公网 IP：docker run myip
  # 希望加入 -i 这参数，docker run myip curl -s https://ip.cn -i
  # 使用ENTRYPOIN优化
  FROM ubuntu:18.04
  RUN apt-get update \
      && apt-get install -y curl \
      && rm -rf /var/lib/apt/lists/*
  ENTRYPOINT [ "curl", "-s", "https://ip.cn" ]
  # 直接使用docker run myip  ，  docker run myip -i   # -i作为参数传给 curl
  #		场景二：应用运行前的准备工作
  ```

* ENV 设置环境变量

  ```shell
  ENV <key> <value>
  ENV <key1>=<value1> <key2>=<value2>...
  # 例子
  ENV VERSION=1.0 DEBUG=on \
      NAME="Happy Feet"
  # 定义环境变量NODE_VERSION，使用 $NODE_VERSION 来进行引用
  ENV NODE_VERSION 7.2.0
  ```

* ARG

  ```shell
  # ARG 构建参数，设置环境变量，但是容器运行时是不会存在这些环境变量的
  ARG <参数名>[=<默认值>]
  ```

* VOLUME 定义匿名卷

  ```shell
  VOLUME ["<路径1>", "<路径2>"...]
  VOLUME <路径>
  # 在 Dockerfile 中，可事先指定某些目录挂载为匿名卷，这样在运行时如果用户不指定挂载，其应用也可以正常运行，不会向容器存储层写入大量数据
  VOLUME /data
  # /data 目录就会在运行时自动挂载为匿名卷，任何向 /data 中写入的信息都不会记录进容器存储层，从而保证了容器存储层的无状态化。当然，运行时可以覆盖这个挂载设置
  docker run -d -v mydata:/data xxxx
  ```

* EXPOSE

  ```shell
  # EXPOSE 声明运行时容器提供服务端口，在运行时并不会因为这个声明应用就会开启这个端口的服务
  EXPOSE <端口1> [<端口2>...]
  # 帮助镜像使用者理解这个镜像服务的守护端口，以方便配置映射
  # 运行时使用随机端口映射时，也就是 docker run -P 时，会自动随机映射 EXPOSE 的端口
  # 将 EXPOSE 和在运行时使用 -p <宿主端口>:<容器端口> 区分开来
  # 	-p，将容器的对应端口服务公开给外界访问
  #   EXPOSE 仅仅是声明容器打算使用什么端口而已，并不会自动在宿主进行端口映射
  ```

* WORKDIR

  ```shell
  # WORKDIR 指定工作目录，以后各层的当前目录就被改为指定的目录
  WORKDIR <工作目录路径>
  ```

* USER

  ```shell
  # USER 指定当前用户
  USER <用户名>[:<用户组>]
  # USER 指令和 WORKDIR 相似，都是改变环境状态并影响以后的层
  # 建立 redis 用户，并使用 gosu 换另一个用户执行命令
  RUN groupadd -r redis && useradd -r -g redis redis
  # 下载 gosu
  RUN wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/1.7/gosu-amd64" \
      && chmod +x /usr/local/bin/gosu \
      && gosu nobody true
  # 设置 CMD，并以另外的用户执行
  CMD [ "exec", "gosu", "redis", "redis-server" ]
  ```

* HEALTHCHECK 健康检查

  ```shell
  # HEALTHCHECK 指令是告诉 Docker 应该如何进行判断容器的状态是否正常
  # HEALTHCHECK [选项] CMD <命令>：设置检查容器健康状况的命令,格式和 ENTRYPOINT 一样，分为 shell 格式，和 exec 格式。返回值0：成功；1：失败；2：保留，不要使用这个值
  # HEALTHCHECK NONE：如果基础镜像有健康检查指令，使用这行可以屏蔽掉其健康检查指令
  # Docker 提供了 HEALTHCHECK 指令，通过该指令指定一行命令，用这行命令来判断容器主进程的服务状态是否还正常，从而比较真实的反应容器实际状态
  # 当在一个镜像指定了 HEALTHCHECK 指令后，用其启动容器，初始状态会为 starting，在 HEALTHCHECK 指令检查成功后变为 healthy，如果连续一定次数失败，则会变为 unhealthy
  --interval=<间隔>：两次健康检查的间隔，默认为 30 秒；
  --timeout=<时长>：健康检查命令运行超时时间，如果超过这个时间，本次健康检查就被视为失败，默认 30 秒；
  --retries=<次数>：当连续失败指定次数后，则将容器状态视为 unhealthy，默认 3 次
  # 例子，有个镜像是个最简单的 Web 服务，希望增加健康检查来判断其 Web 服务是否在正常工作，可用 curl 来帮助判断
  FROM nginx
  RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
  HEALTHCHECK --interval=5s --timeout=3s \
    CMD curl -fs http://localhost/ || exit 1
  # 构建这个镜像
  docker build -t myweb:v1 .
  # 启动一个容器
  docker run -d --name web -p 80:80 myweb:v1
  # 通过 docker container ls 看到最初的状态为 (health: starting)
  # 在等待几秒钟后，再次 docker container ls，就会看到健康状态变化为了 (healthy)
  # 为了帮助排障，健康检查命令的输出（包括 stdout 以及 stderr）都会被存储于健康状态里，可以用 docker inspect 来查看
  docker inspect --format '{{json .State.Health}}' web | python -m json.tool
  ```

* ONBUILD为他人做嫁衣裳

  ```shell
  ONBUILD <其它指令>
  # 其他指令，在当前镜像构建时并不会被执行。只有当以当前镜像为基础镜像，去构建下一级镜像的时候才会被执行
  # Dockerfile 中的其它指令都是为了定制当前镜像而准备的，唯有 ONBUILD 是为了帮助别人定制自己而准备的
  FROM node:slim
  RUN mkdir /app
  WORKDIR /app
  ONBUILD COPY ./package.json /app
  ONBUILD RUN [ "npm", "install" ]
  ONBUILD COPY . /app/
  CMD [ "npm", "start" ]
  # 在构建基础镜像的时候，ONBUILD这三行并不会被执行。然后各个项目的 Dockerfile 就变成了简单地
  FROM my-node
  # 当在各个项目目录中，用这个只有一行的 Dockerfile 构建镜像时，之前基础镜像的那三行 ONBUILD 就会开始执行，成功的将当前项目的代码复制进镜像、并且针对本项目执行 npm install，生成应用镜像
  ```

# 使用多阶段构建

```shell
# Dockerfile
FROM golang:1.9-alpine as builder    # 使用 as 来为某一阶段命名
RUN apk --no-cache add git
WORKDIR /go/src/github.com/go/helloworld/
RUN go get -d -v github.com/go-sql-driver/mysql
COPY app.go .
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app .
FROM alpine:latest as prod
RUN apk --no-cache add ca-certificates
WORKDIR /root/
COPY --from=0 /go/src/github.com/go/helloworld/app .
CMD ["./app"]
# 构建镜像
docker build -t go/helloworld:3 .

# 只构建某一阶段的镜像
	# 当只想构建 builder 阶段的镜像时，增加 --target=builder 参数即可
docker build --target builder -t username/imagename:tag .
# 构建时从其他镜像复制文件
COPY --from=nginx:latest /etc/nginx/nginx.conf /nginx.conf
```

* 构建多种系统架构支持的 Docker 镜像 -- docker manifest 命令

# 使用 `BuildKit` 构建镜像

```shell
# 下一代的镜像构建组件，docker-compose build 命令暂时不支持BuildKit，构建时使用一个新的Dockerfile文件
# 由于 BuildKit 为实验特性，每个 Dockerfile 文件开头都必须加上如下指令
syntax = docker/dockerfile:experimental

# 新的指令来加快镜像构建
RUN --mount=type=cache
# 镜像构建时把 xxx 文件夹挂载上去，在构建完成后，这个 xxx 文件夹会自动卸载，实际的镜像中并不包含 xxx 文件夹

# 将一个镜像（或上一构建阶段）的文件挂载到指定位置
RUN --mount=type=bind

# 将一个 tmpfs 文件系统挂载到指定位置
RUN --mount=type=tmpfs

# 将一个文件挂载到指定位置
RUN --mount=type=secret

# 挂载 ssh 密钥
RUN --mount=type=ssh

# 清理构建缓存
docker builder prune
```

# 其他制作镜像方式

```shell
# 从 rootfs 压缩包导入
docker import [选项] <文件>|<URL>|- [<仓库名>[:<标签>]]
#压缩包可是本地文件、远程Web文件，甚至是从标准输入中得到。压缩包将会在镜像/目录展开，并直接作为镜像第一层提交
docker import \
    http://download.openvz.org/template/precreated/ubuntu-16.04-x86_64.tar.gz \
    openvz/ubuntu:16.04
# 自动下载了 ubuntu-16.04-x86_64.tar.gz 文件，并且作为根文件系统展开导入，并保存为镜像 openvz/ubuntu:16.04
docker history openvz/ubuntu:16.04

# docker save 和 docker load  不推荐了
# 将镜像保存为一个文件，然后传输到另一个位置上，再加载进来
# docker save 命令可以将镜像保存为归档文
docker save alpine -o filename(同名则会覆盖（没有警告）)
docker save alpine | gzip > alpine-latest.tar.gz
# 将 alpine-latest.tar.gz 文件复制到了到了另一个机器上，可以用下面这个命令加载镜像
docker load -i alpine-latest.tar.gz
# 一个命令完成从一个机器将镜像迁移到另一个机器，并且带进度条的功能
docker save <镜像名> | bzip2 | pv | ssh <用户名>@<主机名> 'cat | docker load'
```

# 容器

```shell
# docker run 来创建容器时，Docker 在后台运行的标准操作包括
# 1. 检查本地是否存在指定的镜像，不存在就从公有仓库下载
# 2. 利用镜像创建并启动一个容器
# 3. 分配一个文件系统，并在只读的镜像层外面挂载一层可读写层
# 4. 从宿主主机配置的网桥接口中桥接一个虚拟接口到容器中去
# 5. 从地址池配置一个 ip 地址给容器
# 6. 执行用户指定的应用程序
# 7. 执行完毕后容器被终止

# 导出容器快照到本地文件
docker export 7691a814370e > ubuntu.tar
# 从容器快照文件中再导入为镜像
cat ubuntu.tar | docker import - test/ubuntu:v1.0
# 也可以通过指定 URL 或者某个目录来导入
docker import http://example.com/exampleimage.tgz example/imagerepo
# docker import导入一个容器快照到本地镜像库，将丢弃所有的历史记录和元数据信息，可以重新指定标签等元数据信息
# docker load导入镜像存储文件到本地镜像库，将保存完整记录
```

# 访问仓库

```shell
# 仓库（Repository）是集中存放镜像的地方
# 注册服务器是管理仓库的具体服务器，每个服务器上可以有多个仓库，而每个仓库下面有多个镜像，仓库可以被认为是一个具体的项目或目录
# 例如对于仓库地址 dl.dockerpool.com/ubuntu来说，dl.dockerpool.com 是注册服务器地址，ubuntu 是仓库名

# 命令行界面登录/退出 Docker Hub
docker login
docker logout

# 查找docker search centos的时候通过 --filter=stars=N 参数可以指定仅显示收藏数量为 N 以上的镜像

# 推送镜像
docker tag ubuntu:18.04 username/ubuntu:18.04
docker image ls
docker push username/ubuntu:18.04
docker search username

# 自动构建
# 允许用户通过 Docker Hub 指定跟踪一个目标网站（支持 GitHub 或 BitBucket）上的项目，一旦项目发生新的提交 （commit）或者创建了新的标签（tag），Docker Hub 会自动构建镜像并推送到 Docker Hub 中
	# 配置
	# 1. 登录 Docker Hub
	# 2. 在 Docker Hub 点击右上角头像，在账号设置（Account Settings）中关联（Linked Accounts）目标网站
	# 3. 在 Docker Hub 中新建或选择已有的仓库，在 Builds 选项卡中选择 Configure Automated Builds；
	# 4. 选取一个目标网站中的项目（需要含 Dockerfile）和分支
	# 5. 指定 Dockerfile 的位置，并保存
	# 之后，可以在 Docker Hub 的仓库页面的 Timeline 选项卡中查看每次构建的状态
	
# 私有仓库
docker run -d -p 5000:5000 --restart=always --name registry registry
# 默认情况下，仓库会被创建在容器的 /var/lib/registry 目录下
#  -v 参数将上传的镜像放到本地的 /opt/data/registry 目录
docker run -d \
    -p 5000:5000 \
    -v /opt/data/registry:/var/lib/registry \
    registry
# 例如私有仓库地址为 127.0.0.1:5000
docker tag ubuntu:latest 127.0.0.1:5000/ubuntu:latest
docker push 127.0.0.1:5000/ubuntu:latest
# curl 查看仓库中的镜像， {"repositories":["ubuntu"]}表明镜像已经被成功上传
curl 127.0.0.1:5000/v2/_catalog
# 先删除已有镜像，再尝试从私有仓库中下载这个镜像
docker image rm 127.0.0.1:5000/ubuntu:latest
docker pull 127.0.0.1:5000/ubuntu:latest

# Nexus3.x 的私有仓库 
# 启动 Nexus 容器
docker run -d --name nexus3 --restart=always \
    -p 8081:8081 \
    --mount src=nexus-data,target=/nexus-data \
    sonatype/nexus3
# 等待 3-5 分钟，没有异常退出，使用浏览器打开 http://YourIP:8081 访问 Nexus 了
# 默认帐号是 admin 密码是 admin123
```

# Docker数据管理

* 数据卷（Volumes）

  * 可以在容器之间共享和重用、修改会立马生效、更新不会影响镜像、默认会一直存在，即使容器被删除
  * 被设计用来持久化数据的，它的生命周期独立于容器

  ```shell
  # 创建一个数据卷
  docker volume create my-vol
  # 删除数据卷
  docker volume rm my-vol
  # 需要在删除容器的同时移除数据卷。可在删除容器的时候使用 docker rm -v 这个命令
  # 无主的数据卷可能会占据很多空间，要清理请使用以下命令
  docker volume prune
  # 查看所有的 数据卷
  docker volume ls
  # 查看指定 数据卷 的信息
  docker volume inspect my-vol
  
  # 启动一个挂载数据卷的容器
  # docker run时使用--mount 标记来将数据卷挂载到容器里
  # 创建一个名为 web 的容器，并加载一个 数据卷 到容器的 /webapp 目录
  docker run -d -P \
      --name web \
      # -v my-vol:/wepapp \
      --mount source=my-vol,target=/webapp \
      training/webapp \
      python app.py
  # 查看数据卷的具体信息，数据卷 信息在 "Mounts" Key 
  docker inspect web
  ```

* 挂载主机目录 (Bind mounts)
  * 使用 `--mount` 标记可以指定挂载一个本地主机的目录(必须是绝对路径)到容器中去

  ```shell
  # 加载主机的 /src/webapp 目录到容器的 /opt/webapp目录
  # 默认权限是 读写，用户也可以通过增加 readonly 指定为 只读
  docker run -d -P \
      --name web \
      # -v /src/webapp:/opt/webapp \
      --mount type=bind,source=/src/webapp,target=/opt/webapp,readonly \
      training/webapp \
      python app.py
      
  # 挂载一个本地主机文件作为数据卷
  # --mount 标记也可以从主机挂载单个文件到容器中
  # 记录在容器输入过的命令
  $ docker run --rm -it \
     # -v $HOME/.bash_history:/root/.bash_history \
     --mount type=bind,source=$HOME/.bash_history,target=/root/.bash_history \
     ubuntu:18.04 \
     bash
  root@2affd44b4667:/# history
  1  ls
  2  diskutil list
  ```

# Docker网络

```shell
# 外部访问容器
# -p 则可以指定要映射的端口
# ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort
# 查看映射端口配置
# docker port 来查看当前映射的端口配置，也可以查看到绑定的地址
docker port nostalgic_morse 5000
# -p 标记可多次使用来绑定多个端口
$ docker run -d \
    -p 5000:5000 \
    -p 3000:80 \
    training/webapp \
    python app.py
    
# 容器互联
# 新建网络，-d 参数指定 Docker 网络类型，有 bridge overlay。其中 overlay 网络类型用于 Swarm mode
docker network create -d bridge my-net
# 连接容器，运行一个容器并连接到新建的 my-net 网络
docker run -it --rm --name busybox1 --network my-net busybox sh
# 打开新的终端，再运行一个容器并加入到 my-net 网络
docker run -it --rm --name busybox2 --network my-net busybox sh
# 再打开一个新的终端查看容器信息
docker container ls
# 通过 ping 来证明 busybox1 容器和 busybox2 容器建立了互联关系
# 在 busybox1 容器输入
ping busybox2
# 同理在 busybox2 容器执行 
ping busybox1

# 配置 DNS
# Docker 利用虚拟文件来挂载容器的 3 个相关配置文件来自定义配置容器的主机名和 DNS
# 在容器中使用 mount 命令可以看到挂载信息
$ mount
/dev/disk/by-uuid/1fec...ebdf on /etc/hostname type ext4 ...
/dev/disk/by-uuid/1fec...ebdf on /etc/hosts type ext4 ...
tmpfs on /etc/resolv.conf type tmpfs ...
# 这种机制可以让宿主主机 DNS 信息发生更新后，所有 Docker 容器的 DNS 配置通过 /etc/resolv.conf 文件立刻得到更新
# 配置全部容器的 DNS ，也可以在 /etc/docker/daemon.json 文件中增加以下内容来设置
{
  "dns" : [
    "114.114.114.114",
    "8.8.8.8"
  ]
}
# 这样每次启动的容器 DNS 自动配置为 114.114.114.114 和 8.8.8.8
# 证明其已经生效
docker run -it --rm ubuntu:18.04  cat etc/resolv.conf
# 手动指定容器的配置，docker run时
-h HOSTNAME 或者 --hostname=HOSTNAME 设定容器的主机名，它会被写到容器内的 /etc/hostname 和 /etc/hosts。但它在容器外部看不到，既不会在 docker container ls 中显示，也不会在其他的容器的 /etc/hosts 看到
--dns=IP_ADDRESS 添加 DNS 服务器到容器的 /etc/resolv.conf 中，让容器用这个服务器来解析所有不在 /etc/hosts 中的主机名
--dns-search=DOMAIN 设定容器的搜索域，当设定搜索域为 .example.com 时，在搜索一个名为 host 的主机时，DNS 不仅搜索 host，还会搜索 host.example.com
```

# Docker Compose

* 负责实现对 Docker 容器集群的快速编排

* 允许用户通过一个单独的 `docker-compose.yml` 模板文件（YAML 格式）来定义一组相关联的应用容器为一个项目（project）

* 服务 (`service`)：一个应用的容器，实际上可以包括若干运行相同镜像的容器实例

* 项目 (`project`)：由一组关联的应用容器组成的一个完整业务单元，在 `docker-compose.yml` 文件中定义

* `Compose` 的默认管理对象是项目，通过子命令对项目中的一组容器进行便捷地生命周期管理

  ```shell
  docker-compose --version
  # 使用，用 Python 来建立一个能够记录页面访问次数的 web 网站
  # 1. 新建文件夹，并编写app.py 文件
  from flask import Flask
  from redis import Redis
  
  app = Flask(__name__)
  redis = Redis(host='redis', port=6379)
  
  @app.route('/')
  def hello():
      count = redis.incr('hits')
      return 'Hello World! 该页面已被访问 {} 次。\n'.format(count)
  
  if __name__ == "__main__":
      app.run(host="0.0.0.0", debug=True)
      
  # 2. 编写 Dockerfile 文件
  FROM python:3.6-alpine
  ADD . /code
  WORKDIR /code
  RUN pip install redis flask
  CMD ["python", "app.py"]
  
  # 3. 编写 docker-compose.yml 文件，是 Compose 使用的主模板文件
  version: '3'
  services:
  
    web:
      build: .
      ports:
       - "5000:5000"
  
    redis:
      image: "redis:alpine"
      
  # 运行 compose 项目，访问本地 5000 端口，每次刷新页面，计数就会加 1,http://localhost:5000/
  docker-compose up
  ```

  ```shell
  # # 命令对象将是项目，这意味着项目中所有的服务都会受到命令影响
  docker-compose
  # 将尝试自动完成包括构建镜像，（重新）创建服务，启动服务，并关联服务相关容器的一系列操作
  # 执行 docker-compose [COMMAND] --help 或者 docker-compose help [COMMAND] 可以查看具体某个命令的使用格式
  docker-compose logs
  ```

  ```yaml
  # Compose 模板文件，默认名称docker-compose.yml，格式为 YAML 格式
  version: "3"
  services:
    webapp:
    	# 每个服务都必须通过 image 指令指定镜像或 build 指令（需要 Dockerfile）等来自动构建生成镜像
    	# 如果使用 build 指令，在 Dockerfile 中设置的选项(例如：CMD, EXPOSE, VOLUME, ENV 等) 将会自动被获取，无需在 docker-compose.yml 中再次设置
    	# build: ./dir
      image: examples/web
      ports:
        - "80:80"
      volumes:
        - "/data"
  
  # build指定 Dockerfile 所在文件夹的路径（可以是绝对路径，或者相对 docker-compose.yml 文件的路径）。 Compose 将会利用它自动构建这个镜像，然后使用这个镜像
  version: '3'
  services:
    webapp:
      build:
        context: ./dir		# context 指令指定 Dockerfile 所在文件夹的路径
        dockerfile: Dockerfile-alternate    # dockerfile 指令指定 Dockerfile 文件名
        args:							# arg 指令指定构建镜像时的变量
          buildno: 1
        cache_from:       # cache_from 指定构建镜像的缓存
      		- alpine:latest
     			- corp/web_app:3.14
     	depends_on:         # 解决容器的依赖、启动先后的问题。以下例子中会先启动 redis db 再启动 web
        - db							# web 服务不会等待 redis db 「完全启动」之后才启动
        - redis
  		cap_add:            # 指定容器的内核能力（capacity）分配      
  			- ALL		
  		cap_drop:
  			- NET_ADMIN
  		command: echo "hello world"   # 覆盖容器启动后默认执行的命令
  		configs              # 仅用于 Swarm mode
  		deploy							 # 仅用于 Swarm mode
  		cgroup_parent: cgroups_1          # 指定父 cgroup 组，意味着将继承该组的资源限制
  		container_name: docker-web-container  #指定容器名称。默认将会使用 项目名称_服务名称_序号
  		devices:             # 指定设备映射关系
  			- "/dev/ttyUSB1:/dev/ttyUSB0"
  		dns:								# 自定义 DNS 服务器
  			- 8.8.8.8
  		  - 114.114.114.114
  		dns_search:          # 配置 DNS 搜索域
  			- domain1.example.com
    		- domain2.example.com
    	tmpfs:               # 挂载一个 tmpfs 文件系统到容器
    		- /run
    		- /tmp
    	env_file:             # 从文件中获取环境变量
    		- ./common.env
   		 	- ./apps/web.env
    		- /opt/secrets.env
    	environment:      # 设置环境变量,只给定名称的变量会自动获取运行 Compose 主机上对应变量的值
    		- RACK_ENV=development
    		- SESSION_SECRET
    	expose:          # 暴露端口，但不映射到宿主机，只被连接的服务访问,仅可以指定内部端口为参数
  			- "3000"
   			- "8000"
   		extra_hosts:      # 指定额外的 host 名称映射信息,会在启动后的服务容器中 /etc/hosts 文件中添加
   			- "googledns:8.8.8.8"
   			- "dockerhub:52.1.157.61"
   		healthcheck:      # 通过命令检查容器是否健康运行
    		test: ["CMD", "curl", "-f", "http://localhost"]
    		interval: 1m30s
    		timeout: 10s
    		retries: 3
    	labels:          # 为容器添加 Docker 元数据（metadata）信息。例如可以为容器添加辅助说明信息
    		com.startupteam.description: "webapp for a startup team"
    		com.startupteam.department: "devops department"
    		com.startupteam.release: "rc3 for v1.0"
    	logging:          # 配置日志选项,目前支持三种日志驱动类型json-file,syslog,none
    		driver: "syslog"
    		options:        # 配置日志驱动的相关参数
    			max-size: "200k"
    			max-file: "10"
      		syslog-address: "tcp://192.168.0.42:123"
      network_mode:"bridge"   # 设置网络模式,bridge、host、none
      some-service:
      	networks:             # 配置容器连接的网络
       		- some-network
       		- other-network
      pid: "host"   #跟主机系统共享进程命名空间。打开该选项的容器之间，以及容器和宿主机系统之间可以通过进程 ID 来相互访问和操作
      ports:  # 暴露端口信息，使用宿主端口：容器端口 (HOST:CONTAINER) 格式，或者仅仅指定容器的端口（宿主将会随机选择端口）都可以
      	- "3000"
   			- "8000:8000"
   			- "49100:22"
   			- "127.0.0.1:8001:8001"
   		security_opt:  # 指定容器模板标签（label）机制的默认属性（用户、角色、类型、级别等）。例如配置标签的用户名和角色名
   			- label:user:USER
      	- label:role:ROLE
      stop_signal:SIGUSR1  # 设置另一个信号来停止容器。在默认情况下使用的是 SIGTERM 停止容器
      sysctls:             # 配置容器内核参数
      	net.core.somaxconn: 1024
    		net.ipv4.tcp_syncookies: 0
    	ulimits:  # 指定容器的 ulimits 限制值,例如，指定最大进程数为 65535，指定文件句柄数为 20000（软限制，应用可以随时修改，不能超过硬限制） 和 40000（系统硬限制，只能 root 用户提高）
      	nproc: 65535
     	 	nofile:
        	soft: 20000
        	hard: 40000
  		volumes:  # 数据卷所挂载路径设置。可以设置宿主机路径 （HOST:CONTAINER） 或加上访问模式 （HOST:CONTAINER:ro）,该指令中路径支持相对路径
  			- /var/lib/mysql
   			- cache/:/tmp/cache
   			- ~/configs:/etc/configs/:ro
  
  # 其他指令
  # 此外，还有包括 domainname, entrypoint, hostname, ipc, mac_address, privileged, read_only, shm_size, restart, stdin_open, tty, user, working_dir 等指令，基本跟 docker run 中对应参数的功能一致
  		entrypoint: /code/entrypoint.sh  # 指定服务容器启动后执行的入口文件
  		user: nginx						# 指定容器中运行应用的用户名
  		working_dir: /code		# 指定容器中工作目录
  		domainname: your_website.com # 指定容器中搜索域名、主机名、mac 地址等
  		hostname: test
  		mac_address: 08-00-27-00-0C-0A
  		privileged: true   # 允许容器中运行一些特权命令
  		restart: always # 指定容器退出后的重启策略为始终重启
  		read_only: true # 以只读模式挂载容器的 root 文件系统
  		stdin_open: true # 打开标准输入，可以接受外部输入
  		tty: true  # 模拟一个伪终端
      
  	redis:	
      image: redis      # 指定为镜像名称或镜像 ID。如果镜像在本地不存在，Compose 将会尝试拉取这个镜像
  
    db:
    # 读取变量
  		# Compose 模板文件支持动态读取主机的系统环境变量和当前目录下的 .env 文件中的变量
  		# 如果执行 MONGO_VERSION=3.2 docker-compose up 则会启动一个 mongo:3.2 镜像的容器
  		# 若当前目录存在 .env 文件，执行 docker-compose 命令时将从该文件中读取变量
  		# 在当前目录新建 .env 文件并写入以下内容
  		# # 支持 # 号注释
  			# MONGO_VERSION=3.6
  		# 执行 docker-compose up 则会启动一个 mongo:3.6 镜像的容器
      image: "mongo:${MONGO_VERSION}"
      
  mysql:
    image: mysql
    environment:
      MYSQL_ROOT_PASSWORD_FILE: /run/secrets/db_root_password
    secrets:            # # 存储敏感数据，例如 mysql 服务密码
      - db_root_password
      - my_other_secret
      
  networks:              # 配置容器连接的网络
    some-network:
    other-network:
  secrets:          # 存储敏感数据，例如 mysql 服务密码
    my_secret:
      file: ./my_secret.txt
    my_other_secret:
      external: true
  ```

# Docker Machine

*  Docker 官方编排（Orchestration）项目之一，负责在多种平台上快速安装 Docker 环境

  ```yaml
  docker-machine -v
  ```

# Docker Swarm

* 提供 Docker 容器集群服务，是 Docker 官方对容器云生态进行支持的核心方案

* 将多个 Docker 主机封装为单个大型的虚拟 Docker 主机，快速打造一套容器云平台

* `Swarm mode` 内置 kv 存储功能，提供了众多的新特性，比如：具有容错能力的去中心化设计、内置服务发现、负载均衡、路由网格、动态伸缩、滚动更新、安全传输等。使得 Docker 原生的 `Swarm` 集群具备与 Mesos、Kubernetes 竞争的实力

*  Docker 引擎内置（原生）的集群管理和编排工具

* 节点

  * 运行 Docker 的主机可以主动初始化一个 `Swarm` 集群或者加入一个已存在的 `Swarm` 集群，这样这个运行 Docker 的主机就成为一个 `Swarm` 集群的节点 (`node`)

  * 分为管理 (`manager`) 节点和工作 (`worker`) 节点

  * 管理节点用于 `Swarm` 集群的管理，`docker swarm` 命令基本只能在管理节点执行（节点退出集群命令 `docker swarm leave` 可以在工作节点执行）。一个 `Swarm` 集群可以有多个管理节点，但只有一个管理节点可以成为 `leader`，`leader`通过 `raft` 协议实现

  * 工作节点是任务执行节点，管理节点将服务 (`service`) 下发至工作节点执行。管理节点默认也作为工作节点。也可以通过配置让服务只运行在管理节点

    ![image-20190716212730528](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190716212730528.png)

  * 任务 （`Task`）是 `Swarm` 中的最小的调度单位，目前来说就是一个单一的容器

  * 服务 （`Services`） 是指一组任务的集合，服务定义了任务的属性。服务有两种模式：

    * `replicated services` 按照一定规则在各个工作节点上运行指定个数的任务
    * `global services` 每个工作节点上运行一个任务
    * 两种模式通过 `docker service create` 的 `--mode` 参数指定

    ![image-20190716212915333](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190716212915333.png)

    ```shell
    # 创建 Swarm 集群，包含一个管理节点和两个工作节点的最小 Swarm 集群
    	# 初始化集群
    		#	创建一个 Docker 主机作为管理节点
    		docker-machine create -d virtualbox manager
    		docker-machine ssh manager
    		# 使用 docker swarm init 在管理节点初始化一个 Swarm 集群
    		# 执行 docker swarm init 命令的节点自动成为管理节点
    		docker swarm init --advertise-addr 192.168.99.100
    	# 增加工作节点，继续创建两个 Docker 主机作为工作节点，并加入到集群中
    		docker-machine create -d virtualbox worker1
    		docker-machine ssh worker1
    		docker swarm join \
        --token SWMTKN-1-49nj1cmql0jkz5s954yi3oex3nedyz0fb0xx14ie39trti4wxv-8vxv8rssmk743ojnwacrr2e7c \
        192.168.99.100:2377
        docker-machine create -d virtualbox worker2
        docker-machine ssh worker2
        docker swarm join \
        --token SWMTKN-1-49nj1cmql0jkz5s954yi3oex3nedyz0fb0xx14ie39trti4wxv-8vxv8rssmk743ojnwacrr2e7c \
        192.168.99.100:2377
       # 查看集群
         # 在管理节点使用 docker node ls 查看集群
         docker node ls
    ```

    ```shell
    # 部署服务
    # docker service 命令来管理 Swarm 集群中的服务，该命令只能在管理节点运行
    	# 新建服务
    		# 创建的 Swarm 集群中运行一个名为 nginx 服务
    		docker service create --replicas 3 -p 80:80 --name nginx nginx:1.13.7-alpine
    		# 现在使用浏览器，输入任意节点 IP ,即可看到 nginx 默认页面
    	# 查看服务
    		# 使用 docker service ls 来查看当前 Swarm 集群运行的服务
    		docker service ls
    		# 使用 docker service ps 来查看某个服务的详情
    		docker service ps nginx
    		# 使用 docker service logs 来查看某个服务的日志
    		docker service logs nginx
    	# 服务伸缩
    		# 使用 docker service scale 对一个服务运行的容器数量进行伸缩
    			# 当业务处于高峰期时，需要扩展服务运行的容器数量
    			docker service scale nginx=5
    			# 当业务平稳时，需要减少服务运行的容器数量
    			docker service scale nginx=2
    	# 删除服务
    		# 使用 docker service rm 来从 Swarm 集群移除某个服务
    		docker service rm nginx
    ```

    ```shell
    # 在 Swarm 集群中使用 compose 文件,来配置、启动多个服务
    # docker service create 一次只能部署一个服务,使用 docker-compose.yml 我们可以一次启动多个关联的服务
    ```

# CoreOS

* 提供了运行现代基础设施的特性，支持大规模服务部署，使得在基于最小化的现代操作系统上构建规模化的计算仓库成为了可能
* 特性
  * 一个最小化操作系统、无痛更新、应用作为 Docker 容器运行在 CoreOS 上、支持集群、分布式系统工具、服务发现

# Kubernetes

* 基于 Docker 的开源容器集群管理系统
* 目标是管理跨多个主机的容器，提供基本的部署，维护以及运用伸缩
* 建于 Docker 之上的 `Kubernetes` 可以构建一个容器的调度服务，其目的是让用户透过 `Kubernetes` 集群来进行云端容器集群的管理，而无需用户进行复杂的设置工作。系统会自动选取合适的工作节点来执行具体的容器集群调度处理工作。其核心概念是 `Container Pod`
* 一个 `Pod` 由一组工作于同一物理工作节点的容器构成。这些组容器拥有相同的网络命名空间、IP以及存储配额，也可以根据实际情况对每一个 `Pod` 进行端口映射。此外，`Kubernetes` 工作节点会由主系统进行管理，节点包含了能够运行 Docker 容器所用到的服务



















```shell
mkdir images
vi Dockerfile
	FROM busybox:latest       											# 基础镜像
	MAINTAINER Romin Irani (email@domain.com)       # 镜像作者
	# CMD ["executable","param1","param2"]
	# 	CMD ["date"]
	ENTRYPOINT [“/bin/cat”]   # 使用 CMD 提供的参数值
	CMD [“/etc/passwd”]
docker build -t myimage:latest .
docker images
docker run -it myimage [/bin/sh]
```

```java
mkdir images/nginx
vi Dockerfile
	FROM ubuntu
	MAINTAINER Romin Irani (email@domain.com)
	RUN apt-get update
	RUN apt-get install -y nginx
	ENTRYPOINT [“/usr/sbin/nginx”,”-g”,”daemon off;”]
	EXPOSE 80
docker build -t myubuntu:latest .
docker run -d -p 80:80 --name webserver myubuntu
http://localhost:80
```

