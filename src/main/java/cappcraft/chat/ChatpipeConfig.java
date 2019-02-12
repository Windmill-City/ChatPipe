package cappcraft.chat;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config(modid = ChatPipe.MODID)
public class ChatpipeConfig {
    //CoolQ
    @Comment(value = "Enable CoolQ support?")
    public static boolean enable = false;
    //ws
    @Comment(value = "Server connect to CoolQ")
    public static boolean use_ws = false;
    public static String ws_url = "";
    public static boolean auto_reconnect = true;
    @Comment(value = "TimeUnit: ms")
    public static int reconnect_interval = 3000;
    //reverse ws
    @Comment("CoolQ connect to server")
    public static boolean use_reverse_ws = false;
    @Comment(value = "Can be the same as minecraft's")
    public static int port = 25565;
    @Comment(value = "CoolQ connect to Server")
    public static String connect_access_token = "";
    @Comment(value = "TimeUnit: second | Close connection when timeout, set to negetive value to disable")
    public static int timeout;
    @Comment(value = "Server connect to CoolQ")
    public static String api_access_token = "";

    //group_id
    @Comment(value = "Set which group you want to sync chats")
    public static String[] group_ids = new String[] {};
}
