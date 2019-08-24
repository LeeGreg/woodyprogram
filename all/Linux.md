# Readme

```shell
#Linux是区分大小写的
#一般来说，后面跟的选项如果单字符选项前使用一个减号-。单词选项前使用两个减号--
# 文件类型
- 普通文件-
- 目录d
- 符号链接 l
```



![image-20190809171118202](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809171118202.png)

## 常用

```shell
# 查看文件
less
b向上翻一页
space向下翻一页
shift + g 最后一页
# 查看⽂件的尾部的10⾏
tail -10 
# 查看文件的头部20⾏
head -20
# netstat 工具检测开放端口
netstat -anlp | grep 3306
# lsof 工具检测开放端口
lsof -i:3306
# 使用telnet检测端口是否开放
telnet ip port

# 把内容重定向到指定的文件中 ，有则打开，无则创建
echo
# 将前⾯的结果给后面的命令
管道命令 |
# 重定向 	> 是覆盖模式，	>> 是追加模式 
# 例如: echo "Java3y,zhen de hen xihuan ni" > qingshu.txt 把左边的输出放到右边的⽂件⾥去

# 查看当前⽂件⼤小
ls -lht
du -sh*
# 查看目录详细信息
ls -lh
ll
# 执行sql脚本
在MySQL下执行： source /var/ftp/pub/sogoodsoft.sql;

# 解压
tar -zxvf apache-tomcat-8.5.35.tar.gz  					 # 当前目录
tar zxvf  redis-5.0.2.tar.gz -C /data/program/redis/	 # 指定目录 
# 压缩
tar -czvf filename

# 创建多级目录
mkdir -p data/program/keepalived
#查看目录路径
pwd
# 文本中查找
?6379
/
# 在vi中按u可以撤销一次操作
# “u”两次，文本恢复原样
# 恢复上一步被撤销的操作
Ctrl+r 
# 文本中删除(查看模式)
dd

# 查找进程号
ps -ef | grep tomcat
#干掉进程
kill -9 进程号
# 登陆
ssh -p 22 appuser@192.168.3.123
# scp (-P指定端口)
scp /本地/.. root@192.168.3.123:/... # 本地上传给服务器
scp root@192.168.3.123:/... /本地/.. # 服务器传给本地
#从服务器下载整个目录
scp -r username@servername:/var/www/remote_dir/（远程目录） /var/www/local_dir（本地目录）
#scp -r root@192.168.0.101:/var/www/test  /var/www/  
#上传目录到服务器
scp  -r local_dir username@servername:remote_dir
# scp -r test  root@192.168.0.101:/var/www/   把当前目录下的test目录上传到服务器的/var/www/ 目录
#服务器之间传输文件
scp root@其他服务器IP:/目录/文件	/本服务器目录/文件名
scp root@10.211.55.6:/dyj/tomcat/apache-tomcat-8.5.32.tar.gz apache-tomcat-8.5.32.tar.gz
# 查看动态日志
tail -f ../logs/catalina.out 
tail -f logs/*/all.log
# 访问网址
curl http://www.baidu.com
# 递归删除
rm -rf *
# 递归复制
cp -r /文件 /目标地址
cp -r /../../.* /../..

uname -a
man 2 syscalls
man 2 acct
ls -lrt
which ls
# 查看当前shell的版本
echo $SHELL
# 查看支持的shell的版本
cat /etc/shells
# 查看某指令的用法
man 指令
# 查看端口是否在使用
netstat -nap | grep 5672
# etc 存放系统配置⽂文件
# var ⽤于存放运行时需要改变数据的⽂件
# usr 用于存放系统应用程序，比较重要的⽬录/usr/local 本地管理员软件安装目录
# 查询端口是不是开放的
firewall-cmd --permanent --query-port=8080/tcp
# 添加对外开放端口
firewall-cmd --permanent --add-port=8080/tcp
# 重启防火墙
firewall-cmd --reload
# 停止firewall
systemctl stop firewalld.service 
# 禁止firewall开机启动
systemctl disable firewalld.service 
# 查看程序状态
systemctl status sshd
# 与网络时间同步
ntpdate asia.pool.ntp.org
# 指定url下载文件
wget http://www.linuxde.net/testfile.zip
#连接sftp
sftp  -P 22060 efast@139.224.22.150
#从服务器下载文件
get /home/efast/project/cloudtalk/apache-tomcat-community-cloud/bin/restat.sh /Users/dingyuanjie/Downloads

shutdown
        -r             关机重启
        -h             关机不重启
        now          立刻关机
    halt               关机
    reboot          重启
```

