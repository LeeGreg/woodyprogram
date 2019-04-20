```java
//Execution default-war of goal org.apache.maven.plugins:maven-war-plugin:2.1.1:war failed  ... /efastFR/lib不存在
//解决，直接在efastFR模块下加lib目录即可

// 日志要弄明白
// MDC

// 当报错时，查看控制台的日志，可以将日志级别调低点以查看更多的信息，包括SQL语句以及参数
// DEBUG级别

<dependency>
            <groupId>com.arcsoft</groupId>
            <artifactId>face</artifactId>
            <version>2.1</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/../efastFR/libs/arcsoft-sdk-face-2.1.0.0.jar</systemPath>
        </dependency>
        
  
远程debug
1. jpda 6004
   Java_OPT ...6004
2. 外部telnet端口能通
3. 服务器以/bin/catalina.sh jpda start方式启动
	-bash-4.2$ cd bin/
	-bash-4.2$ ./catalina.sh jpda start

URL urlInfo = new URL(imgUrl);
File file = new File(urlInfo.toURI().getPath());

// 将MultipartFile转为File
private File getFileByMultipartFile(MultipartFile multipartFile) {
  if(multipartFile != null) {
    CommonsMultipartFile cf= (CommonsMultipartFile)multipartFile;
    DiskFileItem fi = (DiskFileItem)cf.getFileItem();
    if(fi != null) {
      return fi.getStoreLocation();
    }
  }
  return null;
}

BufferedImage image = ImageIO.read(inputStream);
image可能为空
```



# 人脸

## 技术

* 引入封装的依赖`com.arcsoft`、`face`、`2.0`
* 配置文件中添加人脸配置信息`appId`、`key`、以及`timeout`，官网申请
* 在`ServletContextListener`实现类中调用人脸识别工具类的init(appid,key)激活人脸识别引擎
* 人脸识别工具类中，根据人脸图片（byte数组）生成特征文件方法、比对人脸相似度方法（之前生成的人脸图片和新拍的人脸图片对比，返回一个0-100之间整数）

## App

* Android和IOS的App中嵌入人脸识别SDK
* 如果用户使用人脸，App将根据用户人脸照片生成人脸特征，然后调用接口将人脸照片和人脸特征上传到OSS上存储，返回存储相对路径，绑定房间时将这两个路径一起上传到平台审核

## 平台

* 生成人脸特征接口
  * 根据传入的人脸照片生成人脸特征文件，将人脸照片和人脸特征存入redis并设置有效期，返回相应的key值（文件名）-imgId、faceId
* 比对人脸相似度接口
  * 根据传入的faceId从redis中取出人脸特征和根据传入的人脸照片生成的人脸特征进行比对，返回比对结果（0-100之间整数）

