package no2.worldthreader.mixin.threadsafe_scoreboard;


import no2.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin extends Scoreboard implements ThreadsafeScoreboard {
//    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);
//    @Shadow
//    @Final
//    private MinecraftServer server;
//    private volatile ImmutableMap<String, Objective> immutableScoreboardObjectives = null;
//
//    private ImmutableMap<String, Objective> getScoreboardObjectivesImmutable() {
//        if (this.immutableScoreboardObjectives == null) {
//            this.readWriteLock.readLock().lock();
//            this.immutableScoreboardObjectives = ImmutableMap.copyOf(((ScoreboardAccess) this).getObjectivesByName());
//            this.readWriteLock.readLock().unlock();
//        }
//        return this.immutableScoreboardObjectives;
//    }
//
//    @Override
//    public Objective addObjective(String name, ObjectiveCriteria criterion2, Component displayName, ObjectiveCriteria.RenderType renderType) {
//        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
//        if (requiresLocking) {
//            this.readWriteLock.writeLock().lock();
//        }
//        try {
//            this.immutableScoreboardObjectives = null;
//            return super.addObjective(name, criterion2, displayName, renderType);
//        } finally {
//            if (requiresLocking) {
//                this.readWriteLock.writeLock().unlock();
//            }
//        }
//    }
//
//    @Override
//    public void removeObjective(Objective objective) {
//        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
//        if (requiresLocking) {
//            this.readWriteLock.writeLock().lock();
//        }
//        try {
//            this.immutableScoreboardObjectives = null;
//            super.removeObjective(objective);
//        } finally {
//            if (requiresLocking) {
//                this.readWriteLock.writeLock().unlock();
//            }
//        }
//    }
//
//    @Override
//    public boolean hasObjective(String name) {
//        return this.getScoreboardObjectivesImmutable().containsKey(name);
//    }
//
//    @Override
//    public Objective getOrCreateObjective(String name) {
//        return this.getScoreboardObjectivesImmutable().get(name);
//    }
//
//    @Nullable
//    @Override
//    public Objective getObjective(@Nullable String name) {
//        return this.getScoreboardObjectivesImmutable().get(name);
//    }
//
//    @Override
//    public Collection<Objective> getObjectives() {
//        return this.getScoreboardObjectivesImmutable().values();
//    }
//
//    @Override
//    public Collection<String> getObjectiveNames() {
//        return this.getScoreboardObjectivesImmutable().keySet();
//    }
//
//
//    @Nullable
//    @Override
//    public Objective getDisplayObjective(int slot) {
//        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
//        if (requiresLocking) {
//            this.readWriteLock.readLock().lock();
//        }
//        try {
//            return super.getDisplayObjective(slot);
//        } finally {
//            if (requiresLocking) {
//                this.readWriteLock.readLock().unlock();
//            }
//        }
//    }
//
//    @Inject(
//            method = "setObjectiveSlot(ILnet/minecraft/scoreboard/ScoreboardObjective;)V",
//            at = @At("HEAD")
//    )
//    private void acquireSetObjectiveSlot(int slot, Objective objective, CallbackInfo ci) {
//        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
//        if (requiresLocking) {
//            this.readWriteLock.writeLock().lock();
//        }
//    }
//
//    @Inject(
//            method = "setObjectiveSlot(ILnet/minecraft/scoreboard/ScoreboardObjective;)V",
//            at = @At("HEAD")
//    )
//    private void releaseSetObjectiveSlot(int slot, Objective objective, CallbackInfo ci) {
//        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
//        if (requiresLocking) {
//            this.readWriteLock.writeLock().unlock();
//        }
//    }
}