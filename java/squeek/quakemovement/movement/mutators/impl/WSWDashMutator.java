package squeek.quakemovement.movement.mutators.impl;

import com.google.common.collect.Sets;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;

import javax.annotation.Nullable;
import java.util.Set;

public class WSWDashMutator extends Mutator {
    Vec3d prevPlayerVel = Vec3d.ZERO;

    @Override
    public boolean groundMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        if (wishdir == Vec3d.ZERO)
            wishdir = QuakeClientPlayer.getMovementDirection (player, 0.f, 1.f).normalize ();
        double playerVel = prevPlayerVel.length ();

        if (playerVel < 0.4165625)
            playerVel = 0.4165625;

        wishdir = wishdir.scale (playerVel);

        player.jump ();
        player.onGround = false;

        player.motionX = wishdir.x;
        player.motionY *= 0.75f;
        player.motionZ = wishdir.z;

        return true;
    }

    @Override
    public boolean airMove(EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input) {
        return false;
    }

    @Override
    public boolean preMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        prevPlayerVel = new Vec3d (player.motionX, 0.0, player.motionZ);
        return false;
    }

    @Override
    public boolean postMove(EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input) {
        return false;
    }

    @Nullable
    @Override
    public Set<MovementInput> listenTo() {
        return Sets.immutableEnumSet (MovementInput.SNEAK);
    }

    @Override
    public MutatorType getType() {
        return MutatorType.MovementOverride;
    }
}
