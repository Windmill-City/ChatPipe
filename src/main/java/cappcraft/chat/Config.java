package cappcraft.chat;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {
    private static Configuration configuration;

    //WebSocketServer
    public static String websocketPath = "/";
    public static int Port = 25564;
    public static boolean Enable_WebsocketServer = true;

    public static void initConfig(File file){
        configuration = new Configuration(file);
        configuration.load();
        String WebSocketServer = "WebSocketServer";
        Enable_WebsocketServer = configuration.getBoolean("Enable_WebSocketServer",WebSocketServer,true,"");
        Port = configuration.getInt("Port",WebSocketServer,25564,1,65535,"WebSocketServer's port , can't be same as MinecraftServer's");
        websocketPath = configuration.getString("websocketPath",WebSocketServer,"/","Set websocketPath ,only url startswith this value can be acceptd");
        configuration.save();
    }
}
