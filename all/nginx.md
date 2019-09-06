# Nginx

## 简介

* 免费、开源、高性能的HTTP服务器、反向代理服务器
  * 使用可扩展的异步事件驱动的方法来处理高并发请求
  * 高度模块化设计、反向代理、负载均衡、HTTP缓存

* 正向代理代理的是客户端
  * 用户A向代理服务器Z发送一个请求并指定目标服务器B，然后代理服务器Z向服务器B转交请求并将获得的内容返回给客户端
  * 正向代理允许客户端通过它访问任意网站并且隐藏客户端自身
* 反向代理代理的是服务端
  * 用户A向代理服务器Z发送请求，服务器Z判断应向何处转发请求并将获得的请求的响应信息返回给用户A，而用户A并不知道自己访问的具体服务器
* Apache、Tomcat、Nginx
  * 都是Http服务器（jsp、html、css、png…），本质都是应用程序，放到服务器上，通过绑定ip和端口来监听Http请求
  * Apache、Nginx是静态web服务器，只能返回静态资源，不能动态解析，够将某一个文本文件的内容通过HTTP协议返回到客户端，但是这些文本文件的内容是固定的
  * tomcat、jetty是jsp/servlet动态服务器，可以做动态解析
    * Tomcat能够动态的生成资源并返回到客户端，不同时间、不同客户端所得到的内容是不同的 
    * Tomcat运行在JVM之上，它和HTTP服务器一样，绑定IP地址并监听TCP端口，同时还包含以下
      职责：
      - 管理Servlet程序的⽣命周期
      - 将URL映射到指定的Servlet进行处理
      - 与Servlet程序合作处理HTTP请求——根据HTTP请求⽣成HttpServletResponse对象并传递给Servlet进⾏行处理，将Servlet中的HttpServletResponse对象⽣成的内容返回给浏览器

## 进程模型

* 多进程的方式工作
  * nginx在启动后，会有一个master进程和多个worker进程
  * 启动nginx的主进程将充当监控进程，而由主进程fork()出来的子进程则充当工作进程
  * nginx也可以单进程模型执行，在这种进程模型下，主进程就是工作进程，没有监控进程
* master进程
  * 读取解析配置、维护worker进程
  * 接收来自外界的信号，向各worker进程发送信号，监控worker进程的运行状态，当worker进程退出后(异常情况下)，会自动重新启动新的worker进程
* worker进程
  * 只需要与master进程通信，基本的网络事件，则是放在worker进程中来处理了
  * 多个worker进程之间是对等的，他们同等竞争来自客户端的请求，各进程互相之间是独立的
  * 一个请求，只可能在一个worker进程中处理，一个worker进程，不可能处理其它进程的请求
  * worker进程的个数是可以设置的，一般会设置与机器cpu核数一致
* 如何处理事件
  * nginx采用多worker的方式来处理请求，每个worker对应一个进程
  * 使用可扩展的异步事件驱动的方法来处理高并发请求
    - 一些事件发生源（网卡、磁盘）来产生事件，由事件模块来收集、分发事件，然后许多模块会注册自己感兴趣的事件，在有事件产生时，事件模块会把事件分发到相应的模块中进行处理
    - Nginx不会使用进程作为事件消费者，事件消费者只能是某个模块，只有事件收集器、分发器才有资格占用进程资源，它们会在分发某个事件时调用事件消费模块使用当前占用的进程资源
      - 事件消费者只是被事件分发者进程短期调用而已
      - 如产生"请求建立TCP连接事件"、"TCP连接可读事件"、"TCP连接关闭事件"、"TCP连接可写事件"、"异步读磁盘成功事件"，在事件收集、分发者进程的一次处理过程中，这5个事件按照顺序被收集后，将开始使用当前进程分发事件，从而调用相应的事件消费者模块来处理事件，分发、调用也是有序的
      - 这种设计模式使得网络性能、用户感知的请求时延都得到了提升，每个用户的请求所产生的事件会及时响应，整个服务器的网络吞吐量都会由于事件的及时响应而增大，但是每个消费者不能有阻塞行为
