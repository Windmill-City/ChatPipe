package cappcraft.chat.network.message;

import cappcraft.chat.ChatPipe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class CommandMessage implements IMessage {
    public final MessageType type = MessageType.COMMAND;
    public CommandTpye command = null;
    public String sender;
    public String value;
    @Expose(deserialize = false)
    private static DedicatedServer server = (DedicatedServer) FMLCommonHandler.instance().getMinecraftServerInstance();

    @Override
    public IMessage onDeseralize(JsonObject in) {
        command = CommandTpye.valueOf(in.get("command").getAsString());
        switch (command){
            case MCCOMMAND:
                sender = in.get("sender").getAsString();
                value = in.get("value").getAsString();
                if(sender.isEmpty() || value.isEmpty()){
                    throw new IllegalArgumentException("Command sender and value could not be null");
                }
        }
        ;return this;
    }

    @Override
    public void onSeralize(JsonObject in) {
    }

    @Override
    public void onReceived(ChannelHandlerContext ctx, Gson gson) {
        switch (command) {
            case RESTART:
                ChatPipe.proxy.restart();
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
                break;
            case STOPSYNC:
                MinecraftForge.EVENT_BUS.unregister(ChatPipe.proxy.chatHandler);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
                break;
            case STARTSYNC:
                MinecraftForge.EVENT_BUS.register(ChatPipe.proxy.chatHandler);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
                break;
            case MCCOMMAND:
                execute(ctx, gson);
                break;
        }
    }

    private void execute(ChannelHandlerContext ctx, Gson gson){
        ICommandSender player  = server.getConfigurationManager().func_152612_a(sender);
        if(player != null){
            server.addPendingCommand(value,new CommandSender(player, ctx, gson));
            return;
        }else if(sender.equals("Server")){
            server.addPendingCommand(value,new CommandSender(server, ctx, gson));
            return;
        }
        throw new IllegalArgumentException("Sender Not Exist");
    }
    private class CommandSender implements ICommandSender {
        ICommandSender sender;
        ChannelHandlerContext ctx;
        Gson gson;
        public CommandSender(ICommandSender sender,ChannelHandlerContext ctx, Gson gson)
        {
            this.sender = sender;
            this.ctx = ctx;
            this.gson = gson;
        }

        @Override
        public String getCommandSenderName() {
            return sender.getCommandSenderName();
        }

        @Override
        public IChatComponent func_145748_c_() {
            return sender.func_145748_c_();
        }

        @Override
        public void addChatMessage(IChatComponent chatComponent) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS,chatComponent.getUnformattedText()))));
        }

        @Override
        public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_) {
            return sender.canCommandSenderUseCommand(p_70003_1_,p_70003_2_);
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return sender.getPlayerCoordinates();
        }

        @Override
        public World getEntityWorld() {
            return sender.getEntityWorld();
        }
    }
}
