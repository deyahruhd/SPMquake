package squeek.quakemovement.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MathHelper {
    public static double logScale(double x, double pow) {
        return (Math.log (x + Math.exp (-pow)) + pow) / pow;
    }

    public static BlockPos getImminentCollisionBlock (World world, EntityPlayer player, Vec3d vel, boolean doTop) {
        double playerWidth = player.width * 0.99 / 2.0;
        RayTraceResult lowerBlock = null, upperBlock = null;

        // Check lower bounding box (corresponding with lower)
        List<Vec3d> vecs = new ArrayList <>();
        vecs.add (player.getPositionVector ().add ( playerWidth, 0.0,  playerWidth));
        vecs.add (player.getPositionVector ().add ( playerWidth, 0.0, -playerWidth));
        vecs.add (player.getPositionVector ().add (-playerWidth, 0.0, -playerWidth));
        vecs.add (player.getPositionVector ().add (-playerWidth, 0.0,  playerWidth));

        for (Vec3d v : vecs) {
            lowerBlock = world.rayTraceBlocks (v, v.add (vel));

            if (lowerBlock != null && lowerBlock.typeOfHit != RayTraceResult.Type.MISS)
                break;
            else
                lowerBlock = null;
        }

        if (doTop) {
            vecs.clear();
            vecs.add(player.getPositionVector().add(playerWidth, player.height, playerWidth));
            vecs.add(player.getPositionVector().add(playerWidth, player.height, -playerWidth));
            vecs.add(player.getPositionVector().add(-playerWidth, player.height, -playerWidth));
            vecs.add(player.getPositionVector().add(-playerWidth, player.height, playerWidth));

            for (Vec3d v : vecs) {
                upperBlock = world.rayTraceBlocks (v, v.add (vel));

                if (upperBlock != null && upperBlock.typeOfHit != RayTraceResult.Type.MISS)
                    break;
                else
                    upperBlock = null;
            }
        }

        // Exclusive or between the two bounding boxes. If both upper and lower traces results in a collided block, then
        // the player naturally collides with both of them, and so we treat them as a solid block wall

        // This condition is primarily for ramp jumps
        if (lowerBlock != null && upperBlock == null)
            return lowerBlock.getBlockPos ();
        else if (lowerBlock == null && upperBlock != null)
            return upperBlock.getBlockPos ();
        else
            return null;
    }
}
