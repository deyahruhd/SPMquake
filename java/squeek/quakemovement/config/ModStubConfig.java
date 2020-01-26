package squeek.quakemovement.config;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class ModStubConfig {
    public final boolean ENABLED;
    public final boolean JUMP_INDICATORS_ENABLED;

    public final double ACCELERATE;
    public final double Q1_AIR_ACCELERATE;
    public final double Q3_AIR_ACCELERATE;
    public final double Q1_MAX_AIR_ACCEL_PER_TICK;
    public final double Q3_MAX_AIR_ACCEL_PER_TICK;

    public final double INCREASED_FALL_DISTANCE;

    public ModStubConfig (boolean enable,
                          boolean jumpIndicator,
                          double groundAccel,
                          double q1Accel,
                          double q3Accel,
                          double q1MaxAccel,
                          double q3MaxAccel,
                          double fallInc) {
        ENABLED = enable;
        JUMP_INDICATORS_ENABLED = jumpIndicator;

        ACCELERATE = groundAccel;
        Q1_AIR_ACCELERATE = q1Accel;
        Q3_AIR_ACCELERATE = q3Accel;
        Q1_MAX_AIR_ACCEL_PER_TICK = q1MaxAccel;
        Q3_MAX_AIR_ACCEL_PER_TICK = q3MaxAccel;

        INCREASED_FALL_DISTANCE = fallInc;
    }

    public void to (ByteBuf buf) {
        buf.writeBoolean (ENABLED);
        buf.writeBoolean (JUMP_INDICATORS_ENABLED);

        buf.writeDouble (ACCELERATE);
        buf.writeDouble (Q1_AIR_ACCELERATE);
        buf.writeDouble (Q3_AIR_ACCELERATE);
        buf.writeDouble (Q1_MAX_AIR_ACCEL_PER_TICK);
        buf.writeDouble (Q3_MAX_AIR_ACCEL_PER_TICK);

        buf.writeDouble (INCREASED_FALL_DISTANCE);
    }

    public static ModStubConfig from (ByteBuf buf) {
        return new ModStubConfig (
                buf.readBoolean (), // enabled
                buf.readBoolean (), // jump indicators enabled

                buf.readDouble (),  // ground accel
                buf.readDouble (),  // q1 air accel
                buf.readDouble (),  // q3 air accel
                buf.readDouble (),  // q1 max air accel
                buf.readDouble (),  // q3 max air accel

                buf.readDouble ()   // fall dist increase
        );
    }
}
