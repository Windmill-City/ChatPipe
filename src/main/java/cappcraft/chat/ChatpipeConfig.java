package cappcraft.chat;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ChatpipeConfig {
    private static Configuration configuration;

    public static void initConfig(File file){
        configuration = new Configuration(file);
        configuration.load();
        load();
        configuration.save();
    }

    public static void load(){
        enable = configuration.getBoolean("enable", "CoolQ", enable, "Enable CoolQ support?");
        use_ws = configuration.getBoolean("use_ws", "CoolQ", use_ws, "Server connect to CoolQ");
        use_reverse_ws = configuration.getBoolean("use_reverse_ws", "CoolQ", use_reverse_ws, "CoolQ connect to server");
        auto_reconnect = configuration.getBoolean("auto_reconnect", "CoolQ", auto_reconnect, "");

        ws_url = configuration.getString("ws_url","CoolQ", "", "" );
        connect_access_token = configuration.getString("connect_access_token","CoolQ", "", "CoolQ connect to server" );
        api_access_token = configuration.getString("api_access_token","CoolQ", "", "Server connect to CoolQ" );

        port = configuration.getInt("ws_reverse_port", "CoolQ", port,0,65535,"Can be the same as minecraft's");
        reconnect_interval = configuration.getInt("reconnect_interval", "CoolQ", reconnect_interval,0,65535,"TimeUnit: ms");
        timeout = configuration.getInt("timeout", "CoolQ", timeout,0,65535,"TimeUnit: second | Close connection when timeout, set to negetive value to disable");

        group_ids = configuration.getStringList("group_ids","CoolQ",group_ids,"Set which group you want to sync chats");
    }
    //CoolQ
    public static boolean enable = false;
    //ws
    public static boolean use_ws = false;
    public static String ws_url = "";
    public static boolean auto_reconnect = true;
    public static int reconnect_interval = 3000;
    //reverse ws
    public static boolean use_reverse_ws = false;
    public static int port = 25565;
    public static String connect_access_token = "";
    public static int timeout = -1;
    public static String api_access_token = "";

    //group_id
    public static String[] group_ids = new String[] {};
}
