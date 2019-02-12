package cappcraft.chat.network;

import cappcraft.chat.ChatpipeConfig;
import cappcraft.chat.coolq.CoolQClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class WebsocketClient {
    private final URI websocketURI;
    private final EventLoopGroup group;
    private final WebsocketClient instance;
    public WebsocketClient(URI uri, EventLoopGroup group){
        websocketURI = uri;
        this.group = group;
        instance = this;
    }

    public void start(){
        Bootstrap bootstrap = new  Bootstrap();
        String protocol = websocketURI.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add("Authorization","Token " + ChatpipeConfig.api_access_token);
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String)null, true,httpHeaders);
        WebSocketClientProtocolHandler handler = new WebSocketClientProtocolHandler(handshaker);

        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                if (ChatpipeConfig.auto_reconnect)
                    ch.pipeline().addLast(new ConnectionWatchDog(instance));
                if (ChatpipeConfig.timeout >= 0) {
                    ch.pipeline().addLast(new ReadTimeoutHandler(ChatpipeConfig.timeout));
                }
                ch.pipeline().addLast(new HttpClientCodec()).addLast(new HttpObjectAggregator(8192))
                        .addLast(new ChunkedWriteHandler()).addLast(handler).addLast(CoolQClientHandler.INSTANCE);
            }
        });
        bootstrap.connect(websocketURI.getHost(),websocketURI.getPort()).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                group.schedule(() -> {
                    start();
                }, ChatpipeConfig.reconnect_interval, TimeUnit.MILLISECONDS);
            }
        });
    }

    private class ConnectionWatchDog extends ChannelInboundHandlerAdapter {
        private int readidlecount = 0;
        private final WebsocketClient client;

        private ConnectionWatchDog(WebsocketClient client) {
            this.client = client;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            readidlecount = 0;
            ctx.fireChannelRead(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            client.group.schedule(() -> {
                ctx.close();
                client.start();
            }, ChatpipeConfig.reconnect_interval, TimeUnit.MILLISECONDS);
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE){
                readidlecount++;
                if(readidlecount > 3){
                    client.group.schedule(() -> {
                        ctx.fireChannelInactive();
                    }, ChatpipeConfig.reconnect_interval, TimeUnit.MILLISECONDS);
                }
                return;
            }
            super.userEventTriggered(ctx, evt);
        }
    }
}