* 好处
  * 对于每个worker进程来说，独立的进程，不需要加锁，省掉了锁带来的开销以及上下文切换的消耗，每个请求占用的内存也很少，更多的并发数只会占用更多的内存而已
  * 独立的进程之间互不干扰，一个进程退出后，其他进程可正常工作，而且master进程则很快启动新的worker进程
* worker_processes 1; 工作进程数，设置成cpu 总核心数
  * 更多的worker数，只会导致进程来竞争cpu资源了，从而带来不必要的上下文切换，将某一个进程绑定在某一个核上，这样就不会因为进程的切换带来cache的失效
* 最大并发数 = worker_processes * worker_connections

## 使用

* 下载（1.15.2）、解压、安装`./configure --prefix=/data/program/nginx `、`make && make install`
* `/sbin目录下：`启动： `./nginx`、停止：`./nginx -s stop`、重新加载：`./nginx -s reload`
  * 指定文件启动`-c`：`[root@CentOS sbin]# ./nginx -c /dyj/nginx/nginx-1.15.2/nginx/conf/nginx.conf`
* 安装目录：`conf`、`html`、`logs`、`sbin`
* 验证：`ps -ef|grep nginx`

## 配置

* main

  ```shell
  worker_processes  2; #进程数
  worker_cpu_affinity 01 10; #cpu数
  error_log /var/log/nginx/error.log warn;
  worker_rlimit_nofile 10240; # too many open files
  ```

* event

  ```shell
  events {
      use epoll;  #select、pool、epoll、kqueue
      worker_connections  10240;
      accept_mutex off;  # 打开后，只有一个worker处理请求
  }
  ```

* http

  * 作为web服务器的相关配置

  ```shell
  http {
  	  include       mime.types;					# 媒体文件
      default_type  application/octet-stream;
      charset utf-8;                              # 字符类型
      access_log off;								# 日志
      sendfile        on;
      keepalive_timeout  65;
      gzip on; 									#压缩
      gzip_min_length 5k;
      gzip_buffers 4 16k;
      gzip_comp_level 8;
      gzip_types text/css application/xml text/javascript image/jpeg image/png image/gif;
      gzip_vary on;
      proxy_temp_path /dyj/nginx/program;
      proxy_cache_path /dyj/nginx/program/proxy_cache levels=1:2 keys_zone=mic:200m max_size=1g;
      include /etc/nginx/conf.d/*.conf;			# server配置文件目录
      underscores_in_headers on;					  # 允许变量带有_
  }
  ```

### 虚拟主机配置

* 在一台主机上通过虚拟的路由配置，让外部感觉是多台服务器

  * 基于ip的虚拟主机

  * 基于端口号的虚拟主机

  * 基于域名的虚拟主机

    * 子域名、二级域名

    ```shell
    server {
       listen       80;
       server_name  bbs.gupaoedu.com;     //子域名
       location / {
    	  root html;
          index  bbs.html;
       }
    }
    ```

* location

  * 实现uri到文件系统路径的映射

  * 配置语法

    * location [= | ~* | ^~ ] /uri/ {...}

  * 配置规则

    * location = /uri 精准匹配
    * location ^~ /uri 前缀匹配
    * location ~ /uri
    * location / 通用匹配

  * 规则的优先级

    1. 精准匹配是优先级最高
    2. 普通匹配(最长的匹配)
    3. 正则匹配

  * 实际使用建议

    ```shell
    location =/ {
    }
    location / {
    }
    # 动静分离
    location ~* \.(gif|....)${
    }
    ```

* 日志配置

  ```shell
  # 通过access_log进行日志记录 
  # nginx中有两条是配置日志的：一条是log_format 来设置日志格式 ； 另外一条是access_log 
  1. access_log  格式
  2. error_log  logs/error.log  notice;
     logo声明    路径及文件名      日志标识
  ```

