package cappcraft.chat.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelAccepter extends ChannelInboundHandlerAdapter {
    private final HeaderChecker checker = new HeaderChecker();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final Channel child = (Channel) msg;
        child.pipeline().addLast(checker);
        ctx.fireChannelRead(msg);
    }
}
