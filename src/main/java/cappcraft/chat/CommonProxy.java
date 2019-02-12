package cappcraft.chat;

import cappcraft.chat.coolq.CoolQChannel;
import cappcraft.chat.coolq.CoolQClientHandler;
import cappcraft.chat.coolq.CoolQServerHandler;
import cappcraft.chat.network.ChannelAccepter;
import cappcraft.chat.network.CustomInitializer;
import cappcraft.chat.network.WebsocketClient;
import cappcraft.chat.network.WebsocketServer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.minecraft.network.NetworkSystem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import static cappcraft.chat.ChatpipeConfig.*;

public class CommonProxy {
    EventLoopGroup EventLoop =  new NioEventLoopGroup(0,(new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("Chatpipe:CoolQ IO").setPriority(Thread.MIN_PRIORITY).build());
    Channel CoolQServer;
    public Channel CoolQClient;
    ChannelPipeline mcpipeline;
    public ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public void  preinit(FMLPreInitializationEvent event){
        ConfigManager.sync(ChatPipe.MODID, Config.Type.INSTANCE);
    }

    public void initCoolQ(){
        if(enable){
            if(use_ws){
                WebsocketClient client = new WebsocketClient(URI.create(ChatpipeConfig.ws_url), EventLoop);
                client.start();
                MinecraftForge.EVENT_BUS.register(CoolQClientHandler.INSTANCE);
            }
            if(use_reverse_ws){
                MinecraftForge.EVENT_BUS.register(CoolQServerHandler.INSTANCE);
                SocketAddress address = new InetSocketAddress(ChatpipeConfig.port);
                CustomInitializer.INSTANCE.registerInitializer("CoolQChannel", new CoolQChannel());
                if(FMLCommonHandler.instance().getMinecraftServerInstance().getServerPort() == ChatpipeConfig.port)
                {
                    ChatPipe.logger.info("Using MinecraftServer's port for CoolQ connection");
                    //Inject minecraft's channel
                    NetworkSystem ns = FMLCommonHandler.instance().getMinecraftServerInstance().getNetworkSystem();
                    List<ChannelFuture> endpoint = null;
                    try {
                        Field endpoints_Field = ns.getClass().getDeclaredField("endpoints");
//                    Field endpoints_Field = ns.getClass().getDeclaredField("field_151274_e");
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
                    //use different port, start a server to handler websocket connections
                    WebsocketServer server = new WebsocketServer(address, EventLoop);
                    ChatPipe.logger.info("Starting CoolQ WebsocketServer on:" + address.toString());
                    try {
                        CoolQServer = server.start();
                    } catch (Exception e) {
                        ChatPipe.logger.error("Failed to start CoolQ WebsocketServer",e);
                    }
                }
            }
        }
    }

    public void reinitCoolQ(){
        MinecraftForge.EVENT_BUS.unregister(CoolQServerHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.unregister(CoolQClientHandler.INSTANCE);
        if(CoolQServer != null) {
            CoolQServer.close().syncUninterruptibly();
        }
        if(CoolQClient != null){
            CoolQClient.close().syncUninterruptibly();
        }
        //prevent adding multiply accepter when reinit
        if(mcpipeline != null) {
            try {
                mcpipeline.remove(ChannelAccepter.class);
            } catch (Throwable t) {}
        }
        channels.close().syncUninterruptibly();
        initCoolQ();
    }
}
