package cappcraft.chat.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;

public class WebsocketServer {
    private final SocketAddress address;
    private final EventLoopGroup group;
    public WebsocketServer(SocketAddress address, EventLoopGroup group){
        this.address = address;
        this.group = group;
    }
    public ChannelFuture start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(group).channel(NioServerSocketChannel.class).childHandler(new ChatChannelInitializer());
        return b.bind(address).sync();
    }
}
