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

        if (scaledSpeed >= 5.63) {
            double scale = Math.log (scaledSpeed - 4.63);
            scale *= scale;

            ctx.getServerHandler ().player.getFoodStats ().addExhaustion (0.125f * (float) scale);
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