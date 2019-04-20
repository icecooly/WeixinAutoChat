# WeixinAutoChat
微信机器人

```java
MessageHandler handler = new MessageHandler();
WeixinAutoChat chat = new WeixinAutoChat(handler);
handler.chat = chat;
chat.login();
```

Download
--------

Download Jar or grab via Maven:
```xml
<dependency>
  <groupId>com.github.icecooly</groupId>
  <artifactId>WeixinAutoChat</artifactId>
  <version>1.0.1</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.github.icecooly:WeixinAutoChat:1.0.1'
```

----------------

常见的场景

* 定时发消息给你的朋友
* 自动聊天发文本，表情，图片等
* 自动加好友
* 逗人玩
* ...
