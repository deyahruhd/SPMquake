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

	private static final String ACCELERATE_NAME = "groundAccelerate";
	private static final double ACCELERATE_DEFAULT = 100.0D;

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

	private static Property ENABLED_PROPERTY;
	private static final String ENABLED_NAME = "enabled";
	private static final boolean ENABLED_DEFAULT = true;

	private static Property JUMP_INDICATORS_ENABLED_PROPERTY;
	private static final String JUMP_INDICATORS_ENABLED_NAME = "jumpIndicatorsEnabled";
	private static final boolean JUMP_INDICATORS_ENABLED_DEFAULT = true;

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
		ENABLED_PROPERTY = config.get(CATEGORY_MOVEMENT, ENABLED_NAME, ENABLED_DEFAULT, "turns off/on the quake-style movement for the client");
		JUMP_INDICATORS_ENABLED_PROPERTY = config.get(CATEGORY_MOVEMENT, JUMP_INDICATORS_ENABLED_NAME, JUMP_INDICATORS_ENABLED_DEFAULT, "enables the on-screen jump press HUD elements");

		VALUES = new ModStubConfig (
				ENABLED_PROPERTY.getBoolean(ENABLED_DEFAULT),
				JUMP_INDICATORS_ENABLED_PROPERTY.getBoolean(JUMP_INDICATORS_ENABLED_DEFAULT),
				config.get(CATEGORY_MOVEMENT, ACCELERATE_NAME, ACCELERATE_DEFAULT, "a higher value means you accelerate faster on the ground").getDouble(ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q1_AIR_ACCELERATE_NAME, Q1_AIR_ACCELERATE_DEFAULT, "acceleration applied when holding only a strafe key").getDouble(Q1_AIR_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q3_AIR_ACCELERATE_NAME, Q3_AIR_ACCELERATE_DEFAULT, "acceleration applied when holding forward + a strafe key").getDouble(Q3_AIR_ACCELERATE_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q1_MAX_AIR_ACCEL_PER_TICK_NAME, Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT, "maximum speed attainable per tick when holding only a strafe key").getDouble(Q1_MAX_AIR_ACCEL_PER_TICK_DEFAULT),
				config.get(CATEGORY_MOVEMENT, Q3_MAX_AIR_ACCEL_PER_TICK_NAME, Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT, "maximum speed attainable per tick when holding forward + a strafe key").getDouble(Q3_MAX_AIR_ACCEL_PER_TICK_DEFAULT),
				(float) (config.get(CATEGORY_MOVEMENT, INCREASED_FALL_DISTANCE_NAME, INCREASED_FALL_DISTANCE_DEFAULT, "increases the distance needed to fall in order to take fall damage; this is a server-side setting").getDouble(INCREASED_FALL_DISTANCE_DEFAULT)));

		save();
	}

	public static void setEnabled(boolean enabled)
	{
		VALUES = new ModStubConfig (
				enabled,
				VALUES.JUMP_INDICATORS_ENABLED,
				VALUES.ACCELERATE,
				VALUES.Q1_AIR_ACCELERATE,
				VALUES.Q3_AIR_ACCELERATE,
				VALUES.Q1_MAX_AIR_ACCEL_PER_TICK,
				VALUES.Q3_MAX_AIR_ACCEL_PER_TICK,
				VALUES.INCREASED_FALL_DISTANCE);

		ENABLED_PROPERTY.set (enabled);
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
