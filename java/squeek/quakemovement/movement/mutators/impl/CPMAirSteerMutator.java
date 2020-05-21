package squeek.quakemovement.movement.mutators.impl;

import com.google.common.collect.Sets;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class CPMAirSteerMutator extends Mutator {
    @Override
    public boolean groundMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        return false;
    }

    @Override
    public boolean airMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        boolean strafemove = (
                input == MovementInput.FOR_LEFT ||
                        input == MovementInput.LEFT ||
                        input == MovementInput.BACK_LEFT ||
                        input == MovementInput.FOR_RIGHT ||
                        input == MovementInput.RIGHT ||
                        input == MovementInput.BACK_RIGHT
        );

        boolean forwardmove = (
                input == MovementInput.FOR_LEFT ||
                        input == MovementInput.FORWARD ||
                        input == MovementInput.FOR_RIGHT ||
                        input == MovementInput.BACK_LEFT ||
                        input == MovementInput.BACK ||
                        input == MovementInput.BACK_RIGHT
        );

        QMovementBase.quake_AirAccelerate (player, wishspeed, (float) ModConfig.VALUES.Q3_AIR_ACCELERATE, (float) ModConfig.VALUES.Q3_MAX_AIR_ACCEL_PER_TICK,
                0.0f, wishdir.x, wishdir.z, strafemove, forwardmove);
        quake_CPMAirSteer (player, wishspeed, wishdir.x, wishdir.z);

        return true;
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
        return Sets.immutableEnumSet (MovementInput.FORWARD, MovementInput.BACK);
    }

    @Override
    public MutatorType getType() {
        return MutatorType.MovementOverride;
    }


    private void quake_CPMAirSteer(EntityPlayer player, double wishspeed, double wishX, double wishZ) {
        // Assumed to be holding +forward or +backward

        Vec3d playerVel = new Vec3d (player.motionX, 0.0, player.motionZ);
        double currSpeed = playerVel.length ();
        Vec3d playerVelDir = playerVel.scale (1.0 / currSpeed);

        Vec3d wishdir = new Vec3d (wishX, 0.0, wishZ);

        double dot = playerVelDir.dotProduct (wishdir);
        double k = 1.6 * ModConfig.VALUES.CPM_AIR_STEER_ACCELERATE * dot;

        // We can't change direction while slowing down
        if (dot > 0) {
            playerVel = new Vec3d (playerVelDir.x * currSpeed + wishX * k, 0.0,
                                   playerVelDir.z * currSpeed + wishZ * k).normalize ().scale (currSpeed);

            player.motionX = playerVel.x;
            player.motionZ = playerVel.z;
        }
    }
}