```shell
#安装目录/data/program/nginx/conf
主配置文件中包含其他配置文件，例如extra/proxydemo.conf
http{
	...
	#请求内容最大容量
	client_max_body_size 100m;
	#/data/program/nginx/conf/extra
	include extra/*.conf;
}

#辅配置文件
#/data/program/nginx/conf/extra/proxydemo.conf
server{
    listen 80;
    server_name localhost;
    location /s {
    	#访问10.211.55.6:80/s会转发到10.211.55.7:8080/s
        proxy_pass http://10.211.55.7:8080;
        #nginx将客户端请求中相关信息传给应用服务器tomcat
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        #获取中间代理路径
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

 String remoteAddr = request.getRemoteAddr(); # 代理地址
 String ngip = request.getHeader("X-Real_IP");# 真实地址
```

## 模块

* 核心模块

* 标准模块 http模块

  * ngx_http_core_module

    ```shell
    server{
    	listen port
    	server_name
    	root ...
    }
    error_page   500 502 503 504  /50x.html;
    	location = /50x.html {
    	root   html;
    }
    ```

  * ngx_http_access_module

    * 实现基于ip的访问控制功能
      * `allow address | CIDR | unix: | all;`
      * `deny address | CIDR | unix: | all;`
    * 自上而下检查，一旦匹配，将生效，条件严格的置前

    ```shell
    location / {
        #禁止所有访问
        deny all;
        root /html;
        index index.html index.htm
    }
    ```

  * ngx_http_rewrite_module

    * rewrite作用：⽀持url重写、⽀持if判断、return，但不支持else

    * rewrite功能就是，使用nginx提供的全局变量或⾃己设置的变量，结合正则表达式和标志位实现 url重写以及重定向

    * rewrite作用范围: 只能放在`server{}`,`location{}`,`if{}`中，并且只能对域名后边的除去传递的参数外的字符串串起作⽤

    * 与location的区别

      * rewrite是在同⼀域名内更改获取资源的路径，而location是对一类路径做控制访问或反向代理，可以proxy_pass到其他机器

    * rewrite也会写在location里，执行顺序

      1. 执行server块的rewrite指令
      2. 执行location匹配
      3. 执行选定的location中的rewrite指令

      * 如果其中某步URI被重写，则重新循环执行1-3，直到找到真实存在的文件;循环超过10次，则返回500 Internal Server Error错误

* 第三方模块

  * 原来所安装的配置，必在重新安装新模块的时候，不能直接make install

  * http_stub_status_module

    * 获取状态、连接数

      ```shell
      location /status {
      	stub_status;
      }
      ```

  * http_random_index_module

    * 随机显示主页

      ```shell
      location / {
          root   html;
          random_index on;
          index  index.html index.htm;
      }
      ```

## 反向代理

* `location{}`中配置`proxy_pass`

  * 访问`server_name:listen/location`会跳转到`proxy_pass/location`

* 默认自带`proxy_pass`指令，只需要修改配置文件就可以实现反向代理

* `proxy_pass` 既可以是ip地址，也可以是域名，同时还可以指定端口

* nginx不能获取带_的参数，如`inteface_version`，如要获取，需设置允许：在`http{}`中设置`underscores_in_headers  on` 

  ```shell
  server{
      listen 80;
      server_name localhost;
      location /s {
      	#访问10.211.55.6:80/s会转发到10.211.55.7:8080/s
          proxy_pass http://10.211.55.7:8080;
          #nginx将客户端请求中相关信息传给应用服务器tomcat
          proxy_set_header Host $host;
          proxy_set_header X-Real-IP $remote_addr;
          #获取中间代理路径
          proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      }
  }
  ```

## 负载均衡

* 利用一定的分配策略，大量并发访问或数据流量分担到多个单元上分别处理

* 语法：` server address [paramters]`

