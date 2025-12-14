package no2.worldthreader.mixin.fixes.entity_reference;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import no2.worldthreader.common.mixin_support.interfaces.EntityLookupExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(EntityLookup.class)
public class EntityLookupMixin<T extends EntityAccess> implements EntityLookupExtended {

    @Shadow
    @Final
    private Map<UUID, T> byUuid;

    @Override
    public Set<UUID> worldthreader$copyUUIDSet() {
        return new ObjectOpenHashSet<>(this.byUuid.keySet());
    }
}
