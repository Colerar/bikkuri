# Bikkuri

License under [AGPLv3](LICENSE) | 基于 APGLv3 协议开源

[/src/main/resources/images](/src/main/resources/images) 中的图片并未按 AGPLv3 许可, 这些作品的权利被保留。

## Build

- 需要 JDK 17

### 打包成 cli 形式

1. Clone 本仓库
2. 运行命令 `./gradlew :installDist` or `gralew.bat :intallDist`
3. 等待一会儿, 可在 `build/install/` 下看到 `Bikkuri`
4. 运行 `Bikkuri` 下的 `./bin/Bikkuri` 或 `./bin/Bikkuri.bat`, 检查是否能够启动
5. Done

## Usage

> 本程序依赖 `mirai-console` 但并不提供能装载进 `plugins` 的 `.jar` 文件，因为 `plugin` 模式依赖问题太多。请直接使用 cli 方式启动。

需要准备 1 个用于发送验证码的 B 站帐号，1 个 QQ 机器人帐号，1 个审核群和 1 个目标群。审核群不应该有普通成员。

- 可使用 `mirai-console` 内建指令 `autologin` 设置自动登录帐号。
- 可使用 `loginbili` 登录 B 站帐号。<br></br>
- 需要将机器人拉入审核群和目标群，并将其设为管理。同时将两个群都设为需要管理员审核才能进入。并关闭小于 100 人自动同意（以及类似选项）。<br></br>
- 在审核群 `/config switch` 开启机器人（`/config` 指令需要 管理/群主 权限）
- `/config target <目标群号>` 绑定目标群
- `/config bind <UID>` 绑定目标 UP 主，也就是需要审核的牌子的主播的 UID。
- `/config autokick <时间长度>` 设置自动踢出的时间, 以秒为单位, `0` 表示不会自动踢出, 推荐 `300` 秒

之后可将审核群号公开, 或进行测试（发送`/s` `/sign` 或者 `/验证`）。

**Optional**

- 建议替换 [/src/main/resources/images](/src/main/resources/images) 中的图片并重新编译打包。

**审核流程概览**

1. 申请者通过公开群号等途径申请进入审核群
2. 等待机器人同意申请
   - 若当前无人审核(无普通群成员): 直接放入
   - 若当前有人审核(存在普通成员): 加入队列，等候前方审核完毕
3. 入群后机器人会自动 @ 提醒输入命令开始审核
4. 要求发送 UID 验证牌子情况
   - 验证成功: 进入下一步
   - 验证失败: 根据失败情况发送提示
5. 通过B站帐号发送验证码，等待回复
   - 验证成功: 进入下一步
   - 验证失败: 根据失败情况发送提示
6. 机器人将发送提示，并私聊群号码
7. 等待申请者申请目标群后，自动同意并踢出审核群。

全局有一个超时踢出计时器，根据 `/config autokick` 自动踢人。

如有问题或建议可<a href="mailto:233hbj@gmail.com" target="_blank">邮件联系我</a>
