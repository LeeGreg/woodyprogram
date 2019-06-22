```shell
# 下载
# 配置用户信息
git config --global user.name "woody"
git config --global user.email "woody@163.com"
git config --list
# SourceTree 图形化界面
# 初始化版本库
git init
echo "git repo2" >> test.txt
git add test.txt
git commit -m "repo2 first commit"
git status
# 暂存区回退到工作区
git reset HEAD bash_demo.txt
# 清理工作区
git checkout -- bash_demo.txt
# 远程仓库回滚
git log   # 获取 commitID
git reset --hard commitID
git status
# 清空文件（本地、远程仓库）
git rm bash_demo.txt
git status
git commit -m "..."
```

```shell
# 创建SSH key
ssh-keygen -t rsa -C "woody@163.com"
# 系统用户目录
cd .ssh/
ll   # id_rsa、id_rsa.pub、known_hosts
cat id_rsa.pub   # 内容复制到github的setting设置的SSH and GPG keys的 Key中
# 判断本地和github是否是通的
ssh -T git@github.com

# 远程创建一个空仓库 demo4.git
# 本地初始化仓库
echo "# tyler_muke" >> README.md
git init 
git add README.md
git commit -m "first commit"
# 添加到远程仓库
git remote add origin git@github.com:tylerdemo/demo4.git
# 本地仓库和远程仓库关联起来
# git pull origin master --allow-unrelated-histories
git push -u origin master
```

```shell
# 克隆仓库
git clone git@github.com:tylerdemo/demo4.git
git add ..
git commit ..
git push    # 默认本地已与远程绑定，可直接push推送
```

```shell
# 标签管理  版本-回滚
git tag  # 查看所有标签
git tag name  # 创建标签
# git tag -a name -m "comment"  # 指定提交信息
git tag -d name # 删除标签
git push origin name # 标签发布
```

```shell
# 分支管理
git init 
echo "xxx" >> branch.txt
git add baranch.txt
git commit -m "xx"
git status
# 创建分支
git branch feature_x
git branch       # 查看分支（*当前分支master）
git checkout feature_x # feature_x
echo "new feature" >> baranch.txt
git add barnch.txt
git commit -m "new feature add"
# 合并到master分支
git checkout master
git merge feature_x
# 删除分支
git branch -d feature_x
```

# Git

* 开源分布式管理控制系统
  * 对待数据方式：
    * svn，将保存的信息看做是一组基本文件和每个文件随时间逐步累积的差异
    * git，存储每个文件与初始版本的差异。存储项目随时间改变的快照。提交更新时，对当时的全部文件制作一个快照并保存这个快照的索引，如果文件没有修改，不再重新存储该文件，而只是保留一个链接指向之前存储的文件。git对待数据更像一个快照流。
    * Git 保存的不是文件的变化或者差异，而是一系列不同时刻的文件快照
    * Git 的分支实质上仅是包含所指对象校验和（长度为 40 的 SHA-1 值字符串）的文件，所以它的创建和销毁都异常高效。 创建一个新分支就相当于往一个文件中写入 41 个字节（40 个字符和 1 个换行符）
    * 其他版本控制系统，创建分支时，将所有的项目文件都复制一遍，并保存到一个特定的目录
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

## 案例

* 开发某个网站；为实现某个新的需求，创建一个分支；在这个分支上开展工作

* 严重的问题需要紧急修补

