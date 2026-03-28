package dev.apexstudios.apexcore.api.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public interface StringHelper {
    static String toEnglishName(String registryName) {
        return Stream.of(registryName.split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }
}
