package dev.apexstudios.apexcore.core.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class GhostVertexConsumer extends VertexConsumerWrapper {
    private final int alpha;

    public GhostVertexConsumer(VertexConsumer parent, int alpha) {
        super(parent);

        this.alpha = alpha;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return super.setColor(r, g, b, (a * alpha) / 0xFF);
    }
}
