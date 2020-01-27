package squeek.quakemovement.moveimpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.handler.NetworkHandler;
import squeek.quakemovement.handler.HungerJumpPacket;

public class QuakeClientPlayer
{
	private static Random random = new Random();

	private static Method setDidJumpThisTick = null;
	private static Method setIsJumping = null;

	// PGB
	private static long playerAirbornTime     = 0;

	// Wall clipping
	private static long playerGroundTouchTime = 0;

	// Sliding
	private static float playerSlide 		  = -1.f;

	// Ramp jump
	private static double  playerActualVelY    = 0.0;
	private static double  playerRampJumpY     = 0.0;
	private static boolean wasPlayerCollided   = false;

	static
	{
		try
		{
			if (Loader.isModLoaded("squeedometer"))
			{
				Class<?> hudSpeedometer = Class.forName("squeek.speedometer.HudSpeedometer");
				setDidJumpThisTick = hudSpeedometer.getDeclaredMethod("setDidJumpThisTick", boolean.class);
				setIsJumping = hudSpeedometer.getDeclaredMethod("setIsJumping", boolean.class);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static boolean moveEntityWithHeading(EntityPlayer player, float sidemove, float upmove, float forwardmove)
	{
		if (!player.world.isRemote)
			return false;

		if (! ModQuakeMovement.shouldDoQuakeMovement (player))
			return false;

		boolean didQuakeMovement;
		double d0 = player.posX;
		double d1 = player.posY;
		double d2 = player.posZ;

		if ((player.capabilities.isFlying || player.isElytraFlying()) && player.getRidingEntity() == null)
			return false;
		else {
			player.setSprinting (false);
			didQuakeMovement = quake_moveEntityWithHeading(player, sidemove, upmove, forwardmove);
		}

		if (didQuakeMovement)
			player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);

		return didQuakeMovement;
	}

	public static void beforeOnLivingUpdate(EntityPlayer player)
	{
		if (!player.world.isRemote)
			return;

		if (setDidJumpThisTick != null)
		{
			try
			{
				setDidJumpThisTick.invoke(null, false);
			}
			catch (Exception e)
			{
			}
		}

		if (setIsJumping != null)
		{
			try
			{
				setIsJumping.invoke(null, isJumping(player));
			}
			catch (Exception e)
			{
			}
		}
	}

	public static boolean moveRelativeBase(Entity entity, float sidemove, float upmove, float forwardmove, float friction)
	{
		if (!(entity instanceof EntityPlayer))
			return false;

		return moveRelative((EntityPlayer)entity, sidemove, forwardmove, upmove, friction);
	}

	public static boolean moveRelative(EntityPlayer player, float sidemove, float upmove, float forwardmove, float friction)
	{
		if (!player.world.isRemote)
			return false;

		if (!ModQuakeMovement.shouldDoQuakeMovement (player))
			return false;

		if ((player.capabilities.isFlying && player.getRidingEntity() == null) || player.isInWater() || player.isInLava() || player.isOnLadder())
		{
			return false;
		}

		return true;
	}

	public static void afterJump(EntityPlayer player)
	{
		if (!player.world.isRemote)
			return;

		if (!ModQuakeMovement.shouldDoQuakeMovement (player))
			return;

		// undo this dumb thing
		if (player.isSprinting())
		{
			float f = player.rotationYaw * 0.017453292F;
			player.motionX += MathHelper.sin(f) * 0.2F;
			player.motionZ -= MathHelper.cos(f) * 0.2F;
		}

		if (setDidJumpThisTick != null)
		{
			try
			{
				setDidJumpThisTick.invoke(null, true);
			}
			catch (Exception e)
			{
			}
		}
	}

	/* =================================================
	 * START HELPERS
	 * =================================================
	 */

	public static double getSpeed(EntityPlayer player)
	{
		return MathHelper.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
	}

	private static float getSlipperiness(EntityPlayer player)
	{
		long time = System.currentTimeMillis ();

		float f2 = 1.00F;

		// The second condition replicates a mechanic in CPMA known as plasma ground boosting - using plasma
		// near you, right before you land on the ground, grants you a 50-200ms period of 0 friction. This can
		// allow doubling or even tripling your speed with a well timed and quick circle jump.
		// For now it's activated when receiving knockback from an explosion
		if (player.onGround && (time - playerAirbornTime > ModConfig.VALUES.KNOCKBACK_SLICK_TICKS))
		{
			BlockPos groundPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.getEntityBoundingBox().minY) - 1, MathHelper.floor(player.posZ));
			Block ground = player.world.getBlockState(groundPos).getBlock();

			if (ground.slipperiness < 1.0) {
				float curvedSlip = MathHelper.clamp(1.f + (float) Math.pow(ground.slipperiness - 1, 5), 0.f, 1.f);

				f2 = ground.slipperiness * 0.91F;
			}
		}
		return f2;
	}

