# Fun

* 客户端与服务端双向通信
* 客户端登录
* 客户端与服务端收发消息
* 构建客户端与服务端Pipeline
* 拆包粘包
* 热插拔客户端身份校验
*  客户端互聊
* 群聊发起与通知
* 群聊成员管理：加入、退出、获取成员列表
* 群聊消息的收发
* 心跳与空闲检测

# Util

* IDUtil
* SessionUtil

# Session

* Session

# Attribute

* Attributes

# Serialize

* Serializer
* SerializerAlgorithm
* JsonSerializer

# Codec

* Spliter
* PacketDecoder
* PacketEncoder
* PacketCodecHandler

# Protocol

- Packet
- PacketCodec

## command

* command

## request

* CreateGroupRequestPacket
* GroupMessageRequestPacket
* HeartBeatRequestPacket
* JoinGroupRequestPacket
* ListGroupMembersRequestPacket
* LoginRequestPacket
* LogoutRequestPacket
* MessageRequestPacket
* QuitGroupRequestPacket

## response

* CreateGroupResponsePacket
* GroupMessageResponsePacket
* HeartBeatResponsePacket
* JoinGroupResponsePacket
* ListGroupMembersResponsePacket
* LoginResponsePacket
* LogoutResponsePacket
* MessageResponsePacket
* QuitGroupResponsePacket

# Handler

* IMIdleStateHandler

# Server

* NettyServer

## Handler

* AuthHandler
* CreateGroupRequestHandler
* GroupMessageRequestHandler
* HeartBeatRequestHandler
* IMHandler
* JoinGroupRequestHandler
* ListGroupMembersRequestHandler
* LoginRequestHandler
* LogoutRequestHandler
* MessageRequestHandler
* QuitGroupRequestHandler

# Client

* NettyClient

## console

* ConsoleCommand

### command

* ConsoleCommandManager
* CreateGroupConsoleCommand
* JoinGroupConsoleCommand
* ListGroupMembersConsoleCommand
* LoginConsoleCommand
* LogoutConsoleCommand
* QuitGroupConsoleCommand
* SendToGroupConsoleCommand
* SendToUserConsoleCommand

## handler

* CreateGroupResponseHandler
* GroupMessageResponseHandler
* HeartBeatTimerHandler
* JoinGroupResponseHandler
* ListGroupMembersResponseHandler
* LoginResponseHandler
* LogoutResponseHandler
* MessageResponseHandler
* QuitGroupResponseHandler







