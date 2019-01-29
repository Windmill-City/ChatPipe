package cappcraft.chat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy{
    public static void  preinit(){}
    public static void  init(){}
    public static void  finished(){}
}
