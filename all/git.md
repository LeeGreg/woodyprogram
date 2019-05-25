# Git

* 开源分布式管理控制系统
  * 对待数据方式：
    * svn，将保存的信息看做是一组基本文件和每个文件随时间逐步累积的差异
    * git，存储每个文件与初始版本的差异。存储项目随时间改变的快照。提交更新时，对当时的全部文件制作一个快照并保存这个快照的索引，如果文件没有修改，不再重新存储该文件，而只是保留一个链接指向之前存储的文件。git对待数据更像一个快照流。
    * Git 保存的不是文件的变化或者差异，而是一系列不同时刻的文件快照
  * 操作方式：
    * git，几乎所有操作都是本地执行
      * 本地磁盘有项目完整历史、本地提交，有网络时再上传
  * git保证完整性
    * git中所有数据在存储前都会计算校验，git能发现传送过程中丢失信息或损坏文件
    * git数据库中保存的信息都是以文件内容的哈希值来索引，而不是文件名
  * git操作几乎只向Git数据库中增加数据
  * git三种状态：已修改、已暂存、已提交
  * git三个工作区：
    * 工作目录：修改文件
    * 暂存区域：将文件的快照放入暂存区域
    * Gi 仓库：将快照永久性存储到Git仓库目录

## 本地创建分支和远程非master分支关联

```java
// 方式一
/Users/dingyuanjie/Documents/code/work/yunduijiang [git::master]
// git checkout -b 本地分支名称 origin/远程分支名称
> git checkout -b face2.0 origin/face2.0
  Branch 'face2.0' set up to track remote branch 'face2.0' from 'origin'.
Switched to a new branch 'face2.0'
```

```java
// 方法二
// git clone -b 分支名称 仓库地址
> git clone -b face2.0 http://139.224.22.150:7803/cloudtalk/lianzhiyun.git
```

## 本地指向远程某分支

```java
1. 远程创建新分支face_server
2. git pull
3. git branch -a 
4. git checkout 远程新分支face_server
本地就提交到远程新分支face_server上了
```

## 场景

* 远程github创建仓库`https://github.com/xiaojieWoody/githubTest.git`
* 本地选择一个目录`git clone https://github.com/xiaojieWoody/githubTest.git`
* 修改文件，然后本地提交到远程
  * `git add .`、`git commit -m "第一次提交"`、`git push`
* `git branch`列出本地所有分支：`* master`
* `git branch -r`列出所有远程分支：`origin/HEAD -> origin/master`、`origin/master`
* 创建并切换到本地新分支：`git checkout -b test01`
  * 修改文件并提交，
    * `git add .`、`git commit -m "xxx"`、
      * 远程会创建对应的分支：` git push --set-upstream origin test01`
      * 提交到指定远程分支上：`git push origin test06:master`

![image-20190520103813668](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190520103813668.png)

* 新建代码库

  ```shell
  # 在当前目录新建一个Git代码库
  $ git init
  # 新建一个目录，将其初始化为Git代码库
  $ git init [project-name]
  # 下载一个项目和它的整个代码历史
  $ git clone [url] [自定义名称]
  # 默认情况下，git clone命令会自动设置本地master分支跟踪克隆的远程仓库的master分支
  # git pull通常会从最初克隆的服务器上抓取数据并自动尝试合并到当前所在的分支
  ```

* `git remote show origin`

  * 可以查看本地分支所对应pull和push的远程分支
  
* 别名

  * `git config --global alias.ci commit`
    * `git commit` 时，只需要输入 `git ci`

* 忽略文件

  ```shell
  cat .gitignore
  # 忽略所有以 .o或.a结尾的文件
  .*[oa]  
  # 忽略以~结尾的文件
  .~
  # no .a files
  *.a
  !lib.a   # 不管怎样，跟踪lib.a
  /TODO    # 当前目录下只忽略这 TODO file
  build/   # 忽略build目录中所有文件
  doc/*.txt # 忽略doc一级目录下txt文件
  doc/**/*.pdf #忽略 doc目录下所有pdf文件
  ```

* 远程同步

  ```shell
  # 下载远程仓库的所有变动
  $ git fetch [remote]
  # 显示所有远程仓库，可以用第一段字符串pb代替整个URL，如git fetch pb
  $ git remote -v
  # 显示某个远程仓库的信息
  $ git remote show [remote]
  # 增加一个新的远程仓库，并命名
  $ git remote add [shortname] [url]
  # 取回远程仓库的变化，并与本地分支合并
  $ git pull [remote] [branch]
  # 上传本地指定分支到远程仓库，先pull再push
  $ git push [remote] [branch]
  # 强行推送当前分支到远程仓库，即使有冲突
  $ git push [remote] --force
  # 推送所有分支到远程仓库
  $ git push [remote] --all
  ```

* 配置

  ```shell
  # Git的设置文件为.gitconfig，它可以在用户主目录下（全局配置），也可以在项目目录下（项目配置）
  # 显示当前的Git配置
  $ git config --list
  # 编辑Git配置文件
  $ git config -e [--global]
  # 设置提交代码时的用户信息
  $ git config [--global] user.name "[name]"
  $ git config [--global] user.email "[email address]"
  ```

* 增加/删除文件

  ```shell
  # 添加指定文件到暂存区
  $ git add [file1] [file2] ...
  # 添加指定目录到暂存区，包括子目录
  $ git add [dir]
  # 添加当前目录的所有文件到暂存区
  $ git add .
  # 添加每个变化前，都会要求确认
  # 对于同一个文件的多处变化，可以实现分次提交
  $ git add -p
  # 删除工作区文件，并且将这次删除放入暂存区
  $ git rm [file1] [file2] ...
  # 停止追踪指定文件，但该文件会保留在工作区
  $ git rm --cached [file]
  # 改名文件，并且将这个改名放入暂存区
  $ git mv [file-original] [file-renamed]
  ```

