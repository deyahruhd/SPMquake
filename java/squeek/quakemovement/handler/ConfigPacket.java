package squeek.quakemovement.handler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.quakemovement.ModInfo;
import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.config.ModStubConfig;
import squeek.quakemovement.movement.MovementSet;
import squeek.quakemovement.movement.QuakeClientPlayer;

public class ConfigPacket implements IMessage, IMessageHandler <ConfigPacket, IMessage> {
    ModStubConfig serverValues;

    public ConfigPacket () {
    }

    public ConfigPacket (ModStubConfig values) {
        // We should be on the server side. Copy the server config's values into the packet
        serverValues = values;
    }

    @Override
    @SideOnly (Side.CLIENT)
    public IMessage onMessage (final ConfigPacket packet, MessageContext ctx) {
        Minecraft.getMinecraft ().addScheduledTask (new Runnable() {
            @Override
            public void run() {
                ModConfig.CLIENT_VALUES = ModConfig.VALUES;
                ModConfig.VALUES = packet.serverValues;

                QuakeClientPlayer.movementPhysics = new MovementSet (ModConfig.VALUES.MOVEMENT_SET);
            }});

        return null;
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        serverValues = ModStubConfig.from (buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        serverValues.to (buf);
    }
}