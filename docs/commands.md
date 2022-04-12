# 命令索引

> [返回导航](../README.md)

在控制台输入命令无需斜杠 `/` 前缀，在 QQ 内需要 `/` 前缀。

如果权限不匹配，机器人不会回复。

`<>` 表示必需参数 `[]` 表示可选参数。

## 任何人可用

即包含控制台、群内管理、普通成员、机器人好友都可用。

### bstatus / bikkuri

- 查看 bikkuri 系统状态。包括系统、启动时长、CPU占用、内存占用

### version

- 查看 Bikkuri 版本信息

## 控制台可用

### autologin

- mirai 自带的 QQ 自动登录指令
- `autologin <QQ> <密码>`

### loginbili

- 登录 B 站命令
- `autologin <qr|sms|pwd>` 选项分别对应，手机扫码登录、短信验证登录、密码登录
- 推荐使用扫码登录

## 群内普通成员可用

### sign

- 别名 `s` `验证`。此命令特别设置了触发关键词：**验证**。无需输入 `/` 前缀。(实际上正则匹配长这样：`(["“”]?(开始)?(验证|驗證)["“”]?|^.+/验证$)`)
- 此命令仅在 `/config switch` 开启时可用

## 群内管理可用

### approve

- 别名 `ap`
- 该指令用于查询机器人批准了的群成员
- `/approve queryqq <QQ|At>` 可查询对应 QQ 的记录
- `/approve querybili <B站UID>` 可查询对应 B 站 UID 的记录

### backup

- `/backup run` 用于储存所有群员列表
- 会在后台 `mirai/data/me.hbj.bikkuri/member_backup/群号/日期和时间.csv` 下，生成逗号分隔符表格
- 备份过程非常快，即使是几千人的大群也只需要几秒钟

### blocklist
    
- `/blocklist` 屏蔽指令，缩写 `/block` `/b`
- `/block help` 显示帮助页面
- `/block list/ls` [页码] 查看当前屏蔽列表
- `/block add <At|QQ号>` 添加某人到屏蔽列表
- `/block kick <At|QQ号>` 添加某人到屏蔽列表，同时移出本群
- `/block ban <At|QQ号>` 添加某人到屏蔽列表，移出本群，同时使用QQ的拉黑功能
- `/block remove/rm <At|QQ号>` 将某人移出屏蔽列表

### checklogin

- `/checklogin` 缩写 `/cl` 检查机器人 B 站登录状态

### config 

- `/config` 配置指令，缩写 `/c`
- `/config list` 查看当前配置
- `/config switch` 开启机器人
- `/config target <目标群号>` 绑定目标群
- `/config bind <UID>` 绑定目标 UP 主，也就是需要审核的牌子的主播的 **UID**
- `/config autokick <时间长度>` 设置自动踢出的时间, 以秒为单位, `0` 表示不会自动踢出, 推荐 `300` 秒
- `/config mode <recv|send>` 设置验证器模式

> [返回导航](../README.md)
