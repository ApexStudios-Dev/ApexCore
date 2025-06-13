package dev.apexstudios.apexcore.lib.multiblock;

import java.util.function.Consumer;

public interface MultiBlockProperties {
    MultiBlockProperty MB_1x1x2 = create(builder -> builder.sized(1, 1, 2));
    MultiBlockProperty MB_1x2x2 = create(builder -> builder.sized(1, 2, 2));
    MultiBlockProperty MB_1x2x1 = create(builder -> builder.sized(1, 2, 1));
    MultiBlockProperty MB_1x3x2 = create(builder -> builder.sized(1, 3, 2));

    private static MultiBlockProperty create(Consumer<MultiBlockProperty.Builder> consumer) {
        return MultiBlockProperty.create(consumer);
    }
}
