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

    // overspeed for the hunger jump caps in blocks/sec, if the player is above this cap then
    // they will receive exhaustion on a scale of log squared speed, multiplied by the exhaustion scale
    public final double OVERSPEED;
    // set overspeed exhaustion scale to 0.0 to disable
    public final double OVERSPEED_EXHAUSTION_SCALE;

    // number of milliseconds that a player will receive ground slick for after taking explosion knockback
    public final int KNOCKBACK_SLICK_TIME;
    // number of milliseconds after a jump that the player will maintain their momentum into a wall
    public final int WALL_CLIP_TIME;

    // multiplier for ramp jump elasticity; set to 0.0 to completely disable ramp jumps
    public final double RAMP_JUMP_SCALE;

    public final double INCREASED_FALL_DISTANCE;

    public final String ARMOR_REQ;

    public ModStubConfig (boolean enable,
                          int jumpIndicator,
                          double groundAccel,
                          double q1Accel,
                          double q3Accel,
                          double q1MaxAccel,
                          double q3MaxAccel,
                          double cpmAirSteerAccel,
                          double cpmAirUndersteer,
                          double overspeed,
                          double overspeedScale,
                          int slickTime,
                          int clipTime,
                          double fallInc,
                          double rampJumpScale,
                          String qualifiedArmorName) {
        ENABLED = enable;
        JUMP_INDICATORS_MODE = jumpIndicator;

        ACCELERATE = groundAccel;
        Q1_AIR_ACCELERATE = q1Accel;
        Q3_AIR_ACCELERATE = q3Accel;
        Q1_MAX_AIR_ACCEL_PER_TICK = q1MaxAccel;
        Q3_MAX_AIR_ACCEL_PER_TICK = q3MaxAccel;

        CPM_AIR_STEER_ACCELERATE = cpmAirSteerAccel;
        CPM_AIR_UNDERSTEER = cpmAirUndersteer;

        OVERSPEED = overspeed;
        OVERSPEED_EXHAUSTION_SCALE = overspeedScale;

        KNOCKBACK_SLICK_TIME = slickTime;
        WALL_CLIP_TIME = clipTime;

        RAMP_JUMP_SCALE = rampJumpScale;

        INCREASED_FALL_DISTANCE = fallInc;

        ARMOR_REQ = qualifiedArmorName;
    }

    public void to (ByteBuf buf) {
        buf.writeInt (ARMOR_REQ.length ());
        buf.writeBytes (ARMOR_REQ.getBytes ());

        buf.writeBoolean (ENABLED);
        buf.writeInt (JUMP_INDICATORS_MODE);

        buf.writeDouble (ACCELERATE);
        buf.writeDouble (Q1_AIR_ACCELERATE);
        buf.writeDouble (Q3_AIR_ACCELERATE);
        buf.writeDouble (Q1_MAX_AIR_ACCEL_PER_TICK);
        buf.writeDouble (Q3_MAX_AIR_ACCEL_PER_TICK);

        buf.writeDouble (CPM_AIR_STEER_ACCELERATE);
        buf.writeDouble (CPM_AIR_UNDERSTEER);

        buf.writeDouble (OVERSPEED);
        buf.writeDouble (OVERSPEED_EXHAUSTION_SCALE);

        buf.writeInt (KNOCKBACK_SLICK_TIME);
        buf.writeInt (WALL_CLIP_TIME);

        buf.writeDouble (RAMP_JUMP_SCALE);

        buf.writeDouble (INCREASED_FALL_DISTANCE);
    }

    public static ModStubConfig from (ByteBuf buf) {
        int count = buf.readInt ();
        byte [] armorBytes = new byte [count];
        buf.readBytes (armorBytes);

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

                buf.readDouble (),  // overspeed minimum
                buf.readDouble (),  // overspeed scale factor

                buf.readInt (), // knockback slick ticks
                buf.readInt (), // wall clip ticks

                buf.readDouble (), // ramp jump scale factor

                buf.readDouble (),   // fall dist increase

                new String (armorBytes)
        );
    }
}
