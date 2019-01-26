package cappcraft.chat;

import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    public static Thread server;
    public static void  preinit(){}

    public static void init(){
        MinecraftForge.EVENT_BUS.register(ChatHandler.class);
    }

    public static void finished(){
        if(Config.Enable_WebsocketServer){
            WebsocketServer websocketServer = new WebsocketServer(Config.Port);
               server = new Thread(() -> {
                   try {
                       websocketServer.run();
                   } catch (Exception e) {
                       ChatPipe.logger.error("Fail to Start WebSocketServer at Port:" + Config.Port,e);
                   }
               });
            server.setName("ChatPipe:WebSocketServer");
            server.setPriority(Thread.MIN_PRIORITY);
            server.setDaemon(true);
            server.start();
            ChatPipe.logger.info(" Started WebSocketServer at Port:" + Config.Port);
        }
    }
}
