# readme

```shell
# 什么是git
开源分布式版本控制系统，可以有效、高速地处理从很小到非常大的项目版本管理
# 让Git显示颜色，会让命令输出看起来更醒目
git config --global color.ui true
# 版本回退
A -> B	-> C
Server（C）回滚到B
1. 本地reset到B
git log
	B commit 3832343nsesd32sf3423dsfsdssd2342
git reset --hard 3832343nsesd32sf3423dsfsdssd2342
2. 远端
git push -f origin master 

# Git的版本回退速度非常快，因为Git在内部有个指向当前版本的HEAD指针，当你回退版本的时候，Git仅仅是把HEAD从指向要回腿的版本

# 记录的每一次命令
git reflog
```

## git和svn的区别

Git是分布式版本控制，SVN是集中式版本控制

- 集中式版本控制系统
  - 版本库是集中存放在中央服务器，要先从中央服务器取得最新的版本
  - 必须联网才能工作
- 分布式版本控制系统
  - 没有“中央服务器”，每个人的电脑上都是一个完整的版本库，即使没有网络也一样可以Commit，查看历史版本记录，创建项目分支等操作，等网络再次连接上Push到Server端
  - 分布式版本控制系统通常也有一台充当“中央服务器”的电脑，但这个服务器的作用仅仅是用来方便“交换”大家的修改，没有它大家也一样干活，只是交换修改不方便而已
  - Git把内容按元数据方式存储，而SVN是按文件：因为,.git目录是处于自己机器上的一个克隆版的版本库，它拥有中心版本库上所有的东西，例如标签，分支，版本记录等。.git目录的体积大小跟.svn比较相对小很多
  - Git的内容的完整性要优于SVN: GIT的内容存储使用的是SHA-1哈希算法。这能确保代码内容的完整性，确保在遇到磁盘故障和网络问题时降低对版本库的破坏
  - Git下载下来后，在OffLine状态下可以看到所有的Log，SVN不可以
  - Git没有一个全局版本号，而SVN有
  - 在SVN，分支是一个完整的目录。且这个目录拥有完整的实际文件，改一个分支，还得让其他人重新切分支重新下载。而 Git，每个工作成员可以任意在自己的本地版本库开啟无限个分支，只要不合并及提交到主要版本库，没有一个工作成员会被影响
- Push/pull要联网
- 如果Server硬盘坏了怎么办？使用不同公司的硬盘
- git保证完整性

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

ll -a
cd .git/
# HEAD ： 在Git中，用HEAD表示当前版本，也就是最新的提交commitID
# 上一个版本就是HEAD^，上上一个版本就是HEAD^^，当然往上100个版本写100个^比较容易数不过来，所以写成HEAD~100
# HEAD指向的版本就是当前版本，因此，Git允许们在版本的历史之间穿梭，使用命令git reset --hard commit_id
# 穿梭前，用git log可以查看提交历史，以便确定要回退到哪个版本
# 要重返未来，用git reflog查看命令历史，以便确定要回到未来的哪个版本

# 工作区（Working Directory）
工作区有一个隐藏目录.git，这个不算工作区，而是Git的版本库
# 版本库（Repository）
.git
stage（或者叫index）的暂存区，还有Git自动创建的第一个分支master，以及指向master的一个指针叫HEAD
git add		把文件修改添加到暂存区
git commit	把暂存区的所有内容提交到当前分支
# 需要提交的文件修改通通放到暂存区，然后，一次性提交暂存区的所有修改

# 第一次修改 -> git add -> 第二次修改 -> git commit
git commit只负责把暂存区的修改提交了，也就是第一次的修改被提交了，第二次的修改不会被提交
# 查看工作区和版本库里面最新版本的区别
git diff HEAD -- filename
# 每次修改，如果不用git add到暂存区，那就不会加入到commit中

# fetch
本地与远端同步一次，分支不会立马更新，更新需要 git pull origin master
# git pull
更新本地仓库至最新改动，相当于是git fetch + git merge
git pull -r，也就是git pull –rebase，相当于git fetch + git rebase
# push
本地推送到远端
# 推送分支，就是把该分支上的所有本地提交推送到远程库。推送时，要指定本地分支，这样，Git就会把该分支推送到远程库对应的远程分支上
git push origin master
# 要推送其他分支，比如dev，就改成
git push origin dev
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

