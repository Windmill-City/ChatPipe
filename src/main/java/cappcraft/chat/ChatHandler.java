package cappcraft.chat;

import cappcraft.chat.network.ExternelChatHandler;
import cappcraft.chat.network.message.ChatMessage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.ServerChatEvent;

public class ChatHandler {
    @SubscribeEvent
    public void onClientChatReceived(ServerChatEvent chat){
        if(!chat.message.startsWith("/")){
            ExternelChatHandler.INSTANCE.sendOutbound(new ChatMessage(chat.username, chat.message));
        }
    }
}