* 切换到你的线上分支（production branch）；为这个紧急任务新建一个分支，并在其中修复它；在测试通过之后，切换回线上分支，然后合并这个修补分支，最后将改动推送到线上分支；切换回最初工作的分支上，继续工作

  ```shell
  # 解决的公司使用的问题追踪系统中的 #53 问题，新建一个分支并同时切换到那个分支上
  git checkout -b iss53
  # 继续在 #53 问题上工作，并且做了一些提交，iss53 分支在不断的向前推进
  # 紧急问题等待解决
  1. 修改全部提交了，然后切换回 master 分支。切换分支的时候，Git 会重置工作目录，使其看起来像回到了在那个分支上最后一次提交的样子，需确保切换前必要的修改都已提交
  git checkout master
  2. 建立一个针对该紧急问题的分支hotfix，提交修改
  git checkout -b hotfix
  3. 将分支hotfix合并回 master 分支来部署到线上
  git checkout master
  git merge hotfix
  # 快进（fast-forward）
  试图合并两个分支时，如果顺着一个分支走下去能够到达另一个分支，那么 Git 在合并两者的时候，只会简单的将指针向前推进（指针右移），因为这种情况下的合并操作没有需要解决的分歧
  4. 删除 hotfix 分支
  git branch -d hotfix
  5. 切回iss53问题的那个分支
  git checkout iss53
  6. 如果需要拉取 hotfix 所做的修改， 将master 分支合并入 iss53 分支
  git merge master 
  # master 分支所在提交并不是 iss53 分支所在提交的直接祖先，Git 会使用两个分支的末端所指的快照（master和iss53）,以及这两个分支的工作祖先(创建hotfix分支前的master结点)，做一个简单的三方合并，结果做了一个新的快照并且自动创建一个新的提交指向它
  git checkout master
  git merge iss53
  # 对#53问题的修改和有关 hotfix 的修改都涉及到同一个文件的同一处，在合并它们的时候就会产生合并冲突
  git merge iss53
  # 此时 Git 做了合并，但是没有自动地创建一个新的合并提交。 Git 会暂停下来，等待你去解决合并产生的冲突
  # 可以在合并冲突后的任意时刻使用 git status 命令来查看那些因包含合并冲突而处于未合并（unmerged）状态的文件
  git status
  # 任何因包含合并冲突而有待解决的文件，都会以未合并状态标识出来
  # Git 会在有冲突的文件中加入标准的冲突解决标记，这样可打开这些包含冲突的文件然后手动解决冲突
  # 为了解决冲突，你必须选择使用由 ======= 分割的两部分中的一个，或者你也可以自行合并这些内容
  # 解决了所有文件里的冲突之后，对每个文件使用 git add 命令来将其标记为冲突已解决。 一旦暂存这些原本有冲突的文件，Git 就会将它们标记为冲突已解决
  git add
  # 可以再次运行 git status 来确认所有的合并冲突都已被解决
  git status
  # 输入 git commit 来完成合并提交
  ```

* 合并分支

  * merge
    * 会使用两个分支的末端所指的快照，以及这两个分支的共同祖先，做一个简单的三方合并，结果做了一个新的快照并且自动创建一个新的提交指向它
  * rebase变基
    * `git checkout experiment`；`git rebase master`；`git checkout master`；`git merge experiment`
    * 首先找到这两个分支（即当前分支 `experiment`、变基操作的目标基底分支 `master`）的最近共同祖先 `C2`，然后对比当前分支相对于该祖先的历次提交，提取相应的修改并存为临时文件，然后将当前分支指向目标基底 `C3`, 最后以此将之前另存为临时文件的修改依序应用，现在回到 `master` 分支，进行一次快进合并
  * 这两种整合方法的最终结果没有任何区别，但是变基使得提交历史是一条直线没有分叉而更加整洁

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

  * `git commit -a -m "xxx"`
  * 提交工作区自上次commit之后的变化，直接到仓库区  跳过add

  ```shell
  # 提交暂存区到仓库区
  $ git commit -m [message]
  # 提交暂存区的指定文件到仓库区
  $ git commit [file1] [file2] ... -m [message]
  # 提交时显示所有diff信息
  $ git commit -v
  # 使用一次新的commit，替代上一次提交
  # 如果代码没有任何新变化，则用来改写上一次commit的提交信息
  $ git commit --amend -m [message]
  # 重做上一次commit，并包括指定文件的新变化
  $ git commit --amend [file1] [file2] ...
  ```

