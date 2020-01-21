package squeek.quakemovement;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class ModConfig
{
	public static final String CATEGORY_MOVEMENT = "movement";

	public static double ACCELERATE;
	private static final String ACCELERATE_NAME = "groundAccelerate";
	private static final double ACCELERATE_DEFAULT = 30.0D;

	public static double AIR_ACCELERATE;
	private static final String AIR_ACCELERATE_NAME = "airAccelerate";
	private static final double AIR_ACCELERATE_DEFAULT = 30.0D;

	public static double INCREASED_FALL_DISTANCE;
	private static final String INCREASED_FALL_DISTANCE_NAME = "fallDistanceThresholdIncrease";
	private static final double INCREASED_FALL_DISTANCE_DEFAULT = 0.0D;

	public static double MAX_AIR_ACCEL_PER_TICK;
	private static final String MAX_AIR_ACCEL_PER_TICK_NAME = "maxAirAccelerationPerTick";
	private static final double MAX_AIR_ACCEL_PER_TICK_DEFAULT = 0.045D;

	public static boolean ENABLED;
	private static Property ENABLED_PROPERTY;
	private static final String ENABLED_NAME = "enabled";
	private static final boolean ENABLED_DEFAULT = true;

	public static Configuration config;

	public static void init(File file)
	{
		if (config == null)
		{
			config = new Configuration(file);
			load();
		}
	}

	public static void load()
	{
		AIR_ACCELERATE = config.get(CATEGORY_MOVEMENT, AIR_ACCELERATE_NAME, AIR_ACCELERATE_DEFAULT, "a higher value means you can turn more sharply in the air without losing speed").getDouble(AIR_ACCELERATE_DEFAULT);
		MAX_AIR_ACCEL_PER_TICK = config.get(CATEGORY_MOVEMENT, MAX_AIR_ACCEL_PER_TICK_NAME, MAX_AIR_ACCEL_PER_TICK_DEFAULT, "a higher value means faster air acceleration").getDouble(MAX_AIR_ACCEL_PER_TICK_DEFAULT);
		ACCELERATE = config.get(CATEGORY_MOVEMENT, ACCELERATE_NAME, ACCELERATE_DEFAULT, "a higher value means you accelerate faster on the ground").getDouble(ACCELERATE_DEFAULT);

		INCREASED_FALL_DISTANCE = (float) (config.get(CATEGORY_MOVEMENT, INCREASED_FALL_DISTANCE_NAME, INCREASED_FALL_DISTANCE_DEFAULT, "increases the distance needed to fall in order to take fall damage; this is a server-side setting").getDouble(INCREASED_FALL_DISTANCE_DEFAULT));

		ENABLED_PROPERTY = config.get(CATEGORY_MOVEMENT, ENABLED_NAME, ENABLED_DEFAULT, "turns off/on the quake-style movement for the client (essentially the saved value of the ingame toggle keybind)");
		ENABLED = ENABLED_PROPERTY.getBoolean(ENABLED_DEFAULT);

		save();
	}

	public static void setEnabled(boolean enabled)
	{
		ModConfig.ENABLED = enabled;
		ENABLED_PROPERTY.set(enabled);
		save();
	}

	public static void save()
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
