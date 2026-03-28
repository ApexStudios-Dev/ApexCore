package dev.apexstudios.apexcore.xplat.common;

import dev.apexstudios.registree.xplat.Registree;
import net.minecraft.resources.Identifier;

public interface ApexCoreXplat {
    String ID = "apexcore";
    Registree REGISTREE = Registree.create(ID);

    default void init() {

    }

    static Identifier identifier(String identifier) {
        return Identifier.fromNamespaceAndPath(ID, identifier);
    }

    static String id(String identifier) {
        return ID + Identifier.NAMESPACE_SEPARATOR + identifier;
    }
}