* 分支

  * `git checkout [branch-name]`

    * 切换到指定分支，并更新工作区
    * HEAD指回指定分支；工作目录恢复成指定分支所指向的快照内容

  * 远程跟踪分支

    * 是远程分支状态的引用，是你不能移动的本地引用，当你做任何网络通信操作时，它们会自动移动
    * 像是你上次连接到远程仓库时，那些分支所处状态的书签
    * 以 `(remote)/(branch)` 形式命名
      * “origin” 是当你运行 `git clone` 时默认的远程仓库名字。 如果你运行 `git clone -o booyah`，那么你默认的远程分支名字将会是 `booyah/master`
      *  “master” 是当你运行 `git init` 时默认的起始分支名字
      * Git 的 `clone` 命令会为你自动将其命名为 `origin`，拉取它的所有数据，创建一个指向它的 `master` 分支的指针，并且在本地将其命名为 `origin/master`
      * Git 也会给你一个与 origin 的 `master` 分支在指向同一个地方的本地 `master` 分支

    ![image-20190528171938728](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190528171938728.png)

    * 如果你在本地的 `master` 分支做了一些工作，然而在同一时间，其他人推送提交并更新了它的 `master` 分支，那么你的提交历史将向不同的方向前进。也许，只要你不与 origin 服务器连接，你的 `origin/master` 指针就不会移动

      ![image-20190528172111479](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190528172111479.png)

    * 如果要同步你的工作，运行 `git fetch origin` 命令。 这个命令查找 “origin” 是哪一个服务器，从中抓取本地没有的数据，并且更新本地数据库，移动 `origin/master` 指针指向新的、更新后的位置

      ![image-20190528172225577](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190528172225577.png)

    * 运行 `git remote add` 命令添加一个新的远程仓库引用到当前的项目，将这个远程仓库命名为 `teamone`，将其作为整个 URL 的缩写。

      * `git remote add teamone git://git.team1.ourcompany.com`

    * 现在，可以运行 `git fetch teamone` 来抓取远程仓库 `teamone` 有而本地没有的数据。 因为那台服务器上现有的数据是 `origin` 服务器上的一个子集，所以 Git 并不会抓取数据而是会设置远程跟踪分支 `teamone/master` 指向 `teamone` 的 `master` 分支

      ![image-20190528174816084](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190528174816084.png)

    * 推送

      * 想要公开分享一个分支时，需要将其推送到有写入权限的远程仓库上
        * 本地的分支并不会自动与远程仓库同步 - 你必须显式地推送想要分享的分支
      * 如果希望和别人一起在名为 `serverfix` 的分支上工作，你可以像推送第一个分支那样推送它。 运行 `git push (remote) (branch)`:`git push origin serverfix`
        * “推送本地的 serverfix 分支来更新远程仓库上的 serverfix 分支。”
      * 也可以运行 `git push origin serverfix:serverfix`，它会做同样的事 - 相当于它说，“推送本地的 serverfix 分支，将其作为远程仓库的 serverfix 分支” 
        * 可以通过这种格式来推送本地分支到一个命名不相同的远程分支
      * 可以运行 `git push origin serverfix:awesomebranch` 来将本地的 `serverfix` 分支推送到远程仓库上的 `awesomebranch` 分支
        * 下一次其他协作者从服务器上抓取数据时，他们会在本地生成一个远程分支 `origin/serverfix`，指向服务器的 `serverfix` 分支的引用。`git fetch origin`
        * 可以运行 `git merge origin/serverfix` 将这些工作合并到当前所在的分支。 如果想要在自己的 `serverfix` 分支上工作，可以将其建立在远程跟踪分支之上：
        * `git checkout -b serverfix origin/serverfix`

    * 跟踪分支

      * 从一个远程跟踪分支检出一个本地分支会自动创建所谓的 “跟踪分支”

      * 跟踪分支是与远程分支有直接关系的本地分支。 如果在一个跟踪分支上输入 `git pull`，Git 能自动地识别去哪个服务器上抓取、合并到哪个分支

      * 当克隆一个仓库时，它通常会自动地创建一个跟踪 `origin/master` 的 `master` 分支

      * 想要将本地分支与远程分支设置为不同名字

        * `git checkout -b sf origin/serverfix`
        * 现在，本地分支 `sf` 会自动从 `origin/serverfix` 拉取。

      * 设置已有的本地分支跟踪一个刚刚拉取下来的远程分支，或者想要修改正在跟踪的上游分支，你可以在任意时间使用 `-u` 或 `--set-upstream-to` 选项运行 `git branch` 来显式地设置

        * `git branch -u origin/serverfix`

      * 如果想要查看设置的所有跟踪分支，可以使用 `git branch` 的 `-vv` 选项。 这会将所有的本地分支列出来并且包含更多的信息，如每一个分支正在跟踪哪个远程分支与本地分支是否是领先、落后或是都有

        * 如果想要统计最新的领先与落后数字，需要在运行此命令前抓取所有的远程仓库。 可以像这样做：`$ git fetch --all; git branch -vv`

        ```shell
        $ git branch -vv
          iss53     7e424c3 [origin/iss53: ahead 2] forgot the brackets
          master    1ae2a45 [origin/master] deploying index fix
        * serverfix f8674d9 [teamone/server-fix-good: ahead 3, behind 1] this should do it
          testing   5ea463a trying something new
        ```

    * 拉取

      * 当 `git fetch` 命令从服务器上抓取本地没有的数据时，它并不会修改工作目录中的内容。 它只会获取数据然后让你自己合并
      * 然而，有一个命令叫作 `git pull` 在大多数情况下它的含义是一个 `git fetch`紧接着一个 `git merge` 命令
      * 通常单独显式地使用 `fetch` 与 `merge` 命令会更好一些

    * 删除远程分支

      *  可以运行带有 `--delete` 选项的 `git push` 命令来删除一个远程分支。 

        * 如果想要从服务器上删除 `serverfix` 分支

          ```shell
          $ git push origin --delete serverfix
          To https://github.com/schacon/simplegit
           - [deleted]         serverfix
          ```

        * 基本上这个命令做的只是从服务器上移除这个指针。 Git 服务器通常会保留数据一段时间直到垃圾回收运行，所以如果不小心删除掉了，通常是很容易恢复的

  ```shell
  # HEAD指向当前所在的本地分支，HEAD 分支随着提交操作自动向前移动
  
  # 列出所有本地分支
  $ git branch
  # 查看每一个分支的最后一次提交
  git branch -v 
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

  

