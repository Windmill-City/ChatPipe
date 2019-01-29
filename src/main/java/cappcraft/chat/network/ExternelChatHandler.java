package cappcraft.chat.network;

import cappcraft.chat.ChatPipe;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.HashSet;

@ChannelHandler.Sharable
public class ExternelChatHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    public final HashSet<ChannelHandlerContext> contexts = new HashSet<>();
    public static ExternelChatHandler INSTANCE = new ExternelChatHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        handlerWebSocketFrame(ctx,msg);
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg){
        if(msg instanceof TextWebSocketFrame){
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager()
                    .sendChatMsgImpl(new ChatComponentText(((TextWebSocketFrame) msg).text()),false);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        contexts.add(ctx);
        ChatPipe.logger.info("[Connected] Address: " + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        contexts.remove(ctx);
        ChatPipe.logger.info("[DisConnected] Address: " + ctx.channel().remoteAddress().toString());
    }

    public void sendChatOutbound(String msg){
        for (ChannelHandlerContext ctx : contexts) {
            ctx.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }
}
