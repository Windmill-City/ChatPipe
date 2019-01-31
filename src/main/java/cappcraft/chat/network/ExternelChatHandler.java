package cappcraft.chat.network;

import cappcraft.chat.ChatPipe;
import cappcraft.chat.network.message.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

@ChannelHandler.Sharable
public class ExternelChatHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    public static final ExternelChatHandler INSTANCE = new ExternelChatHandler();
    public static final Gson gson = new GsonBuilder().registerTypeAdapter(IMessage.class,MessageTypeAdaptor.INSTANCE).create();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        handlerWebSocketFrame(ctx,msg);
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg){
        if(msg instanceof TextWebSocketFrame){
            try {
                ChatPipe.logger.info(((TextWebSocketFrame) msg).text());
                gson.fromJson(((TextWebSocketFrame) msg).text(),IMessage.class).onReceived(ctx,gson);
            }catch (Throwable t){
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.FAIL, t.getMessage()))));
                return;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ChatPipe.proxy.channels.add(ctx.channel());
        ChatPipe.logger.info("[Connected] Address: " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChatPipe.proxy.channels.remove(ctx);
        ChatPipe.logger.info("[DisConnected] Address: " + ctx.channel().remoteAddress().toString());
    }

    public void sendOutbound(ChatMessage msg){
        ChatPipe.proxy.channels.write(new TextWebSocketFrame(gson.toJson(msg)));
        ChatPipe.proxy.channels.flush();
    }
}
