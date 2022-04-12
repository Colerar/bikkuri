# 配置介绍

> [返回导航](../README.md)

在 `mirai/data/me.hbj.bikkuri` 下有 `_General.yml` 配置。

一般情况无需特别配置，看不懂就请保持默认。

标有 !! 为可能有用的。

```yaml
retryTimes: 5 #  在遇到 IO 错误时的重试次数
joinRequiredLevel: 21 # 进群需要的粉丝牌子等级
keygen: # !! 验证码设置
  # pattern 默认使用了 base58 所用的字符
  # 可自行修改为数字：如 1234567890
  pattern: 123456789QWERTYUPASDFGHJKLZXCVBNMqwertyuiopasdfghjkzxcvbnm
  # 验证码长度
  length: 6
  # 验证码超时时间
  timeout: 300000
time: # 重连和任务扫猫相关 均为 ms 如无特别需求不用修改
  timeout: 5000 # 超时
  autoApprove: 2000 # 自动批准的刷新时间
  autoKick: 5000 # 自动踢出的刷新时间
  messageNoticeBetweenKick: 500 # kick 之前消息的延迟
  guardJobScan: 10000 # 舰长更新任务扫描间隔
  guardCleanup: 300000 # 舰长清除间隔
  reconnectNoResponse: 35000 # 多长时间没收到直播消息流重连
  reconnectIOExceptionMs: 1000 # 对于直播消息流, IO 错误时的重连延迟
  reconnectNoInternetMs: 5000 # 对于直播消息流, 无网络时的重连延迟
randomReply: # !! 随机回复延迟, 最小延迟不会超过 0
  mode: MATH_LOG # 可用 NONE FIXED BETWEEN SCALE MATH_LOG
  fixedValue: 100 # FIXED 模式：固定时间延迟
  betweenRange: # BETWEEN 在 first 和 second 之间随机
    first: 100
    second: 1000
  scaleCoefficient: # SCALE 字符 * 系数, 系数在 first 和 second 之间随机
    first: 15.0
    second: 20.0
  log: # MATH_LOG 
    # 基础延迟: 系数 * log_{底数} 长度^{指数} + 常数 + 抖动
    # 抖动: -jitter 到 +jitter 之间随机
    # 默认配置已经相当可用
    coefficient: 20.0 # 系数
    base: 2.0 # 底数
    pow: 10 # 指数
    constant: 300.0 # 常数
    jitter: 300 # 抖动
```
