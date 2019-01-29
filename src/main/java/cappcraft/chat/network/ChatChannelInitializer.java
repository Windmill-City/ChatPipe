package cappcraft.chat.network;

import cappcraft.chat.Config;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class ChatChannelInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        if(Config.timeout >= 0){
            ch.pipeline().addLast(new ReadTimeoutHandler(Config.timeout));
        }
        ch.pipeline().addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(128)).addLast(new WebSocketServerProtocolHandler(Config.websocketPath)).addLast(ExternelChatHandler.INSTANCE);
    }
}
