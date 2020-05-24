package squeek.quakemovement.movement;

import com.google.gson.Gson;
import net.minecraft.util.ResourceLocation;
import squeek.quakemovement.ModInfo;
import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.movement.mutators.Mutator;
import squeek.quakemovement.movement.mutators.impl.ViewBobMutator;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MovementSet {
    public static final Gson gson = new Gson ();
    public static final String VANILLA_MOVEMENT = "{\"base\":\"vanilla\",\"overrides\":[],\"passives\":[]}";
    public SortedSet<Mutator> mutators = new TreeSet<> (new Mutator.MutatorComparator ());

    private Mutator movementBase = null;

    public MovementSet (Mutator ... mutatorArgs) {
        if (mutatorArgs == null || mutatorArgs.length == 0)
            return;

        for (Mutator m : mutatorArgs) {
            mutators.add (m);
            if (m.getType () == Mutator.MutatorType.MovementBase)
                movementBase = m;
        }

        mutators.add (ModQuakeMovement.mutatorRegistry.getValue (new ResourceLocation (ModInfo.MODID, "view_bobbing")));

        if (movementBase == null)
            mutators.clear ();
    }

    public MovementSet (String marshalled) {
        MovementSetRepresentation msr = gson.fromJson (marshalled, MovementSetRepresentation.class);

        if (msr.base.equals ("vanilla"))
            return;

        movementBase = ModQuakeMovement.mutatorRegistry.getValue (new ResourceLocation (msr.base));
        mutators.add (movementBase);

        for (String overrideRegistryName : msr.overrides)
            mutators.add (ModQuakeMovement.mutatorRegistry.getValue (new ResourceLocation (overrideRegistryName)));
        for (String passiveRegistryName : msr.passives)
            mutators.add (ModQuakeMovement.mutatorRegistry.getValue (new ResourceLocation (passiveRegistryName)));

        mutators.add (ModQuakeMovement.mutatorRegistry.getValue (new ResourceLocation (ModInfo.MODID, "view_bobbing")));

        if (movementBase == null)
            mutators.clear ();
    }

    public String marshal () {
        if (mutators.size () == 0)
            return VANILLA_MOVEMENT;

        ArrayList <String> overrides = new ArrayList<>();
        ArrayList <String> passives  = new ArrayList<>();

        for (Mutator m : mutators) {
            String registryName = m.getRegistryName ().toString ();

            if (m.getType () == Mutator.MutatorType.MovementOverride)
                overrides.add (registryName);
            else if (m.getType () == Mutator.MutatorType.MovementPassive && ! (m instanceof ViewBobMutator))
                passives.add (registryName);
        }

        MovementSetRepresentation msr = new MovementSetRepresentation (
                movementBase.getRegistryName ().toString (),
                overrides.toArray (new String [overrides.size ()]),
                passives.toArray  (new String [passives.size ()])
                );

        return gson.toJson (msr);
    }

    public static class MovementSetRepresentation {
        public MovementSetRepresentation (String base, String [] overrides, String [] passives) {
            this.base = base;

            if (overrides == null || overrides.length == 0)
                this.overrides = new String [0];
            else
                this.overrides = overrides;

            if (passives == null || overrides.length == 0)
                this.passives = new String [0];
            else
                this.passives = passives;
        }

        public final String base;
        public final String [] overrides;
        public final String [] passives;
    }
}
