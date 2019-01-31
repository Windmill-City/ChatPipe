package cappcraft.chat;

import cappcraft.chat.network.ExternelChatHandler;
import cappcraft.chat.network.message.ChatMessage;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatHandler {
    @SubscribeEvent
    public void onClientChatReceived(ServerChatEvent chat){
        if(!chat.getMessage().startsWith("/")){
            ExternelChatHandler.INSTANCE.sendOutbound(new ChatMessage(chat.getUsername(), chat.getMessage()));
        }
    }
}
