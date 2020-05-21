package squeek.quakemovement.movement.mutators.impl;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.config.ModConfig;

import javax.annotation.Nullable;

public class QAnisotropicMovementBase extends Q3MovementBase {
    @Override
    public final boolean airMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
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

        if ((strafemove && ! forwardmove) || (! strafemove && forwardmove)) {
            airAccel =    (float) ModConfig.VALUES.Q1_AIR_ACCELERATE;
            maxAirAccel = (float) ModConfig.VALUES.Q1_MAX_AIR_ACCEL_PER_TICK;
        } else {
            airAccel =    (float) ModConfig.VALUES.Q3_AIR_ACCELERATE;
            maxAirAccel = (float) ModConfig.VALUES.Q3_MAX_AIR_ACCEL_PER_TICK;
        }

        quake_AirAccelerate(player, wishspeed, airAccel, maxAirAccel, 0.0f, wishdir.x, wishdir.z, strafemove, forwardmove);

        return false;
    }
}
