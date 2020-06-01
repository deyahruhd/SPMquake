package squeek.quakemovement.handler;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import squeek.quakemovement.ModInfo;

public class NetworkHandler {
    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper (ModInfo.MODID);

    public static void init() {
        INSTANCE.registerMessage(ConfigPacket.class, ConfigPacket.class, 0, Side.CLIENT);
    }
}
