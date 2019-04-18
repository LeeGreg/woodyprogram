# Git

## 本地创建分支和远程非master分支关联

```java
/Users/dingyuanjie/Documents/code/work/yunduijiang [git::master]
// git checkout -b 本地分支名称 origin/远程分支名称
> git checkout -b face2.0 origin/face2.0
  Branch 'face2.0' set up to track remote branch 'face2.0' from 'origin'.
Switched to a new branch 'face2.0'
/Users/dingyuanjie/Documents/code/work/yunduijiang [git::face2.0]
> git status
On branch face2.0
Your branch is up to date with 'origin/face2.0'.
nothing to commit, working tree clean
```

```java
// 方法二
// git clone -b 分支名称 仓库地址
> git clone -b face2.0 http://139.224.22.150:7803/cloudtalk/lianzhiyun.git
> git status
On branch face2.0
Your branch is up to date with 'origin/face2.0'.
nothing to commit, working tree clean
```



