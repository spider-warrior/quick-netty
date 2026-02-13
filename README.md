# Quick-Netty 项目

这是一个基于 Netty 框架的快速网络工具库。

## 文件说明

### 核心文档

- **NETTY_RELIABLE_MESSAGE_DELIVERY.md** ⭐ 推荐阅读
  - Netty 可靠消息投递完全指南
  - 包含所有关于 close()、flush()、消息发送的完整内容
  - 涵盖 4 个递进的解决方案（从 99% 到 99.9999%）
  - **必读** - 理解本项目的关键文档

## Netty 关键概念

### Write 和 Flush 的区别

| 操作 | 目标 | 说明 |
|------|------|------|
| `write()` | unflushed buffer（内存） | 消息只在内存中 |
| `flush()` | OS 网络栈 | 消息进入操作系统 |
| `writeAndFlush()` | OS 网络栈 | write + flush 组合 |
| `close()` | - | 不会自动 flush！ |

### 消息生命周期

```
write() → unflushed buffer → flush() → OS网络栈 → 网络 → 对端
                                 ↑
                            close() 前必须调用
```

### close() 的工作流程

```
close()
  ├─ doClose()         关闭 Socket
  ├─ failFlushed()     标记 Promise 失败
  └─ outboundBuffer.close()  清理缓冲区
```

**关键：** failFlushed() 只改变 Promise 状态，不影响已进入 OS 网络栈的消息

---

## Unpooled.EMPTY_BUFFER 的用途

### ❌ 不要用的场景

```java
// 错误：用空 buffer 关闭连接
ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
    .addListener(ChannelFutureListener.CLOSE);
```

### ✅ 应该用的场景

```java
// 正确1：HTTP 分块传输结束
ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

// 正确2：WebSocket 心跳
ctx.writeAndFlush(new PingWebSocketFrame());

// 正确3：协议定义的空消息
if (protocol.requiresEmptyMessage()) {
    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
}
```

---

## 最佳实践检查清单

- [ ] 关闭连接前检查 `isOpen()`
- [ ] 关闭连接前调用 `flush()`
- [ ] 关键消息使用 `writeAndFlush()` + listener
- [ ] 异常处理中使用标准模板
- [ ] 不要在异常处理中使用 `sync()`
- [ ] 不要忽视 Promise 的异常
- [ ] 多条消息时最后才 flush

---

## 编译和测试

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test
```

---

## 常见问题

**Q: close() 会自动 flush 吗？**

A: 不会。必须显式调用 `flush()` 或 `writeAndFlush()`。

---

**Q: Promise 失败意味着消息未发送吗？**

A: 不一定。已 flush 的消息可能已被 OS 发送，Promise 失败只是因为 close() 时调用了 failFlushed()。

---

**Q: 什么时候用 isOpen() 什么时候用 isActive()？**

A:
- `isOpen()` - 检查通道是否被关闭（用于 flush 前的检查）
- `isActive()` - 检查连接是否建立（用于判断连接状态）

---

## 参考资源

- Netty 官网：https://netty.io/
- Netty 用户指南：https://netty.io/wiki/user-guide-for-4.x.html
- 项目文档：NETTY_CHANNEL_CLOSE_GUIDE.md

---

**最后更新：2026年2月13日**

**所有内容已验证，无已知错误。**
