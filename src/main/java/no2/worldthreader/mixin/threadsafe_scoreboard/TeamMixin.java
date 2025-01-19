package no2.worldthreader.mixin.threadsafe_scoreboard;

import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

@Mixin(PlayerTeam.class)
public abstract class TeamMixin extends Team {

    private final AtomicReference<Component> atomicDisplayName = new AtomicReference<>();
    private final AtomicReference<Component> atomicPrefix = new AtomicReference<>(CommonComponents.EMPTY);
    private final AtomicReference<Component> atomicSuffix = new AtomicReference<>(CommonComponents.EMPTY);
    private final AtomicBoolean atomicFriendlyFire = new AtomicBoolean(true);
    private final AtomicBoolean atomicShowFriendlyInvisibles = new AtomicBoolean(true);
    private final AtomicReference<Team.Visibility> atomicNameTagVisibilityRule = new AtomicReference<>(Team.Visibility.ALWAYS);
    private final AtomicReference<Team.Visibility> atomicDeathMessageVisibilityRule = new AtomicReference<>(Team.Visibility.ALWAYS);
    private final AtomicReference<ChatFormatting> atomicColor = new AtomicReference<>(ChatFormatting.RESET);
    private final AtomicReference<Team.CollisionRule> atomicCollisionRule = new AtomicReference<>(Team.CollisionRule.ALWAYS);
//    @Shadow
//    @Final
//    private Scoreboard scoreboard;
//    @Mutable
//    @Shadow
//    @Final
//    private Set<String> playerList;
//
//    @Inject(
//            method = "<init>",
//            at = @At("RETURN")
//    )
//    private void createThreadsafeCollections(CallbackInfo ci) {
//        if (this.scoreboard instanceof ThreadsafeScoreboard) {
//            this.playerList = new ConcurrentHashMap<String, String>().keySet("");
//        }
//    }

//    @Redirect(
//            method = "getFormattedName()Lnet/minecraft/text/MutableText;",
//            at = @At(value = "FIELD", target = "Lnet/minecraft/scoreboard/Team;displayName:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD)
//    )
//    public Component getFormattedName(PlayerTeam team) {
//        return this.atomicDisplayName.get();
//    }
//
//    @Inject(method = "setDisplayName", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setDisplayName(Component displayName, CallbackInfo ci) {
//        this.atomicDisplayName.set(displayName);
//    }
//
//    @Inject(method = "setPrefix", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setPrefix(Component prefix, CallbackInfo ci) {
//        this.atomicPrefix.set(prefix == null ? CommonComponents.EMPTY : prefix);
//    }
//
//    @Inject(method = "setSuffix", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setSuffix(Component suffix, CallbackInfo ci) {
//        this.atomicSuffix.set(suffix == null ? CommonComponents.EMPTY : suffix);
//    }
//
//    @Inject(method = "setFriendlyFireAllowed", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setFriendlyFireAllowed(boolean friendlyFire, CallbackInfo ci) {
//        this.atomicFriendlyFire.set(friendlyFire);
//    }
//
//    @Inject(method = "setShowFriendlyInvisibles", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setShowFriendlyInvisibles(boolean showFriendlyInvisibles, CallbackInfo ci) {
//        this.atomicShowFriendlyInvisibles.set(showFriendlyInvisibles);
//    }
//
//    @Inject(method = "setNameTagVisibilityRule", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setNameTagVisibilityRule(Visibility nameTagVisibilityRule, CallbackInfo ci) {
//        this.atomicNameTagVisibilityRule.set(nameTagVisibilityRule);
//    }
//
//    @Inject(method = "setDeathMessageVisibilityRule", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setDeathMessageVisibilityRule(Visibility deathMessageVisibilityRule, CallbackInfo ci) {
//        this.atomicDeathMessageVisibilityRule.set(deathMessageVisibilityRule);
//    }
//
//    @Inject(method = "setCollisionRule", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setCollisionRule(CollisionRule collisionRule, CallbackInfo ci) {
//        this.atomicCollisionRule.set(collisionRule);
//    }
//
//    @Inject(method = "setColor", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
//    public void setColor(ChatFormatting color, CallbackInfo ci) {
//        this.atomicColor.set(color);
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public Component getPrefix() {
//        return this.atomicPrefix.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public Component getSuffix() {
//        return this.atomicSuffix.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Override
//    @Overwrite
//    public boolean isAllowFriendlyFire() {
//        return this.atomicFriendlyFire.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Override
//    @Overwrite
//    public boolean canSeeFriendlyInvisibles() {
//        return this.atomicShowFriendlyInvisibles.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Override
//    @Overwrite
//    public Team.Visibility getNameTagVisibility() {
//        return this.atomicNameTagVisibilityRule.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Override
//    @Overwrite
//    public Team.Visibility getDeathMessageVisibility() {
//        return this.atomicDeathMessageVisibilityRule.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Override
//    @Overwrite
//    public Team.CollisionRule getCollisionRule() {
//        return this.atomicCollisionRule.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Override
//    @Overwrite
//    public ChatFormatting getColor() {
//        return this.atomicColor.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public Component getDisplayName() {
//        return this.atomicDisplayName.get();
//    }
//
//
//    @Override
//    public MutableComponent getFormattedName(Component name) {
//        MutableComponent mutableText = Component.empty().append(this.getPrefix()).append(name).append(this.getSuffix());
//        ChatFormatting formatting = this.getColor();
//        if (formatting != ChatFormatting.RESET) {
//            mutableText.withStyle(formatting);
//        }
//        return mutableText;
//    }
}
