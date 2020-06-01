package squeek.quakemovement.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;
import squeek.quakemovement.movement.mutators.impl.GroundBoostMutator;
import squeek.quakemovement.movement.mutators.impl.OverbounceMutator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MathHelper {
    public static double logScale(double x, double pow) {
        return (Math.log (x + Math.exp (-pow)) + pow) / pow;
    }

    public static BlockPos getImminentCollisionBlock (World world, EntityPlayer player, Vec3d vel, @Nullable Vec3d pos, boolean doTop) {
        Vec3d playerPos = pos;
        if (playerPos == null)
            playerPos = player.getPositionVector ();

        double playerWidth = (player.width / 2.0) * 0.999;
        RayTraceResult lowerBlock = null, upperBlock = null;

        // Check lower bounding box (corresponding with lower)
        List<Vec3d> vecs = new ArrayList <>();
        vecs.add (playerPos.add ( playerWidth, 0.0,  playerWidth));
        vecs.add (playerPos.add ( playerWidth, 0.0, -playerWidth));
        vecs.add (playerPos.add (-playerWidth, 0.0, -playerWidth));
        vecs.add (playerPos.add (-playerWidth, 0.0,  playerWidth));

        for (Vec3d v : vecs) {
            lowerBlock = world.rayTraceBlocks (v, v.add (vel));

            if (lowerBlock != null && lowerBlock.typeOfHit != RayTraceResult.Type.MISS)
                break;
            else
                lowerBlock = null;
        }

        if (doTop) {
            vecs.clear();
            vecs.add (playerPos.add ( playerWidth, player.height,  playerWidth));
            vecs.add (playerPos.add ( playerWidth, player.height, -playerWidth));
            vecs.add (playerPos.add (-playerWidth, player.height, -playerWidth));
            vecs.add (playerPos.add (-playerWidth, player.height,  playerWidth));

            for (Vec3d v : vecs) {
                upperBlock = world.rayTraceBlocks (v, v.add (vel));

                if (upperBlock != null && upperBlock.typeOfHit != RayTraceResult.Type.MISS)
                    break;
                else
                    upperBlock = null;
            }
        }

        if (lowerBlock != null)
            return lowerBlock.getBlockPos ();
        else if (upperBlock != null)
            return upperBlock.getBlockPos ();
        else
            return null;
    }

    public static boolean predictOverbounce (EntityPlayer player) {
        boolean overbounceEnabled = false;

        for (Mutator m : QuakeClientPlayer.movementPhysics.mutators) {
            if (m instanceof OverbounceMutator)
                overbounceEnabled = true;
        }

        if (! overbounceEnabled)
            return false;

        // Get the block below the player
        BlockPos pos = getImminentCollisionBlock (player.world, player, new Vec3d (0.0, -256.0, 0.0), null, false);

        if (! player.onGround && player.fallDistance >= 1.5 && pos != null && player.world.getBlockState (pos).isFullBlock ()) {
            int steps = 0;
            double playerPosY = player.posY;
            double playerVelY = player.motionY;
            double blockY     = pos.getY () + 1.0;
            while (playerPosY >= blockY && steps < 10000) {
                // Pre move: Determine overbounce

                Vec3d playerVel    = new Vec3d (player.motionX, player.motionY, player.motionZ);
                Vec3d playerHorVel = new Vec3d (player.motionX, 0.0, player.motionZ);
                double horVelMagnitude = playerHorVel.length ();

                if ((playerPosY % 1.0) < OverbounceMutator.OB_MAX_HEIGHT &&
                    (playerPosY % 1.0) > 0.0 && horVelMagnitude <= (21.30 / 20.0) &&
                    pos.getY () == (int) Math.floor (playerPosY - 1.0))
                        return true;

                // Move step:
                playerPosY += playerVelY;

                // Post move step: Apply gravity
                if (player.world.isRemote && (!player.world.isBlockLoaded(new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ)) || !player.world.getChunk(new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ)).isLoaded())) {
                    return false;
                }
                double scaledGravity = (0.08D *
                        net.minecraft.util.math.MathHelper.clamp((float) (System.currentTimeMillis() - GroundBoostMutator.playerAirbornTime) /
                                    ((float) ModConfig.VALUES.KNOCKBACK_SLICK_TIME * GroundBoostMutator.explosionStr), 0.00F, 1.0F));
                playerVelY -= scaledGravity;

                // playerVelY *= 0.9800000190734863D;

                steps ++;
            }
        }
        return false;
    }
}
