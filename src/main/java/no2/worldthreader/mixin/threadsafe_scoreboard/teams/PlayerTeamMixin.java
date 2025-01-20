package no2.worldthreader.mixin.threadsafe_scoreboard.teams;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.scoreboard.MutablePlayerTeam;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("NullableProblems")
@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin extends Team implements MutablePlayerTeam {

    @Shadow
    @Final
    private Scoreboard scoreboard;

    @Mutable
    @Shadow
    @Final
    private Set<String> players;

    @Unique
    private ImmutableSet<String> playersReadOnly;

    @Inject(
            method = "<init>(Lnet/minecraft/world/scores/Scoreboard;Ljava/lang/String;)V", at = @At("RETURN")
    )
    protected void init(Scoreboard scoreboard, String string, CallbackInfo ci) {
        if (this.scoreboard instanceof ThreadsafeScoreboard) {
            this.playersReadOnly = ImmutableSet.copyOf(this.players);
        }
    }

    public Collection<String> getPlayers() {
        if (this.scoreboard instanceof ThreadsafeScoreboard) {
            return this.playersReadOnly;

        }
        return this.players;
    }

    @Override
    public Set<String> worldthreader$getMutablePlayerSet() {
        this.ensureSafe();
        return this.players;
    }

    @Unique
    private void ensureSafe() {
        if (this.scoreboard instanceof ThreadsafeScoreboard threadsafeScoreboard) {
            threadsafeScoreboard.worldthreader$ensureExclusiveScoreboardAccess();
        }
    }

    @Override
    public void worldthreader$onMutablePlayerSetModifiedExternally() {
        if (this.scoreboard instanceof ThreadsafeScoreboard threadsafeScoreboard) {
            threadsafeScoreboard.worldthreader$crashIfNoExclusiveScoreboardAccess();
            this.playersReadOnly = ImmutableSet.copyOf(this.players);
        }
    }

    @Inject(method = "setDisplayName", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;displayName:Lnet/minecraft/network/chat/Component;"))
    public void setDisplayName(Component displayName, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setPlayerPrefix", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;playerPrefix:Lnet/minecraft/network/chat/Component;"))
    public void setPlayerPrefix(Component prefix, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setPlayerSuffix", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;playerSuffix:Lnet/minecraft/network/chat/Component;"))
    public void setPlayerSuffix(Component suffix, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setAllowFriendlyFire", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;allowFriendlyFire:Z"))
    public void setAllowFriendlyFire(boolean friendlyFire, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setSeeFriendlyInvisibles", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;seeFriendlyInvisibles:Z"))
    public void setSeeFriendlyInvisibles(boolean showFriendlyInvisibles, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setNameTagVisibility", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;nameTagVisibility:Lnet/minecraft/world/scores/Team$Visibility;"))
    public void setNameTagVisibility(Visibility nameTagVisibilityRule, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setDeathMessageVisibility", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;deathMessageVisibility:Lnet/minecraft/world/scores/Team$Visibility;"))
    public void setDeathMessageVisibility(Visibility deathMessageVisibilityRule, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setCollisionRule", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;collisionRule:Lnet/minecraft/world/scores/Team$CollisionRule;"))
    public void setCollisionRule(CollisionRule collisionRule, CallbackInfo ci) {
        this.ensureSafe();
    }

    @Inject(method = "setColor", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/scores/PlayerTeam;color:Lnet/minecraft/ChatFormatting;"))
    public void setColor(ChatFormatting color, CallbackInfo ci) {
        this.ensureSafe();
    }
}
