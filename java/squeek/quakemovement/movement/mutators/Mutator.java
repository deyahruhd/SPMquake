package squeek.quakemovement.movement.mutators;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Set;

/**
 A base movement mutator class. Upon each call of quake_moveEntityWithHeading in QuakeClientPlayer,
 every enabled mutator's preMove and postMove methods are called depending on the movement inputs being specified.


 */
public abstract class Mutator {
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
        FORWARD,
        FOR_RIGHT,
        RIGHT,
        BACK_RIGHT,
        BACK,
        BACK_LEFT,
        LEFT,
        FOR_LEFT,
        JUMP,
        SNEAK,
        ITEM_USE,
        ITEM_SWING
    }

    /**
     * Movement method called before the invocation of player.move ().
     *
     * @param player  The client player.
     * @param wishdir The player's wish dir.
     * @param input   If applicable, the MovementInput which triggered this Mutator
     * @return true, if this is a MovementOverride Mutator and the MovementInput should not be propagated further down
     *         the list of active Mutators, or false otherwise.
     */
    public abstract boolean preMove  (EntityPlayerSP player, Vec3d wishdir, MovementInput input);

    /**
     * Movement method called after the invocation of player.move ().
     *
     * @param player  The client player.
     * @param wishdir The player's wish dir.
     * @param input   If applicable, the MovementInput which triggered this Mutator
     * @return true, if this is a MovementOverride Mutator and the MovementInput should not be propagated further down
     *         the list of active Mutators, or false otherwise.
     */
    public abstract boolean postMove (EntityPlayerSP player, Vec3d wishdir, MovementInput input);

    /**
     * The set of all movement inputs this Mutator listens to.
     *
     * For Mutators which return MovementOverride from getType(), they must specify a non-null, non-empty subset of
     * MovementInputs.
     *
     * For Mutators which return MovementBase from getType(), listenTo is not called, and thus can be null.
     *
     * For Mutatotrs which return MovementPassive from getType(), listenTo must return a null set.
     */
    @Nullable
    public abstract Set <MovementInput> listenTo ();

    /**
     * @return The movement type of the Mutator.
     */
    public abstract MutatorType getType ();
}
