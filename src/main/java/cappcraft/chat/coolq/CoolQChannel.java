package cappcraft.chat.coolq;

import cappcraft.chat.ChatpipeConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class CoolQChannel extends ChannelInitializer {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        if(ChatpipeConfig.timeout >= 0)
            ch.pipeline().addLast(new ReadTimeoutHandler(ChatpipeConfig.timeout));
        ch.pipeline().addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(8192))
                .addLast(new ChunkedWriteHandler()).addLast(new WebSocketServerProtocolHandler("/",false)).addLast(CoolQServerHandler.INSTANCE);
    }
}