* 负载均衡算法

  * 轮询算法(默认)

    - 每个请求按时间顺序逐一分配到不同的后端服务，如果后端服务器宕机以后，会自动踢出

  * ip_hash 

    - 根据请求的ip地址进行hash值来做转发

    - 每个请求按访问IP的哈希结果分配，使来自同一个IP的访客固定访问一台后端服务器，并且可以有效解决动态网页存在的session共享问题

      ```shell
      upstream bakend {  
      	ip_hash;  
      	server 192.168.0.14:88;  
      	server 192.168.0.15:80;  
      } 
      ```

  * 权重轮询 
    - weight的值越大分配到的访问概率越高，主要用于后端每台服务器性能不均衡的情况下
    - `server 192.169.11.159:8080 weight=1;`
  * fair 
    - 根据服务器的响应时间来分配请求
    - 可以根据页面大小和加载时间长短智能地进行负载均衡，也就是根据后端服务器的响应时间来分配请求，响应时间短的优先分配。
    - Nginx本身不支持fair，如果需要这种调度算法，则必须安装`upstream_fair`模块
  * url_hash
    - 按访问的URL的哈希结果来分配请求，使每个URL定向到一台后端服务器，可以进一步提高后端缓存服务器的效率
    - Nginx本身不支持`url_hash`，如果需要这种调度算法，则必须安装Nginx的hash软件包

  ```shell
  #负载均衡
  upstream tomcat {
  	## 如果请求尝试超过2次，60s内不再重新请求
  	server 192.168.11.159:8080 max_fails=2 fail_timeout=60s;
  	server 192.168.11.161:8080 max_fails=2 fail_timeout=60s;
  }
  
  server {
  	listen 80;
  	server_name localhost;
  	location / {
          #负载均衡
        proxy_pass http://tomcat;
  
        #proxy_set_header Host $proxy_host 
        #客户端访问地址10.211.55.6
     		# key value
        proxy_set_header Host $host;    
        # 10.211.55.2
        proxy_set_header X-Real-IP $remote_addr;
        # 10.211.55.2
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  	}
  }
  
  <h1><%=request.getHeader("X-Forwarded-For") %></h1>  #10.211.55.2
  <h1><%=request.getHeader("X-Real-IP") %></h1>		 #10.211.55.2
  <h1><%=request.getHeader("Host") %></h1>			 #10.211.55.6
  ```

* proxy_next_upstream

  * 这个配置表示当向一台上有服务器转发请求出现错误的时候，继续换一台服务器来处理这个请求。

  * 语法：proxy_next_upstream [error | timeout | invalid_header | http_500 | http_502 | http_503 | http_504 |http_404 | off ];

  * 默认：proxy_next_upstream error timeout;

  * 配置块：http、server、location

    ```shell
    upstream example_upstream{
      server 192.168.0.1 max_fails=1 fail_timeout=30s;
      server 192.168.0.2 max_fails=1 fail_timeout=30s backup;
    }
    location /example/ {
       proxy_pass http://example_upstream/;
       proxy_set_header Host: test.example.com;
       proxy_set_head X-real-ip $remote_addr;
       # 这个服务的访问，出现了500或者超时的情况，会自动重试到下一个服务器去
       proxy_next_upstream error timeout http_500;
       #  proxy_next_upstream error timeout http_500 non_idemponent; 
       # 像 post, lock, patch 这种会对服务器造成不幂等的方法，默认是不进行重试的，如果一定要进行重试，则要加上这个配置：non_idemponent
    }
    ```

## 跨域访问

* 原理

  *  同源策略是浏览器的安全策略，不是`HTTP`协议的一部分
  * 服务器端调用HTTP接口只是使用HTTP协议，不会执行JS脚本，不需要同源策略，也就不存在跨域问题
  * 通过nginx配置一个代理服务器做跳板机，反向代理访问服务接口

* 在`location{}`中设置`add_header`值

  ```shell
  add_header 'Access-Control-Allow-Origin' '*'; // #允许来自所有的访问地址
  add_header 'Access-Control-Allow-Methods' 'GET,PUT,POST,DELETE,OPTIONS'; //#支持的请求方式
  add_header 'Access-Control-Allow-Header' 'Content-Type,*'; //#支持的媒体类型
  ```

## 动静分离

* 必须依赖服务器生存的称为动，不需要依赖容器的比如css/js或者图片等，这类就叫静

