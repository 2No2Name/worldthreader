package no2.worldthreader.mixin.dimension_change.departure.triggers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import no2.worldthreader.common.mixin_support.interfaces.UnsafeOwnerAccess;
import no2.worldthreader.common.thread.WorldThreadingManager;
import no2.worldthreader.mixin.threading_compatibility.entity_owners.ProjectileMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends ProjectileMixin implements UnsafeOwnerAccess {

    @Unique
    private boolean hasServerPlayerAsOwner;

    @Shadow
    private long ticketTimer;


    public ThrownEnderpearlMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    //Todo Replace the ender pearl death, chunk ticket code as it forces serialization many ticks/every tick in plausible scenarios

    @Override
    public void setCachedOwnerWrapped(Projectile theEnderPearl, Entity cachedOwner) {
        Entity previousOwner = this.worldthreader$getCachedOwnerUnsafe();
        super.setCachedOwnerWrapped(theEnderPearl, cachedOwner);
        if (previousOwner == cachedOwner) {
            return;
        }
        boolean isPlayer = cachedOwner instanceof ServerPlayer;
        this.hasServerPlayerAsOwner = isPlayer; //TODO removal of the server player is not noticed until getOwner is called
        if (isPlayer) {
            this.ensureThreadsafeAccess(cachedOwner);
            //Register the enderpearl more reliably than vanilla. Then omit redundant registering during the enderpearl tick which would require exclusive world access
            ((ServerPlayer) cachedOwner).registerEnderPearl((ThrownEnderpearl) (Object) this);
        }
    }

    @Unique
    private void ensureThreadsafeAccess(Entity cachedOwner) {
        if (!WorldThreadingManager.isThreadOwningWorld((ServerLevel) cachedOwner.level())) {
            Objects.requireNonNull(this.getServer()).getLevel(cachedOwner.level().dimension());
        }
    }

    /**
     * @author 2No2Name
     * @reason Fast prototyping for: Replace the ender pearl death, chunk ticket code as it forces serialization many ticks/every tick in plausible scenarios
     */
    @Overwrite
    public void tick() {
        int i = SectionPos.blockToSectionCoord(this.position().x());
        int j = SectionPos.blockToSectionCoord(this.position().z());
        if (this.shouldDiscardWhenOwnerIsDeadPlayer() && this.isOwnerDeadPlayer()) {
            this.discard();
        } else {
            super.tick();
        }

        this.updateTicket(i, j);
    }

    @Unique
    private void updateTicket(int i, int j) {
        if (this.isAlive()) {
            BlockPos blockPos = BlockPos.containing(this.position());
            if ((--this.ticketTimer <= 0L || i != SectionPos.blockToSectionCoord(blockPos.getX()) || j != SectionPos.blockToSectionCoord(blockPos.getZ()))
                    && isOwnerServerPlayer()) {
                updateChunkLoadTicket();
            }
        }
    }

    @Unique
    private boolean isOwnerServerPlayer() {
//        return this.getOwner() instanceof ServerPlayer;
        return this.hasServerPlayerAsOwner;
    }

    @Unique
    private void updateChunkLoadTicket() {
        long result = 0L;
        if (this.level() instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = this.chunkPosition();
//            serverPlayer2.registerEnderPearl((ThrownEnderpearl) (Object) this); //Commented line to omit redundant pearl registration as discussed above (avoids needing exclusive world access)
            serverLevel.resetEmptyTime();
            result = ServerPlayer.placeEnderPearlTicket(serverLevel, chunkPos) - 1L;
        };
        this.ticketTimer = result;
    }

    @Unique
    private boolean shouldDiscardWhenOwnerIsDeadPlayer() {
        return this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH);
    }

    @Unique
    private boolean isOwnerDeadPlayer() {
        Entity entity = this.getOwner();
        return entity instanceof ServerPlayer && !entity.isAlive();
    }
}
