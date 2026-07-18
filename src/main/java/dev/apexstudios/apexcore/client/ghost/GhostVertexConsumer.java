package dev.apexstudios.apexcore.client.ghost;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

final class GhostVertexConsumer extends VertexConsumerWrapper {
    private final boolean isInvalid;

    public GhostVertexConsumer(VertexConsumer parent, boolean isInvalid) {
        super(parent);

        this.isInvalid = isInvalid;
    }

    private int alpha() {
        var startingAlpha = 170;
        var maxDeviation = 40;
        var speed = 2F;

        if(isInvalid) {
            startingAlpha = 200;
        }

        var seconds = Util.getMillis() / 1000D;
        var wave = Math.sin(seconds * speed) * maxDeviation;
        var alpha = Math.round(startingAlpha + wave);
        return Math.clamp(alpha, 0, 255);
    }

    private int color(int color) {
        var white = ARGB.color(alpha(), 0xFFFFFF);
        var result = ARGB.multiply(white, color);

        if(isInvalid) {
            return ARGB.multiply(CommonColors.RED, result);
        }

        return result;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
        super.addVertex(
                x, y, z,
                color(color),
                u, v,
                overlayCoords,
                lightCoords,
                nx, ny, nz
        );
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return super.setColor(r, g, b, (alpha() * a) / 0xFF);
    }

    @Override
    public VertexConsumer setColor(float r, float g, float b, float a) {
        return setColor(
                ARGB.as8BitChannel(r),
                ARGB.as8BitChannel(g),
                ARGB.as8BitChannel(b),
                ARGB.as8BitChannel(a)
        );
    }

    @Override
    public VertexConsumer setColor(int packedColor) {
        return super.setColor(color(packedColor));
    }
}
