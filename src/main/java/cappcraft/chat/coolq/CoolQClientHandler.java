package cappcraft.chat.coolq;

import cappcraft.chat.ChatPipe;
import cappcraft.chat.ChatpipeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.ServerChatEvent;

import static cappcraft.chat.ChatPipe.proxy;
import static cappcraft.chat.coolq.GroupMessage.getWithNoCQCode;
import static io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE;

@ChannelHandler.Sharable
public class CoolQClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    public static CoolQClientHandler INSTANCE = new CoolQClientHandler();
    private static final AttributeKey<String> Server_Role = new  AttributeKey("Server_Role");
    private final Gson gson = new GsonBuilder().registerTypeAdapter(GroupMessage.class, new  GroupMessage.GroupMessageDeserializer()).create();

    @SubscribeEvent
    public void onChatEvent(ServerChatEvent chat){
        for (Channel ch :
                proxy.channels) {
            if(ch.attr(Server_Role) != null && ch.attr(Server_Role).get().equals("Universal")){
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
                    params.remove("group_id");
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if(msg instanceof TextWebSocketFrame){
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
        ChatPipe.logger.info("[Server -> CoolQ] [Disconnected] CoolQ Address:" + ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt == HANDSHAKE_COMPLETE){
            ctx.channel().attr(Server_Role).set("Universal");
            ChatPipe.logger.info("[Server -> CoolQ] [Connected] CoolQ Address:" + ctx.channel().remoteAddress());
            proxy.channels.add(ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }
}