* 在Nginx的conf目录下，有一个mime.types文件

  * 用户访问一个网站，然后从服务器端获取相应的资源通过浏览器进行解析渲染最后展示给用户，而服务端可以返回各种类型的内容，比如xml、jpg、png、gif、flash、MP4、html、css等等，那么浏览器就是根据mime-type来决定用什么形式来展示的
  * 服务器返回的资源给到浏览器时，会把媒体类型告知浏览器，这个告知的标识就是Content-Type，比如`Content-Type:text/html`

  ```shell
  location ~ .*\.(js|css|png|svg|ico|jpg)$ {
  	# 防盗链
  	valid_referers none blocked 192.168.11.160 www.gupaoedu.com;
  	if ($invalid_referer) {
  		return 404; 
  	}
  	# 静态资源存放目录，在html同级目录下创建static-resource目录
  	# 把应用的这些类型js|css|png|svg|ico|jpg文件放到该目录
  	# 删除应用的这些类型文件
  	root static-resource;
  	# 缓存
  	expires 1d; 
  }
  ```

  ```shell
  server {
  	listen       80;
  	server_name  localhost;
  	location / {
  		root   html;
  		index  index.html index.htm;
  	}
  	#localhost:80/image/时会访问本机的/usr/local/myImage/image/ 目录
  	#localhost:80/image/1.jpg
  	location /image/ {
  		root   /usr/local/myImage/;
  		autoindex on;
  	}
  }
  ```

  ```shell
  #静态文件服务器
  upstream static {
   server 192.168.11.159:8080;
  }
  
  server {
   listen 80;
   server_name localhost;
   location ~ .*\.(js|css|png|svg|ico|jpg)$ {
       root static-resource;
       #静态资源服务器
       proxy_pass http:static; 
   }
  }
  ```

* Nginx本身就是一个高性能的静态web服务器
* 静态文件基本上变化不大，所以动静分离以后可以对静态文件进行缓存、或者压缩提高网站性能

### 缓存

* 当一个客户端请求web服务器, 请求的内容可以从以下几个地方获取：浏览器缓存中、缓存服务器、服务器中

* Nginx可以通过expires设置缓存，比如可针对图片做缓存，因为图片这类信息基本上不会改变

* `content-encoding:gzip`

* `在location中设置expires`

* 格式`expires 30s|m|h|d`

  ```shell
  location ~ .*.(jpg|jpeg|gif|bmp|png|js|css|ico)$ { 
  	root static; 
  	expires 1d;
  } 
  ```

### 压缩

* 静态文件，比如图片、脚本、样式等等，可能本身会比较大，网络传输的时候就会比较慢，从而导致网站的渲染速度

* Nginx中提供了一种`Gzip`的压缩优化手段，可以对后端的文件进行压缩传输，能够降低文件的大小来提高传输效率

* 在`http`模块中配置

  ```shell
  gzip  on;
  gzip_min_length 5k;  #小于多少就不做压缩
  gzip_comp_level 3;   #压缩级别， 级别越高，压缩越小，但是会占用CPU资源 
  # 对哪些类型的文件做压缩 (conf/mime.conf)
  gzip_types application/javascript image/jpeg image/svg+xml;
  # 设置gzip申请内存的大小，作用是按指定大小的倍数申请内存空间。
  gzip_buffers 4 32k; # 4 16k代表按照原始数据 大小以16k为单位的4倍申请内存。
  gzip_vary on;       # 是否传输gzip压缩标识;避免浪费不支持的也压缩，所以根据客户端的HTTP头来判断，是否需要压缩
  ```

## 防盗链

* 图片不希望被第三方调用造成服务器的负载以及消耗比较多的流量问题

* 语法: `valid_referers none | blocked | server_names | string …;`

  * “Referer”请求头为指定值时，内嵌变量$invalid_referer被设置为空字符串，否则这个变量会被置成“1”。
  * 查找匹配时不区分大小写，其中none表示缺少referer请求头、blocked表示请求头存在，但是它的值被防火墙或者代理服务器删除、server_names表示==`referer`==请求头包含指定的虚拟主机名

  ```shell
  llocation ~ .*.(gif|jpg|ico|png|css|svg|js)$ { 
  	# 所有来自至gupao.com 192.168.11.153和域名中包含google的站点都可以访问到当前站点的图片
  	# 如果来源域名不在这个列表中，那么$invalid_referer为1
  	valid_referers none blocked *.gupao.com 192.168.11.153 server_names ~\.google\.;
  	if ($invalid_referer) { 
  		return 404;
  	}
  	root static;
  }
  ```

