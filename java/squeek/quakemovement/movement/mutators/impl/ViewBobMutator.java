package squeek.quakemovement.movement.mutators.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class ViewBobMutator extends Mutator {
    // View bobbing jump duck
    private static long playerGroundLandTime  = 0;
    private static boolean wasOnGround = true;

    private static double prevScaledPlayerWalkDist = -1.f;
    private static double scaledPlayerWalkDist     = -1.f;
    private static float prevPlayerWalkedDist      = -1.f;
    private static float speedScale                = 0.f;
    private static float prevSpeedScale            = 0.f;
    private static float prevPlayerFallDistance    = 0.f;

    private static float linearSpeedScale = 0.f;
    private static float prevLinearSpeedScale = 0.f;

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
        if (player.onGround && ! wasOnGround && prevPlayerFallDistance > 1.0)
            playerGroundLandTime = 0;

        if (Double.isNaN (scaledPlayerWalkDist) || Double.isNaN (prevScaledPlayerWalkDist)) {
            scaledPlayerWalkDist = 0.f;
            prevScaledPlayerWalkDist = 0.f;
        }

        wasOnGround = player.onGround;

        playerGroundLandTime ++;


        double playerSpeed = QuakeClientPlayer.getSpeed (player);

        float rawSpeedScale = (player.onGround && ! QuakeClientPlayer.isJumping (player) && ! player.isSneaking ())
                ? (float) (squeek.quakemovement.helper.MathHelper.logScale (playerSpeed, 2.0) * 2.0) : 0.f;
        float delta = player.distanceWalkedModified - prevPlayerWalkedDist;

        // Update previous variables
        prevPlayerWalkedDist = player.distanceWalkedModified;
        prevLinearSpeedScale = linearSpeedScale;
        prevSpeedScale = speedScale;
        prevScaledPlayerWalkDist = scaledPlayerWalkDist;
        prevPlayerFallDistance = player.fallDistance;

        // Update walked distance
        float scaledDelta = (float) squeek.quakemovement.helper.MathHelper.logScale (delta, 8.0) * 0.9f;
        scaledPlayerWalkDist += scaledDelta;

        // Perform lerps
        speedScale = (float) (speedScale + (rawSpeedScale - speedScale) * 0.22);
        linearSpeedScale = (float) (linearSpeedScale + (QuakeClientPlayer.getSpeed (player) - linearSpeedScale) * 0.22);

        return false;
    }

    @Override
    public boolean postMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
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

    public static boolean applyNormalBobbing (float partialTicks)
    {
        Entity renderEntity = Minecraft.getMinecraft().getRenderViewEntity ();

        return renderEntity instanceof EntityPlayer && ModQuakeMovement.shouldDoQuakeMovement ((EntityPlayer) renderEntity);
    }

    public static boolean applyCustomBobbing (float partialTicks, boolean doSpeedSway) {
        Entity renderEntity = Minecraft.getMinecraft().getRenderViewEntity ();
        if (renderEntity instanceof EntityPlayer && ModQuakeMovement.shouldDoQuakeMovement ((EntityPlayer) renderEntity)) {
            EntityPlayer entityplayer = (EntityPlayer) renderEntity;

            float smoothTime = (entityplayer.ticksExisted + partialTicks) * 0.05f;

            float smoothedSpeedScale = prevSpeedScale + (speedScale - prevSpeedScale) * partialTicks;


            float smoothedLinearSpeedScale = prevLinearSpeedScale + (linearSpeedScale - prevLinearSpeedScale) * partialTicks;
            float smoothedLandDelta = ((playerGroundLandTime - 1) + partialTicks) * 0.3f;
            double smoothedScaledPlayerWalkDist = prevScaledPlayerWalkDist + (scaledPlayerWalkDist - prevScaledPlayerWalkDist) * partialTicks;

            float fasterBob = entityplayer.isSneaking () ||
                              entityplayer.getHeldItemMainhand ().getItem ().getRegistryName ().toString ().hashCode () == -763487865
                              ? 1.5f
                              : 1.125f;

            long hashCode = entityplayer.getHeldItemMainhand ().getItem ().getRegistryName ().toString ().hashCode ();

            if (hashCode == -763487865)
                fasterBob = 1.5f;
            else if (hashCode == -1729522652)
                fasterBob = 0.95f;

            float f1 = -(float) (smoothedScaledPlayerWalkDist) * 0.225f * 0.8f;

            float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            float speedSwayX = (float) Math.cos (smoothTime * 0.31 * Math.PI) * smoothedLinearSpeedScale * 9.4f;
            float speedSwayY = (float) Math.sin (smoothTime * 0.42 * Math.PI) * smoothedLinearSpeedScale * 6.4f;
            float walkSwayX  = MathHelper.sin (f1 * (float) Math.PI * fasterBob) * -5.5f / (fasterBob * fasterBob * fasterBob);
            float walkSwayY  = Math.abs (MathHelper.cos (f1 * (float) Math.PI * fasterBob)) * 2.6666f / fasterBob;


            float jumpDuck = (float) - Math.pow (2.0, - Math.pow (smoothedLandDelta, 1.5)) * smoothedLandDelta * 0.35f;

            if (! doSpeedSway) {
                speedSwayX = 0.0F;
                speedSwayY = 0.0F;
                walkSwayX = 0.0F;
                walkSwayY = 0.0F;
            }

            float in = doSpeedSway ? - jumpDuck : jumpDuck;
            float playerPitch = entityplayer.rotationPitch;
            Vec3d jumpDuckCorrected = new Vec3d (0.0F, - in * Math.cos (playerPitch * Math.PI / 180.0),
                    - in * Math.sin (playerPitch * Math.PI / 180.0));

            GlStateManager.translate(0.0F, jumpDuckCorrected.y, jumpDuckCorrected.z);
            GlStateManager.rotate(( smoothedSpeedScale * MathHelper.sin(f1 * fasterBob * (float)Math.PI * 0.8f) * f2 * 3.0F / fasterBob), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(( Math.abs(smoothedSpeedScale * MathHelper.cos(f1 * fasterBob * (float)Math.PI - 0.2F) * f2) * 5.0F / fasterBob), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(speedSwayX + smoothedSpeedScale * walkSwayX, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(f3 + speedSwayY + smoothedSpeedScale * walkSwayY, 1.0F, 0.0F, 0.0F);
        }
        return false;
    }
}
