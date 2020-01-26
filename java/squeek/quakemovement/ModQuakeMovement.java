package squeek.quakemovement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.handler.ConfigPacket;
import squeek.quakemovement.handler.NetworkHandler;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, name="SPMquake", acceptedMinecraftVersions="[1.12.2]", dependencies = "after:squeedometer", guiFactory = ModInfo.CONFIG_GUI_FACTORY_CLASS)
public class ModQuakeMovement
{
	private static final ResourceLocation JUMP_INDICATORS = new ResourceLocation("spmquake:textures/gui/jumpindicators.png");

	// The instance of your mod that Forge uses.
	@Instance(value = ModInfo.MODID)
	public static ModQuakeMovement instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModConfig.init (event.getSuggestedConfigurationFile ());
		MinecraftForge.EVENT_BUS.register (new ModConfig ());
		MinecraftForge.EVENT_BUS.register (this);
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

	@SideOnly (Side.CLIENT)
	@SubscribeEvent
	public void onPostDraw (RenderGameOverlayEvent.Post event) {
		if (! shouldDoQuakeMovement (Minecraft.getMinecraft().player) || ! ModConfig.VALUES.JUMP_INDICATORS_ENABLED)
			return;

		int l = event.getResolution ().getScaledWidth ();
		int i1 = event.getResolution ().getScaledHeight ();

		Minecraft.getMinecraft ().getTextureManager().bindTexture(JUMP_INDICATORS);
		GlStateManager.enableAlpha ();

		if (Minecraft.getMinecraft ().gameSettings.keyBindForward.isKeyDown ())
			drawTexturedModalRect(l / 2 - 7, i1 / 2 - 7 - 16, 0, 0, 16, 16);

		if (Minecraft.getMinecraft ().gameSettings.keyBindLeft.isKeyDown ())
			drawTexturedModalRect (l / 2 - 7 - 16, i1 / 2 - 7, 16, 0, 16, 16);

		if (Minecraft.getMinecraft ().gameSettings.keyBindBack.isKeyDown ())
			drawTexturedModalRect(l / 2 - 7, i1 / 2 - 7 + 16, 32, 0, 16, 16);

		if (Minecraft.getMinecraft ().gameSettings.keyBindRight.isKeyDown ())
			drawTexturedModalRect (l / 2 - 7 + 16, i1 / 2 - 7, 48, 0, 16, 16);

		if (Minecraft.getMinecraft ().gameSettings.keyBindJump.isKeyDown ())
			drawTexturedModalRect(l / 2 - 7 + 16, i1 / 2 - 7 - 16, 0, 16, 16, 16);

		if (Minecraft.getMinecraft ().gameSettings.keyBindSneak.isKeyDown ())
			drawTexturedModalRect(l / 2 - 7 - 16, i1 / 2 - 7 + 16, 16, 16, 16, 16);

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		Minecraft.getMinecraft ().getTextureManager().bindTexture(Gui.ICONS);
	}

	@SideOnly (Side.CLIENT)
	private static void drawTexturedModalRect (int x, int y, int textureX, int textureY, int width, int height) {
		float f = 0.015625F;
		float f1 = 0.015625F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double)(x + 0), (double)(y + height), 0.0)
					 .tex((double)((float)(textureX + 0) * 0.015625F), (double)((float)(textureY + height) * 0.015625F)).endVertex();
		bufferbuilder.pos((double)(x + width), (double)(y + height), 0.0)
					 .tex((double)((float)(textureX + width) * 0.015625F), (double)((float)(textureY + height) * 0.015625F)).endVertex();
		bufferbuilder.pos((double)(x + width), (double)(y + 0), 0.0)
					 .tex((double)((float)(textureX + width) * 0.015625F), (double)((float)(textureY + 0) * 0.015625F)).endVertex();
		bufferbuilder.pos((double)(x + 0), (double)(y + 0), 0.0)
				     .tex((double)((float)(textureX + 0) * 0.015625F), (double)((float)(textureY + 0) * 0.015625F)).endVertex();
		tessellator.draw();
	}

	public static boolean shouldDoQuakeMovement (EntityPlayer player) {
		return ModConfig.VALUES.ENABLED;
	}
}