	private static float minecraft_getMoveSpeed(EntityPlayer player)
	{
		//float f2 = getSlipperiness(player);

		//float f3 = 0.16277136F / (f2 * f2 * f2);

		return player.getAIMoveSpeed(); // * f3;
	}

	private static float[] getMovementDirection(EntityPlayer player, float sidemove, float forwardmove)
	{
		float f3 = sidemove * sidemove + forwardmove * forwardmove;
		float[] dir = {0.0F, 0.0F};

		if (f3 >= 1.0E-4F)
		{
			f3 = MathHelper.sqrt(f3);

			if (f3 < 1.0F)
			{
				f3 = 1.0F;
			}

			f3 = 1.0F / f3;
			sidemove *= f3;
			forwardmove *= f3;
			float f4 = MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F);
			float f5 = MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F);
			dir[0] = (sidemove * f5 - forwardmove * f4);
			dir[1] = (forwardmove * f5 + sidemove * f4);
		}

		return dir;
	}

	private static float quake_getMoveSpeed(EntityPlayer player)
	{
		float baseSpeed = player.getAIMoveSpeed();
		return !player.isSneaking() ? baseSpeed * 2.15F : baseSpeed * 1.11F;
	}

	private static float quake_getMaxMoveSpeed(EntityPlayer player)
	{
		float baseSpeed = player.getAIMoveSpeed();
		return baseSpeed * 2.15F;
	}

	private static void spawnBunnyhopParticles(EntityPlayer player, int numParticles)
	{
		// taken from sprint
		int j = MathHelper.floor(player.posX);
		int i = MathHelper.floor(player.posY - 0.20000000298023224D - player.getYOffset());
		int k = MathHelper.floor(player.posZ);
		IBlockState blockState = player.world.getBlockState(new BlockPos(j, i, k));

		if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE)
		{
			for (int iParticle = 0; iParticle < numParticles; iParticle++)
			{
				player.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, player.posX + (random.nextFloat() - 0.5D) * player.width, player.getEntityBoundingBox().minY + 0.1D, player.posZ + (random.nextFloat() - 0.5D) * player.width, -player.motionX * 4.0D, 1.5D, -player.motionZ * 4.0D, Block.getStateId(blockState));
			}
		}
	}

	private static final Field isJumping = ReflectionHelper.findField(EntityLivingBase.class, "isJumping", "field_70703_bu", "bd");

	private static boolean isJumping(EntityPlayer player)
	{
		try
		{
			return isJumping.getBoolean(player);
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static void applyExplosionToSPPlayer (Explosion e, float str) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		Vec3d ePos = e.getPosition ();
		float sqDist = (float) player.getDistanceSq (ePos.x, ePos.y, ePos.z);
		if (sqDist <= (str * str)) {
			float normalizedDist = MathHelper.sqrt (sqDist) / str;
			Vec3d diff = ePos.subtract (new Vec3d (player.posX, player.posY, player.posZ)).normalize();

			boolean specialCondition = false;

			if (specialCondition) {
				normalizedDist = 1.f - (float) Math.pow (normalizedDist, 4); // Curved accordingly
				normalizedDist *= -0.5f;
			} else {
				normalizedDist = 1.f - (float) Math.pow (normalizedDist, 2); // Curved accordingly
				normalizedDist *= -0.25f;
			}

			diff = diff.scale (str * normalizedDist);

			player.addVelocity (diff.x, diff.y, diff.z);

		}
		playerAirbornTime = System.currentTimeMillis();
	}

	private static void doHungerJump (EntityPlayer e) {
		NetworkHandler.INSTANCE.sendToServer (new HungerJumpPacket (Minecraft.getMinecraft ().player));
	}

	public static void applyJumpVelToEntity (EntityLivingBase e, double speed) {
		if (!(e instanceof EntityPlayer) || !ModQuakeMovement.shouldDoQuakeMovement((EntityPlayer) e))
			e.motionY = speed;
		else if (e.onGround) {
			double prevMotionY = e.motionY;

			e.motionY = speed;

			double rampJumpAdd = 0.0;

			if (playerActualVelY >= 0.0)
				rampJumpAdd = (Math.ceil(playerActualVelY / speed) * speed);

			// Actual velocity is the player's jump velocity without a ramp jump
			playerActualVelY = e.motionY;
			// But set the ramp jump accordingly in case we do end up wall clipping
			playerRampJumpY  = playerActualVelY + rampJumpAdd;

			doHungerJump ((EntityPlayer) e);
		}
	}

	public static void setEntityVelocity(Entity entity, double x, double y, double z) {
		if (entity instanceof EntityPlayerSP) {
			double speed = getSpeed ((EntityPlayer) entity);
			double scale = 1.0 / ((speed + 1.0) * (speed + 1.0));

			entity.addVelocity(x * scale, y * scale, z * scale);
		} else
			entity.setVelocity (x, y, z);
	}

	/* =================================================
	 * END HELPERS
	 * =================================================
	 */

	/* =================================================
	 * START MINECRAFT PHYSICS
	 * =================================================
	 */

	private static void minecraft_ApplyGravity(EntityPlayer player)
	{
		if (player.world.isRemote && (!player.world.isBlockLoaded(new BlockPos((int)player.posX, (int)player.posY, (int)player.posZ)) || !player.world.getChunk (new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ)).isLoaded()))
		{
			if (player.posY > 0.0D)
			{
				player.motionY = -0.1D;
			}
			else
			{
				player.motionY = 0.0D;
			}

			playerActualVelY = player.motionY;
		}
		else
		{
			// gravity
			player.motionY -= 0.08D;
			playerActualVelY -= 0.08D;
		}

		// air resistance
		player.motionY   *= 0.9800000190734863D;
		playerActualVelY *= 0.9800000190734863D;
	}

	private static void minecraft_ApplyFriction(EntityPlayer player, float momentumRetention)
	{
		player.motionX *= momentumRetention;
		player.motionZ *= momentumRetention;
	}

	private static void minecraft_ApplyLadderPhysics(EntityPlayer player)
	{
		if (player.isOnLadder())
		{
			float f5 = 0.15F;

			if (player.motionX < (-f5))
			{
				player.motionX = (-f5);
			}

			if (player.motionX > f5)
			{
				player.motionX = f5;
			}

			if (player.motionZ < (-f5))
			{
				player.motionZ = (-f5);
			}

			if (player.motionZ > f5)
			{
				player.motionZ = f5;
			}

			player.fallDistance = 0.0F;

			if (player.motionY < -0.15D)
			{
				player.motionY = -0.15D;
			}

			boolean flag = player.isSneaking();

			if (flag && player.motionY < 0.0D)
			{
				player.motionY = 0.0D;
			}
		}
	}

	private static void minecraft_ClimbLadder(EntityPlayer player)
	{
		if (player.collidedHorizontally && player.isOnLadder())
		{
			player.motionY = 0.2D;
		}
	}

	private static void minecraft_SwingLimbsBasedOnMovement(EntityPlayer player)
	{
		player.prevLimbSwingAmount = player.limbSwingAmount;
		double d0 = player.posX - player.prevPosX;
		double d1 = player.posZ - player.prevPosZ;
		float f6 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

		if (f6 > 1.0F)
		{
			f6 = 1.0F;
		}

		player.limbSwingAmount += (f6 - player.limbSwingAmount) * 0.4F;
		player.limbSwing += player.limbSwingAmount;
	}

	private static void minecraft_WaterMove(EntityPlayer player, float sidemove, float upmove, float forwardmove)
	{
		double d0 = player.posY;
		player.moveRelative(sidemove, upmove, forwardmove, 0.04F);
		player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);
		player.motionX *= 0.800000011920929D;
		player.motionY *= 0.800000011920929D;
		player.motionZ *= 0.800000011920929D;
		player.motionY -= 0.02D;

		if (player.collidedHorizontally && player.isOffsetPositionInLiquid(player.motionX, player.motionY + 0.6000000238418579D - player.posY + d0, player.motionZ))
		{
			player.motionY = 0.30000001192092896D;
		}
	}

	/* =================================================
	 * END MINECRAFT PHYSICS
	 * =================================================
	 */

	/* =================================================
	 * START QUAKE PHYSICS
	 * =================================================
	 */

	/**
	 * Moves the entity based on the specified heading.  Args: strafe, forward
	 */
	public static boolean quake_moveEntityWithHeading(EntityPlayer player, float sidemove, float upmove, float forwardmove)
	{
		// take care of ladder movement using default code
		if (player.isOnLadder())
		{
			return false;
		}
		// take care of lava movement using default code
		else if ((player.isInLava() && !player.capabilities.isFlying))
		{
			return false;
		}
		else if (player.isInWater() && !player.capabilities.isFlying)
		{
			return false;
		}
		else
		{
			// get all relevant movement values
			float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMoveSpeed(player) : 0.0F;
			float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
			boolean onGroundForReal = player.onGround && !isJumping(player);
			float momentumRetention = getSlipperiness(player);

			double sv_accelerate = ModConfig.VALUES.ACCELERATE;

			// ground movement
			if (onGroundForReal)
			{
				playerActualVelY = 0.0;
				float dynamicCap = -1.0f;
				if (player.isSneaking () && getSpeed (player) > 0.21540) {
					if (playerSlide < 0.f)
						playerSlide = 0.999f;
					wishspeed *= 3.50f;
					sv_accelerate = ModConfig.VALUES.SLIDE_ACCELERATE;
					momentumRetention = 0.99f;
					dynamicCap = (float) getSpeed (player) * (1.f - (playerSlide - 1.f) * (playerSlide - 1.f));
					playerSlide = Math.max (playerSlide - 0.01f, 0.f);
				} else {
					playerSlide = -1.f;
				}
				minecraft_ApplyFriction(player, momentumRetention);

				if (wishspeed == 0.f && dynamicCap > 0.f) {
					Vec3d hardCappedVel = new Vec3d (player.motionX, 0.0, player.motionZ).normalize().scale (dynamicCap);
					player.motionX = hardCappedVel.x;
					player.motionZ = hardCappedVel.z;
				} else
					quake_Accelerate(player, wishspeed, dynamicCap, wishdir[0], wishdir[1], sv_accelerate);

				// Force reset so people can't store their velocities while on the ground
				playerGroundTouchTime = 0;

			}
			// air movement
			else
			{
				playerSlide = -1.f;
				quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], sidemove != 0.f, forwardmove != 0.f);
			}

			if (getSpeed (player) > 0.21540 && (System.currentTimeMillis () - playerGroundTouchTime > ModConfig.VALUES.WALL_CLIP_TICKS)
					&& player.onGround && ! onGroundForReal)
				playerGroundTouchTime = System.currentTimeMillis ();

			Vec3d previousVel = new Vec3d (player.motionX, player.motionY, player.motionZ);

			// apply velocity
			player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);

			if ((System.currentTimeMillis () - playerGroundTouchTime <= ModConfig.VALUES.WALL_CLIP_TICKS)) {
				if (player.collidedHorizontally)
					player.setVelocity(previousVel.x, playerActualVelY, previousVel.z);
				else if (wasPlayerCollided)
					player.setVelocity(previousVel.x, playerRampJumpY, previousVel.z);
			}

			// HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
			minecraft_ApplyGravity(player);

		}

		playerActualVelY = player.motionY;
		playerRampJumpY  = (playerActualVelY + playerRampJumpY) / 2.0;
		wasPlayerCollided = player.collidedHorizontally;


		// swing them arms
		minecraft_SwingLimbsBasedOnMovement(player);

		return true;
	}

	private static void quake_Accelerate(EntityPlayer player, float wishspeed, float cap, double wishX, double wishZ, double accel)
	{
		double addspeed, accelspeed, currentspeed;

		// Determine veer amount
		// this is a dot product
		currentspeed = player.motionX * wishX + player.motionZ * wishZ;

		// See how much to add
		addspeed = wishspeed - currentspeed;

		// If not adding any, done.
		if (addspeed <= 0)
			return;

		// Determine acceleration speed after acceleration
		accelspeed = accel * wishspeed / getSlipperiness(player) * 0.05F;

		// Cap it
		if (accelspeed > addspeed)
			accelspeed = addspeed;

		// Adjust pmove vel.
		player.motionX += accelspeed * wishX;
		player.motionZ += accelspeed * wishZ;

		if (cap > 0.f && (player.motionX * player.motionX + player.motionZ * player.motionZ) > (cap * cap)) {
			Vec3d hardCappedVel = new Vec3d (player.motionX, 0.0, player.motionZ).normalize().scale (cap);
			player.motionX = hardCappedVel.x;
			player.motionZ = hardCappedVel.z;
		}
	}

	private static void quake_AirAccelerate(EntityPlayer player, float wishspeed, double wishX, double wishZ, boolean strafe, boolean forward)
	{
		double addspeed, accelspeed, currentspeed;

		float wishspd = wishspeed;

		float dynamicAccel = (float) ModConfig.VALUES.Q3_AIR_ACCELERATE;
		float maxAirAcceleration = (float) ModConfig.VALUES.Q3_MAX_AIR_ACCEL_PER_TICK;
		if (strafe && !forward) {
			dynamicAccel = (float) ModConfig.VALUES.Q1_AIR_ACCELERATE;
			maxAirAcceleration = (float) ModConfig.VALUES.Q1_MAX_AIR_ACCEL_PER_TICK;
		}

		if (wishspd > maxAirAcceleration)
			wishspd = maxAirAcceleration;

		// Determine veer amount
		// this is a dot product
		currentspeed = player.motionX * wishX + player.motionZ * wishZ;

		// See how much to add
		addspeed = wishspd - currentspeed;

		// If not adding any, done.
		if (addspeed <= 0)
			return;

		// Determine acceleration speed after acceleration
		accelspeed = dynamicAccel * wishspeed * 0.05F;

		// Cap it
		if (accelspeed > addspeed)
			accelspeed = addspeed;

		// Adjust pmove vel.
		player.motionX += accelspeed * wishX;
		player.motionZ += accelspeed * wishZ;
	}

	/* =================================================
	 * END QUAKE PHYSICS
	 * =================================================
	 */
}