```shell
# 本地项目推送到git上
mkdir test10
cd test10
ll -a # 没.git
# 创建git仓库
1. git init
	ll -a # .gti已有
	touch 10.txt
	git status
	git ac 'init'
	git status
	# 远端创建目录 git@git.oschina.net:gupaoedu_com_vip/test10.git  项目必须是空的，不包括readme文件
# 远端与本地建立联系
2. git remote add origin git@git.oschina.net:gupaoedu_com_vip/test10.git
	git config --list
3. （git fetch 可选）
   git pull origin master
4. git push -u origin master
```

## checkout

```shell
1. 切新分支 # -b新增
	git checkout -b dev-0416-demo		# 创建并切换到新分支
	git checkout master        			# 切回到master
2. 本地撤销更改（更改了文件内容）
	git checkout .                      # 所有文件
	git checkout 1.txt					# 只1.txt
	
# 丢弃工作区的修改
git checkout -- file
# 总之，就是让这个文件回到最近一次git commit或git add时的状态
# 一种是file自修改后还没有被放到暂存区，现在，撤销修改就回到和版本库一模一样的状态
# 一种是file已经添加到暂存区后，又作了修改，现在，撤销修改就回到添加到暂存区后的状态

场景1：当改乱了工作区某个文件的内容，想直接丢弃工作区的修改时，用命令git checkout -- file。
场景2：当不但改乱了工作区某个文件的内容，还添加到了暂存区时，想丢弃修改，分两步，第一步用命令git reset HEAD <file>，就回到了场景1，第二步按场景1操作。
场景3：已经提交了不合适的修改到版本库时，想要撤销本次提交，参考版本回退一节，不过前提是没有推送到远程库
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

## 分支

```shell
# 列出所有分支，当前分支前面会标一个*号
git branch
# 把dev分支的工作成果合并到master分支上
git checkout master
git merge dev           # 合并指定分支到当前分支

# 删除dev分支
git branch -d dev

# 开发一个新feature，最好新建一个分支；
# 如果要丢弃一个没有被合并过的分支，可以通过git branch -D <name>强行删除。
git branch -D <name>

# 多人协作
首先，可以试图用git push origin <branch-name>推送自己的修改；
如果推送失败，则因为远程分支比你的本地更新，需要先用git pull试图合并；
如果合并有冲突，则解决冲突，并在本地提交；
没有冲突或者解决掉冲突后，再用git push origin <branch-name>推送就能成功！

如果git pull提示no tracking information，则说明本地分支和远程分支的链接关系没有创建，用命令git branch --set-upstream-to <branch-name> origin/<branch-name>。

# 在本地创建和远程分支对应的分支，本地和远程分支的名称最好一致
git checkout -b branch-name origin/branch-name
# 建立本地分支和远程分支的关联
git branch --set-upstream branch-name origin/branch-name
```

## stash

```shell
# 栈，不建议用，可能忘记之前入栈的操作
当未做完功能时要切换到另一个分支，但是不想提交没完成的代码导致生成多次log

# stash功能，可以把当前工作现场“储藏”起来，等以后恢复现场后继续工作
git stash
# 用git status查看工作区，就是干净的（除非有没有被Git管理的文件），因此可以放心地创建分支来修复bug

# 工作现场存到哪去了？用git stash list命令看看
git stash list
# 恢复指定的stash
git stash apply stash@{0}

# 恢复
1. 用git stash apply恢复，但是恢复后，stash内容并不删除，需要用git stash drop来删除
2. 用git stash pop，恢复的同时把stash内容也删了

# 当手头工作没有完成时，先把工作现场git stash一下，然后去修复bug，修复后，再git stash pop，回到工作现场
```

##  merge/rebase

```shell
# merge 合并分支
远端merge    # 远端发起merge request
# 合并其他分支到你的当前分支
git merge <branch>

git merge原理：
	git 会自动根据两个分支的共同祖先commit和两个分支的最新提交进行一个三方合并，然后将合并中修改的内容生成一个新的 commit，简单来说就合并两个分支并生成一个新的提交

git rebase原理：
	两个分支一个master，一个develop，执行git rebase develop时，git 会从两个分支的共同祖先开始提取当前分支（此时是master分支）上的修改，再将 master 分支指向目标分支的最新提交（此时是develop分支）处，然后将刚刚提取的修改应用到这个最新提交后面，如果提取的修改有多个，那git将依次应用到最新的提交后面。将在原始分支上的已提取的commit删除。
	
可以看出merge结果能够体现出时间线，但是r	ebase会打乱时间线
rebase看起来简洁，但是merge看起来不太简洁
最终结果是都把代码合起来了