```shell
# 如果提示符的最后一个字符是“#”, 而不是“$”, 那么这个终端会话就有超级用户权限
# 简单命令
# 显示系统当前时间和日期
[me@linuxbox ~]$ date
# 默认显示当前月份的日历
[me@linuxbox ~]$ cal
# 查看磁盘剩余空间的数量，输入 df:
[me@linuxbox ~]$ df
# 显示空闲内存的数量，输入命令 free
[me@linuxbox ~]$ free

# 符号 "." 指的是工作目录，".." 指的是工作目录的父目录
# 以 "." 字符开头的文件名是隐藏文件。这仅表示，ls 命令不能列出它们， 用 ls -a 命令就可以了
# 文件名和命令名是大小写敏感的
# Linux 没有“文件扩展名”的概念
# 得到更多的细节
[me@linuxbox ~]$ ls -l
# ls 命令有两个选项， “l” 选项产生长格式输出，“t”选项按文件修改时间的先后来排序
[me@linuxbox ~]$ ls -lt
# 加上长选项 “--reverse”，则结果会以相反的顺序输出：
[me@linuxbox ~]$ ls -lt --reverse
# -a	列出所有文件，甚至包括文件名以圆点开头的默认会被隐藏的隐藏文件
# -r	以相反的顺序来显示结果。通常，ls 命令的输出结果按照字母升序排列
# -S	命令输出结果按照文件大小来排序。
# -t	按照修改时间来排序。

# 当调用 file 命令后，file 命令会打印出文件内容的简单描述
[me@linuxbox ~]$ file picture.jpg

# mkdir - 创建目录
mkdir dir1 dir2 dir3	# 会创建三个目录，名为 dir1, dir2, dir3。

# echo － 显示一行文本
# 在标准输出中打印出它的文本参数，传递到 echo 命令的任一个参数都会在（屏幕上）显示出来
[me@linuxbox ~]$ echo this is a test
# 表达式
[me@linuxbox ~]$ echo $(((5**2) * 3))  #75
[me@linuxbox ~]$ echo Number_{1..5}
Number_1  Number_2  Number_3  Number_4  Number_5

# 用别名（alias）创建自己的命令
# 查明是否"foo"命令名已经存在系统中
[me@linuxbox ~]$ type foo
[me@linuxbox ~]$ alias foo='cd /usr; ls; cd -'
# 删除别名，使用 unalias 命令
[me@linuxbox ~]$ unalias foo
# type 会显示命令的类别
[me@linuxbox ~]$ type ls
# which － 显示一个可执行程序的位置
[me@linuxbox ~]$ which ls
# help － 得到 shell 内部命令的帮助文档
[me@linuxbox ~]$ help cd
# --help - 显示用法信息
[me@linuxbox ~]$ mkdir --help
# man － 显示程序手册页
[me@linuxbox ~]$ man ls
# apropos － 显示适当的命令
[me@linuxbox ~]$ apropos floppy
# info － 显示程序 Info 条目
[me@linuxbox ~]$ info coreutils
# 查看目录大小 du -h /home带有单位显示目录信息
du                 
#查看磁盘大小 df -h 带有单位显示磁盘信息
df     
#查看网络情况
ifconfig    
#测试网络连通
ping        
#显示网络状态信息
netstat       
#命令不会用了，找男人  如：man ls
man      
#清屏
clear              
#对命令重命名 如：alias showmeit="ps -aux" ，另外解除使用unaliax showmeit
alias              
# 杀死进程，可以先用ps 或 top命令查看进程的id，然后再用kill命令杀死进程
kill 
```

## 压缩

```shell
gzip
bzip2
tar:                打包压缩
     -c              归档文件
     -x              压缩文件
     -z              gzip压缩文件
   -j              bzip2压缩文件
     -v              显示压缩或解压缩过程 v(view)
     -f              使用档名
例子：
    tar -cvf /home/abc.tar /home/abc              只打包，不压缩
    tar -zcvf /home/abc.tar.gz /home/abc        打包，并用gzip压缩
    tar -jcvf /home/abc.tar.bz2 /home/abc      打包，并用bzip2压缩
    当然，如果想解压缩，就直接替换上面的命令  tar -cvf  / tar -zcvf  / tar -jcvf 中的“c” 换成“x” 就可以了
    
#将目录里所有jpg文件打包成jpg.tar后，并且将其用gzip压缩，生成一个gzip压缩过的包，命名为jpg.tar.gz
tar -czf jpg.tar.gz *.jpg   

- gzip filename
- bzip2 filename
- tar -czvf filename

#解压
tar -zxvf text.tar.gz

- gzip -d filename.gz
- bzip2 -d filename.bz2
- tar -zxvf filename.tar.gz
```



## 移动光标

```shell
# Ctrl-a			移动光标到行首。
# Ctrl-e			移动光标到行尾。
# Ctrl-f			光标前移一个字符；和右箭头作用一样。
# Ctrl-b			光标后移一个字符；和左箭头作用一样。
# Alt-f				光标前移一个字。
# Alt-b				光标后移一个字。
# Ctrl-l			清空屏幕，移动光标到左上角。clear 命令完成同样的工作。

[me@linuxbox ~]$ history | less
# !number 重复历史列表中第 number 行的命令
```

## 权限

