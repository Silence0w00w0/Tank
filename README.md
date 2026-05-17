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

## 控制

- `WASD` 或方向键：移动
- `Space` 或 `J`：发射
- `P`：暂停 / 继续
- `R`：重开
- `Enter`：从菜单开始或进入下一关

## 第一版功能

- 26x20 固定格子战场，32px tile
- 3 个 JSON 内置关卡，基地四周有砖墙保护
- 单机模式和双人局域网联机模式
- 砖墙、钢墙、水、草、冰五类地形
- 基础型、快速型、重甲型、强弹型四类敌人
- 加速、护盾、强化弹、清屏、加命五类道具
- 基地、生命、分数、最高分、暂停、失败、通关流程
- AI 生成的复古像素素材源图，切分后通过 LibGDX atlas 使用

## 测试

```powershell
.\gradlew.bat test
```
