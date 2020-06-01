package squeek.quakemovement.movement.mutators.impl;

import com.sun.javafx.geom.Vec2d;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.helper.MathHelper;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public abstract class QMovementBase extends Mutator {
    protected float groundAccel, airAccel, maxAirAccel;

    public QMovementBase(float groundAccel, float airAccel, float maxAirAccel) {
        this.groundAccel = groundAccel;
        this.airAccel    = airAccel;
        this.maxAirAccel = maxAirAccel;
    }

    @Override
    public final boolean groundMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        float dynamicCap = -1.0f;

        if (wishspeed == 0.f && dynamicCap > 0.f) {
            Vec3d hardCappedVel = new Vec3d (player.motionX, 0.0, player.motionZ).normalize().scale (dynamicCap);
            player.motionX = hardCappedVel.x;
            player.motionZ = hardCappedVel.z;
        } else
            quake_Accelerate(player, wishspeed, dynamicCap, wishdir.x, wishdir.z, groundAccel);


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

        quake_AirAccelerate(player, wishspeed, airAccel, maxAirAccel, 0.0f, wishdir.x, wishdir.z, strafemove, forwardmove);

        return false;
    }

    @Override
    public final boolean preMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        OverbounceMutator.obPrediction = MathHelper.predictOverbounce (player);

        float momentumRetention = QuakeClientPlayer.getSlipperiness (player);

        if (player.onGround && ! QuakeClientPlayer.isJumping (player))
            QuakeClientPlayer.minecraft_ApplyFriction (player, momentumRetention);

        return false;
    }

    @Override
    public final boolean postMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        // HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
        QuakeClientPlayer.minecraft_ApplyGravity (player);

        return false;
    }

    @Nullable
    @Override
    public Set<MovementInput> listenTo() {
        return null;
    }

    @Override
    public MutatorType getType() {
        return MutatorType.MovementBase;
    }



    public static void quake_Accelerate(EntityPlayer player, float wishspeed, float cap, double wishX, double wishZ, double accel)
    {
        double addspeed, accelspeed, currentspeed;

        // Determine veer amount
        // this is a dot product
        currentspeed = player.motionX * wishX + player.motionZ * wishZ;

        // See how much to add
        addspeed = wishspeed - currentspeed;

        // If not adding any, done.
        if (addspeed <= 0)
            return;

        // Determine acceleration speed after acceleration
        accelspeed = accel * wishspeed / (QuakeClientPlayer.getSlipperiness(player) * 0.05F);

        // Cap it
        if (accelspeed > addspeed)
            accelspeed = addspeed;

        // Adjust pmove vel.
        player.motionX += accelspeed * wishX;
        player.motionZ += accelspeed * wishZ;

        if (cap > 0.f && (player.motionX * player.motionX + player.motionZ * player.motionZ) > (cap * cap)) {
            Vec3d hardCappedVel = new Vec3d (player.motionX, 0.0, player.motionZ).normalize().scale (cap);
            player.motionX = hardCappedVel.x;
            player.motionZ = hardCappedVel.z;
        }
    }
    public static void quake_AirAccelerate(EntityPlayer player, float wishspeed, float accel, float maxAccel, float cap, double wishX, double wishZ, boolean strafe, boolean forward)
    {
        double addspeed, accelspeed, currentspeed;

        float wishspd = wishspeed;

        if (wishspd > maxAccel)
            wishspd = maxAccel;

        // Determine veer amount
        // this is a dot product
        currentspeed = player.motionX * wishX + player.motionZ * wishZ;

        // See how much to add
        addspeed = wishspd - currentspeed;

        // If not adding any, done.
        if (addspeed <= 0)
            return;

        // Determine acceleration speed after acceleration
        accelspeed = accel * wishspeed * 0.05F;

        // Cap it
        if (accelspeed > addspeed)
            accelspeed = addspeed;

        // Adjust pmove vel.
        player.motionX += accelspeed * wishX;
        player.motionZ += accelspeed * wishZ;

        if (cap > 0.f && (player.motionX * player.motionX + player.motionZ * player.motionZ) > (cap * cap)) {
            Vec3d hardCappedVel = new Vec3d (player.motionX, 0.0, player.motionZ).normalize().scale (cap);
            player.motionX = hardCappedVel.x;
            player.motionZ = hardCappedVel.z;
        }
    }
}
