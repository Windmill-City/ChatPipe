package cappcraft.chat.coolq;

import cappcraft.chat.ChatPipe;
import cappcraft.chat.ChatpipeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.ServerChatEvent;

import static cappcraft.chat.ChatPipe.proxy;
import static cappcraft.chat.coolq.GroupMessage.getWithNoCQCode;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
public class CoolQServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final AttributeKey<String> SelfID = new  AttributeKey("SelfID");
    private static final AttributeKey<String> Client_Role = new  AttributeKey("Client_Role");
    private final Gson gson = new GsonBuilder().registerTypeAdapter(GroupMessage.class, new  GroupMessage.GroupMessageDeserializer()).create();
    public static CoolQServerHandler INSTANCE = new CoolQServerHandler();

    @SubscribeEvent
    public void onChatEvent(ServerChatEvent chat){
        for (Channel ch :
                proxy.channels) {
            if(ch.attr(Client_Role) != null && ch.attr(Client_Role).get().matches("API|Universal")){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "send_group_msg");
                JsonObject params = new JsonObject();
                params.addProperty("message", chat.component.getUnformattedText());
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
                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager()
                        .sendChatMsgImpl(new ChatComponentTranslation("chat.type.announcement"
                                , new Object[] {gmessage.sender.card.isEmpty() ? gmessage.sender.nickname : gmessage.sender.card, new ChatComponentText(chat)}),false);
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
                ctx.channel().attr(SelfID).set(headers.get("X-Self-ID"));
                ctx.channel().attr(Client_Role).set(headers.get("X-Client-Role"));
                if (!ChatpipeConfig.connect_access_token.isEmpty()) {
                    if (headers.get("Authorization") == null || !headers.get("Authorization").regionMatches(6, ChatpipeConfig.connect_access_token, 0, ChatpipeConfig.connect_access_token.length())) {
                        ctx.writeAndFlush(new DefaultHttpResponse(HTTP_1_1, FORBIDDEN)).addListener((ChannelFutureListener) future -> future.channel().close());
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
