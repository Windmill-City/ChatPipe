package cappcraft.chat.network;

import cappcraft.chat.Config;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

@ChannelHandler.Sharable
public class HeaderChecker extends ChannelInboundHandlerAdapter {
    private final StringBuilder seq = new StringBuilder(3);
    private final HeaderParser parser = new HeaderParser(seq, 3);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.channel().pipeline().remove(this);
        if(msg instanceof ByteBuf){
            ByteBuf byteBuf = (ByteBuf) msg;
            if(byteBuf.forEachByte(parser) != -1 && seq.toString().equals("GET")) {
                ctx.channel().pipeline().remove("timeout");
                ctx.channel().pipeline().remove("splitter");
                ctx.channel().pipeline().remove("decoder");
                ctx.channel().pipeline().remove("prepender");
                ctx.channel().pipeline().remove("encoder");
                ctx.channel().pipeline().remove("packet_handler");
                ctx.channel().pipeline().addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(128)).addLast(new WebSocketServerProtocolHandler(Config.websocketPath)).addLast(ExternelChatHandler.INSTANCE);
            }
            parser.reset();
        }
        ctx.fireChannelActive().fireChannelRead(msg);
    }

    private class HeaderParser implements ByteBufProcessor {
        private final StringBuilder seq;
        private final int maxLength;
        private int size = 0;

        public HeaderParser(StringBuilder seq, int maxLength) {
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
            seq.delete(0,maxLength);
            size = 0;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }
}
