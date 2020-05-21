package squeek.quakemovement.movement.mutators.impl;

import squeek.quakemovement.config.ModConfig;

public class Q1MovementBase extends QMovementBase {
    public Q1MovementBase() {
        super ((float) ModConfig.VALUES.ACCELERATE,
               (float) ModConfig.VALUES.Q1_AIR_ACCELERATE,
               (float) ModConfig.VALUES.Q1_MAX_AIR_ACCEL_PER_TICK);
    }
}
