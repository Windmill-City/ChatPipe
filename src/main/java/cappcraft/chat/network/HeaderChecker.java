package cappcraft.chat.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

@ChannelHandler.Sharable
public class HeaderChecker extends ChannelInboundHandlerAdapter {
    private final AppendableCharSequence seq = new AppendableCharSequence(3);
    private final HeaderParser parser = new HeaderParser(seq, 3);
    private final ChannelInitializer childhandler = new ChatChannelInitializer();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.channel().pipeline().remove(this);
        if(msg instanceof ByteBuf){
            ByteBuf byteBuf = (ByteBuf) msg;
            if(byteBuf.forEachByte(parser) != -1 && seq.toString().equals("GET")) {
                ctx.channel().pipeline().remove("timeout");
                ctx.channel().pipeline().remove("legacy_query");
                ctx.channel().pipeline().remove("splitter");
                ctx.channel().pipeline().remove("decoder");
                ctx.channel().pipeline().remove("prepender");
                ctx.channel().pipeline().remove("packet_handler");
                ctx.channel().pipeline().addFirst(childhandler).fireChannelActive();
            }
            parser.reset();
        }
        ctx.fireChannelRead(msg);
    }

    private class HeaderParser implements ByteProcessor {
        private final AppendableCharSequence seq;
        private final int maxLength;
        private int size = 0;

        public HeaderParser(AppendableCharSequence seq, int maxLength) {
            this.seq = seq;
            this.maxLength = maxLength;
        }

        @Override
        public boolean process(byte value) throws Exception {
            if (++size > maxLength)
                return false;
            char nextByte = (char) value;
            seq.append(nextByte);
            return true;
        }

        public void reset(){
            seq.reset();
            size = 0;
        }
    }
}
