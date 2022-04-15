# Bikkuri

License under [AGPLv3](LICENSE) | 基于 APGLv3 协议开源

## Build

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

## Usage

> 本程序依赖 `mirai-console` 但并不提供能装载进 `plugins` 的 `.jar` 文件，因为 `plugin` 模式依赖问题太多。请直接使用 cli 方式启动。

使用 `java -jar Bikkuri.jar` 运行 Bikkuri 机器人程序。

首次开启时，可能需要运行两次才能正常启动。

运行后会在运行目录生成 `mirai` 文件夹，用于存放数据。

- [基础配置](./docs/basic.md)
- [审核流程](./docs/audit.md)
- [命令索引](./docs/commands.md)
- [配置介绍](./docs/config.md)

## Contact

如有问题或建议可 <a href="mailto:233hbj@gmail.com" target="_blank">邮件联系我</a>
或 [提 issue](https://github.com/Colerar/bikkuri/issues/new/choose)
