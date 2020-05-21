package squeek.quakemovement.movement.mutators.impl;

import squeek.quakemovement.config.ModConfig;

public class Q3MovementBase extends QMovementBase {
    public Q3MovementBase() {
        super ((float) ModConfig.VALUES.ACCELERATE,
               (float) ModConfig.VALUES.Q3_AIR_ACCELERATE,
               (float) ModConfig.VALUES.Q3_MAX_AIR_ACCEL_PER_TICK);
    }
}
