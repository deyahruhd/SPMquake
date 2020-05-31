package squeek.quakemovement.movement.mutators.impl;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class RampJumpMutator extends Mutator {
    private Vec3d previousPos = Vec3d.ZERO;
    private Vec3d previousVel = Vec3d.ZERO;

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
        previousPos = new Vec3d (player.posX, player.posY, player.posZ);
        previousVel = new Vec3d (player.motionX, player.motionY, player.motionZ);
        return false;
    }

    @Override
    public boolean postMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        boolean onGroundForReal = player.onGround && !QuakeClientPlayer.isJumping (player);

        if (player.collided && ! onGroundForReal) {
            Vec3d pos = player.getPositionVector ();

            double playerWidth = player.width * 0.5 + 0.1;
            if (player.collidedVertically)
                if (previousVel.y > 0)
                    pos = pos.add(0.0, player.getEyeHeight() + 0.51, 0.0);
                else
                    pos = pos.add(0.0, -0.11, 0.0);

            previousVel = new Vec3d (previousVel.x, Math.signum (previousVel.y) * (3.92 - 3.92 * Math.exp (-0.252534 * Math.abs (previousVel.y))), previousVel.z);

            Vec3d n = QuakeClientPlayer.getStairNormal (player.world, player, previousVel, previousPos);
            Vec3d d = previousVel.normalize ();

            if (n != null && n.dotProduct(d) < 0.0) {
                Vec3d r = d.subtract(n.scale(d.dotProduct(n) * 2));

                double elasticity = MathHelper.clamp(1.15 - Math.pow(r.dotProduct(n), 4.0), 0.15, 1.0);


                r = r.scale(elasticity * previousVel.length () * ModConfig.VALUES.RAMP_JUMP_SCALE);

                player.setVelocity(r.x, r.y, r.z);
                previousVel = r;
            }
        }

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
}
