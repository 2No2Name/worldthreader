package _2no2name.worldthreader.mixin.threadsafe_scoreboard;


import _2no2name.worldthreader.common.mixin_support.interfaces.MinecraftServerExtended;
import _2no2name.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import com.google.common.collect.ImmutableMap;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
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
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);
    @Shadow
    @Final
    private MinecraftServer server;
    private volatile ImmutableMap<String, ScoreboardObjective> immutableScoreboardObjectives = null;

    private ImmutableMap<String, ScoreboardObjective> getScoreboardObjectivesImmutable() {
        if (this.immutableScoreboardObjectives == null) {
            this.readWriteLock.readLock().lock();
            this.immutableScoreboardObjectives = ImmutableMap.copyOf(((ScoreboardAccess) this).getObjectives());
            this.readWriteLock.readLock().unlock();
        }
        return this.immutableScoreboardObjectives;
    }

    @Override
    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion2, Text displayName, ScoreboardCriterion.RenderType renderType) {
        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
        if (requiresLocking) {
            this.readWriteLock.writeLock().lock();
        }
        try {
            this.immutableScoreboardObjectives = null;
            return super.addObjective(name, criterion2, displayName, renderType);
        } finally {
            if (requiresLocking) {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    @Override
    public void removeObjective(ScoreboardObjective objective) {
        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
        if (requiresLocking) {
            this.readWriteLock.writeLock().lock();
        }
        try {
            this.immutableScoreboardObjectives = null;
            super.removeObjective(objective);
        } finally {
            if (requiresLocking) {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    @Override
    public boolean containsObjective(String name) {
        return this.getScoreboardObjectivesImmutable().containsKey(name);
    }

    @Override
    public ScoreboardObjective getObjective(String name) {
        return this.getScoreboardObjectivesImmutable().get(name);
    }

    @Nullable
    @Override
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        return this.getScoreboardObjectivesImmutable().get(name);
    }

    @Override
    public Collection<ScoreboardObjective> getObjectives() {
        return this.getScoreboardObjectivesImmutable().values();
    }

    @Override
    public Collection<String> getObjectiveNames() {
        return this.getScoreboardObjectivesImmutable().keySet();
    }


    @Nullable
    @Override
    public ScoreboardObjective getObjectiveForSlot(int slot) {
        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
        if (requiresLocking) {
            this.readWriteLock.readLock().lock();
        }
        try {
            return super.getObjectiveForSlot(slot);
        } finally {
            if (requiresLocking) {
                this.readWriteLock.readLock().unlock();
            }
        }
    }

    @Inject(
            method = "setObjectiveSlot(ILnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD")
    )
    private void acquireSetObjectiveSlot(int slot, ScoreboardObjective objective, CallbackInfo ci) {
        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
        if (requiresLocking) {
            this.readWriteLock.writeLock().lock();
        }
    }

    @Inject(
            method = "setObjectiveSlot(ILnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD")
    )
    private void releaseSetObjectiveSlot(int slot, ScoreboardObjective objective, CallbackInfo ci) {
        boolean requiresLocking = ((MinecraftServerExtended) this.server).isTickMultithreaded();
        if (requiresLocking) {
            this.readWriteLock.writeLock().unlock();
        }
    }
}