```shell
# 用 id 命令，来找到关于自己身份的信息
[me@linuxbox ~]$ id
# 
[me@linuxbox ~]$ > foo.txt
[me@linuxbox ~]$ ls -l foo.txt
-rw-rw-r-- 1 me   me   0 2008-03-06 14:52 foo.txt
# 列表的前十个字符是文件的属性
# -		一个普通文件
# d		一个目录
# l		一个符号链接
# c		一个字符设备文件
# b		一个块设备文件
# 剩下的九个字符，叫做文件模式，代表着文件所有者，文件组所有者，和其他人的读，写，执行权限

# chmod 更改文件或目录的模式（权限）
# 二进制 7 (rwx)，6 (rw-)，5 (r-x)，4 (r--)，和 0 (---)
[me@linuxbox ~]$ chmod 600 foo.txt
[me@linuxbox ~]$ ls -l foo.txt
-rw------- 1 me    me    0  2008-03-06 14:52 foo.txt
# chmod 命令支持一种符号表示法，来指定文件模式。
# 符号表示法分为三部分：更改会影响谁， 要执行哪个操作，要设置哪种权限。
# 通过字符 “u”，“g”，“o”，和 “a” 的组合来指定 要影响的对象
# u			"user"的简写，意思是文件或目录的所有者。
# g			用户组。
# o			"others"的简写，意思是其他所有的人
# a			"all"的简写，是"u", "g"和“o”三者的联合。
# u+x		为文件所有者添加可执行权限。
# u-x		删除文件所有者的可执行权限
# +x		为文件所有者，用户组，和其他所有人添加可执行权限。 等价于 a+x。
# o-rw		除了文件所有者和用户组，删除其他人的读权限和写权限
# go=rw		给群组的主人和任意文件拥有者的人读写权限。如果群组的主人或全局之前已经有了执行的权限，他们将			被移除。
# u+x,go=rw	给文件拥有者执行权限并给组和其他人读和执行的权限。多种设定可以用逗号分开

# umask － 设置默认权限
# 当创建一个文件时，umask 命令控制着文件的默认权限。
# umask 命令使用八进制表示法来表达从文件模式属性中删除一个位掩码

# 更改身份
# su － 以其他用户身份和组 ID 运行一个 shell
# su 命令用来以另一个用户的身份来启动 shell
su [-[l]] [user]
# 启动超级用户的 shell
[me@linuxbox ~]$ su -
# 当工作完成后， 输入"exit"，则返回到原来的 shell
# 只执行单个命令，而不是启动一个新的可交互的 shell
su -c 'command'

# sudo － 以另一个用户身份执行命令
# sudo 不会重新启动一个 shell，也不会加载另一个 用户的 shell 运行环境
 
# chown 
# 更改文件或目录的所有者和用户组。使用这个命令需要超级用户权限

# passwd 命令，来设置或更改用户密码
passwd [user]
[me@linuxbox ~]$ passwd
(current) UNIX password:
New UNIX password:
```

## 进程

```shell
# 查看进程
[me@linuxbox ~]$ ps x
# STAT 进程当前状态
# R			运行,进程正在运行或准备运行
# S			正在睡眠, 进程没有运行，而是，正在等待一个事件， 比如说，一个按键或者网络数据包
# D			不可中断睡眠。进程正在等待 I/O，比方说，一个磁盘驱动器的 I/O
# T			已停止. 已经指示进程停止运行
# Z			一个死进程或“僵尸”进程。这是一个已经终止的子进程，但是它的父进程还没有清空它
# N			低优先级进程。 一个低优先级进程（一个“好”进程）只有当其它高优先级进程执行之后，才会得到处理器				时间
# 能够显示属于每个用户的进程信息
[me@linuxbox ~]$ ps aux

# 用 top 命令动态查看进程
[me@linuxbox ~]$ top

# 中断一个进程
Ctrl-c

# 把一个进程放置到后台(执行)
# 启动一个程序，让它立即在后台运行，在程序命令之后，加上"&"字符
[me@linuxbox ~]$ xlogo &

# 进程返回到前台
# 使用 fg 命令，让一个进程返回前台执行，fg 命令之后，跟随着一个百分号和工作序号（叫做 jobspec）
[me@linuxbox ~]$ jobs
[1]+ Running        xlogo &
[me@linuxbox ~]$ fg %1
xlogo

#  输入 Ctrl-z，可以停止一个前台进程

# 通过 kill 命令给进程发送信号
kill -9 PID  #杀死

#显示指定文件的详细信息，比ls更详细
stat 
```



## 网络

```shell
# Netstat 命令用于显示各种网络相关信息
-a (all)显示所有选项，默认不显示LISTEN相关
-t (tcp)仅显示tcp相关选项
-u (udp)仅显示udp相关选项
-n 拒绝显示别名，能显示数字的全部转化成数字。
-l 仅列出有在 Listen (监听) 的服務状态
-p 显示建立相关链接的程序名
-r 显示路由信息，路由表
-e 显示扩展信息，例如uid等
-s 按各个协议进行统计
-c 每隔一个固定时间，执行该netstat命令。
提示：LISTEN和LISTENING的状态只有用-a或者-l才能看到
只列出所有监听 tcp 端口 netstat -lt
只列出所有监听 udp 端口 netstat -lu
找出运行在指定端口的进程 netstat -an | grep ':80'
netstat -anop | grep 808 | grep LIST
#查看端口是否监听
netstat  -anp |grep 22
# 查看开放的tcp端口
netstat -ntpl
# 列出所有 tcp 端口 
netstat -at
# 列出所有 udp 端口 
netstat -au
```

