**项目简介：**

本项目是基于 **Java+Vue3** 开发、使⽤**微信小程序**和 Web 站点实现的互联网**高并发租房**和**即时通信**项目。项目包含房源管理端和小程序 C 端，涵盖房源管理，租户和房东沟通，房源上下架等整套业务流程。核心技术包括微服务、分布式集群、高并发、分布式对象存储、高速缓存、消息队列、即时通信、解决方案设计。

**技术栈：**

Vue3、Redis、MySQL、RabbitMQ、Spring Cloud、WebSocket、Nacos、MyBatis-Plus、JWT

**项目特色：**

1. 获取房源信息：采用 Redis 多级缓存提升性能，并运用策略模式实现房源的灵活筛选排序；
2. 在线咨询：基于 WebSocket 实现租户与房东的实时通信，利用 RabbitMQ 转发到其他服务器，保证分布式场景下的通信稳定。
3. 无状态登录：基于 JWT 实现无状态登录认证，用户可使用微信以及手机验证码方式登录/注册。
4. 集成外部服务：集成腾讯地图、阿里云短信验证、OSS 对象存储等接口，并通过 Nacos 配置中心实现各个服务配置的动态管理。
5. 多线程处理：使用多线程异步处理方式来获取下拉数据等需多步执行的操作，确保繁琐任务的单机处理效率。
6. 定时任务：通过定时任务自动更新房源状态，并使用 Redisson 分布式锁确保集群环境下的任务唯一执行。

**管理端**
<img width="1600" height="731" alt="image" src="https://github.com/user-attachments/assets/2fb456b7-99a6-437e-9656-b76c35f5748e" />
<img width="1600" height="731" alt="image" src="https://github.com/user-attachments/assets/8ea723fd-0767-425d-993e-46e42a97b0d4" />
<img width="1600" height="731" alt="image" src="https://github.com/user-attachments/assets/c1fe3321-3ce1-4683-8c66-e4fe01e56b1b" />
<img width="1600" height="731" alt="image" src="https://github.com/user-attachments/assets/cf91ea75-4d52-4faa-8084-24fbe41680e0" />

**用户端**

<img width="300" height="638" alt="image" src="https://github.com/user-attachments/assets/cdd8aeeb-f712-4ce2-a07e-9a9358f243b3" />
<img width="300" height="643" alt="image" src="https://github.com/user-attachments/assets/de53ecd6-3727-42df-9a80-febd813c970b" />

<img width="295" height="638" alt="image" src="https://github.com/user-attachments/assets/1d70fa7a-1ac5-4fe6-96b8-f922c3bc0fb5" />
<img width="298" height="642" alt="image" src="https://github.com/user-attachments/assets/f14a15f9-2962-4bcf-908c-eaf930d4a77e" />
<img width="296" height="645" alt="image" src="https://github.com/user-attachments/assets/c6f5ed01-a252-42f9-a76b-8eabf0dd7fbf" />