* 审核通过用户
  * 解析并存储读取的身份证信息（包括证件号、姓名、籍贯等）
  * 如果人脸有效期值不为null，设置有效期值（开始-结束）（一个月、三个月等），启始时间为审核时间
  * 如果有人脸照片（传入的faceId不为null）
    * App提交的人脸审核和平台修改提交的人脸审核，通过和前端约定faceId格式来判断
      * 设置人脸图片和特征在OSS上存储的路径imgPath、facePath，使用人脸属性为1，人脸照片状态为审核通过
      * 平台修改人脸后的审核（是生成人脸特征接口返回的faceId格式）
        * 获取generateFormId，如果不为null，则App提交就有人脸，然后平台修改了人脸
          * 根据ImgId、faceId从redis中获取人脸图片和特征，然后调用阿里OSS工具类方法(输入流,文件名,存储路径)存入相应的OSS目录中
          * (文件路径及名称,companyCode,formId,groupId)将人脸照片和特征信息存入附件表单表和附件表，附件表单表中formId和generateFormId对应、groupId来对应不同状态的人脸审核（待审核、审核通过、审核驳回，每条绑定信息的三个状态值分别最多只有一个），人脸特征的groupId为face
          * 根据generateFormId、groupId为待审核删除app提交过来待审核的附件信息
        * generateFormId为null，则app没有提交人脸，平台给用户新增
          * 设置generateFormId，和上面一样（文件存入OSS、文件信息添加到两个附件表）
      * App提交的人脸审核
        * 根据generateFormId、groupId，将人脸特征groupId从待审核改为审核通过
        * 通过generateFormId和groupId为face从附件表中找到人脸特征在OSS上的路径
  * 如果没有人脸照片
    * 使用人脸属性为0，人脸照片状态为无照片
  * 更新用户绑定信息（性别、人脸有效期、人员居住类型、生日、generateFormId、证件姓名/编码、人脸图片路径）
  * 生成随机6位整数的紧急密码，存入紧急密码表（和绑定信息表关联）
  * 更新用户绑定状态信息（基本信息审核通过、人脸照片状态、紧急密码等）
  * 获取网易云账号
    * 每个用户分配一个网易云账号，如果绑定多个房间，则共用同一个网易云账号
    * 根据注册时的userId和app类型去网易云账号表中查找，如果没有，则分配网易云账号
      * 网易账号表和网易账号关联表（通过网易账号表id关联），查询出网易云账号关联表中没关联的网易云账号信息
        * 如果为空，则调用网易云工具类中方法来生成网易云账号，返回并保存到这两个表中
        * 如果不为空，则返回第一个并保存到网易云账号关联表中
    * 如果有则直接返回
  * 获取需推送的门口机
    * 通过companyCode获取所有社区节点信息（单表中，上下级节点通过parentId关联，根节点parentId为0）
    * 遍历所有节点，构造社区树（遍历，如果parentId为0，则返回，否则找到该节点的上一级节点并设置其子节点为该节点），然后根据绑定房间的areaId，找到房间节点到根节点的节点路径（所需推送节点）
      * 需推送app用户信息（基本信息、网易云账号）
      * 推送紧急密码
      * 推送用户ID、人脸特征OSS地址及有效期
* 审核拒绝
  * 如果存在人脸照片，删除之前审核拒绝的、将groupId从待审核改为审核拒绝、照片状态改为审核拒绝、基本信息审核拒绝，更新绑定状态表
* 用户冻结/解冻
  * 将用户绑定状态改为冻结/审核通过
  * 将用户的人脸特征（如果有使用人脸）、网易云账号推送到门口机删除/新增（和门口机定义操作状态新增、修改、删除 ）
* 用户解绑
  - 将用户绑定状态改为解绑
  - 将用户的人脸特征（如果有使用人脸）、网易云账号（判断没有其他绑定关系）推送到门口机删除
* 通过易销盒子读取身份证信息
  * 获取tokenKey：根据易销盒子的配置信息（serverUrl、username、passward），通过httpClient调用，返回TokenKey
    * 创建默认的httpClient实例
    * 创建httppost并设置Header、Entity属性值以及连接超时等配置
    * httpClient执行httppost返回结果
  * 获取读卡器信息：根据读卡器的（CardUrl）和TokenKey，通过httpClient调用，返回读卡器信息
  * 获取身份证信息：根据读卡器的（ServerUrl）和读卡器信息，通过httpClient调用，返回身份证信息
  * 返回的身份证信息有固定格式，根据格式解析得到相关数据
* 后台新增用户
  * 用户名UUID、generateFormId为UUID、绑定状态为已绑定、人脸照片状态为审核通过、设置人脸有效期
  * 平台页面会先调用根据人脸照片生成人脸特征接口，将人脸照片和特征存入redis，返回相应的key：imgId、faceId
  * 从redis中取出人脸照片和特征，上传到OSS；并将人脸照片和人脸特征相关信息存入附件及附件表单表（generateFormId、groupId-人脸特征为face、人脸照片为userPicture））
  * 将用户信息、人脸特征OSS路径推送给相关门口机
* 信息管理
  * 当有人脸照片时（根据前端faceId是否为null来判断）
    * 如果人脸照片修改过，则faceId为生成特征时UUID格式，否则不是该格式
      * 如果generateFormId为null，则说明之前没有人人脸照片，则UUID生成generateFormId
      * 根据前端传递的faceId、imgId，从redis中找到对应人脸特征和人脸图片，上传到OSS，然后，存入附件表和附件表单表（groupId分别为userPicture、face）
      * 
    * 如果没有修改过，则判断人脸照片状态不能是审核拒绝的状态
    * 人脸照片状态为审核通过
  * 没有人脸照片时，人脸照片状态为无照片
  * 更新用户基本信息、绑定状态

## 门口机