## vi

```shell
# vi
# 普通模式
 G 用于直接跳转到文件尾
 n 继续查找下⼀个
 yy 复制⼀⾏
# p				小 p 命令把剪切板中的文本粘贴到光标位置之后
# P				大 P 命令把文本粘贴到光标之前
 dd 删除一行⽂本
 x 删除光标所在的字符
 u 取消上一次编辑操作(undo)
 
 ZZ ⽤于存盘退出Vi
 ZQ ⽤于不存盘退出Vi
 /和? ⽤于查找字符串
 
# 0 (零按键) 	  移动到当前行的行首
# ^			    移动到当前行的第一个非空字符
# $				移动到当前行的末尾
# w				移动到下一个单词或标点符号的开头
# W				移动到下一个单词的开头，忽略标点符号
# b				移动到上一个单词或标点符号的开头
# B				移动到上一个单词的开头，忽略标点符号
# Ctrl-f 		向下翻一页
# Ctrl-b		向上翻一页
# numberG		移动到第 number 行。例如，1G 移动到文件的第一行
# G				移动到文件末尾。

# 按下“u” 按键，当在命令模式下，vi 将会撤销所做的最后一次修改 
 
# 插入模式
在 Normal 模式下输入插入命令 i、 a 、 o 进入insert模式
回到Normal模式下，按 Esc 键即可

:set number 在编辑⽂件时显示行号
:set all 显示所有可以设置的选项

# 命令行模式
:wq ⽤于存盘退出Vi
:q! ⽤于不存盘退出Vi
:q ⽤于直接退出Vi (未做修改)
```

## mv

```shell
# mv 移动和重命名文件
mv item1 item2				# 把文件或目录 “item1” 移动或重命名为 “item2”
mv item... directory		# 把一个或多个条目从一个目录移动到另一个目录中
# -i 		在重写一个已经存在的文件之前，提示用户确认信息。如果不指定这个选项，mv 命令会默认重写文件内容
# -u		当把文件从一个目录移动另一个目录时，只是移动不存在的文件， 或者文件内容新于目标目录相对应文件				的内容
# -v		当操作 mv 命令时，显示翔实的操作信息
mv file1 file2		# 移动 file1 到 file2。如果 file2 存在，它的内容会被 file1 的内容重写。 如果 						file2 不存在，则创建 file2。 每种情况下，file1 不再存在
mv -i file1 file2	# 除了如果file2存在的话，在file2被重写之前，用户会得到提示信息外
mv file1 file2 dir1	# 移动 file1 和 file2 到目录 dir1 中。dir1 必须已经存在
mv dir1 dir2		# 如果目录dir2不存在，创建目录 dir2，并且移动目录dir1的内容到目录dir2中，同时删						除目录 dir1。如果目录 dir2 存在，移动目录 dir1（及它的内容）到目录 dir2
```

## ln

```shell
# 即可创建硬链接，也可以创建符号链接
ln file link		# 创建硬链接，就为文件创建了一个额外的目录条目。
	# 局限性
		# 一个硬链接不能关联它所在文件系统之外的文件，一个链接不能关联与链接本身不在同一个磁盘分区上的文件
		# 一个硬链接不能关联一个目录
ln -s item link		# 创建符号链接，"item" 可以是一个文件或是一个目录
	# 符号链接，建立符号链接的目的是为了克服硬链接的两个缺点
		# 通过创建一个特殊类型的文件，这个文件包含一个关联文件或目录的文本指针
		# 一个符号链接指向一个文件，而且这个符号链接本身与其它的符号链接几乎没有区别
		# 往一个符号链接里面写入东西，那么相关联的文件也被写入
		# 当删除一个符号链接时，只有这个链接被删除，而不是文件自身
		# 如果先于符号链接删除文件，这个链接仍然存在，但是不指向任何东西
```



## cp

```shell
# cp - 复制文件和目录
# 复制单个文件或目录"item1"到文件或目录"item2"
cp item1 item2
cp file1 file2
# 复制多个项目（文件或目录）到一个目录下
cp item... directory
cp file1 file2 dir1
cp dir1/* dir2	
# -a			复制文件和目录，以及它们的属性，包括所有权和权限
# -i			在重写已存在文件之前，提示用户确认。如果这个选项不指定， cp 命令会默认重写文件
				cp -i file1 file2
# -r 			递归地复制目录及目录中的内容
				cp -r dir1 dir2 
				#复制目录 dir1 中的内容到目录 dir2。如果目录 dir2 不存在， 创建目录 dir2，操作完成					后，目录 dir2 中的内容和 dir1 中的一样。 如果目录 dir2 存在，则目录 dir1 (和目录					中的内容)将会被复制到 dir2 中
# -u			当把文件从一个目录复制到另一个目录时，仅复制 目标目录中不存在的文件，或者是文件内容新于					目标目录中已经存在的文件
# -v			显示翔实的命令操作信息
# 从 /etc 目录复制 passwd 文件到当前工作目录下
[me@linuxbox playground]$ cp /etc/passwd .
```



