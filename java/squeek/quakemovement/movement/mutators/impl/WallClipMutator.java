package squeek.quakemovement.movement.mutators.impl;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class WallClipMutator extends Mutator {
    private long playerGroundTouchTime = 0;
    private Vec3d previousVel = Vec3d.ZERO;
    //private boolean wasOnGround = false;

    @Override
    public boolean groundMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        // Force reset so people can't store their velocities while on the ground
        playerGroundTouchTime = 0;
        return false;
    }

    @Override
    public boolean airMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        return false;
    }

    @Override
    public boolean preMove (EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        previousVel = new Vec3d (player.motionX, player.motionY, player.motionZ);
        boolean onGroundForReal = player.onGround && !QuakeClientPlayer.isJumping (player);

        if (QuakeClientPlayer.getSpeed (player) > 0.21540 && (System.currentTimeMillis () - playerGroundTouchTime > ModConfig.VALUES.WALL_CLIP_TIME)
                && player.onGround && ! onGroundForReal) {
            playerGroundTouchTime = System.currentTimeMillis();
        }

        return false;
    }

    @Override
    public boolean postMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {

        // Wall clip
        if ((System.currentTimeMillis () - playerGroundTouchTime <= ModConfig.VALUES.WALL_CLIP_TIME) && player.collidedHorizontally) {
            player.setVelocity(previousVel.x, player.motionY, previousVel.z);
            previousVel = new Vec3d(previousVel.x, player.motionY, previousVel.z);
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