# 人工修改冲突，然后执行git add <filename>将它们标记为合并成功
# 在合并改动之前，也可以使用git diff <source_branch> <target_branch>命令查看

master——v2
从v2切个分支v3，然后合并v3到v2，会再创建v4，master——v4

# 冲突
1. 本地为主
	在冲突中，删除远端不同的内容
	git ac 'merge dev'
	git push origin dev-0416-demo
	
# rebase
# 本地分支比远程分支超前3个提交，用git log --graph --pretty=oneline --abbrev-commit查看
git rebase
# 最后，通过push操作把本地分支推送到远程
# rebase操作的特点 ：
把分叉的提交历史“整理”成一条直线，看上去更直观。缺点是本地的分叉提交已经被修改过了

# rebase操作可以把本地未push的分叉提交历史整理成直线
# rebase的目的是使得我们在查看历史提交的变化时更容易，因为分叉的提交需要三方对比
```

## tag

```shell
# 标签也是版本库的一个快照，但其实它就是指向某个commit的指针
# tag就是一个让人容易记住的有意义的名字，它跟某个commit绑在一起
# 创建和删除标签都是瞬间完成的

# 首先，切换到需要打标签的分支上
git branch
* dev
  master
git checkout master
# 打新标签
git tag v1.0
# 查看所有标签,标签不是按时间顺序列出，而是按字母排序的
git tag
# 查看标签信息
git show <tagname>
# 删除
git tag -d v0.1
# 创建的标签都只存储在本地，不会自动推送到远程
# 推送某个标签到远程
git push origin <tagname>
# 一次性推送全部尚未推送到远程的本地标签
git push origin --tags
# 要删除远程标签
# 先从本地删除
git tag -d v0.9
# 再远程删除
git push origin :refs/tags/v0.9

# 给之前commit打标签
git log --pretty=oneline --abbrev-commit
git tag v0.9 commit_id

# 标签总是和某个commit挂钩。如果这个commit既出现在master分支，又出现在dev分支，那么在这两个分支上都可以看到这个标签
```

## alias

```shell
git config --list
alias.ac=!git add -A && git commit -m

git config --global alias.st status
git config --global alias.co checkout
git config --global alias.ci commit
	git ci -m "bala bala bala..."
git config --global alias.br branch

# git reset HEAD file可以把暂存区的修改撤销掉（unstage），重新放回工作区。既然是一个unstage操作，就可以配置一个unstage别名
git config --global alias.unstage 'reset HEAD'
git unstage test.py			# 实际执行 git reset HEAD test.py

# 配置一个git last，让其显示最后一次提交信息：
git config --global alias.last 'log -1'

# git lg
git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"

# 每个仓库的Git配置文件都放在.git/config文件中
# 别名就在[alias]后面，要删除别名，直接把对应的行删掉即可
# 而当前用户的Git配置文件放在用户主目录下的一个隐藏文件.gitconfig中
# 配置别名也可以直接修改这个文件，如果改错了，可以删掉文件重新通过命令配置。
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

## 搭建git服务器

```shell
# 自己搭建一台Git服务器作为私有仓库使用
# 需要准备一台运行Linux的机器
# 1. 安装git
	sudo apt-get install git
# 2. 创建一个git用户，用来运行git服务
	sudo adduser git
# 3. 创建证书登录
	收集所有需要登录的用户的公钥，就是他们自己的id_rsa.pub文件，把所有公钥导入到/home/git/.ssh/authorized_keys文件里，一行一个
# 4. 初始化Git仓库：
	先选定一个目录作为Git仓库，假定是/srv/sample.git，在/srv目录下输入命令
		sudo git init --bare sample.git
	Git就会创建一个裸仓库，裸仓库没有工作区，因为服务器上的Git仓库纯粹是为了共享，所以不让用户直接登录到服务器上去改工作区，并且服务器上的Git仓库通常都以.git结尾。然后，把owner改为git：
		sudo chown -R git:git sample.git
# 5. 禁用shell登录：
	出于安全考虑，第二步创建的git用户不允许登录shell，这可以通过编辑/etc/passwd文件完成。找到类似下面的一行：
		git:x:1001:1001:,,,:/home/git:/bin/bash
	改为
		git:x:1001:1001:,,,:/home/git:/usr/bin/git-shell
    这样，git用户可以正常通过ssh使用git，但无法登录shell，因为我们为git用户指定的git-shell每次一登录就自动退出
# 6. 克隆远程仓库：
	现在，可以通过git clone命令克隆远程仓库了，在各自的电脑上运行
		git clone git@server:/srv/sample.git
```