## 如何查找特定的文件

```shell
# locate - 查找文件的简单方法
# 通过名字来查找文件
[me@linuxbox ~]$ locate bin/zip
[me@linuxbox ~]$ locate zip | grep bin

# find - 查找文件的复杂方式
[me@linuxbox ~]$ find ~ -type f -name "\*.JPG" -size +1M | wc -l

# 当前目录下递归查找
find -name "xxx.txt"
# 全局查找
find / -name "xxx.java"
# 忽略大小写
find ~ -iname "xxx*.java"
# 查找目录
find /（查找范围） -name '查找关键字' -type d
# 查找tomcat7文件夹所在的位置
find / -name 'tomcat7' -type d 
# 查找文件：
find /（查找范围） -name 查找关键字 -print
#查找server.xml文件的位置
find / -name 'server.xml' -print
find . -name "*.txt" -print
```

## 检索文件内容

```shell
# grep 查找文件里符合条件的字符串
# 从target*文件中筛选出有moo内容的文件
grep "moo" target*
# | 管道操作符，可将指令连接起来，前一个指令的正确输出作为后一个指令的输入
find ～ | grep "target"
# grep -o 筛选出符合正则表达式
grep 'text' xxx.log | grep -o 'engine\[[0-9a-z]*\]'
# grep -v 过滤
ps -ef | grep tomcat | grep -v "grep"
```

## 对文件内容做统计

```shell
awk 适合处理格式化/表格数据
# 一次读取一行文本，按输入分隔符进行切片，切成多个组成部分
# 将切片直接保存在内建的变量中，$1,$2..($3表示行的全部)
# 支持对单个切片的判断，支持循环判断，默认分隔符为空格
awk '{print $1,$4}' netstat.txt     # 获取每行的第一列和第四列数据并打印出来
awk '$1=="tcp" && $2==1{print $0}' netstat.txt  # 第一列的值为tcp，第二列的值为1的所有行打印出来
# 打印第一行（表头，NR是将空格将列分开）
awk '($1=="tcp" && $2==1) || NR==1 {print $0}' netstat.txt 
# 按照指定分隔符进行分隔
awk -F "," '{print $2}' test.txt
# 统计
grep 'text' xxx.log | grep -o 'engine\[[0-9a-z]*\]' | awk '{enginearr[$1]++}END{for(i in enginearr)print i "\t" enginearr[i]}'
```

## 批量替换文本的内容

```shell
sed # 流编译器，适合用于对文本的行内容进行处理
sed -i 's/^Str/String/' test.txt # s字符串、^Str以Str开头的被替换的内容 String替换内容
sed -i 's/\.$/\;/' test.txt  # 每行结尾的.替换成;
sed -i 's/Jack/me/g' test.txt # g表示全部都替换，否则只替换第一个符合条件的
sed -i '/^ *$/d' test.txt  # d删除行   删除空行
sed -i '/Integer/d' test.txt # 删除 Integer开头的行
```

## 系统目录

```shell
# /usr			包含普通用户所需要的所有程序和文件。
# /dev			包含设备结点的特殊目录,内核维护着它支持的设备。
# /etc			包含所有系统层面的配置文件，也包含一系列的 shell 脚本，在系统启动时，这些脚本会运行每个系统服务。这个目录中的任何文件应该是可读的文本文件
				/etc/crontab， 定义自动运行的任务
				/etc/fstab，包含存储设备的列表，以及与他们相关的挂载点。
				/etc/passwd，包含用户帐号列表
# /opt			被用来安装“可选的”软件。这个主要用来存储可能 安装在系统中的商业软件产品
# /var			是可能需要改动的文件存储的地方。各种数据库，假脱机文件， 用户邮件等等，都驻扎在这里
```

## 通配符

```shell
# Data???					以“Data”开头，其后紧接着3个字符的文件
# [abc]*					文件名以"a","b",或"c"开头的文件
# BACKUP.[0-9][0-9][0-9]	以"BACKUP."开头，并紧接着3个数字的文件
# [[:upper:]]*				以大写字母开头的文件
# [![:digit:]]*				不以数字开头的文件
# *[[:lower:]123]			文件名以小写字母结尾，或以 “1”，“2”，或 “3” 结尾的文件
```



## cat、less、more

* cat
  * 一次显示整个文件:cat filename
  * 从键盘创建一个文件:cat > filename 只能创建新文件,不能编辑已有文件
  * 将几个文件合并为一个文件:cat file1 file2 > file
  
* more
  * Enter 向下n行，需要定义。默认为1行
  * 空格键 向下滚动一屏
  * Ctrl+B 返回上一屏
  * = 输出当前行的行号
  * :f 输出文件名和当前行的行号
  * q 退出more
  
