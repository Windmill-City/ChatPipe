package cappcraft.chat;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatHandler {
    @SubscribeEvent
    public static void onClientChatReceived(ServerChatEvent chat){
        if(!chat.getMessage().startsWith("/")){
            WebSocketHandler.INSTANCE.sendChatOutbound(new TextWebSocketFrame("[" + chat.getUsername() + "]: " + chat.getMessage()));
        }
    }
}
