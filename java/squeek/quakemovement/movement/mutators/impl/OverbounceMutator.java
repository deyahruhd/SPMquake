package squeek.quakemovement.movement.mutators.impl;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.helper.MathHelper;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class OverbounceMutator extends Mutator {
    public static final double OB_MAX_HEIGHT = (7.0 * 4.30 / 320.0);
    public static boolean obPrediction = false;
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
        if (player.onGround || player.fallDistance < 3.0)
            return false;
        Vec3d playerVel    = new Vec3d (player.motionX, player.motionY, player.motionZ);
        Vec3d playerHorVel = new Vec3d (player.motionX, 0.0, player.motionZ);
        double velMagnitude    = playerVel.length ();
        double horVelMagnitude = playerHorVel.length ();

        if ((player.posY % 1.0) < OB_MAX_HEIGHT && (player.posY % 1.0) > 0.0 && horVelMagnitude <= (21.30 / 20.0)) {
            // We want to find any block directly below the player (thus activating an overbounce)

            BlockPos floorBlock = MathHelper.getImminentCollisionBlock (player.world, player, playerVel, null, false);

            if (floorBlock != null && player.world.getBlockState (floorBlock).isFullBlock () && floorBlock.getY () == (int) Math.floor (player.posY - 1.0)) {
                Vec3d clipped = q3ClipVel (playerVel, new Vec3d (0.0, 1.0, 0.0), 1.001f);
                Vec3d newVel = clipped.normalize ().scale (velMagnitude);

                player.motionX = newVel.x;
                player.motionY = newVel.y;
                player.motionZ = newVel.z;

                QuakeClientPlayer.minecraft_ApplyGravity (player);
            }
        }

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

    private Vec3d q3ClipVel (Vec3d playerVel, Vec3d normal, float overbounce) {
        double backoff = playerVel.dotProduct (normal);

        if ( backoff < 0 )
            backoff *= overbounce;
        else
            backoff /= overbounce;

        return playerVel.subtract (normal.scale (backoff));
    }
}