* less
  
  ```shell
  # less 命令是一个用来浏览文本文件的程序
  [me@linuxbox ~]$ less /etc/passwd
  # q				退出 less 程序
  # b				向上翻滚一页
  # space			向下翻滚一页
  # G				移动到最后一行
  # g				移动到开头一行
  # /charaters	向前查找指定的字符串
  # n				向前查找下一个出现的字符串，这个字符串是之前所指定查找的
  # h				显示帮助屏幕
  - -N 显示每行的行号
  - 空格键 滚动一页
  - 回车键 滚动一行
  - b 向后翻一页
  - d 向后翻半页
  - u 向前滚动半页
  - /字符串：向下搜索“字符串”的功能
  - ?字符串：向上搜索“字符串”的功能
  - n：重复前一个搜索（与 / 或 ? 有关）
  - N：反向重复前一个搜索（与 / 或 ? 有关）
  ```

## 重定向

```shell
# cat － 连接文件
	# 读取一个或多个文件，然后复制它们到标准输出 cat [file]
# sort － 排序文本行
# uniq － 报道或省略重复行
# grep － 打印匹配行
# wc － 打印文件中换行符，字，和字节个数
# head － 输出文件第一部分
# tail - 输出文件最后一部分

# 使用 ">" 重定向符来重定向输出结果
[me@linuxbox ~]$ ls -l /usr/bin > ls-output.txt
[me@linuxbox ~]$ ls -l ls-output.txt
[me@linuxbox ~]$ less ls-output.txt
# ">>"重定向符把重定向结果追加到文件内容后面，而不是从开头重写文件
[me@linuxbox ~]$ ls -l /usr/bin >> ls-output.txt

# 重定向标准错误
# 文件流的前三个看作标准输入，输出和错误，shell 内部参考它们为文件描述符0，1和2
# shell 提供了一种表示法来重定向文件，使用文件描述符
# 因为标准错误和文件描述符2一样，用这种表示法来重定向标准错误
[me@linuxbox ~]$ ls -l /bin/usr 2> ls-error.txt
# 文件描述符"2"，紧挨着放在重定向操作符之前，来执行重定向标准错误到文件 ls-error.txt 任务

# 重定向标准输出和错误到同一个文件
# 第一个，传统的方法， 在旧版本 shell 中也有效：
[me@linuxbox ~]$ ls -l /bin/usr > ls-output.txt 2>&1
# 注意重定向的顺序安排非常重要。标准错误的重定向必须总是出现在标准输出 重定向之后，要不然它不起作用
# bash 版本提供了第二种方法
[me@linuxbox ~]$ ls -l /bin/usr &> ls-output.txt

# 处理不需要的输出
# 通过重定向输出结果 到一个特殊的叫做"/dev/null"的文件
# 这个文件是系统设备，叫做位存储桶，它可以 接受输入，并且对输入不做任何处理
[me@linuxbox ~]$ ls -l /bin/usr 2> /dev/null

# 管道线
# 命令可以从标准输入读取数据，然后再把数据输送到标准输出，
# 使用管道操作符"|"（竖杠），一个命令的 标准输出可以管道到另一个命令的标准输入
[me@linuxbox ~]$ ls -l /usr/bin | less

# 过滤器
# 把几个命令放在一起组成一个管道线
# 过滤器接受输入，以某种方式改变它，然后 输出它
[me@linuxbox ~]$ ls /bin /usr/bin | sort | less

# uniq - 报道或忽略重复行
# uniq 命令经常和 sort 命令结合在一起使用，默认情况下，从数据列表中删除任何重复行
[me@linuxbox ~]$ ls /bin /usr/bin | sort | uniq | less
# 想看到 重复的数据列表，让 uniq 命令带上"-d"选项
[me@linuxbox ~]$ ls /bin /usr/bin | sort | uniq -d | less

# wc － 打印行，字和字节数
[me@linuxbox ~]$ wc ls-output.txt
# "-l"选项限制命令输出只能 报道行数
# 查看有序列表中程序个数
[me@linuxbox ~]$ ls /bin /usr/bin | sort | uniq | wc -l

# grep － 打印匹配行
# 用来找到文件中的匹配文本
[me@linuxbox ~]$ ls /bin /usr/bin | sort | uniq | grep zip
# "-i"导致 grep 忽略大小写当执行搜索时（通常，搜索是大小写 敏感的
# "-v"选项会告诉 grep 只打印不匹配的行

# head / tail － 打印文件开头部分/结尾部分
# head 命令打印文件的前十行，而 tail 命令打印文件的后十行
# 可以通过"-n"选项来调整命令打印的行数
[me@linuxbox ~]$ head -n 5 ls-output.txt
[me@linuxbox ~]$ tail -n 5 ls-output.txt
# 也能用在管道线中
[me@linuxbox ~]$ ls /usr/bin | tail -n 5
# 允许实时的浏览文件
[me@linuxbox ~]$ tail -f /var/log/messages

# tee － 从 Stdin 读取数据，并同时输出到 Stdout 和文件
# tee 程序从标准输入读入数据，并且同时复制数据 到标准输出（允许数据继续随着管道线流动）和一个或多个文件
# 在 grep 过滤管道线的内容之前，来捕捉整个目录列表到文件 ls.txt
[me@linuxbox ~]$ ls /usr/bin | tee ls.txt | grep zip
```

