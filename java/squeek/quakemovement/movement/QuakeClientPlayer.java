package squeek.quakemovement.movement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.handler.NetworkHandler;
import squeek.quakemovement.handler.HungerJumpPacket;
import squeek.quakemovement.movement.mutators.Mutator;
import squeek.quakemovement.movement.mutators.impl.*;

public class QuakeClientPlayer {
	private static Method setDidJumpThisTick = null;
	private static Method setIsJumping = null;

	private static TreeSet<Mutator> mutators = new TreeSet<> (new Mutator.MutatorComparator());

	static {
		mutators.add(new QAnisotropicMovementBase());
		mutators.add(new CPMAirSteerMutator());
		mutators.add(new RampJumpMutator());
		mutators.add(new WallClipMutator());
		mutators.add(new GroundBoostMutator());
		mutators.add(new ViewBobMutator());
		try {
			if (Loader.isModLoaded("squeedometer")) {
				Class<?> hudSpeedometer = Class.forName("squeek.speedometer.HudSpeedometer");
				setDidJumpThisTick = hudSpeedometer.getDeclaredMethod("setDidJumpThisTick", boolean.class);
				setIsJumping = hudSpeedometer.getDeclaredMethod("setIsJumping", boolean.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void doAccel (EntityPlayer player, Vec3d wishdir, float wishspeed, Mutator.MovementInput input) {
		boolean propagateInput = input != null;
		boolean onGroundForReal = player.onGround && !isJumping(player);
		for (Mutator m : mutators) {
			if ((((m.getType () == Mutator.MutatorType.MovementOverride && m.listenTo () != null && input != null && m.listenTo().contains (input))
					|| m.getType () == Mutator.MutatorType.MovementBase && Mutator.BASE_INPUT_SET.contains (input)) && propagateInput) ||
					m.getType() == Mutator.MutatorType.MovementPassive) {
				boolean res = onGroundForReal ? m.groundMove ((EntityPlayerSP) player, wishdir, wishspeed, input) :
						m.airMove ((EntityPlayerSP) player, wishdir, wishspeed, input);

				onGroundForReal = player.onGround && !isJumping(player);

				if (propagateInput)
					propagateInput = !res;
				else if (m.getType () == Mutator.MutatorType.MovementPassive)
					propagateInput = false;
			}
		}
	}

	/**
	 * Moves the entity based on the specified heading.  Args: strafe, forward
	 */
	public static boolean quake_moveEntityWithHeading(EntityPlayer player, float sidemove, float upmove, float forwardmove) {
		ViewBobMutator viewBobbing = (ViewBobMutator) mutators.last();
		Vec3d wishdir = getMovementDirection(player, sidemove, forwardmove).normalize();

		Mutator.MovementInput input = null;
		if (sidemove != 0.f || forwardmove != 0.f) {
			if (forwardmove > 0.f) {
				if (sidemove > 0.f)
					input = Mutator.MovementInput.FOR_LEFT;
				else if (sidemove < 0.f)
					input = Mutator.MovementInput.FOR_RIGHT;
				else
					input = Mutator.MovementInput.FORWARD;
			} else if (forwardmove < 0.f) {
				if (sidemove > 0.f)
					input = Mutator.MovementInput.BACK_LEFT;
				else if (sidemove < 0.f)
					input = Mutator.MovementInput.BACK_RIGHT;
				else
					input = Mutator.MovementInput.BACK;
			} else {
				if (sidemove > 0.f)
					input = Mutator.MovementInput.LEFT;
				else if (sidemove < 0.f)
					input = Mutator.MovementInput.RIGHT;
			}
		}


		// take care of ladder movement using default code
		if (player.isOnLadder()) {
			// Update view bobbing
			viewBobbing.preMove((EntityPlayerSP) player, wishdir, input);
			return false;
		}
		// take care of lava movement using default code
		else if ((player.isInLava() && !player.capabilities.isFlying)) {
			// Update view bobbing
			viewBobbing.preMove((EntityPlayerSP) player, wishdir, input);
			return false;
		} else if (player.isInWater() && !player.capabilities.isFlying) {
			// Update view bobbing
			viewBobbing.preMove((EntityPlayerSP) player, wishdir, input);
			return false;
		} else if ((player.capabilities.isFlying || player.isElytraFlying()) && player.getRidingEntity() == null) {
			// Update view bobbing
			viewBobbing.preMove((EntityPlayerSP) player, wishdir, input);
			return false;
		} else {
			// get all relevant movement values
			float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMoveSpeed(player) : 0.0F;

			for (Mutator m : mutators) {
				m.preMove ((EntityPlayerSP) player, wishdir, input);
			}

			doAccel (player, wishdir, wishspeed, input);

			if (player.isSneaking ())
				doAccel (player, wishdir, wishspeed, Mutator.MovementInput.SNEAK);

			if (QuakeClientPlayer.isJumping (player) && player.onGround)
				doAccel (player, wishdir, wishspeed, Mutator.MovementInput.JUMP);

			// apply velocity
			player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);
			for (Mutator m : mutators) {
				m.postMove ((EntityPlayerSP) player, wishdir, input);
			}
		}

		// swing them arms
		minecraft_SwingLimbsBasedOnMovement(player);

		return true;
	}

	public static boolean moveEntityWithHeading(EntityPlayer player, float sidemove, float upmove, float forwardmove) {
		if (!player.world.isRemote)
			return false;

		if (!ModQuakeMovement.shouldDoQuakeMovement(player))
			return false;

		boolean didQuakeMovement;
		double d0 = player.posX;
		double d1 = player.posY;
		double d2 = player.posZ;

		player.setSprinting(false);
		didQuakeMovement = quake_moveEntityWithHeading(player, sidemove, upmove, forwardmove);

		if (didQuakeMovement)
			player.addMovementStat(player.posX - d0, player.posY - d1, player.posZ - d2);

		return didQuakeMovement;
	}

	public static void beforeOnLivingUpdate(EntityPlayer player) {
		if (!player.world.isRemote)
			return;

		if (setDidJumpThisTick != null) {
			try {
				setDidJumpThisTick.invoke(null, false);
			} catch (Exception e) {
			}
		}

		if (setIsJumping != null) {
			try {
				setIsJumping.invoke(null, isJumping(player));
			} catch (Exception e) {
			}
		}
	}

	public static boolean moveRelative(EntityPlayer player, float sidemove, float upmove, float forwardmove, float friction) {
		if (!player.world.isRemote)
			return false;

		if (!ModQuakeMovement.shouldDoQuakeMovement(player))
			return false;

		if ((player.capabilities.isFlying && player.getRidingEntity() == null) || player.isInWater() || player.isInLava() || player.isOnLadder()) {
			return false;
		}

		return true;
	}

	/**
	 * ASM-hooked methods
	 */
	public static boolean moveRelativeBase(Entity entity, float sidemove, float upmove, float forwardmove, float friction) {
		if (!(entity instanceof EntityPlayer))
			return false;

		return moveRelative((EntityPlayer) entity, sidemove, forwardmove, upmove, friction);
	}

	public static void doHungerJump(EntityPlayer e) {
		NetworkHandler.INSTANCE.sendToServer(new HungerJumpPacket(Minecraft.getMinecraft().player));
	}

	public static void afterJump(EntityPlayer player) {
		if (!player.world.isRemote)
			return;

		if (!ModQuakeMovement.shouldDoQuakeMovement(player))
			return;

		// undo this dumb thing
		if (player.isSprinting()) {
			float f = player.rotationYaw * 0.017453292F;
			player.motionX += MathHelper.sin(f) * 0.2F;
			player.motionZ -= MathHelper.cos(f) * 0.2F;
		}

		if (setDidJumpThisTick != null) {
			try {
				setDidJumpThisTick.invoke(null, true);
			} catch (Exception e) {
			}
		}
	}

	/* =================================================
	 * START HELPERS
	 * =================================================
	 */

	public static double getSpeed(EntityPlayer player) {
		return MathHelper.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
	}

	public static float getSlipperiness(EntityPlayer player) {
		long time = System.currentTimeMillis();

		float f2 = 1.00F;

		// The second condition replicates a mechanic in CPMA known as plasma ground boosting - using plasma
		// near you, right before you land on the ground, grants you a 50-200ms period of 0 friction. This can
		// allow doubling or even tripling your speed with a well timed and quick circle jump.
		// For now it's activated when receiving knockback from an explosion
		if (player.onGround && (time - GroundBoostMutator.playerAirbornTime > ModConfig.VALUES.KNOCKBACK_SLICK_TIME * GroundBoostMutator.explosionStr)) {
			BlockPos groundPos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.getEntityBoundingBox().minY) - 1, MathHelper.floor(player.posZ));
			Block ground = player.world.getBlockState(groundPos).getBlock();

			if (ground.slipperiness < 1.0) {
				f2 = ground.slipperiness * 0.91F;
			}
		}
		return f2;
	}

	public static Vec3d getMovementDirection(EntityPlayer player, float sidemove, float forwardmove) {
		float f3 = sidemove * sidemove + forwardmove * forwardmove;
		float[] dir = {0.0F, 0.0F};

		if (f3 >= 1.0E-4F) {
			f3 = MathHelper.sqrt(f3);

			if (f3 < 1.0F) {
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

		return new Vec3d (dir [0], 0.f, dir [1]);
	}

	public static float quake_getMoveSpeed(EntityPlayer player) {
		float baseSpeed = player.getAIMoveSpeed();
		return !player.isSneaking() ? baseSpeed * 2.15F : baseSpeed * 1.11F;
	}

	private static final Field isJumping = ReflectionHelper.findField(EntityLivingBase.class, "isJumping", "field_70703_bu", "bd");

	public static boolean isJumping(EntityPlayer player) {
		try {
			return isJumping.getBoolean(player);
		} catch (Exception e) {
			return false;
		}
	}

	public static Vec3d getStairNormal(World w, Vec3d pos, double scanWidth, double playerWidth) {
		Vec3d stairNormal = Vec3d.ZERO;

		for (BlockPos scanPos : new BlockPos[]{
				new BlockPos(new Vec3d(pos.x + scanWidth, pos.y, pos.z + scanWidth)),
				new BlockPos(new Vec3d(pos.x + scanWidth, pos.y, pos.z - scanWidth)),
				new BlockPos(new Vec3d(pos.x - scanWidth, pos.y, pos.z - scanWidth)),
				new BlockPos(new Vec3d(pos.x - scanWidth, pos.y, pos.z + scanWidth))}) {
			IBlockState state = w.getBlockState(scanPos);

			if (state.getBlock() instanceof BlockStairs) {
				EnumFacing face = state.getValue(BlockStairs.FACING);

				Vec3i horizontalDir = face.getDirectionVec();
				int verticalDir = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP
						? -1
						: 1;

				stairNormal = stairNormal.add(new Vec3d(-horizontalDir.getX(), verticalDir, -horizontalDir.getZ()));
			}
		}

		return (stairNormal == Vec3d.ZERO) ? null : stairNormal.normalize();
	}

	public static void minecraft_ApplyGravity(EntityPlayer player) {
		if (player.world.isRemote && (!player.world.isBlockLoaded(new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ)) || !player.world.getChunk(new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ)).isLoaded())) {
			if (player.posY > 0.0D) {
				player.motionY = -0.1D;
			} else {
				player.motionY = 0.0D;
			}
		} else {
			// gravity
			double scaledGravity = (0.08D *
					MathHelper.clamp((float) (System.currentTimeMillis() - GroundBoostMutator.playerAirbornTime) /
							((float) ModConfig.VALUES.KNOCKBACK_SLICK_TIME * GroundBoostMutator.explosionStr), 0.00F, 1.0F));
			player.motionY -= scaledGravity;
		}

		// air resistance
		player.motionY *= 0.9800000190734863D;
	}

	public static void minecraft_ApplyFriction(EntityPlayer player, float momentumRetention) {
		player.motionX *= momentumRetention;
		player.motionZ *= momentumRetention;
	}

	private static void minecraft_SwingLimbsBasedOnMovement(EntityPlayer player) {
		player.prevLimbSwingAmount = player.limbSwingAmount;
		double d0 = player.posX - player.prevPosX;
		double d1 = player.posZ - player.prevPosZ;
		float f6 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

		if (f6 > 1.0F) {
			f6 = 1.0F;
		}

		player.limbSwingAmount += (f6 - player.limbSwingAmount) * 0.4F;
		player.limbSwing += player.limbSwingAmount;
	}

	/* END HELPERS */
}