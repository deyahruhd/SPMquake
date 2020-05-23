package squeek.quakemovement.movement.mutators;

import com.google.common.collect.Sets;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.IForgeRegistryEntry;
import squeek.quakemovement.movement.mutators.impl.ViewBobMutator;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Set;

/**
 A base movement mutator class. Upon each call of quake_moveEntityWithHeading in QuakeClientPlayer,
 every enabled mutator's preMove and postMove methods are called depending on the movement inputs being specified.


 */
public abstract class Mutator extends IForgeRegistryEntry.Impl <Mutator> {
    public enum MutatorType {
        MovementOverride,
        MovementBase,
        MovementPassive
    }

    /**
     * The movement inputs which the Mutator should listen to.
     *
     * When the player specifies a MovementInput using the keyboard or mouse, and an applicable Mutator listens to that
     * MovementInput as specified in listenTo(), quake_moveEntityWithEntity calls its preMove
     * and postMove methods with that MovementInput.
     *
     * The Mutator can then test MovementInput and activate their effects accordingly.
     */
    public enum MovementInput {
        FORWARD    (false,  true),
        FOR_RIGHT  (true,   true),
        RIGHT      (true,   false),
        BACK_RIGHT (true,   true),
        BACK       (false,  true),
        BACK_LEFT  (true,   true),
        LEFT       (true,   false),
        FOR_LEFT   (true,   true),
        JUMP       (false,  false),
        SNEAK      (false,  false),
        ITEM_USE   (false,  false),
        ITEM_SWING (false,  false);

        boolean strafe, forward;

        MovementInput (boolean isStrafe, boolean isForward) {
            strafe = isStrafe;
            forward = isForward;
        }
    }

    /**
     * Movement methods called when accelerating the player while they are on the ground or in the air, respectively.
     *
     * @param  player    The client player.
     * @param  wishdir   The player's wish dir.
     * @param  wishspeed The player's wish speed.
     * @param  input     If applicable, the MovementInput which triggered this Mutator
     * @return true, if this is a MovementOverride Mutator and the MovementInput should not be propagated further down
     *         the list of active Mutators, or false otherwise.
     */
    public abstract boolean groundMove (EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input);
    public abstract boolean airMove    (EntityPlayerSP player, Vec3d wishdir, float wishspeed, @Nullable MovementInput input);

    /**
     * Movement methods called before the invocation of any acceleration methods. (e.g. groundMove, airMove)
     *
     * @param player  The client player.
     * @param wishdir The player's wish dir.
     * @param input   If applicable, the MovementInput which triggered this Mutator
     * @return true, if this is a MovementOverride Mutator and the MovementInput should not be propagated further down
     *         the list of active Mutators, or false otherwise.
     */
    public abstract boolean preMove (EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input);
    public abstract boolean postMove (EntityPlayerSP player, Vec3d wishdir, @Nullable MovementInput input);

    /**
     * The set of all movement inputs this Mutator listens to.
     *
     * For Mutators which return MovementOverride from getType(), they must specify a non-null, non-empty subset of
     * MovementInputs.
     *
     * For Mutators which return MovementBase from getType(), listenTo is not called, and thus can be null.
     *
     * For Mutators which return MovementPassive from getType(), listenTo must return a null set.
     */
    @Nullable
    public abstract Set <MovementInput> listenTo ();

    /**
     * @return The movement type of the Mutator.
     */
    public abstract MutatorType getType ();

    public static class MutatorComparator implements Comparator<Mutator> {
        @Override
        public int compare(Mutator o1, Mutator o2) {
            // View bobbing is always last in the list
            if (o1 instanceof ViewBobMutator && o2 instanceof ViewBobMutator)
                return 0;
            if (o2 instanceof ViewBobMutator)
                return -1;
            if (o1 instanceof ViewBobMutator)
                return 1;

            if (o1.getType () != o2.getType())
                return o1.getType ().compareTo (o2.getType ());
            else
                // Fallback: Since ordering of mutators of the same type doesn't really matter at this point
                return Integer.compareUnsigned (o1.hashCode(), o2.hashCode ());
        }
    }

    public static Set<MovementInput> BASE_INPUT_SET = Sets.immutableEnumSet (MovementInput.FORWARD, MovementInput.FOR_LEFT,
                                                                             MovementInput.LEFT, MovementInput.BACK_LEFT,
                                                                             MovementInput.BACK, MovementInput.BACK_RIGHT,
                                                                             MovementInput.RIGHT, MovementInput.FOR_RIGHT);
}
