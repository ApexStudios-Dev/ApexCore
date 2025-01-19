package dev.apexstudios.apexcore.lib.menu;

import dev.apexstudios.apexcore.core.ApexCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.joml.Math;

public class SimpleMenuScreen extends AbstractContainerScreen<SimpleMenu> {
    public static final ResourceLocation WINDOW_SPRITE = ApexCore.identifier("window");
    public static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    public SimpleMenuScreen(SimpleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        var cols = AbstractContainerMenu.SLOTS_PER_ROW;
        var rows = menu.slotCount() / cols;
        var itemHandlerHeight = rows * AbstractContainerMenu.SLOT_SIZE;

        imageWidth = AbstractContainerMenu.SLOT_SIZE;
        imageHeight = AbstractContainerMenu.SLOT_SIZE;

        for(var slot : menu.slots) {
            imageWidth = Math.max(imageWidth, slot.x + AbstractContainerMenu.SLOT_SIZE - 1);
            imageHeight = Math.max(imageHeight, slot.y + AbstractContainerMenu.SLOT_SIZE - 1);
        }

        imageWidth += 8;
        imageHeight += 8;

        inventoryLabelY = itemHandlerHeight + AbstractContainerMenu.SLOT_SIZE - 3;
        titleLabelY = 3;

        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        graphics.blitSprite(RenderType::guiTextured, WINDOW_SPRITE, 0, -2, imageWidth, imageHeight + 1);

        for(var slot : menu.slots) {
            graphics.blitSprite(RenderType::guiTextured, SLOT_SPRITE, slot.x - 1, slot.y - 1, AbstractContainerMenu.SLOT_SIZE, AbstractContainerMenu.SLOT_SIZE);
        }

        graphics.pose().popPose();
    }
}
