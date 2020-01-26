package squeek.quakemovement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, name="SPMquake", acceptedMinecraftVersions="[1.12.2]", dependencies = "after:squeedometer", guiFactory = ModInfo.CONFIG_GUI_FACTORY_CLASS)
public class ModQuakeMovement
{
	private static final ResourceLocation JUMP_INDICATORS = new ResourceLocation("spmquake:textures/gui/jumpindicators.png");

	// The instance of your mod that Forge uses.
	@Instance(value = ModInfo.MODID)
	public static ModQuakeMovement instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModConfig.init(event.getSuggestedConfigurationFile());
		MinecraftForge.EVENT_BUS.register(new ModConfig());
		MinecraftForge.EVENT_BUS.register(this);
		if (event.getSide() == Side.CLIENT)
		{
			MinecraftForge.EVENT_BUS.register(new ToggleKeyHandler());
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}

	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event)
	{
		if (!(event.getEntityLiving() instanceof EntityPlayer))
			return;

		if (ModConfig.INCREASED_FALL_DISTANCE != 0.0D)
		{
			event.setDistance((float) (event.getDistance() - ModConfig.INCREASED_FALL_DISTANCE));
		}
	}

	@SubscribeEvent
	public void onPostDraw (RenderGameOverlayEvent.Post event) {
		if (! shouldDoQuakeMovement (Minecraft.getMinecraft().player) || ! ModConfig.JUMP_INDICATORS_ENABLED)
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
		return ModConfig.ENABLED;
	}
}
