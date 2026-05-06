package dev.apexstudios.apexcore.api.menu;

import dev.apexstudios.apexcore.common.ApexCore;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, WINDOW_SPRITE, 0, -2, imageWidth, imageHeight + 1);

        for(var slot : menu.slots) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, slot.x - 1, slot.y - 1, AbstractContainerMenu.SLOT_SIZE, AbstractContainerMenu.SLOT_SIZE);
        }

        graphics.pose().popMatrix();
    }
}
