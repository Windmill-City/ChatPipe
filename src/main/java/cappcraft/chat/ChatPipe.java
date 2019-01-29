package cappcraft.chat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChatPipe.MODID, name = ChatPipe.NAME, version = ChatPipe.VERSION, useMetadata = true,acceptableRemoteVersions = "*")
public class ChatPipe
{
    public static final String MODID = "chatpipe";
    public static final String NAME = "ChatPipe";
    public static final String VERSION = "1.3";

    public static Logger logger;
    @SidedProxy(clientSide = "cappcraft.chat.CommonProxy",serverSide = "cappcraft.chat.ClientProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preinit();
        Config.initConfig(event.getSuggestedConfigurationFile());
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @EventHandler
    public void serverStarted(FMLLoadCompleteEvent event){
        proxy.finished();
    }
}
