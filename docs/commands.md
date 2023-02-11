# 命令索引

> [返回导航](../README.md)

在控制台输入命令无需斜杠 `/` 前缀，在 QQ 内需要 `/` 前缀。

如果权限不匹配，机器人不会回复。

`<>` 表示必需参数 `[]` 表示可选参数。

## 任何人可用

即包含控制台、群内管理、普通成员、机器人好友都可用。

### bstatus / bikkuri

- 查看 bikkuri 系统状态。包括系统、启动时长、CPU占用、内存占用等

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
- `/backup task <cron表达式>` 用于启用定时任务，比如 `/backup task 0|17|1/3|*|*`，表达式使用UTC时间，所以对应每3天的凌晨1点，具体 cron
  表达式的写法可以参照[维基百科 - Cron](https://zh.wikipedia.org/wiki/Cron)，也可直接使用[在线生成工具](https://tool.lu/crontab/)。记得把空格换成竖线 `|`。
- 会在后台 `mirai/data/me.hbj.bikkuri/member_backup/群号/日期和时间.csv` 下，生成逗号分隔符表格
- 备份过程非常快，即使是几千人的大群也只需要几秒钟

### blocklist

- `/blocklist` 屏蔽指令，缩写 `/block` `/b`
- `/block help` 显示帮助页面
- `/block list/ls [页码]` 查看当前屏蔽列表
- `/block add <At|QQ号>` 添加某人到屏蔽列表
- `/block kick <At|QQ号>` 添加某人到屏蔽列表，同时移出本群
- `/block ban <At|QQ号>` 添加某人到屏蔽列表，移出本群，同时使用QQ的拉黑功能
- `/block remove/rm <At|QQ号>` 将某人移出屏蔽列表

**block link**

- 本指令用于同步群之间的屏蔽列表，类似于 Linux 上的软链接，link 后的群的所有操作都会 redirect 到对应的群
- 操作该指令，操作者需要是涉及到的每一个群的管理, 如 update 就需要是本群、更新前链接到的群、更新后链接到的群，三群的管理。
- `b link <add> <目标群号>` 添加拦截名单
- `b link <rm|remove>` 移除当前链接
- `b link <upd|update> <新链接群号>` 更新当前链接
- `b link <now|see>` 查看当前链接到的群

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
- `/config queue <时间长度>` 设置同时最多审核多少人。默认为 1。
- `/config recall <撤回时长>` 设置多久后撤回群号消息，默认为 0，即不撤回。

### joinlimit

- `/joinlimit` 进群次数限制命令，缩写 `/limit`
- `/limit set <次数>` 设置限制
- `/limit rm/reset <At|Q号>` 重置某人的进群限制

### mute

- `/mute <At|QQ号> <时间表达式>` 禁言某人
- `/unmute <At|QQ号>` 解禁某人

表达式介绍：

支持 年 月 日 时 分 秒，最大禁言时长为一个月，最小禁言精度为 1 秒。

如果没有单位，默认为秒。

如：

- `12小时` `1小时50分钟20秒` `1天50小时` `1000秒` `1月` `1年`
- `12h` `1h50m20s` `1d50h` `1000s` `1mon` `1y`

> [返回导航](../README.md)