## 例子

```shell
# 从系统path中寻找指定脚本的解释程序
# #!是一个约定的标记，它告诉系统这个脚本需要什么解释器来执行 /env 是系统的PATH目录中查找
# op_base.sh
#!/usr/bin/env bash  
mkdir code
cd  code
for ((i=0; i<3; i++)); do
    touch test_${i}.txt
    echo "shell很简单" >> test_${i}.txt
done
```

```shell
# 运行 Shell 脚本
# 作为可执行程序
# 设置 op_base.sh可执行权限
# 对于脚本文件，有两个常见的权限设置；权限为755的脚本，则每个人都能执行，和权限为700的 脚本，只有文件所有者能够执行。注意为了能够执行脚本，脚本必须是可读的。
# [me@linuxbox ~]$ chmod 755 op_base.sh
# [me@linuxbox ~]$ ls -l op_base.sh
chmod +x op_base.sh
# 执行
./op_base.sh

#作为参数
/bin/sh op_base.sh
```

```shell
# 变量
# 变量名和等号之间不能有空格,变量后面不能有;
my_name="jack"
my_name='jack';
# 单引号字符串的限制：
	# 单引号里的任何字符都会原样输出，单引号字符串中的变量是无效的
	# 单引号字串中不能出现单引号（对单引号使用转义符后也不行）
# 双引号：
	# 双引号里可以有变量
	# 双引号里可以有变量
	
# 使用时在前面添加$
echo $my_name
echo ${my_name}
```

```shell
# 数据类型
	字符串数字和字符串
# 拼接字符串
my_name="jack";
my_age="20岁"
echo $my_name $my_age
echo $my_name$my_age
# 获取字符串长度
echo ${#my_name}
# 截取字符串
echo ${my_name:0:2}
```

```shell
# 数组
# 用括号来表示数组，数组元素用"空格"符号分割开
name=(name1 name2 name3)
# 单独定义数组的各个分量
ary[0]=name1
ary[1]=name2
ary[3]=name3
# 读取数组
${数组名[下标]}
echo ${name[0]}
# 使用@符号可以获取数组中的所有元素
echo ${name[@]}
# 获取数组的长度
# 获取数组长度的方法与获取字符串长度的方法相同
# 取得数组元素的个数
length=${#name[@]}
echo $length
# 或者
length=${#name[*]}
echo $length
# 取得数组单个元素的长度
lengthn=${#name[n]}
echo $length
```

```shell
# 流程控制
# sh的流程控制不可为空,如果else分支没有语句执行，就不要写这个else
# if
if condition1
then
    command1
elif condition2 
then 
    command2
else
    commandN
fi

# 例如
#!/usr/bin/env bash
a=1
b=2
if [ $a == $b ]
    then
        echo "a 等于 b"
elif [ $a -gt $b ]
    then
        echo "a 大于 b"
elif [ $a -lt $b ]
    then
        echo "a 小于 b"
else
    echo "没有符合的条件"
fi
------------------------------------------------------------------------------------------
# for循环
# 第一种
for index in 1 2 3 4 5; do
    echo "index="$index
done
# 第二种
for ((i=0; i<5; i++)); do
    echo "i="$i
done
------------------------------------------------------------------------------------------
# while
int=1
while(( $int<=5 ))
do
    echo $int
    let "int++"
done
------------------------------------------------------------------------------------------
```

```shell
# shell结合系统命令
在字符处理领域，有grep、awk、sed三剑客
grep负责找出特定的行
awk能将行拆分成多个字段
sed则可以实现更新插入删除等写操作

# 例如定时检测nginx、mysql是否被关闭
path=/var/log
log=${path}/httpd-mysql.log

name=(apache mysql)

exs_init[0]="service httpd start"
exs_init[1]="/etc/init.d/mysqld restart"

for ((i=0; i<2; i++)); do
    echo "检查${name[i]}进程是否存在"
    ps -ef|grep ${name[i]} |grep -v grep
    if [ $? -eq 0 ]; then
        pid=$(pgrep -f ${name[i]})
        echo "`date +"%Y-%m-%d %H:%M:%S"` ${name[$i]} is running with pid $pid" >> ${log}
     else
        $(${exs_init[i]})
        echo "`date +"%Y-%m-%d %H:%M:%S"` ${name[$i]} start success" >> ${log}
    fi
done

# 编辑 /etc/crontab 文件
crontab -e
# 在文件最后添加一行：
*/5 * * * * /xxx/check_nginx.sh > /dev/null 2>&1
# 上表示每 5 分钟，执行一下脚本 /xxx/check_nginx.sh，其中xxx代表路径
# /dev/null 2>&1 的意思是该条shell命令将不会输出任何信息到控制台，也不会有任何信息输出到文件中
# 添加完配置，需要重启才能生效
service crond restart
```

# tomcat远程debug

