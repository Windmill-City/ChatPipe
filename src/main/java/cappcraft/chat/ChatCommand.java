package cappcraft.chat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ChatCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "chatpipe";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/chatpipe reload";
    }

    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(args.length == 1 && args[0].equals("reload")) {
            ChatpipeConfig.load();
            ChatPipe.proxy.reinitCoolQ();
            sender.addChatMessage(new ChatComponentText("Reloaded Config"));
        }else {
            sender.addChatMessage(new ChatComponentText("/chatpipe reload"));
        }
    }
}
