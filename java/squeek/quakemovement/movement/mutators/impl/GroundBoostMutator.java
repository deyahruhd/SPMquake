package squeek.quakemovement.movement.mutators.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class GroundBoostMutator extends Mutator {
    public static long playerAirbornTime     = 0;
    public static float explosionStr         = 0.f;

    @Override
    public boolean groundMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        return false;
    }

    @Override
    public boolean airMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        return false;
    }

    @Override
    public boolean preMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        return false;
    }

    @Override
    public boolean postMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        return false;
    }

    @Nullable
    @Override
    public Set<MovementInput> listenTo() {
        return null;
    }

    @Override
    public MutatorType getType() {
        return MutatorType.MovementPassive;
    }

    public static void applyExplosionToSPPlayer (Explosion e, SPacketExplosion packet) {
        float str = packet.getStrength ();
        float dist = packet.getStrength () + 2.5f;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Vec3d ePos = e.getPosition ();
        Vec3d pPos = new Vec3d (player.posX, player.posY, player.posZ).add (0.0, player.getEyeHeight () / 2.0, 0.0);
        float sqDist = (float) ePos.subtract(pPos).lengthSquared ();
        if (sqDist <= (dist * dist)) {
            explosionStr = MathHelper.sqrt (sqDist) / dist;

            explosionStr = 1.f - (explosionStr * explosionStr * explosionStr); // Curved accordingly
            float normalizedDist = explosionStr * -0.333f;

            Vec3d diff = ePos.subtract (pPos).normalize();
            diff = diff.scale (str * normalizedDist);

            player.addVelocity (diff.x * 0.66f, diff.y, diff.z * 0.66f);

            playerAirbornTime = System.currentTimeMillis();
        }
    }
}
