# Bikkuri

License under [AGPLv3](LICENSE) | 基于 APGLv3 协议开源

## 下载

可前往 [Release](https://github.com/Colerar/bikkuri/releases/latest) 页面下载最新稳定版构建。或前往 [Actions](https://github.com/Colerar/bikkuri/actions) 页面下载开发版构建。

## 构建

- 需要 JDK 17

<details>
<summary>打包成 cli 形式</summary>

1. Clone 本仓库
2. 运行命令 `./gradlew :installDist` or `gradlew.bat :intallDist`
3. 等待一会儿, 可在 `build/install/` 下看到 `Bikkuri`
4. 运行 `Bikkuri` 下的 `./bin/Bikkuri` 或 `./bin/Bikkuri.bat`, 检查是否能够启动
5. Done

</details>

<details>
<summary>打包成 jar 形式</summary>

1. Clone 本仓库
2. 运行命令 `./gradlew :shadowJar` or `gralew.bat :shadowJar`
3. 等待一会儿, 可在 `build/libs/` 下看到 `Bikkuri-版本号-all.jar`
4. 可以改名为简短形式 `mv bikkuri-版本号-all.jar bikkuri.jar`
5. `java -jar bikkuri.jar` 检查是否能够启动
6. Done

</details>

## 使用

使用 `java -jar Bikkuri.jar` 运行 Bikkuri 机器人程序。

首次会弹出提醒，依照提示在 `./config/auto-login.yml` 修改配置。之后即可运行。

在控制台输入 `/help` 查看命令，控制台或群管理身份下，具体命令后跟 `--help` 可查看详细帮助。

- [基础配置](./docs/basic.md)
- [审核流程](./docs/audit.md)

## 联系

如有问题或建议可 <a href="mailto:233hbj@gmail.com" target="_blank">邮件联系我</a>
或 [提 issue](https://github.com/Colerar/bikkuri/issues/new/choose)
