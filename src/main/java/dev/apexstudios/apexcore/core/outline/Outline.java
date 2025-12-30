package dev.apexstudios.apexcore.core.outline;

import com.google.common.collect.Sets;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Outline(
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        float nX,
        float nY,
        float nZ,
        int hash
) {
    public Outline(float x1, float y1, float z1, float x2, float y2, float z2, float nX, float nY, float nZ) {
        this(x1, y1, z1, x2, y2, z2, nX, nY, nZ, hash(x1, y1, z1, x2, y2, z2));
    }

    public Outline(Vector3fc v1, Vector3fc v2, Vector3fc nml) {
        this(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), nml.x(), nml.y(), nml.z());
    }

    public static Outline create(Vector3fc v1, Vector3fc v2) {
        var nml = v2.sub(v1, new Vector3f());
        var scalar = Math.invsqrt(Math.fma(nml.x(), nml.x(), Math.fma(nml.y(), nml.y(), nml.z() * nml.z())));
        nml.mul(scalar);
        var hash = hash(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z());
        return new Outline(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), nml.x(), nml.y(), nml.z(), hash);
    }

    public static List<Outline> extract(BlockAndTintGetter level, BlockPos pos, BlockState blockState, RandomSource random) {
        var lines = Sets.<Outline>newHashSet();
        var extractor = new VertexExtractor(lines::add);
        extract(level, pos, blockState, random, extractor);

        /*if(MultiBlock.isMultiBlock(blockState)) {
            var isOrigin = MultiBlock.getIndex(blockState) == 0;
            var origin = MultiBlock.getOrigin(pos, blockState);
            var originBlockState = level.getBlockState(origin);
            extractor.setOffset(new Vector3f(MultiBlock.getLocalPos(blockState)));
            extract(level, origin, originBlockState, random, extractor);

            MultiBlock.forEachPos(pos, blockState, (otherPos, otherBlockState) -> {
                if(MultiBlock.getIndex(otherBlockState) != 0) {
                    if(!isOrigin) {
                        extractor.setOffset(new Vector3f(MultiBlock.getLocalPos(originBlockState)));
                    } else {
                        extractor.setOffset(new Vector3f(MultiBlock.getLocalPos(otherBlockState)).mul(-1, new Vector3f()));
                    }

                    extract(level, otherPos, otherBlockState, random, extractor);
                }
            });
        } else {
            extract(level, pos, blockState, random, extractor);
        }*/

        return List.copyOf(lines);
    }

    private static void extract(BlockAndTintGetter level, BlockPos pos, BlockState blockState, RandomSource random, VertexExtractor extractor) {
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);

        for(var part : model.collectParts(level, pos, blockState, random)) {
            for(var direction : Direction.values()) {
                for(var quad : part.getQuads(direction)) {
                    extractor.unpack(quad);
                }
            }

            for(var quad : part.getQuads(null)) {
                extractor.unpack(quad);
            }
        }
    }

    private static int hash(float x1, float y1, float z1, float x2, float y2, float z2) {
        var result = Long.hashCode((long) Math.min(x1, x2) * 3200L);
        result = 31 * result + Long.hashCode((long) Math.min(y1, y2) * 3200L);
        result = 31 * result + Long.hashCode((long) Math.min(z1, z2) * 3200L);
        result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3200L);
        result = 31 * result + Long.hashCode((long) Math.max(y1, y2) * 3200L);
        result = 31 * result + Long.hashCode((long) Math.max(z1, z2) * 3200L);
        return result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != Outline.class) {
            return false;
        }

        var other = (Outline) obj;
        return (Mth.equal(x1, other.x1) && Mth.equal(y1, other.y1) && Mth.equal(z1, other.z1) && Mth.equal(x2, other.x2) && Mth.equal(y2, other.y2) && Mth.equal(z2, other.z2)) ||
                (Mth.equal(x1, other.x2) && Mth.equal(y1, other.y2) && Mth.equal(z1, other.z2) && Mth.equal(x2, other.x1) && Mth.equal(y2, other.y1) && Mth.equal(z2, other.z1));
    }
}
