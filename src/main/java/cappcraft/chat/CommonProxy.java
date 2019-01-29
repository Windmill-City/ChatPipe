package cappcraft.chat;

import cappcraft.chat.network.ChatChannelAccepter;
import cappcraft.chat.network.WebsocketServer;
import io.netty.channel.ChannelFuture;
import net.minecraft.network.NetworkSystem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class CommonProxy {
    public static void  preinit(){}

    public static void init(){
        MinecraftForge.EVENT_BUS.register(ChatHandler.class);
    }

    public static void finished(){
        if(Config.useWebsocketServer && FMLCommonHandler.instance().getSide() == Side.SERVER){
            SocketAddress address = new InetSocketAddress(Config.Port);
            if(FMLCommonHandler.instance().getMinecraftServerInstance().getServerPort() == Config.Port)
            {
                ChatPipe.logger.info("Using MinecraftServer's port to synchronize chats");
                //Inject minecraft's channel
                NetworkSystem ns = FMLCommonHandler.instance().getMinecraftServerInstance().getNetworkSystem();
                List<ChannelFuture> endpoint = null;
                try {
                    Field endpoints_Field = ns.getClass().getDeclaredField("endpoints");
                    endpoints_Field.setAccessible(true);
                    endpoint = ((List<ChannelFuture>)endpoints_Field.get(ns));
                    ChatPipe.logger.info("Injecting the NetworkSystem successfully");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    ChatPipe.logger.error("Failed to inject NetworkSystem",e);
                }
                //Add accepter to handle websocket connections
                endpoint.get(0).channel().pipeline().addFirst(new ChatChannelAccepter());
            } else {
                //use different port start an server to handler websocket connections
                WebsocketServer websocketServer = new WebsocketServer(address);
                ChatPipe.logger.info("Starting internal WebsocketServer on:*:" + Config.Port);
                Thread thread = new Thread(() -> {
                    try {
                        websocketServer.run();
                    } catch (Exception e) {
                        ChatPipe.logger.error("Failed to run internal WebsocketServer", e);
                    }
                });
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("ChatPipe:internal WebsocketServer");
                thread.start();
            }
        }
    }
}
