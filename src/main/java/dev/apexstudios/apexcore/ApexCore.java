package dev.apexstudios.apexcore;

import dev.apexstudios.registree.api.Registree;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ApexCore.ID)
public final class ApexCore {
    public static final String ID = "apexcore";
    public static final Registree REGISTREE = Registree.create(ID);

    public ApexCore(IEventBus modBus) {
        REGISTREE.register(modBus);
    }
}
