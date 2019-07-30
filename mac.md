## IDEA 

## 配置

## 快捷键

* Emacs 系的快捷键
  * Ctrl + A：移动到行首
  * Ctrl + E：移动到行尾
  * Ctrl + K：删除到行尾
  * Ctrl + N：移动到下一行
  * Ctrl + P：移动到上一行
  * Ctrl + U：清除当前行
  * Ctrl + R：搜索命令历史
  * Ctrl + W：删除单词
  * Command + R：清屏
  * Command + Shift + H：列出剪切板历史
  * Command + ;：列出输入过的命令
  * Command + d：横着分屏；Command + Shift + d：竖着分屏

* ssh的配置与管理

# Vim

* 在普通模式下，按下 `o`会快速进入输入模式，并且在下一行进行编辑。如果按下大写的`O`会在上一行编辑
* 在当前光标位置开始编辑，可以按下 `a` 或 `i`，前者表示在光标所在字符的右侧开始编辑，后者表示在左侧开始编辑
* `I` 前往行头开始编辑，或者用 `A` 跳转到行尾编辑
* 按下 `dd` 可以快速删除一行，此时被删除的那一行会保存在 Vim 的剪贴板中
* 普通模式下，按下 `p` 可以把 Vim 剪贴板中的内容再拷贝出来
* 如果想交换当前行和下一行，可以输入：`ddp`
* 只想复制，不想删除，可以把命令 `p` 换成 y（表示：yank）
* `cc` 表示删除当前行并且从行首开始编辑
* 撤销上一次修改，可以用 `u`
* 重做，可以用 `Ctrl + R`
* 使用 `/` 进行搜索
* `:1,10s/hello/hallo/g` 表示把第 1 - 10 行中**所有**的 `hello` 换成 `hallo`

# Shell

* 执行shell脚本

  * 调用解释器执行：`sh test.sh`或`bash test.sh`，建议统一使用 bash 即可
    * **实际上是在当前的 Shell 环境中启动了一个子进程去执行**
  * 直接输入文件名运行
    * `./test.sh`，
    * 变更文件的权限：`chmod +x test.sh`
  * 当前shell运行
    * `. ./test.sh`或`source ./test.sh`
    * 不涉及到 Shell 进程的切换，所有变量和函数的定义都是相通的
    * 通过 `.` 和 `srouce` 来调用脚本基本上是一致的，区别在于 `source` 的兼容性更好，因此更加推荐

* 变量

  * 用 `$` 加上变量名就可以引用变量

    * ```bash
      a=1
      echo ${a}  # 推荐
      或
      echo $a
      ```

  * 引号

    * 单引号中的内容完全是字面量，甚至单引号中都无法使用转义字符再打印出单引号
    * 双引号中，如果遇到变量，将会自动转换为变量的值

  * 变量作用域

    * 默认情况下，变量的作用域是当前的 Shell，即使变量定义在函数中也是如此
    * 函数内部的变量要加上 `local` 关键字才不会污染全局作用域

  * 默认全局变量

    * 记录当前所在的目录，通过 `echo $PWD` 查看
    * 表示上一次所在的目录，输入减号 `-` 可以快速跳转到上一次所在的目录
    * 特殊变量
      * `$0`：表示脚本名字，可能是相对路径，当执行 `bash a/b/c/d.sh` 时，`$0` 的值是 `a/b/c/d.sh`
      * `$1、$2、……、$10`：用来表示参数，`$1` 表示第一个参数，以此类推
      * `$#`：表示参数个数
      * `$?`：表示上一个命令的执行结果，0 表示正常结束，非 0 表示出现错误

* 基础语法

  * 条件判断，`[` 和 `[[`，推荐`[[`

    ```shell
    abc="1"
    if [[ $abc = "1" ]]; then
        echo "equal"
    fi
    
    # 或者
    if [ $abc = "1" ]; then
        echo "equal"
    fi
    ```
    * 数字判断

      ```shell
      [[ $abc = 1 ]] && echo "yes" || echo "not"
      [[ $abc == 1 ]] && echo "yes" || echo "not"
      # 或者 
      [[ $abc -eq 1 ]] && echo "yes" || echo "not"
      # 输出结果都是 yes
      ```
      * 不等号可以用 `!=` 或 `-ne` 表示，大于号可以用 `>` 或者 `-gt` 来表示，小于号用 `<` 或者 `-lt` 表示
      * `-ge` 表示大于等于， `-le` 表示小于等于

    * 字符串判断

      * 字符串的判等和数字一致，不同的是可以判断字符串是否为空

      ```shell
      str="" 
      # 未定义和长度为零的字符串都算空字符串
      [[ -z $str ]] && echo "yes" || echo "not" # 输出 yes
      [[ -n $str ]] && echo "yes" || echo "not" # 输出 not
      
      # 字符串还支持模式匹配
      str="hello"
      [[ $str == he* ]] && echo "yes" || echo "not"
      # 模式匹配，以 he 开头的单词都能匹配，hello 满足要求，所以输出 yes
      ```

    * 文件判断

      1. `if [[ -e file ]]`判断是否存在，不限制类型
      2. `if [[ -f file ]]`判断文件是否存在，必须是普通类型的文件，不能是文件夹
      3. `if [[ -d file ]]`判断文件夹是否存在，必须是文件夹，不能是文件

    * 逻辑运算符

      ```shell
      [[ ! $str == h*lo || 1 = 1 ]] && echo "yes" || echo "not"
      # 第一个判断取反，结果为 false，但第二个判断为 true，所以最终效果是输出 yes
      
      [[ $str == h*lo && 1 = 2 ]] && echo "yes" || echo "not"
      # 第二个判断为 false，所以输出 not
      ```

    * if语句

      ```shell
      if [[ expression_1 ]]; then
         echo "condition 1"
      elif [[ expression_2 ]]; then
         echo "condition 2"
      else
         echo "condition else"
      fi
      ```

    * 循环

      ```shell
      for f in `ls`; do
          echo $f
      done
      ```

