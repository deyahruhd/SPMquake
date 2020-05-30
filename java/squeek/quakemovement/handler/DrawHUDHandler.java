package squeek.quakemovement.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.quakemovement.ModQuakeMovement;
import squeek.quakemovement.config.ModConfig;
import squeek.quakemovement.helper.MathHelper;
import squeek.quakemovement.movement.QuakeClientPlayer;

public class DrawHUDHandler {
    private static final ResourceLocation JUMP_INDICATORS = new ResourceLocation("spmquake:textures/gui/jumpindicators.png");

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPostDraw (RenderGameOverlayEvent event) {
        if (!ModQuakeMovement.shouldDoQuakeMovement(Minecraft.getMinecraft().player))
            return;

        int l = event.getResolution().getScaledWidth();
        int i1 = event.getResolution().getScaledHeight();

        if (event instanceof RenderGameOverlayEvent.Post && ModConfig.VALUES.JUMP_INDICATORS_MODE > 1) {

            Minecraft.getMinecraft().getTextureManager().bindTexture(JUMP_INDICATORS);
            GlStateManager.enableAlpha();

            if (Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown())
                drawTexturedModalRect(l / 2 - 7, i1 / 2 - 7 - 16, 0, 0, 16, 16);

            if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown())
                drawTexturedModalRect(l / 2 - 7 - 16, i1 / 2 - 7, 16, 0, 16, 16);

            if (Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown())
                drawTexturedModalRect(l / 2 - 7, i1 / 2 - 7 + 16, 32, 0, 16, 16);

            if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown())
                drawTexturedModalRect(l / 2 - 7 + 16, i1 / 2 - 7, 48, 0, 16, 16);

            if (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown())
                drawTexturedModalRect(l / 2 - 7 + 16, i1 / 2 - 7 - 16, 0, 16, 16, 16);

            if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown())
                drawTexturedModalRect(l / 2 - 7 - 16, i1 / 2 - 7 + 16, 16, 16, 16, 16);

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
        } else if (event.getType () == RenderGameOverlayEvent.ElementType.CROSSHAIRS && ModConfig.VALUES.JUMP_INDICATORS_MODE > 0) {
            event.setCanceled(true);

            Minecraft.getMinecraft().getTextureManager().bindTexture(JUMP_INDICATORS);
            GlStateManager.enableAlpha();

            drawTexturedModalRect(l / 2 - 7, i1 / 2 - 7, 32, 32, 16, 16);

            if (MathHelper.predictOverbounce (Minecraft.getMinecraft ().player))
                drawTexturedModalRect (l / 2 - 7 + 32, i1 / 2 - 7 + 16, 48, 48, 16, 16);

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
        }
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
}
