package squeek.quakemovement;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.handler.ConfigPacket;
import squeek.quakemovement.handler.DrawHUDHandler;
import squeek.quakemovement.handler.NetworkHandler;
import squeek.quakemovement.movement.MovementSet;
import squeek.quakemovement.movement.QuakeClientPlayer;
import squeek.quakemovement.movement.mutators.Mutator;
import squeek.quakemovement.movement.mutators.impl.*;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, name="SPMquake", acceptedMinecraftVersions="[1.12.2]", dependencies = "after:squeedometer", guiFactory = ModInfo.CONFIG_GUI_FACTORY_CLASS)
public class ModQuakeMovement
{
	@Instance(value = ModInfo.MODID)
	public static ModQuakeMovement instance;

	public static IForgeRegistry <Mutator> mutatorRegistry = null;

	public ModQuakeMovement () {
		MinecraftForge.EVENT_BUS.register (this);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModConfig.init (event.getSuggestedConfigurationFile ());
		MinecraftForge.EVENT_BUS.register (new ModConfig ());
		MinecraftForge.EVENT_BUS.register (new DrawHUDHandler ());
		FMLCommonHandler.instance ().bus ().register (this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkHandler.init ();
	}

	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		if (!(event.getEntityLiving() instanceof EntityPlayer))
			return;

		if (ModConfig.VALUES.INCREASED_FALL_DISTANCE != 0.0D)
		{
			event.setDistance((float) (event.getDistance() - ModConfig.VALUES.INCREASED_FALL_DISTANCE));
		}
	}

	@SubscribeEvent
	public void playerLogout(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		if (ModConfig.CLIENT_VALUES != null) {
			ModConfig.VALUES = ModConfig.CLIENT_VALUES;
			ModConfig.CLIENT_VALUES = null;
		}
	}

	@SubscribeEvent
	public void playerLoginServer(final PlayerEvent.PlayerLoggedInEvent event) {
		NetworkHandler.INSTANCE.sendTo (new ConfigPacket (ModConfig.VALUES), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public void playerJump (LivingEvent.LivingJumpEvent event) {
		if (event.getEntity () instanceof EntityPlayer && event.getEntity().world.isRemote && shouldDoQuakeMovement ((EntityPlayer) event.getEntity()))
			QuakeClientPlayer.doHungerJump ((EntityPlayer) event.getEntity ());
	}

	@SubscribeEvent
	public void createRegistries (RegistryEvent.NewRegistry event) {
		mutatorRegistry = new RegistryBuilder<Mutator> ()
				.setName (new ResourceLocation (ModInfo.MODID, "mutator_registry"))
				.setType (Mutator.class)
				.setIDRange (0, Integer.MAX_VALUE - 1)
				.create ();
	}

	@SubscribeEvent
	public void registerMutators (RegistryEvent.Register <Mutator> event) {
		mutatorRegistry.register (new Q1MovementBase ().setRegistryName           (new ResourceLocation (ModInfo.MODID, "q1_movement")));
		mutatorRegistry.register (new Q3MovementBase ().setRegistryName           (new ResourceLocation (ModInfo.MODID, "q3_movement")));
		mutatorRegistry.register (new QAnisotropicMovementBase ().setRegistryName (new ResourceLocation (ModInfo.MODID, "qani_movement")));


		mutatorRegistry.register (new CPMAirSteerMutator ().setRegistryName       (new ResourceLocation (ModInfo.MODID, "cpm_airsteer")));
		mutatorRegistry.register (new GroundBoostMutator ().setRegistryName       (new ResourceLocation (ModInfo.MODID, "ground_boost")));
		mutatorRegistry.register (new RampJumpMutator ().setRegistryName          (new ResourceLocation (ModInfo.MODID, "ramp_jump")));
		mutatorRegistry.register (new WallClipMutator ().setRegistryName          (new ResourceLocation (ModInfo.MODID, "wall_clip")));
		mutatorRegistry.register (new WSWDashMutator ().setRegistryName           (new ResourceLocation (ModInfo.MODID, "warsow_dash")));
		mutatorRegistry.register (new ViewBobMutator ().setRegistryName           (new ResourceLocation (ModInfo.MODID, "view_bobbing")));
	}

	private static String getQMovementTag (ItemStack item) {
		return item.hasTagCompound () ? item.getTagCompound ().getString ("QMovement") : MovementSet.VANILLA_MOVEMENT;
	}

	private static String unmarshalIntoSet (String marshalled, String inBase, List<String> overrides, List <String> passives) {
		MovementSet.MovementSetRepresentation repr = MovementSet.gson.fromJson (marshalled, MovementSet.MovementSetRepresentation.class);

		if (repr == null)
			return inBase;

		if (repr.base != null && ! repr.base.equals ("vanilla") && inBase.equals ("vanilla"))
			inBase = repr.base;

		overrides.addAll(Lists.newArrayList(repr.overrides));
		passives.addAll(Lists.newArrayList(repr.passives));
		return inBase;
	}

	ItemStack previousHeldItem = ItemStack.EMPTY;
	ItemStack previousBoots = ItemStack.EMPTY;
	String currentHeldItemMovement = MovementSet.VANILLA_MOVEMENT;
	String currentBootsMovement = MovementSet.VANILLA_MOVEMENT;

	@SubscribeEvent
	public void playerTick (TickEvent.PlayerTickEvent tick) {
		if (tick.side != Side.CLIENT || tick.phase != TickEvent.Phase.START)
			return;

		EntityPlayer player = tick.player;
		ArrayList<ItemStack> playerEquipment = Lists.newArrayList (player.getEquipmentAndArmor ());
		ItemStack heldItem = playerEquipment.get (0);
		ItemStack boots    = playerEquipment.get (2);

		String heldItemMovementOverride = getQMovementTag (heldItem);
		String bootsMovementOverride = getQMovementTag (boots);

		if (
			// Primary equipped item or boots were swapped
				((heldItem != previousHeldItem && ! heldItem.getItem ().isValidArmor (heldItem, EntityEquipmentSlot.FEET, player)) || boots != previousBoots)
				&&
			// The movement physics of the held item or boots is different from the current ones
				(! heldItemMovementOverride.equals (currentHeldItemMovement) || ! bootsMovementOverride.equals (currentBootsMovement))) {
			ArrayList <String> overrides = new ArrayList <> ();
			ArrayList <String> passives =  new ArrayList<> ();
			String base = "vanilla";
			base = unmarshalIntoSet (heldItemMovementOverride, base, overrides, passives);
			base = unmarshalIntoSet (bootsMovementOverride, base, overrides, passives);
			base = unmarshalIntoSet (ModConfig.VALUES.MOVEMENT_SET, base, overrides, passives);

			currentHeldItemMovement = heldItemMovementOverride;
			currentBootsMovement = bootsMovementOverride;

			if (base.equals ("vanilla")) {
				QuakeClientPlayer.movementPhysics.mutators.clear ();
			} else {
				MovementSet.MovementSetRepresentation mutatedSet = new MovementSet.MovementSetRepresentation (
						base,
						overrides.toArray (new String[0]),
						passives.toArray  (new String[0]));

				String mutatedSetStr = MovementSet.gson.toJson (mutatedSet, MovementSet.MovementSetRepresentation.class);

				QuakeClientPlayer.movementPhysics = new MovementSet (mutatedSetStr);
			}
		}

		previousHeldItem = heldItem;
		previousBoots = boots;
	}

	public static boolean shouldDoQuakeMovement (EntityPlayer player) {
		return ModConfig.VALUES.ENABLED && ! QuakeClientPlayer.movementPhysics.mutators.isEmpty ();
	}
}
