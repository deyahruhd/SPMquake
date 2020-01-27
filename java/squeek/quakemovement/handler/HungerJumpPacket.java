package squeek.quakemovement.handler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.EntityId;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.moveimpl.QuakeClientPlayer;

import java.util.UUID;

public class HungerJumpPacket implements IMessage, IMessageHandler<HungerJumpPacket, IMessage> {
    double speed = 0.0;

    public HungerJumpPacket() {
        if (Minecraft.getMinecraft ().player != null)
            // We should be client side, so write the speed of Minecraft.player
            speed = QuakeClientPlayer.getSpeed (Minecraft.getMinecraft ().player);
    }

    @Override
    public IMessage onMessage (final HungerJumpPacket packet, MessageContext ctx) {
        double scaledSpeed = packet.speed * 20.0;

        EntityPlayerMP player = ctx.getServerHandler ().player;

        if (ModQuakeMovement.shouldDoQuakeMovement (ctx.getServerHandler ().player)
                && scaledSpeed >= ModConfig.VALUES.OVERSPEED) {
            double scale = Math.log (scaledSpeed - ModConfig.VALUES.OVERSPEED + 1.0);
            scale *= scale;

            ctx.getServerHandler ().player.getFoodStats ()
               .addExhaustion ((float) ModConfig.VALUES.OVERSPEED_EXHAUSTION_SCALE * (float) scale);
        }

        return null;
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        speed = buf.readDouble ();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble (speed);
    }
}