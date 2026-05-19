package com.silence.tank;

public final class Texts {
    public static final String GAME_TITLE = "坦克大战";
    public static final String HOST_TITLE = "坦克大战 - 主机";
    public static final String CLIENT_TITLE = "坦克大战 - 客户端";

    public static final String LEVEL = "关卡";
    public static final String SCORE = "分数";
    public static final String HIGH_SCORE = "最高";
    public static final String LIVES = "生命";
    public static final String CONTROLS = "WASD/方向键移动  空格/J开火  P暂停  R重开  Enter确认";

    public static final String CONNECTING = "正在连接";
    public static final String PAUSED = "已暂停";
    public static final String PAUSED_SUBTITLE = "按 P 继续";
    public static final String LEVEL_CLEAR = "关卡完成";
    public static final String LEVEL_CLEAR_SUBTITLE = "按 Enter 进入下一关";
    public static final String GAME_OVER = "游戏失败";
    public static final String GAME_OVER_SUBTITLE = "按 R 或 Enter 重开";
    public static final String VICTORY = "通关胜利";
    public static final String VICTORY_SUBTITLE = "按 R 或 Enter 再玩一次";
    public static final String MENU_LOCAL = "按 Enter 或空格开始";
    public static final String MENU_HOST = "主机：客户端加入后按 Enter 开始";
    public static final String MENU_CLIENT = "客户端：主机准备后按 Enter 开始";

    public static final String HOST_WAITING = "主机等待连接，端口 ";
    public static final String HOST_CLIENT_DISCONNECTED = "主机：客户端已断开";
    public static final String HOST_CONNECTED = "主机已连接 ";
    public static final String HOST_NETWORK_ERROR = "主机网络错误：";
    public static final String HOST_FAILED = "主机启动失败，端口 ";
    public static final String CLIENT_CONNECTING = "客户端正在连接 ";
    public static final String CLIENT_DISCONNECTED = "客户端已断开";
    public static final String CLIENT_CONNECTED = "客户端已连接 ";
    public static final String CLIENT_HOST_DISCONNECTED = "客户端：主机已断开";
    public static final String CLIENT_RETRYING = "客户端重试连接 ";

    public static final String FONT_CHARS = String.join("",
            GAME_TITLE,
            HOST_TITLE,
            CLIENT_TITLE,
            LEVEL,
            SCORE,
            HIGH_SCORE,
            LIVES,
            CONTROLS,
            CONNECTING,
            PAUSED,
            PAUSED_SUBTITLE,
            LEVEL_CLEAR,
            LEVEL_CLEAR_SUBTITLE,
            GAME_OVER,
            GAME_OVER_SUBTITLE,
            VICTORY,
            VICTORY_SUBTITLE,
            MENU_LOCAL,
            MENU_HOST,
            MENU_CLIENT,
            HOST_WAITING,
            HOST_CLIENT_DISCONNECTED,
            HOST_CONNECTED,
            HOST_NETWORK_ERROR,
            HOST_FAILED,
            CLIENT_CONNECTING,
            CLIENT_DISCONNECTED,
            CLIENT_CONNECTED,
            CLIENT_HOST_DISCONNECTED,
            CLIENT_RETRYING,
            "补给线水闸钢铁巢穴缺少内置运行时用法示例自定义端口私有网络允许防火墙"
    );

    private Texts() {
    }
}
