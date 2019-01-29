package cappcraft.chat;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static cappcraft.chat.ChatPipe.proxy;

public class Config {
    private static Configuration config;

    //WebSocketServer
    public static int timeout;
    public static boolean useWebsocketServer = true;
    public static String websocketPath = "/";
    public static int Port = 25565;

    public static void initConfig(File file){
        config = new Configuration(file);
        load();
    }
    private static void load(){
        config.load();
        String WebsocketServer = "WebsocketServer";
        useWebsocketServer = config.getBoolean("enable",WebsocketServer, useWebsocketServer,"WebsocketServer");
        Port = config.getInt("Port",WebsocketServer, Port,0,65535
                ,"WebSocketServer's port,you can use minecraft's port");
        websocketPath = config.getString("websocketPath",WebsocketServer,websocketPath
                ,"(eg: Url = 127.0.0.1/ws Path = /ws)");
        timeout = config.getInt("timeout",WebsocketServer,300,-1,Integer.MAX_VALUE,
                "Set connection timeout time here set to -1 to disable" +
                "/n(you can send PingWebSocketFrame to keep connection and check if server is still alive)");
        config.save();
    }

    public static void reload(){
        load();
        proxy.startWebsocketServer();
    }
}
