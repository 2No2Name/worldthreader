package no2.worldthreader.mixin.entity_id_wireless_redstone_fix;

import no2.worldthreader.common.mixin_support.interfaces.ServerWorldWithWirelessRedstoneFix;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public class ServerWorldMixin implements ServerWorldWithWirelessRedstoneFix {

    private static final int ENTITY_ID_STRIDE = 4;
    private int entityIdChoice = 0;

    /**
     * We waste 3 out of 4 potential entity ids. However, this makes it possible to avoid breaking the wireless redstone
     * that detects the entity id modulo 4 from item entities' falling behavior
     *
     * @param entityIdCounter the shared atomic entity id counter
     * @return The next entity id
     */
    @Override
    public int worldthreader$getNextEntityId(AtomicInteger entityIdCounter) {
        //Replacing entityIdCounter.incrementAndGet()
        int minNextId = entityIdCounter.getAndAdd(ENTITY_ID_STRIDE) + 1;
        this.entityIdChoice = (this.entityIdChoice + 1) % ENTITY_ID_STRIDE;
        return minNextId + entityIdChoice;
    }
}