* 代码提交

  ```shell
  # 提交暂存区到仓库区
  $ git commit -m [message]
  # 提交暂存区的指定文件到仓库区
  $ git commit [file1] [file2] ... -m [message]
  # 提交工作区自上次commit之后的变化，直接到仓库区  跳过add
  $ git commit -a -m "xxx"
  # 提交时显示所有diff信息
  $ git commit -v
  # 使用一次新的commit，替代上一次提交
  # 如果代码没有任何新变化，则用来改写上一次commit的提交信息
  $ git commit --amend -m [message]
  # 重做上一次commit，并包括指定文件的新变化
  $ git commit --amend [file1] [file2] ...
  ```

* 分支

  ```shell
  # HEAD指向当前所在的本地分支，HEAD 分支随着提交操作自动向前移动
  
  # 列出所有本地分支
  $ git branch
  # 列出所有远程分支
  $ git branch -r
  # 列出所有本地分支和远程分支
  $ git branch -a
  # 新建一个分支，但依然停留在当前分支
  $ git branch [branch-name]
  # 新建一个分支，并切换到该分支
  $ git checkout -b [branch]
  # 新建一个分支，指向指定commit
  $ git branch [branch] [commit]
  # 新建一个分支，与指定的远程分支建立追踪关系
  $ git branch --track [branch] [remote-branch]
  # 切换到指定分支，并更新工作区
  $ git checkout [branch-name]
  # 切换到上一个分支
  $ git checkout -
  # 建立追踪关系，在现有分支与指定的远程分支之间
  $ git branch --set-upstream [branch] [remote-branch]
  # 合并指定分支到当前分支
  $ git merge [branch]
  # 选择一个commit，合并进当前分支
  $ git cherry-pick [commit]
  # 删除分支
  $ git branch -d [branch-name]
  # 删除远程分支
  $ git push origin --delete [branch-name]
  $ git branch -dr [remote/branch]
  ```

* 标签

  ```shell
  # 列出所有tag
  $ git tag
  # 新建一个tag在当前commit
  $ git tag [tag]
  # 新建一个tag在指定commit
  $ git tag [tag] [commit]
  # 删除本地tag
  $ git tag -d [tag]
  # 删除远程tag
  $ git push origin :refs/tags/[tagName]
  # 查看tag信息
  $ git show [tag]
  # 提交指定tag
  $ git push [remote] [tag]
  # 提交所有tag
  $ git push [remote] --tags
  # 新建一个分支，指向某个tag
  $ git checkout -b [branch] [tag]
  ```

* 查看信息

  * `git log --oneline --decorate --graph —all`
    * 输出提交历史、各个分支的指向以及项目的分支分叉情况

  ```shell
  # 显示有变更的文件
  $ git status
  # 显示当前分支的版本历史
  $ git log
  git log --pretty=format:"%h %s" --graph
  # 显示commit历史，以及每次commit发生变更的文件
  $ git log --stat
  # 搜索提交历史，根据关键词
  $ git log -S [keyword]
  # 显示某个commit之后的所有变动，每个commit占据一行
  $ git log [tag] HEAD --pretty=format:%s
  # 显示某个commit之后的所有变动，其"提交说明"必须符合搜索条件
  $ git log [tag] HEAD --grep feature
  # 显示某个文件的版本历史，包括文件改名
  $ git log --follow [file]
  $ git whatchanged [file]
  # 显示指定文件相关的每一次diff
  $ git log -p [file]
  # 显示过去5次提交
  $ git log -5 --pretty --oneline
  # 显示所有提交过的用户，按提交次数排序
  $ git shortlog -sn
  # 显示指定文件是什么人在什么时间修改过
  $ git blame [file]
  # 显示暂存区和工作区的差异
  $ git diff
  # 显示暂存区和上一个commit的差异
  $ git diff --cached [file]
  # 显示工作区与当前分支最新commit之间的差异
  $ git diff HEAD
  # 显示两次提交之间的差异
  $ git diff [first-branch]...[second-branch]
  # 显示今天你写了多少行代码
  $ git diff --shortstat "@{0 day ago}"
  # 显示某次提交的元数据和内容变化
  $ git show [commit]
  # 显示某次提交发生变化的文件
  $ git show --name-only [commit]
  # 显示某次提交时，某个文件的内容
  $ git show [commit]:[filename]
  # 显示当前分支的最近几次提交
  $ git reflog
  ```

* 撤销

  ```shell
  # 恢复暂存区的指定文件到工作区
  $ git checkout [file]
  # 恢复某个commit的指定文件到暂存区和工作区
  $ git checkout [commit] [file]
  # 恢复暂存区的所有文件到工作区
  $ git checkout .
  # 重置暂存区的指定文件，与上一次commit保持一致，但工作区不变
  $ git reset [file]
  # 重置暂存区与工作区，与上一次commit保持一致
  $ git reset --hard
  # 重置当前分支的指针为指定commit，同时重置暂存区，但工作区不变
  $ git reset [commit]
  # 重置当前分支的HEAD为指定commit，同时重置暂存区和工作区，与指定commit一致
  $ git reset --hard [commit]
  # 重置当前HEAD为指定commit，但保持暂存区和工作区不变
  $ git reset --keep [commit]
  # 新建一个commit，用来撤销指定commit
  # 后者的所有变化都将被前者抵消，并且应用到当前分支
  $ git revert [commit]
  # 暂时将未提交的变化移除，稍后再移入
  $ git stash
  $ git stash pop
  ```

  

