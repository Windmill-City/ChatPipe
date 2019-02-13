package cappcraft.chat.coolq;

import cappcraft.chat.ChatPipe;
import cappcraft.chat.ChatpipeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cappcraft.chat.ChatPipe.proxy;
import static cappcraft.chat.coolq.GroupMessage.getWithNoCQCode;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class CoolQServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final AttributeKey<String> SelfID = AttributeKey.valueOf("SelfID");
    private static final AttributeKey<String> Client_Role = AttributeKey.valueOf("Client_Role");
    private final Gson gson = new GsonBuilder().registerTypeAdapter(GroupMessage.class, new  GroupMessage.GroupMessageDeserializer()).create();
    public static CoolQServerHandler INSTANCE = new CoolQServerHandler();

    @SubscribeEvent
    public void onChatEvent(ServerChatEvent chat){
        for (Channel ch :
                proxy.channels) {
            if(ch.hasAttr(Client_Role) && ch.attr(Client_Role).get().matches("API|Universal")){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "send_group_msg");
                JsonObject params = new JsonObject();
                params.addProperty("message", chat.getComponent().getUnformattedText());
                params.addProperty("auto_escape", "true");
                jsonObject.add("params", params);
                for (String group_id :
                        ChatpipeConfig.group_ids) {
                    params.addProperty("group_id", group_id);
                    ch.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));
                    ChatPipe.logger.info(jsonObject.toString());
                    params.remove("group_id");
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if(proxy.channels.contains(ctx.channel()) && msg instanceof TextWebSocketFrame){
            JsonObject json = new JsonParser().parse(((TextWebSocketFrame) msg).text()).getAsJsonObject();
            if(json.has("post_type") && json.get("post_type").getAsString().equals("message") && json.has("message_type") && json.get("message_type").getAsString().equals("group")){
                GroupMessage gmessage = gson.fromJson(json, GroupMessage.class);
                String chat = getWithNoCQCode(gmessage.raw_message, true);
                if(chat.isEmpty())
                    return;
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
                        .sendMessage(new TextComponentTranslation("chat.type.announcement"
                                , new Object[] {gmessage.sender.card.isEmpty() ? gmessage.sender.nickname : gmessage.sender.card, new TextComponentString(chat)}));
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(proxy.channels.remove(ctx.channel()))
        ChatPipe.logger.info("[CoolQ -> Server] [Disconnected] Address:" + ctx.channel().remoteAddress().toString() +
                " X-Self-ID:" + ctx.channel().attr(SelfID).get() +
                " X-Client-Role:" + ctx.channel().attr(Client_Role).get());
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            HttpHeaders headers = ((WebSocketServerProtocolHandler.HandshakeComplete) evt).requestHeaders();
            if(headers.get("X-Self-ID") != null && headers.get("X-Client-Role") != null) {
                ctx.channel().attr(SelfID).set(headers.getAsString("X-Self-ID"));
                ctx.channel().attr(Client_Role).set(headers.getAsString("X-Client-Role"));
                if (!ChatpipeConfig.connect_access_token.isEmpty()) {
                    if (headers.get("Authorization") == null || !headers.get("Authorization").regionMatches(6, ChatpipeConfig.connect_access_token, 0, ChatpipeConfig.connect_access_token.length())) {
                        ctx.writeAndFlush(new DefaultHttpResponse(HTTP_1_1, FORBIDDEN)).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                future.channel().close();
                            }
                        });
                        return;
                    }
                }
                proxy.channels.add(ctx.channel());
                ChatPipe.logger.info("[CoolQ -> Server] [Connected] Address:" + ctx.channel().remoteAddress().toString() +
                        " X-Self-ID:" + ctx.channel().attr(SelfID).get() +
                        " X-Client-Role:" + ctx.channel().attr(Client_Role).get());
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
