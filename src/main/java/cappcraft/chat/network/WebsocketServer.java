package cappcraft.chat.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;

public class WebsocketServer {
    public ChannelFuture WebsocketServerChannelFuture;
    private final SocketAddress address;
    private final EventLoopGroup group = new NioEventLoopGroup();
    public WebsocketServer(SocketAddress address){
        this.address = address;
    }
    public void run() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(group).channel(NioServerSocketChannel.class).childHandler(new ChatChannelInitializer());
        WebsocketServerChannelFuture = b.bind(address).sync();
        WebsocketServerChannelFuture.channel().closeFuture().sync();
    }
}
