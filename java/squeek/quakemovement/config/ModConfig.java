package squeek.quakemovement.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.quakemovement.ModInfo;

import java.io.File;

public class ModConfig
{
	public static final String CATEGORY_MOVEMENT = "movement";

	private static final String ENABLED_NAME = "enabled";
	private static final boolean ENABLED_DEFAULT = true;

	private static final String JUMP_INDICATORS_MODE_NAME = "jumpIndicatorMode";
	private static final int JUMP_INDICATORS_MODE_DEFAULT = 1;

	private static final String ACCELERATE_NAME = "groundAccelerate";
	private static final double ACCELERATE_DEFAULT = 100.0D;

	private static final String SLIDE_ACCELERATE_NAME = "groundAccelerate";
	private static final double SLIDE_ACCELERATE_DEFAULT = 3000.0D;

	private static final String Q1_AIR_ACCELERATE_NAME = "q1airAccelerate";
	private static final double Q1_AIR_ACCELERATE_DEFAULT = 6.0D;

	private static final String Q3_AIR_ACCELERATE_NAME = "q3airAccelerate";
	private static final double Q3_AIR_ACCELERATE_DEFAULT = 1.0D;

	private static final String INCREASED_FALL_DISTANCE_NAME = "fallDistanceThresholdIncrease";
	private static final double INCREASED_FALL_DISTANCE_DEFAULT = 0.0D;

	private static final String Q1_MAX_AIR_ACCEL_PER_TICK_NAME = "q1maxAirAccelerationPerTick";
	private static final double Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT = 0.05D;

	private static final String Q3_MAX_AIR_ACCEL_PER_TICK_NAME = "q3maxAirAccelerationPerTick";
	private static final double Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT = 500.0D;

	private static final String OVERSPEED = "overspeed";
	private static final double OVERSPEED_DEFAULT = 6.32D;

	private static final String OVERSPEED_EXHAUST_SCALE = "overspeedScaling";
	private static final double OVERSPEED_EXHAUST_SCALE_DEFAULT = 0.15D;

	private static final String KNOCKBACK_TICKS = "maxKnockbackSlickTicks";
	private static final int KNOCKBACK_TICKS_DEFAULT = 250;

	private static final String WALL_CLIP_TICKS = "maxWallClipTicks";
	private static final int WALL_CLIP_TICKS_DEFAULT = 400;

	private static final String ARMOR_REQ_NAME = "armorRequirement";
	private static final String ARMOR_REQ_DEFAULT = "";

	public static Configuration config = null;
	public static ModStubConfig VALUES;
	public static ModStubConfig CLIENT_VALUES = null;

	public static void init(File file)
	{
		if (config == null)
		{
			config = new Configuration(file);
			load();
		}
	}

	public static void load() {
		VALUES = new ModStubConfig (
				config.get(CATEGORY_MOVEMENT, ENABLED_NAME, ENABLED_DEFAULT, "whether the quake physics are enabled on the client").getBoolean(ENABLED_DEFAULT),
				config.get(CATEGORY_MOVEMENT, JUMP_INDICATORS_MODE_NAME, JUMP_INDICATORS_MODE_DEFAULT, "selects the crosshair and button indicators to render in the HUD:\n0 - vanilla minecraft\n1 - dot only\n2 - dot and movement key indicators").setMinValue (0).setMaxValue (2).getInt (JUMP_INDICATORS_MODE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, ACCELERATE_NAME, ACCELERATE_DEFAULT, "a higher value means you accelerate faster on the ground").getDouble(ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, SLIDE_ACCELERATE_NAME, SLIDE_ACCELERATE_DEFAULT, "higher slide values allow you to turn sharper while sliding").getDouble(SLIDE_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q1_AIR_ACCELERATE_NAME, Q1_AIR_ACCELERATE_DEFAULT, "acceleration applied when holding only a strafe key").getDouble(Q1_AIR_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q3_AIR_ACCELERATE_NAME, Q3_AIR_ACCELERATE_DEFAULT, "acceleration applied when holding forward + a strafe key").getDouble(Q3_AIR_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q1_MAX_AIR_ACCEL_PER_TICK_NAME, Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT, "maximum speed attainable per tick when holding only a strafe key").getDouble(Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q3_MAX_AIR_ACCEL_PER_TICK_NAME, Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT, "maximum speed attainable per tick when holding forward + a strafe key").getDouble(Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT),
				config.get(CATEGORY_MOVEMENT, OVERSPEED, OVERSPEED_DEFAULT, "minimum speed before receiving hunger costs from oversped jumps").getDouble(OVERSPEED_DEFAULT),
				config.get(CATEGORY_MOVEMENT, OVERSPEED_EXHAUST_SCALE, OVERSPEED_EXHAUST_SCALE_DEFAULT, "minimum speed before receiving hunger costs from oversped jumps").getDouble(OVERSPEED_EXHAUST_SCALE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, KNOCKBACK_TICKS, KNOCKBACK_TICKS_DEFAULT, "number of ticks a player is slicked for after receiving knockback").getInt(KNOCKBACK_TICKS_DEFAULT),
				config.get(CATEGORY_MOVEMENT, WALL_CLIP_TICKS, WALL_CLIP_TICKS_DEFAULT, "number of ticks during which a player's momentum is preserved after a jump").getInt(WALL_CLIP_TICKS_DEFAULT),
				(float) (config.get(CATEGORY_MOVEMENT, INCREASED_FALL_DISTANCE_NAME, INCREASED_FALL_DISTANCE_DEFAULT, "increases the distance needed to fall in order to take fall damage; this is a server-side setting").getDouble(INCREASED_FALL_DISTANCE_DEFAULT)),
				config.get(CATEGORY_MOVEMENT, ARMOR_REQ_NAME, ARMOR_REQ_DEFAULT, "the fully-qualified name of an armor piece that should activate the movement for a player (for example, \"minecraft:diamond_boots\")").getString()
		);

		save();
	}

	public static void setEnabled(boolean enabled)
	{
		VALUES = new ModStubConfig (
				enabled,
				VALUES.JUMP_INDICATORS_MODE,
				VALUES.ACCELERATE,
				VALUES.SLIDE_ACCELERATE,
				VALUES.Q1_AIR_ACCELERATE,
				VALUES.Q3_AIR_ACCELERATE,
				VALUES.Q1_MAX_AIR_ACCEL_PER_TICK,
				VALUES.Q3_MAX_AIR_ACCEL_PER_TICK,
				VALUES.OVERSPEED,
				VALUES.OVERSPEED_EXHAUSTION_SCALE,
				VALUES.KNOCKBACK_SLICK_TICKS,
				VALUES.WALL_CLIP_TICKS,
				VALUES.INCREASED_FALL_DISTANCE,
				VALUES.ARMOR_REQ);
		save ();
	}

	public static void save ()
	{
		if (config.hasChanged())
		{
			config.save();
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equalsIgnoreCase(ModInfo.MODID))
		{
			load();
		}
	}
}
