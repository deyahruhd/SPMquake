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

	private static final String Q1_AIR_ACCELERATE_NAME = "q1airAccelerate";
	private static final double Q1_AIR_ACCELERATE_DEFAULT = 12.0D;

	private static final String Q3_AIR_ACCELERATE_NAME = "q3airAccelerate";
	private static final double Q3_AIR_ACCELERATE_DEFAULT = 1.0D;

	private static final String INCREASED_FALL_DISTANCE_NAME = "fallDistanceThresholdIncrease";
	private static final double INCREASED_FALL_DISTANCE_DEFAULT = 0.0D;

	private static final String Q1_MAX_AIR_ACCEL_PER_TICK_NAME = "q1maxAirAccelerationPerTick";
	private static final double Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT = 0.03333333D;

	private static final String Q3_MAX_AIR_ACCEL_PER_TICK_NAME = "q3maxAirAccelerationPerTick";
	private static final double Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT = Double.MAX_VALUE;

	private static final String CPM_AIR_STEER_ACCELERATE_NAME = "airSteerAccelerate";
	private static final double CPM_AIR_STEER_ACCELERATE_DEFAULT = 12.0D;

	private static final String CPM_AIR_UNDERSTEER_NAME = "airUndersteer";
	private static final double CPM_AIR_UNDERSTEER_DEFAULT = 0.8D;

	private static final String OVERSPEED = "overspeed";
	private static final double OVERSPEED_DEFAULT = 6.32D;

	private static final String OVERSPEED_EXHAUST_SCALE = "overspeedScaling";
	private static final double OVERSPEED_EXHAUST_SCALE_DEFAULT = 0.15D;

	private static final String KNOCKBACK_TIME = "maxKnockbackSlickTime";
	private static final int KNOCKBACK_TIME_DEFAULT = 250;

	private static final String WALL_CLIP_TIME = "maxWallClipTime";
	private static final int WALL_CLIP_TIME_DEFAULT = 400;

	private static final String RAMP_JUMP_SCALE = "rampJumpScaling";
	private static final double RAMP_JUMP_SCALE_DEFAULT = 0.98;

	private static final String MOVEMENT_SET_JSON = "movementSet";
	private static final String MOVEMENT_SET_JSON_DEFAULT =
			"{\"base\": \"spmquake:qani_movement\", \"overrides\": [\"spmquake:cpm_airsteer\"], \"passives\": [\"spmquake:ground_boost\", \"spmquake:ramp_jump\", \"spmquake:wall_clip\"]}";

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
				config.get(CATEGORY_MOVEMENT, Q1_AIR_ACCELERATE_NAME, Q1_AIR_ACCELERATE_DEFAULT, "acceleration applied when holding only a strafe key").getDouble(Q1_AIR_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q3_AIR_ACCELERATE_NAME, Q3_AIR_ACCELERATE_DEFAULT, "acceleration applied when holding forward + a strafe key").getDouble(Q3_AIR_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q1_MAX_AIR_ACCEL_PER_TICK_NAME, Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT, "maximum speed attainable per tick when holding only a strafe key").getDouble(Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q3_MAX_AIR_ACCEL_PER_TICK_NAME, Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT, "maximum speed attainable per tick when holding forward + a strafe key").getDouble(Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT),
				config.get(CATEGORY_MOVEMENT, CPM_AIR_STEER_ACCELERATE_NAME, CPM_AIR_STEER_ACCELERATE_DEFAULT, "acceleration applied while holding only forward").getDouble(CPM_AIR_STEER_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, CPM_AIR_UNDERSTEER_NAME, CPM_AIR_UNDERSTEER_DEFAULT, "W steering turn radius factor; lower values result in easier W turning, but result in more understeering with sharp turns").setMinValue (0.0).setMaxValue (1.0).getDouble(CPM_AIR_STEER_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, OVERSPEED, OVERSPEED_DEFAULT, "minimum speed before receiving hunger costs from oversped jumps").getDouble(OVERSPEED_DEFAULT),
				config.get(CATEGORY_MOVEMENT, OVERSPEED_EXHAUST_SCALE, OVERSPEED_EXHAUST_SCALE_DEFAULT, "scaling of hunger cost from oversped jumps").getDouble(OVERSPEED_EXHAUST_SCALE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, KNOCKBACK_TIME, KNOCKBACK_TIME_DEFAULT, "number of milliseconds a player is slicked for after receiving knockback").getInt(KNOCKBACK_TIME_DEFAULT),
				config.get(CATEGORY_MOVEMENT, WALL_CLIP_TIME, WALL_CLIP_TIME_DEFAULT, "number of milliseconds during which a player's momentum is preserved after a jump").getInt(WALL_CLIP_TIME_DEFAULT),
				config.get(CATEGORY_MOVEMENT, RAMP_JUMP_SCALE, RAMP_JUMP_SCALE_DEFAULT, "scaling of ramp jump speed after hitting stairs").setMinValue (0.0).setMaxValue (1.0).getDouble (RAMP_JUMP_SCALE_DEFAULT),
				(float) (config.get(CATEGORY_MOVEMENT, INCREASED_FALL_DISTANCE_NAME, INCREASED_FALL_DISTANCE_DEFAULT, "increases the distance needed to fall in order to take fall damage; this is a server-side setting").getDouble(INCREASED_FALL_DISTANCE_DEFAULT)),
				config.get(CATEGORY_MOVEMENT, MOVEMENT_SET_JSON, MOVEMENT_SET_JSON_DEFAULT, "the JSON string representation of a combination of movement mutators").getString()
		);

		save();
	}

	public static void setEnabled(boolean enabled)
	{
		VALUES = new ModStubConfig (
				enabled,
				VALUES.JUMP_INDICATORS_MODE,
				VALUES.ACCELERATE,
				VALUES.Q1_AIR_ACCELERATE,
				VALUES.Q3_AIR_ACCELERATE,
				VALUES.Q1_MAX_AIR_ACCEL_PER_TICK,
				VALUES.Q3_MAX_AIR_ACCEL_PER_TICK,
				VALUES.CPM_AIR_STEER_ACCELERATE,
				VALUES.CPM_AIR_UNDERSTEER,
				VALUES.OVERSPEED,
				VALUES.OVERSPEED_EXHAUSTION_SCALE,
				VALUES.KNOCKBACK_SLICK_TIME,
				VALUES.WALL_CLIP_TIME,
				VALUES.RAMP_JUMP_SCALE,
				VALUES.INCREASED_FALL_DISTANCE,
				VALUES.MOVEMENT_SET);
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
