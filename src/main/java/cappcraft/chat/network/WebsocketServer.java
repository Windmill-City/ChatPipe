package cappcraft.chat.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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
    public Channel start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(group).channel(NioServerSocketChannel.class).childHandler(CustomInitializer.INSTANCE);
        return b.bind(address).sync().channel();
    }
}
