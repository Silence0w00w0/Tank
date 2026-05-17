# Tank

复古像素风的 Java LibGDX 桌面版坦克大战。

## 运行

```powershell
.\gradlew.bat lwjgl3:run
```

也可以在 IntelliJ IDEA 中打开根目录的 `build.gradle`，然后运行 `lwjgl3` 模块的 `run` 任务。

## 局域网双人

主机控制 P1，客户端控制 P2。两台机器需要在同一局域网内，并允许 TCP `54555` 端口通过防火墙。

主机：

```powershell
.\gradlew.bat lwjgl3:run --args="--host"
```

客户端：

```powershell
.\gradlew.bat lwjgl3:run --args="--connect 主机IP"
```

自定义端口：

```powershell
.\gradlew.bat lwjgl3:run --args="--host --port 54556"
.\gradlew.bat lwjgl3:run --args="--connect 主机IP --port 54556"
```

## 免 Java 便携包

生成 Windows 便携版：

```powershell
.\gradlew.bat lwjgl3:packagePortable
```

产物：

```text
lwjgl3\build\distributions\Tank-Windows-Portable.zip
```

把这个 ZIP 发给另一台 Windows 电脑。对方解压后不需要源码、Gradle、IDEA 或已安装 Java：

- 单机：双击 `Tank-Single.bat`
- 主机：双击 `Tank-Host.bat`
- 客户端：运行 `Tank-Client.bat 主机IP`
- 自定义端口：`Tank-Host.bat --port 54556`，客户端运行 `Tank-Client.bat 主机IP --port 54556`

## 控制

- `WASD` 或方向键：移动
- `Space` 或 `J`：发射
- `P`：暂停 / 继续
- `R`：重开
- `Enter`：从菜单开始或进入下一关

## 第一版功能

- 26x20 固定格子战场，32px tile
- 3 个 JSON 内置关卡，基地贴底边并由 5 格砖墙保护
- 单机模式和双人局域网联机模式
- 砖墙、钢墙、水、草、冰五类地形
- 基础型、快速型、重甲型、强弹型四类敌人
- 玩家复活后自身无敌 3 秒，基地 5 格保护墙同步免伤 3 秒
- 敌人刷新开局较慢，随本关时间推进逐渐加快
- 道具由击杀敌方坦克后随机掉落，15 秒未拾取会消失，掉落率和强力道具池随关卡递增
- 加固基地道具会把基地 5 格保护墙强化为钢墙
- 我方子弹与敌方子弹相撞会互相抵消
- 普通子弹敌我同速，只有玩家强化弹会获得更快弹速
- 关卡结束页必须按 `Enter` 进入下一关，开火键和等待不会跳过
- 基地、生命、分数、最高分、暂停、失败、通关流程
- AI 生成的复古像素素材源图，切分后通过 LibGDX atlas 使用

## 测试

```powershell
.\gradlew.bat test
```