* 命令串联

  * 管道

    * 允许不同脚本、命令之间互相传递数据
    * `ls | grep 'a'`
      * 默认情况下，命令 `ls` 会把当前目录下的文件输出到屏幕上，但如果通过管道符号 `|`，它就会把输出结果传递给下一个命令
      * 命令 `grep` 恰好支持从管道中读取数据，因此上面这行脚本的含义实际上是**在当前目录内寻找名称含有字母** `a` **的文件**

    ```shell
    function before {
        echo 'output'
    }
    
    function after {
        read in
        echo "Read from pipiline: "${in}
    }
    
    before | after
    # 输出结果为：
    # Read from pipiline: output
    ```

  * 重定向

    * 最简单的使用场景就是把原本输出到屏幕的内容，重定向到文件中

    * unix 系统中有三种特殊的文件描述符

      *  0 表示标准输入，它一般指的是键盘，1 表示标准输出，2 表示错误输出，它们一般都表示屏幕

      * `ls exist.sh not_exist.sh 1>success 2>fail`

        * 首先是要展示两个文件，假设一个文件存在，另一个文件不存在（从名字就能看出来了），这样会产生一行标准输出和一行错误输出。`1>success` 的意思是把标准输出重定向到 success 这个文件，类似的，`2 > fail` 表示把错误信息输出到 fail 这个文件

        * 类似的语法
          * `ls exist.sh not_exist.sh >success 2>&1`
          * 如果 `>` 前面不加数字，默认是标准输出。而 `2>&1` 则表示让错误输出使用和标准输出相同的重定向方式。因此这个命令等价于 `ls exist.sh not_exist.sh 1>success 2>success`

  * 过滤输出

    * 只希望用到一个命令的功能， 但不希望它产生任何输出
      * `command > /dev/null 2>&1`
        * 把标准输出和错误输出都重定向到 `/dev/null` 文件，只是一个特定的文件(内容都会被抛弃掉)

  * 输入重定向

    * 要想拷贝某个文件中的内容到剪贴板，`cat file | pbcopy`
      * 更高效、更直接的写法可减少一次 IO 操作，`pbcopy < file`

  * 函数的返回值

    * 在函数的结尾可以使用 `return` 关键字，然而需要注意的是，调用函数后的返回结果，并不是 `return` 的内容，而是 `echo` 的内容。至于 return 的内容，则可以通过 `$?` 这个特殊变量来读取

      ```shell
      function foo {
          echo 'output'
          return 1
      }
      
      a=`foo`
      echo $?        # 输出 1
      echo $a        # 输出 output
      ```

    * 在 `if` 语句中，除了可以进行普通的判断外，还可以直接根据命令的执行结果进行判断。此时读取的依然是 `return` 的结果

    * 判断当前目录下是否存在某个文件，放到 `if` 中就可以写为

      ```shell
      if `ls | grep -q 'a'` ; then
          echo "yes"
      else
          echo "no"
      fi
      ```

* 错误处理

  * 开启错误处理，使用 shell 中的错误处理有助于发现错误，更好的调试代码

  * 检测未定义变量

    * `set -u` 可以在遇到未定义变量时抛出错误，而不是忽略它

      ```shell
      # 这里的变量 bar 没有定义，shell 的默认方案是忽略掉它。这就可能带来隐藏的问题，所以通过 set -u 选项来强制报错
      set -u
      echo $bar
      ```

  * 报错时退出

    * set -e 选项来强制报错时退出执行脚

      ```shell
      # bbbb 和 ssss 都是不存在的指令。而加上以后，这里只会有一个报错就立刻 exit 了
      set -e
      bbbb
      ssss
      ```

    * 用管道的写法，得到的返回值是最后一个命令的返回值，如果中间的命令出错，是不能被 `set -e` 捕获

      ```shell
      set -e
      
      bs | ls
      echo 'reach here'
      
      # 得到的输出结果将是
      
      aaa.sh: line 3: bs: command not found
      test.sh
      reach here
      # bs 这个指令虽然不存在，但程序还是没有退出，而是执行到了结尾。因此 set -e 通常需要配合 set -o pipfail 来使用，这样管道中的任何一个指令出错，都会导致程序退出
      ```

  * 调试执行
    * 如果想知道每一行都执行了什么代码，可以用 `set -x` 选项，**通常我们在 Jenkins 等工具里可以这么用，方便追查问题**
    * 

  

