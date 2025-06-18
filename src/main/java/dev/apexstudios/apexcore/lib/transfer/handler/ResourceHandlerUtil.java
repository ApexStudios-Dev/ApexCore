package dev.apexstudios.apexcore.lib.transfer.handler;

import dev.apexstudios.apexcore.lib.transfer.resource.IResource;

public interface ResourceHandlerUtil {
    static boolean isEmpty(IResource resource, int amount) {
        return amount <= 0 || resource.isEmpty();
    }
}