## 配置https的请求

* 创建服务器私钥
* 创建签名请求的证书csr（核心内容是一个公钥）
* 去除使用私钥是的口令验证
* 标记证书使⽤私钥和csr
* tomcat增加对https的支持
  * Connector 8080节点加入 redirectPort="443" proxyPort="443"
  * redirectPort :当http请求有安全约束才会转到443端⼝使用ssl传输

# 高可用方案

* Nginx 作为反向代理服务器，所有的流量都会经过 Nginx

## keepalived

* 是集群管理中保证集群高可用的一个服务软件，用来防止单点故障
  * 起初，专门监控集群系统中各服务节点的状态
    * 如果某个服务节点出现异常，Keepalived将检测到并踢出该节点，当节点被修复后会自动加入服务器集群中
  * 后来实现了`VRRP`协议的功能，基于VRRP协议来实现的LVS服务高可用方案，可以利用其来避免单点故障
    * VRRP，虚拟路由冗余协议，就是把两台或多态路由器设备虚拟成一个设备，实现主备高可用
    * 一个LVS服务会有多台服务器运行Keepalived，一台为主服务器（MASTER）(只有主服务器提供服务)，另外为备份服务器（BACKUP），但是对外表现为一个虚拟IP，主服务器会发送特定的消息给备份服务器，当备份服务器收不到这个消息的时候，即主服务器宕机的时候， 备份服务器就会接管虚拟IP，继续提供服务，从而保证了高可用性
* Keepalived是实现前端高可用，常用的前端高可用的组合有，LVS+Keepalived、Nginx+Keepalived、HAproxy+Keepalived
* Keepalived 软件起初是专为 LVS 负载均衡软件设计的，用来管理并监控 LVS 集群系统中各个服务节点的状态，后来又加入了可以实现高可用的 VRRP 功能。因此，Keepalived 除了能够管理 LVS 软件外，还可以作为其他服务(例如:Nginx、Haproxy、MySQL 等)的高可用解决方案软件
* LVS 是 Linux Virtual Server 的缩写，也就是 Linux 虚拟服务器，它是工作在四层的负载均衡，类似于 Haproxy, 主要用于实现对服务器集群的负载均衡
* 关于四层负载，osi 网络层次模型的 7 层模型(应用层、表示层、会话层、传输层、网络层、数据链路层、物理层)；四层负载就是基于传输层，也就是ip+端口的负载；而七层负载就是需要基于 URL 等应用层的信息来做负载，同时还有二层负载(基于 MAC)、三层负载(IP)
  * 常见的四层负载有:LVS、F5; 
  * 七层负载有:Nginx、HAproxy; 在软件层面，Nginx/LVS/HAProxy 是使用得比较广泛的三种负载均衡软件
* 对于中小型的 Web 应用，可以使用 Nginx、大型网站或者重要的服务并且服务比较多的时候，可以考虑使用 LVS

* 应用（nginx+keepalived）
  * 安装nginx、keepalived
  * keepalived配置：
    * 主从配置文件都配置：
      * 服务器唯一标识
      * 设置lvs的状态，MASTER和BACKUP两种，必须大写 
      * master 的优先级必须大于 backup
      * 虚拟服务器和真实服务器
  * `service keepalived start`

# 网关

## OpenResty

* 通过 Lua 扩展 Nginx 实现的可伸缩的 Web 平台，内部集成了大量精良的 Lua 库、第三方模块以及大多数的依赖项。
  * 用于方便地搭建能够处理超高并发、扩展性极高的动态 Web 应用、Web 服务和动态网关
* Openresty 会把每个请求分为不同阶段，在这些阶段中挂载 lua 执行脚本实现自定义行为扩展
  * 如身份认证、权限鉴定、灰度发布、分流、日志记录等，可以避免重复校验等
  * 在请求真正到达上游服务之前，Lua 可以随心所欲的做复杂的访问控制和安全检测
  * 随心所欲的操控响应头里面的信息

```shell
# 引入lua库
# 可以连接redis、数据库
# 可根据数据做逻辑判断
```

