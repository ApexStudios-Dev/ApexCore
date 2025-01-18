package dev.apexstudios.apexcore.core.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class FluidVertexConsumer extends VertexConsumerWrapper {
    private final PoseStack pose;
    private final BlockPos pos;

    public FluidVertexConsumer(VertexConsumer parent, PoseStack pose, BlockPos pos) {
        super(parent);

        this.pose = pose;
        this.pos = pos;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        var dX = pos.getX() & 15;
        var dY = pos.getY() & 15;
        var dZ = pos.getZ() & 15;

        return parent.addVertex(
                pose.last().pose(),
                x - dX,
                y - dY,
                z - dZ
        );
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return parent.setNormal(pose.last(), x, y, z);
    }
}
