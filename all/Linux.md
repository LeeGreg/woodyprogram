![image-20190809171118202](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809171118202.png)

```shell
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
```

* 如何查找特定的文件

  ```shell
  # 当前目录下递归查找
  find -name "xxx.txt"
  # 全局查找
  find / -name "xxx.java"
  # 忽略大小写
  find ~ -iname "xxx*.java"
  ```

* 检索文件内容

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

* 对文件内容做统计

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

* 批量替换文本的内容

  ```shell
  sed # 流编译器，适合用于对文本的行内容进行处理
  sed -i 's/^Str/String/' test.txt # s字符串、^Str以Str开头的被替换的内容 String替换内容
  sed -i 's/\.$/\;/' test.txt  # 每行结尾的.替换成;
  sed -i 's/Jack/me/g' test.txt # g表示全部都替换，否则只替换第一个符合条件的
  sed -i '/^ *$/d' test.txt  # d删除行   删除空行
  sed -i '/Integer/d' test.txt # 删除 Integer开头的行
  ```

* cat、less、more

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
    * -N 显示每行的行号
    * 空格键 滚动一页
    * 回车键 滚动一行
    * b 向后翻一页
    * d 向后翻半页
    * u 向前滚动半页
    * /字符串：向下搜索“字符串”的功能
    * ?字符串：向上搜索“字符串”的功能
    * n：重复前一个搜索（与 / 或 ? 有关）
    * N：反向重复前一个搜索（与 / 或 ? 有关）

* vim