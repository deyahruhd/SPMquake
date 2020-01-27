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
import squeek.quakemovement.handler.DrawHUDHandler;
import squeek.quakemovement.handler.NetworkHandler;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, name="SPMquake", acceptedMinecraftVersions="[1.12.2]", dependencies = "after:squeedometer", guiFactory = ModInfo.CONFIG_GUI_FACTORY_CLASS)
public class ModQuakeMovement
{
	// The instance of your mod that Forge uses.
	@Instance(value = ModInfo.MODID)
	public static ModQuakeMovement instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModConfig.init (event.getSuggestedConfigurationFile ());
		MinecraftForge.EVENT_BUS.register (new ModConfig ());
		MinecraftForge.EVENT_BUS.register (this);
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

	public static boolean shouldDoQuakeMovement (EntityPlayer player) {
		return ModConfig.VALUES.ENABLED;
	}
}
