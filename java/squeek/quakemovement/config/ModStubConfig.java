package squeek.quakemovement.config;

import io.netty.buffer.ByteBuf;

public class ModStubConfig {
    public final boolean ENABLED;
    public final int JUMP_INDICATORS_MODE;

    // vars for ground and air acceleration
    public final double ACCELERATE;
    public final double Q1_AIR_ACCELERATE;
    public final double Q3_AIR_ACCELERATE;
    public final double Q1_MAX_AIR_ACCEL_PER_TICK;
    public final double Q3_MAX_AIR_ACCEL_PER_TICK;

    public final double CPM_AIR_STEER_ACCELERATE;
    public final double CPM_AIR_UNDERSTEER;

    // number of milliseconds that a player will receive ground slick for after taking explosion knockback
    public final int KNOCKBACK_SLICK_TIME;
    // number of milliseconds after a jump that the player will maintain their momentum into a wall
    public final int WALL_CLIP_TIME;

    public final boolean NERF_AUTO_HOP;

    // multiplier for ramp jump elasticity; set to 0.0 to completely disable ramp jumps
    public final double RAMP_JUMP_SCALE;

    public final double INCREASED_FALL_DISTANCE;

    public final String MOVEMENT_SET;

    public ModStubConfig (boolean enable,
                          int jumpIndicator,
                          double groundAccel,
                          double q1Accel,
                          double q3Accel,
                          double q1MaxAccel,
                          double q3MaxAccel,
                          double cpmAirSteerAccel,
                          double cpmAirUndersteer,
                          int slickTime,
                          int clipTime,
                          boolean nerfAutoHop,
                          double rampJumpScale,
                          double fallInc,
                          String movementSetJson) {
        ENABLED = enable;
        JUMP_INDICATORS_MODE = jumpIndicator;

        ACCELERATE = groundAccel;
        Q1_AIR_ACCELERATE = q1Accel;
        Q3_AIR_ACCELERATE = q3Accel;
        Q1_MAX_AIR_ACCEL_PER_TICK = q1MaxAccel;
        Q3_MAX_AIR_ACCEL_PER_TICK = q3MaxAccel;

        CPM_AIR_STEER_ACCELERATE = cpmAirSteerAccel;
        CPM_AIR_UNDERSTEER = cpmAirUndersteer;

        KNOCKBACK_SLICK_TIME = slickTime;
        WALL_CLIP_TIME = clipTime;

        NERF_AUTO_HOP = nerfAutoHop;

        RAMP_JUMP_SCALE = rampJumpScale;

        INCREASED_FALL_DISTANCE = fallInc;

        MOVEMENT_SET = movementSetJson;
    }

    public void to (ByteBuf buf) {
        buf.writeInt (MOVEMENT_SET.length ());
        buf.writeBytes (MOVEMENT_SET.getBytes ());

        buf.writeBoolean (ENABLED);
        buf.writeInt (JUMP_INDICATORS_MODE);

        buf.writeDouble (ACCELERATE);
        buf.writeDouble (Q1_AIR_ACCELERATE);
        buf.writeDouble (Q3_AIR_ACCELERATE);
        buf.writeDouble (Q1_MAX_AIR_ACCEL_PER_TICK);
        buf.writeDouble (Q3_MAX_AIR_ACCEL_PER_TICK);

        buf.writeDouble (CPM_AIR_STEER_ACCELERATE);
        buf.writeDouble (CPM_AIR_UNDERSTEER);

        buf.writeInt (KNOCKBACK_SLICK_TIME);
        buf.writeInt (WALL_CLIP_TIME);

        buf.writeBoolean (NERF_AUTO_HOP);

        buf.writeDouble (RAMP_JUMP_SCALE);

        buf.writeDouble (INCREASED_FALL_DISTANCE);
    }

    public static ModStubConfig from (ByteBuf buf) {
        int count = buf.readInt ();
        byte [] movementSet = new byte [count];
        buf.readBytes (movementSet);

        return new ModStubConfig (
                buf.readBoolean (), // enabled
                buf.readInt (),     // jump indicators enabled

                buf.readDouble (),  // ground accel
                buf.readDouble (),  // q1 air accel
                buf.readDouble (),  // q3 air accel
                buf.readDouble (),  // q1 max air accel
                buf.readDouble (),  // q3 max air accel

                buf.readDouble (),  // cpm air steer
                buf.readDouble (),  // cpm air understeer

                buf.readInt (), // knockback slick ticks
                buf.readInt (), // wall clip ticks

                buf.readBoolean(), // nerf auto hop

                buf.readDouble (), // ramp jump scale factor

                buf.readDouble (),   // fall dist increase

                new String (movementSet)
        );
    }
}
