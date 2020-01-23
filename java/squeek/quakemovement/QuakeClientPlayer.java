package squeek.quakemovement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class QuakeClientPlayer
{
	private static Random random = new Random();

	private static boolean didJumpThisTick = false;
	private static List<float[]> baseVelocities = new ArrayList<float[]>();

	private static Method setDidJumpThisTick = null;
	private static Method setIsJumping = null;

	// PGB
	private static long playerAirbornTime 		 = 0;

	// Wall clipping
	private static boolean playerWasTouchingWall = false;
	private static long playerWallTouchTime 	 = 0;
	private static Vec3d playerJumpVel 			 = Vec3d.ZERO;

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

		if (!ModConfig.ENABLED)
			return false;

		boolean didQuakeMovement;
		double d0 = player.posX;
		double d1 = player.posY;
		double d2 = player.posZ;

		if ((player.capabilities.isFlying || player.isElytraFlying()) && player.getRidingEntity() == null)
			return false;
		else
			didQuakeMovement = quake_moveEntityWithHeading(player, sidemove, upmove, forwardmove);

		if (didQuakeMovement)
			player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);

		return didQuakeMovement;
	}

	public static void beforeOnLivingUpdate(EntityPlayer player)
	{
		if (!player.world.isRemote)
			return;

		didJumpThisTick = false;
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

		if (!baseVelocities.isEmpty())
		{
			baseVelocities.clear();
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

		if (!ModConfig.ENABLED)
			return false;

		if ((player.capabilities.isFlying && player.getRidingEntity() == null) || player.isInWater() || player.isInLava() || player.isOnLadder())
		{
			return false;
		}

		// this is probably wrong, but its what was there in 1.10.2
		float wishspeed = friction;
		wishspeed *= 2.15f;
		float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
		float[] wishvel = new float[]{wishdir[0] * wishspeed, wishdir[1] * wishspeed};
		baseVelocities.add(wishvel);

		return true;
	}

	public static void afterJump(EntityPlayer player)
	{
		if (!player.world.isRemote)
			return;

		if (!ModConfig.ENABLED)
			return;

		// undo this dumb thing
		if (player.isSprinting())
		{
			float f = player.rotationYaw * 0.017453292F;
			player.motionX += MathHelper.sin(f) * 0.2F;
			player.motionZ -= MathHelper.cos(f) * 0.2F;
		}

		didJumpThisTick = true;
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

	private static double getSpeed(EntityPlayer player)
	{
		return MathHelper.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
	}

	private static float getSurfaceFriction(EntityPlayer player)
	{
		float f2 = 1.0F;

		if (player.onGround)
		{
			BlockPos groundPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.getEntityBoundingBox().minY) - 1, MathHelper.floor(player.posZ));
			Block ground = player.world.getBlockState(groundPos).getBlock();
			f2 = 1.0F - ground.slipperiness;
		}

		return f2;
	}

	private static float getSlipperiness(EntityPlayer player)
	{
		long time = System.currentTimeMillis ();

		float f2 = 1.00F;

		// The second condition replicates a mechanic in CPMA known as plasma ground boosting - using plasma
		// near you, right before you land on the ground, grants you a 50-200ms period of 0 friction. This can
		// allow doubling or even tripling your speed with a well timed and quick circle jump.
		// For now it's activated when receiving knockback from an explosion
		if (player.onGround && (time - playerAirbornTime > 250))
		{
			BlockPos groundPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.getEntityBoundingBox().minY) - 1, MathHelper.floor(player.posZ));
			Block ground = player.world.getBlockState(groundPos).getBlock();

			f2 = ground.slipperiness * 0.91F;
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

	public static void updateAirborneTimer () {
		playerAirbornTime = System.currentTimeMillis();
	}

	public static void setEntityVelocity(Entity entity, double x, double y, double z) {
		if ((x * x + y * y + z * z) > 0) {
			entity.setVelocity (x, y, z);
		}
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
		}
		else
		{
			// gravity
			player.motionY -= 0.08D;
		}

		// air resistance
		player.motionY *= 0.9800000190734863D;
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

	public static void minecraft_moveEntityWithHeading(EntityPlayer player, float sidemove, float upmove, float forwardmove)
	{
		// take care of water and lava movement using default code
		if ((player.isInWater() && !player.capabilities.isFlying)
				|| (player.isInLava() && !player.capabilities.isFlying))
		{
			player.travel(sidemove, upmove, forwardmove);
		}
		else
		{
			// get friction
			float momentumRetention = getSlipperiness(player);

			// alter motionX/motionZ based on desired movement
			player.moveRelative(sidemove, upmove, forwardmove, minecraft_getMoveSpeed(player));

			// make adjustments for ladder interaction
			minecraft_ApplyLadderPhysics(player);

			// do the movement
			player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);

			// climb ladder here for some reason
			minecraft_ClimbLadder(player);

			// gravity + friction
			minecraft_ApplyGravity(player);
			minecraft_ApplyFriction(player, momentumRetention);

			// swing them arms
			minecraft_SwingLimbsBasedOnMovement(player);
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

			// ground movement
			if (onGroundForReal)
			{
				// apply friction before acceleration so we can accelerate back up to maxspeed afterwards
				//quake_Friction(); // buggy because material-based friction uses a totally different format
				minecraft_ApplyFriction(player, momentumRetention);

				double sv_accelerate = ModConfig.ACCELERATE;

				if (wishspeed != 0.0F)
				{
					// alter based on the surface friction
					sv_accelerate *= minecraft_getMoveSpeed(player) * 2.15F / wishspeed;

					quake_Accelerate(player, wishspeed, wishdir[0], wishdir[1], sv_accelerate);
				}

				if (!baseVelocities.isEmpty())
				{
					float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
					// add in base velocities
					for (float[] baseVel : baseVelocities)
					{
						player.motionX += baseVel[0] * speedMod;
						player.motionZ += baseVel[1] * speedMod;
					}
				}
			}
			// air movement
			else
			{
				double sv_airaccelerate = ModConfig.AIR_ACCELERATE;
				quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], sv_airaccelerate);
			}

			if (onGroundForReal)
				playerJumpVel = Vec3d.ZERO;

			if (getSpeed (player) > 0.21540 && (System.currentTimeMillis () - playerWallTouchTime > 400) && player.onGround && ! playerWasTouchingWall) {
				playerWallTouchTime = System.currentTimeMillis ();
				playerJumpVel = new Vec3d (player.motionX, 0.0, player.motionZ);
			}

			if ((System.currentTimeMillis () - playerWallTouchTime <= 400) && ! player.collidedHorizontally && playerWasTouchingWall && playerJumpVel.length() > 0.21540) {
				player.setVelocity (playerJumpVel.x, player.motionY, playerJumpVel.z);
				playerWallTouchTime = 0;
			}

			playerWasTouchingWall = player.collidedHorizontally;

			// apply velocity
			player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);

			// HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
			minecraft_ApplyGravity(player);
		}

		// swing them arms
		minecraft_SwingLimbsBasedOnMovement(player);

		return true;
	}

	private static void quake_ApplyWaterFriction(EntityPlayer player, double friction)
	{
		player.motionX *= friction;
		player.motionY *= friction;
		player.motionZ *= friction;

		/*
		float speed = (float)(player.getSpeed());
		float newspeed = 0.0F;
		if (speed != 0.0F)
		{
			newspeed = speed - 0.05F * speed * friction; //* player->m_surfaceFriction;

			float mult = newspeed/speed;
			player.motionX *= mult;
			player.motionY *= mult;
			player.motionZ *= mult;
		}

		return newspeed;
		*/

		/*
		// slow in water
		player.motionX *= 0.800000011920929D;
		player.motionY *= 0.800000011920929D;
		player.motionZ *= 0.800000011920929D;
		*/
	}

	@SuppressWarnings("unused")
	private static void quake_WaterAccelerate(EntityPlayer player, float wishspeed, float speed, double wishX, double wishZ, double accel)
	{
		float addspeed = wishspeed - speed;
		if (addspeed > 0)
		{
			float accelspeed = (float) (accel * wishspeed * 0.05F);
			if (accelspeed > addspeed)
			{
				accelspeed = addspeed;
			}

			player.motionX += accelspeed * wishX;
			player.motionZ += accelspeed * wishZ;
		}
	}

	private static void quake_WaterMove(EntityPlayer player, float sidemove, float upmove, float forwardmove)
	{
		double lastPosY = player.posY;

		// get all relevant movement values
		float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMaxMoveSpeed(player) : 0.0F;
		float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
		boolean isSharking = isJumping(player) && player.isOffsetPositionInLiquid(0.0D, 1.0D, 0.0D);
		double curspeed = getSpeed(player);

		if (!isSharking || curspeed < 0.078F)
		{
			minecraft_WaterMove(player, sidemove, upmove, forwardmove);
		}
		else
		{
			if (curspeed > 0.098)
				quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], ModConfig.ACCELERATE);
			else
				quake_Accelerate(player, .0980F, wishdir[0], wishdir[1], ModConfig.ACCELERATE);

			player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);

			player.motionY = 0.0D;
		}

		// water jump
		if (player.collidedHorizontally && player.isOffsetPositionInLiquid(player.motionX, player.motionY + 0.6000000238418579D - player.posY + lastPosY, player.motionZ))
		{
			player.motionY = 0.30000001192092896D;
		}

		if (!baseVelocities.isEmpty())
		{
			float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
			// add in base velocities
			for (float[] baseVel : baseVelocities)
			{
				player.motionX += baseVel[0] * speedMod;
				player.motionZ += baseVel[1] * speedMod;
			}
		}
	}

	private static void quake_Accelerate(EntityPlayer player, float wishspeed, double wishX, double wishZ, double accel)
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
	}

	private static void quake_AirAccelerate(EntityPlayer player, float wishspeed, double wishX, double wishZ, double accel)
	{
		double addspeed, accelspeed, currentspeed;

		float wishspd = wishspeed;
		float maxAirAcceleration = (float) ModConfig.MAX_AIR_ACCEL_PER_TICK;

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
		accelspeed = accel * wishspeed * 0.05F;

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