```shell
# 查找占用端口的线程 
lsof -i :8080

tomcat/bin/catalina.sh文件第一行
CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF8 -Djava.net.preferIPv4Stack=true  -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Duser.timezone=GMT+8  -Xms256m -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=256m"

后面加上
-Xdebug -Xrunjdwp:transport=dt_socket,address=6004,server=y,suspend=n

CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF8 -Djava.net.preferIPv4Stack=true  -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Duser.timezone=GMT+8  -Xms256m -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=256m -Xdebug -Xrunjdwp:transport=dt_socket,address=6004,server=y,suspend=n"
```

# SVN

```shell
# 更新本地文件
svn update 本地文件路径
# 下拉文件到本地
svn checkout http://路径(目录或文件的全路径)　[本地目录全路径] --username　用户名
# 提交文件
svn add /路径/文件名    
svn commit -m "注释"
# 上传改动后文件
svn commit -m "add test file for my test" test.php
# 切换svn工作空间
cd 工作空间
svn list #查看要切换的工作空间和当前工作空间目录是否一样
svn switch http://139.224.22.150:8080/svn/community/manage/src/branch/cloudTalk_phase2/branch/bugfix_20180510_release
http://139.224.22.150:8080/svn/community/manage/src/branch/cloudTalk_phase2/170630_online
```

# 虚拟机

```shell
1. 安装VirtualBox
2. 安装Vagrant
3. 下载centos7.box
4. vagrant box add 名字 centos7.box路径
5. mkdir vagrant-cluster  vim Vagrantfile
6. 桌面上的share目录将与虚拟机内的/home/vagrant/share目录内容实时同步
	mkdir ~/Desktop/share
7. vagrant up #创建虚拟机
8. 用户/密码: vagrant/vagrant
	vagrant ssh: SSH登陆虚拟机
	vagrant halt: 关闭虚拟机
	vagrant destroy: 删除虚拟机
	vagrant ssh-config 查看虚拟机SSH配置
9. 进入Vagrantfile目录
	启动单个虚拟机：vagrant up node1	
   多个：vagrant up node1 node3
   全部：vagrant up 
10. 切换到Vagrantfile所在的目录
	vagrant ssh node1
11. 或者直接登陆
	ssh vagrant@192.168.55.1
	
192.168.56.121	
node1: 22 (guest) => 2222 (host) (adapter 1)
SSH address: 127.0.0.1:2222
SSH username: vagrant

192.168.56.122
node2: 22 (guest) => 2200 (host) (adapter 1)
SSH address: 127.0.0.1:2200
SSH username: vagrant

192.168.56.123
node3: 22 (guest) => 2201 (host) (adapter 1)
SSH address: 127.0.0.1:2201
SSH username: vagrant

#更改配置后生效
vagrant reload --provision
	
初始化box：vagrant init
启动虚拟机：vagrant up
登录虚拟机：vagrant ssh
显示box列表：vagrant box list
添加box：vagrant box add
删除box：vagrant box remove
关机：vagrant halt
重启：vagrant reload
销毁：vagrant destroy
打包虚拟机环境：vagrant package
```

```shell
# Vultr：dingyuanjie0108@163.com
进入/Applications/workspace/vagrant-cluster
vagrant up
ssh -p 22 root@192.168.56.121
ssh -p 22 root@192.168.56.123
ssh -p 22 root@192.168.56.123
vagrant
```

```shell
# Vagrantfile
Vagrant.configure("2") do |config|
  config.vm.box = "/Applications/workspace/study/vagrant-centos-7.2.box"  
  config.vm.define :node1 do |node1|
      node1.vm.hostname = "node1"
      node1.vm.network "private_network", ip: "192.168.55.121"
      node1.vm.provider "virtualbox" do |v|
        v.name = "node1-121"
        v.memory = "1024"
        v.cpus = "2"
      end
  end
  config.vm.define :node2 do |node2|
      node2.vm.hostname = "node2"
      node2.vm.network :private_network, ip: "192.168.55.122"
      node2.vm.provider "virtualbox" do |v|
          v.name = "node2-122"
          v.memory = "1024"
          v.cpus = "2"
      end
  end
  config.vm.define :node3 do |node3|
      node3.vm.hostname = "node3"
      node3.vm.network "private_network", ip: "192.168.55.123"
      node3.vm.provider "virtualbox" do |v|
          v.name = "node3-123"
          v.memory = "1024"
          v.cpus = "2"
      end
  end
end
```

# 服务器配置翻墙

```shell
#配置
yum install pip
yum search shadowsocks
pwd
curl -O https://bootstrap.pypa.io/get-pip.py
mv get-pip.py ~/
cd ~
python get-pip.py
pip install shadowsocks
cd /
mkdir dyj
cd dyj
vi vultr.json
    {
        "server":"66.42.93.222",
        "server_port":8388,
        "local_port":1080,
        "password":"woodyfine",
        "timeout":600,
        "method":"aes-256-cfb"
    }
firewall-cmd --zone=public --add-port=8388/tcp --permanent
firewall-cmd --reload
# 开启翻墙
[root@vultr dyj]# ssserver -c ./vultr.json -d start
```

* 下载shadowsocket软件

