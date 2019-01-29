package cappcraft.chat;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    private static Configuration config;

    //WebSocketServer
    public static boolean useWebsocketServer = true;
    public static String websocketPath = "/";
    public static int Port = 2556;

    public static void initConfig(File file){
        config = new Configuration(file);
        config.load();
        String WebsocketServer = "WebsocketServer";
        useWebsocketServer = config.getBoolean("enable",WebsocketServer, useWebsocketServer,"WebsocketServer");
        Port = config.getInt("Port",WebsocketServer, Port,0,65535
                ,"WebSocketServer's port,you can use minecraft's port");
        websocketPath = config.getString("websocketPath",WebsocketServer,websocketPath
                ,"Block connections of which request url doesn't start with this path(eg: Url = 127.0.0.1/ws Path = /ws)");
        config.save();
    }
}
