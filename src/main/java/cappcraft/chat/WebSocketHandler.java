package cappcraft.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashSet;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private HashSet<Channel> channels = new HashSet<>();
    public static WebSocketHandler INSTANCE = new WebSocketHandler();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        handlerWebSocketFrame(ctx,msg);
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg){
        if(msg instanceof TextWebSocketFrame){
            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(new TextComponentString(((TextWebSocketFrame) msg).text()),false);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        channels.add(ctx.channel());
        super.channelActive(ctx);
        ChatPipe.logger.info("WebSocket: [Connected] Address: " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        channels.remove(ctx.channel());
        super.channelInactive(ctx);
        ChatPipe.logger.info("WebSocket: [DisConnected] Address: " + ctx.channel().remoteAddress().toString());
    }

    public void sendChatOutbound(TextWebSocketFrame msg){
        for (Channel channel : channels) {
            channel.writeAndFlush(msg);
        }
    }
}
