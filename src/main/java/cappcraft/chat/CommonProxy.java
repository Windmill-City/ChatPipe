package cappcraft.chat;

import cappcraft.chat.network.ChannelAccepter;
import cappcraft.chat.network.WebsocketServer;
import cappcraft.chat.network.message.MessageType;
import cappcraft.chat.network.message.MessageTypeAdaptor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.minecraft.network.NetworkSystem;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class CommonProxy {
    ChannelFuture wsfuture;
    ChannelPipeline mcpipeline;
    public ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public ChatHandler chatHandler = new ChatHandler();
    private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("WebSocket IO #%d").setDaemon(true).build());;
    public void  preinit(){}

    public void init(){
        MinecraftForge.EVENT_BUS.register(chatHandler);
    }

    public void finished(){
        startWebsocketServer();
        MessageTypeAdaptor.INSTANCE.registerMessage(MessageType.COMMAND);
    }

    public void startWebsocketServer(){
        if(Config.useWebsocketServer){
            SocketAddress address = new InetSocketAddress(Config.Port);
            if(FMLCommonHandler.instance().getMinecraftServerInstance().getServerPort() == Config.Port)
            {
                ChatPipe.logger.info("Using MinecraftServer's port to synchronize chats");
                //Inject minecraft's channel
                NetworkSystem ns = FMLCommonHandler.instance().getMinecraftServerInstance().func_147137_ag();
                List<ChannelFuture> endpoint = null;
                try {
//                    Field endpoints_Field = ns.getClass().getDeclaredField("endpoints");
                    Field endpoints_Field = ns.getClass().getDeclaredField("field_151274_e");
                    endpoints_Field.setAccessible(true);
                    endpoint = ((List<ChannelFuture>)endpoints_Field.get(ns));
                    mcpipeline = endpoint.get(0).channel().pipeline();
                    ChatPipe.logger.info("Injected the NetworkSystem successfully");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    ChatPipe.logger.error("Failed to inject NetworkSystem",e);
                }
                //Add accepter to handle websocket connections
                endpoint.get(0).channel().pipeline().addFirst(new ChannelAccepter());
            } else {
                //use different port start an server to handler websocket connections
                WebsocketServer server = new WebsocketServer(address,eventLoopGroup);
                ChatPipe.logger.info("Starting internal WebsocketServer on:" + address.toString());
                try {
                    wsfuture = server.start();
                } catch (Exception e) {
                    ChatPipe.logger.error("Failed to start internal WebsocketServer",e);
                }
            }
        }
    }

    public void restart(){
        if(wsfuture != null) {
            wsfuture.channel().close().syncUninterruptibly();
        }
        //prevent adding multiply accepter when restarting
        if(mcpipeline != null) {
            try {
                mcpipeline.remove(ChannelAccepter.class);
            } catch (Throwable t) {}
        }
        channels.close().syncUninterruptibly();
        startWebsocketServer();
    }
}
