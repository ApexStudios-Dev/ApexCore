package dev.apexstudios.apexcore.lib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class UnmodifiableBoundingBox extends BoundingBox {
    public static final UnmodifiableBoundingBox INFINITE = new UnmodifiableBoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public UnmodifiableBoundingBox(BlockPos pos) {
        super(pos);
    }

    public UnmodifiableBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public BoundingBox encapsulate(BoundingBox box) {
        return new UnmodifiableBoundingBox(
                Math.min(minX(), box.minX()),
                Math.min(minY(), box.minY()),
                Math.min(minZ(), box.minZ()),
                Math.max(maxX(), box.maxX()),
                Math.max(maxY(), box.maxY()),
                Math.max(maxZ(), box.maxZ())
        );
    }

    @Override
    public BoundingBox encapsulate(BlockPos pos) {
        return new UnmodifiableBoundingBox(
                Math.min(minX(), pos.getX()),
                Math.min(minY(), pos.getY()),
                Math.min(minZ(), pos.getZ()),
                Math.max(maxX(), pos.getX()),
                Math.max(maxY(), pos.getY()),
                Math.max(maxZ(), pos.getZ())
        );
    }

    @Override
    public BoundingBox move(int x, int y, int z) {
        return new UnmodifiableBoundingBox(
                minX() + x,
                minY() + y,
                minZ() + z,
                maxX() + x,
                maxY() + y,
                maxZ() + z
        );
    }

    @Override
    public BoundingBox move(Vec3i vector) {
        return move(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public BoundingBox moved(int x, int y, int z) {
        return move(x, y, z);
    }

    @Override
    public BoundingBox inflatedBy(int value) {
        return inflatedBy(value, value, value);
    }

    @Override
    public BoundingBox inflatedBy(int x, int y, int z) {
        return new UnmodifiableBoundingBox(
                minX() - x,
                minY() - y,
                minZ() - z,
                maxX() + x,
                maxY() + y,
                maxZ() + z
        );
    }

    public static BoundingBox fromCorners(Vec3i first, Vec3i second) {
        return new UnmodifiableBoundingBox(
                Math.min(first.getX(), second.getX()),
                Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()),
                Math.max(first.getX(), second.getX()),
                Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ())
        );
    }

    public static BoundingBox infinite() {
        return INFINITE;
    }

    public static BoundingBox orientBox(int structureMinX, int structureMinY, int structureMinZ, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Direction facing) {
        return switch (facing) {
            case NORTH -> new UnmodifiableBoundingBox(
                    structureMinX + xMin, structureMinY + yMin,
                    structureMinZ - zMax + 1 + zMin,
                    structureMinX + xMax - 1 + xMin,
                    structureMinY + yMax - 1 + yMin,
                    structureMinZ + zMin
            );

            case WEST -> new UnmodifiableBoundingBox(
                    structureMinX - zMax + 1 + zMin, structureMinY + yMin,
                    structureMinZ + xMin,
                    structureMinX + zMin,
                    structureMinY + yMax - 1 + yMin,
                    structureMinZ + xMax - 1 + xMin
            );

            case EAST -> new UnmodifiableBoundingBox(
                    structureMinX + zMin, structureMinY + yMin,
                    structureMinZ + xMin,
                    structureMinX + zMax - 1 + zMin,
                    structureMinY + yMax - 1 + yMin,
                    structureMinZ + xMax - 1 + xMin
            );

            default -> new UnmodifiableBoundingBox(
                    structureMinX + xMin, structureMinY + yMin,
                    structureMinZ + zMin,
                    structureMinX + xMax - 1 + xMin,
                    structureMinY + yMax - 1 + yMin,
                    structureMinZ + zMax - 1 + zMin
            );
        };
    }

    public static BoundingBox from(BoundingBox bounds) {
        return bounds instanceof UnmodifiableBoundingBox unmodifiable ? unmodifiable : new UnmodifiableBoundingBox(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
    }
}
