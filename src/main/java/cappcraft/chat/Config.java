package cappcraft.chat;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    private static Configuration config;

    //WebSocketServer
    public static boolean useWebsocketServer = true;
    public static boolean checkPath = false;
    public static String websocketPath = "/";
    public static int Port = 25565;

    public static void initConfig(File file){
        config = new Configuration(file);
        config.load();
        String WebsocketServer = "WebsocketServer";
        useWebsocketServer = config.getBoolean("enable",WebsocketServer, useWebsocketServer,"WebsocketServer");
        Port = config.getInt("Port",WebsocketServer, Port,0,65535
                ,"WebSocketServer's port,you can use minecraft's port");
        checkPath = config.getBoolean("checkPath",WebsocketServer,checkPath
                ,"Block connections of which request url doesn't start with the path following(eg: Url = 127.0.0.1/ws Path = /ws)");
        websocketPath = config.getString("websocketPath",WebsocketServer,websocketPath
                ,"(eg: Url = 127.0.0.1/ws Path = /ws)");
        config.save();
    }
}
