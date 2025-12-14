package no2.worldthreader.common.interdimensional;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityLookup;
import no2.worldthreader.common.mixin_support.interfaces.EntityLookupExtended;
import no2.worldthreader.mixin.fixes.entity_reference.PersistentEntitySectionManagerAccessor;
import no2.worldthreader.mixin.fixes.entity_reference.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public record InterdimensionalEntityInfo(Reference2ReferenceOpenHashMap<ServerLevel, Set<UUID>> existingEntities) {

    public InterdimensionalEntityInfo(Iterable<ServerLevel> levels) {
        this(new Reference2ReferenceOpenHashMap<>());
        for (ServerLevel level : levels) {
            //noinspection unchecked
            EntityLookup<@NotNull Entity> visibleEntityStorage = ((PersistentEntitySectionManagerAccessor<Entity>) (((ServerLevelAccessor) level).getPersistentEntitySectionManager())).getVisibleEntityStorage();
            this.existingEntities.put(level, ((EntityLookupExtended) visibleEntityStorage).worldthreader$copyUUIDSet());
        }
    }
}
