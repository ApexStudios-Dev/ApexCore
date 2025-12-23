package dev.apexstudios.apexcore.lib.menu;

import dev.apexstudios.apexcore.core.ApexCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class SimpleMenuScreen extends AbstractContainerScreen<SimpleMenu> {
    public static final Identifier WINDOW_SPRITE = ApexCore.identifier("window");
    public static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");

    public SimpleMenuScreen(SimpleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, DEFAULT_IMAGE_WIDTH, 114 + menu.rowCount() * 18);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_SPRITE, 0, -2, imageWidth, imageHeight + 1);

        for(var slot : menu.slots) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, slot.x - 1, slot.y - 1, AbstractContainerMenu.SLOT_SIZE, AbstractContainerMenu.SLOT_SIZE);
        }

        graphics.pose().popMatrix();
    }
}
