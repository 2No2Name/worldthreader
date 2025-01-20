package no2.worldthreader.mixin.threadsafe_scoreboard.teams;

import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("NullableProblems")
@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin extends Team {

    @Unique
    private final AtomicReference<Component> atomicDisplayName = new AtomicReference<>();
    @Unique
    private final AtomicReference<Component> atomicPlayerPrefix = new AtomicReference<>(CommonComponents.EMPTY);
    @Unique
    private final AtomicReference<Component> atomicPlayerSuffix = new AtomicReference<>(CommonComponents.EMPTY);
    @Unique
    private final AtomicBoolean atomicAllowFriendlyFire = new AtomicBoolean(true);
    @Unique
    private final AtomicBoolean atomicSeeFriendlyInvisibles = new AtomicBoolean(true);
    @Unique
    private final AtomicReference<Team.Visibility> atomicNameTagVisibility = new AtomicReference<>(Team.Visibility.ALWAYS);
    @Unique
    private final AtomicReference<Team.Visibility> atomicDeathMessageVisibility = new AtomicReference<>(Team.Visibility.ALWAYS);
    @Unique
    private final AtomicReference<ChatFormatting> atomicColor = new AtomicReference<>(ChatFormatting.RESET);
    @Unique
    private final AtomicReference<Team.CollisionRule> atomicCollisionRule = new AtomicReference<>(Team.CollisionRule.ALWAYS);
    @Shadow
    @Final
    private Scoreboard scoreboard;

    @Mutable
    @Shadow
    @Final
    private Set<String> players;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void createThreadsafeCollections(CallbackInfo ci) {
        if (this.scoreboard instanceof ThreadsafeScoreboard) {
            this.players = new ConcurrentHashMap<String, String>().keySet("");
        }
    }

    @Inject(method = "setDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setDisplayName(Component displayName, CallbackInfo ci) {
        this.atomicDisplayName.set(displayName);
    }

    @Inject(method = "setPlayerPrefix", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setPlayerPrefix(Component prefix, CallbackInfo ci) {
        this.atomicPlayerPrefix.set(prefix == null ? CommonComponents.EMPTY : prefix);
    }

    @Inject(method = "setPlayerSuffix", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setPlayerSuffix(Component suffix, CallbackInfo ci) {
        this.atomicPlayerSuffix.set(suffix == null ? CommonComponents.EMPTY : suffix);
    }

    @Inject(method = "setAllowFriendlyFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setAllowFriendlyFire(boolean friendlyFire, CallbackInfo ci) {
        this.atomicAllowFriendlyFire.set(friendlyFire);
    }

    @Inject(method = "setSeeFriendlyInvisibles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setSeeFriendlyInvisibles(boolean showFriendlyInvisibles, CallbackInfo ci) {
        this.atomicSeeFriendlyInvisibles.set(showFriendlyInvisibles);
    }

    @Inject(method = "setNameTagVisibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setNameTagVisibility(Visibility nameTagVisibilityRule, CallbackInfo ci) {
        this.atomicNameTagVisibility.set(nameTagVisibilityRule);
    }

    @Inject(method = "setDeathMessageVisibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setDeathMessageVisibility(Visibility deathMessageVisibilityRule, CallbackInfo ci) {
        this.atomicDeathMessageVisibility.set(deathMessageVisibilityRule);
    }

    @Inject(method = "setCollisionRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setCollisionRule(CollisionRule collisionRule, CallbackInfo ci) {
        this.atomicCollisionRule.set(collisionRule);
    }

    @Inject(method = "setColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;onTeamChanged(Lnet/minecraft/world/scores/PlayerTeam;)V"))
    public void setColor(ChatFormatting color, CallbackInfo ci) {
        this.atomicColor.set(color);
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public Component getDisplayName() {
        return this.atomicDisplayName.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public Component getPlayerPrefix() {
        return this.atomicPlayerPrefix.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public Component getPlayerSuffix() {
        return this.atomicPlayerSuffix.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public boolean isAllowFriendlyFire() {
        return this.atomicAllowFriendlyFire.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public boolean canSeeFriendlyInvisibles() {
        return this.atomicSeeFriendlyInvisibles.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public Team.Visibility getNameTagVisibility() {
        return this.atomicNameTagVisibility.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public Team.Visibility getDeathMessageVisibility() {
        return this.atomicDeathMessageVisibility.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public Team.CollisionRule getCollisionRule() {
        return this.atomicCollisionRule.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public ChatFormatting getColor() {
        return this.atomicColor.get();
    }
    
    @Redirect(
            method = "getFormattedDisplayName()Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/PlayerTeam;displayName:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD)
    )
    public Component getFormattedName(PlayerTeam team) {
        return this.atomicDisplayName.get();
    }

    @Redirect(
            method = "getFormattedName",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/PlayerTeam;playerPrefix:Lnet/minecraft/network/chat/Component;")
    )
    private Component getAtomicPlayerPrefix(PlayerTeam instance) {
        return this.atomicPlayerPrefix.get();
    }
    @Redirect(
            method = "getFormattedName",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/PlayerTeam;playerSuffix:Lnet/minecraft/network/chat/Component;")
    )
    private Component getAtomicPlayerSuffix(PlayerTeam instance) {
        return this.atomicPlayerSuffix.get();
    }
}
