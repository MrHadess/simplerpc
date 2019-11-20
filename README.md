# SimpleRPC

## 具备跨网通信的RPC通信框架
### 基于WebSocket + Json底层进行数据交换通信，使用Netty + Gson构建实现


### 优势：
#### 1 针对跨网环境进行框架针对性设计
#### 2 较完善的断线重连机制(应对多种通信环节的断线重连)，有效规避因暂时性断网导致即使网络恢复，服务仍然不可用
#### 3 容易上手，简单的API便可简单实现RPC功能
#### 4 多样化初始化及启动，支持CLI or Code 进行服务启动，配置支持 Code配置 or Xml文件加载
#### 6 支持验证码验证通信，及SSL加密通信。可拒绝非法连接接入，有效避免非法调用以及数据泄露问题
