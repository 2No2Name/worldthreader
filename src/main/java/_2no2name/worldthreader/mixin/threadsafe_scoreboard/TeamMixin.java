package _2no2name.worldthreader.mixin.threadsafe_scoreboard;

import _2no2name.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

@Mixin(Team.class)
public abstract class TeamMixin extends AbstractTeam {

    private final AtomicReference<Text> atomicDisplayName = new AtomicReference<>();
    private final AtomicReference<Text> atomicPrefix = new AtomicReference<>(ScreenTexts.EMPTY);
    private final AtomicReference<Text> atomicSuffix = new AtomicReference<>(ScreenTexts.EMPTY);
    private final AtomicBoolean atomicFriendlyFire = new AtomicBoolean(true);
    private final AtomicBoolean atomicShowFriendlyInvisibles = new AtomicBoolean(true);
    private final AtomicReference<AbstractTeam.VisibilityRule> atomicNameTagVisibilityRule = new AtomicReference<>(AbstractTeam.VisibilityRule.ALWAYS);
    private final AtomicReference<AbstractTeam.VisibilityRule> atomicDeathMessageVisibilityRule = new AtomicReference<>(AbstractTeam.VisibilityRule.ALWAYS);
    private final AtomicReference<Formatting> atomicColor = new AtomicReference<>(Formatting.RESET);
    private final AtomicReference<AbstractTeam.CollisionRule> atomicCollisionRule = new AtomicReference<>(AbstractTeam.CollisionRule.ALWAYS);
    @Shadow
    @Final
    private Scoreboard scoreboard;
    @Mutable
    @Shadow
    @Final
    private Set<String> playerList;

    @Shadow
    @Final
    private String name;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void createThreadsafeCollections(CallbackInfo ci) {
        if (this.scoreboard instanceof ThreadsafeScoreboard) {
            this.playerList = new ConcurrentHashMap<String, String>().keySet("");
        }
        this.atomicDisplayName.set(Text.literal(this.name));
    }

    @Redirect(
            method = "getFormattedName()Lnet/minecraft/text/MutableText;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/scoreboard/Team;displayName:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD)
    )
    public Text getFormattedName(Team team) {
        return this.atomicDisplayName.get();
    }

    @Inject(method = "setDisplayName", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setDisplayName(Text displayName, CallbackInfo ci) {
        this.atomicDisplayName.set(displayName);
    }

    @Inject(method = "setPrefix", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setPrefix(Text prefix, CallbackInfo ci) {
        this.atomicPrefix.set(prefix == null ? ScreenTexts.EMPTY : prefix);
    }

    @Inject(method = "setSuffix", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setSuffix(Text suffix, CallbackInfo ci) {
        this.atomicSuffix.set(suffix == null ? ScreenTexts.EMPTY : suffix);
    }

    @Inject(method = "setFriendlyFireAllowed", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setFriendlyFireAllowed(boolean friendlyFire, CallbackInfo ci) {
        this.atomicFriendlyFire.set(friendlyFire);
    }

    @Inject(method = "setShowFriendlyInvisibles", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setShowFriendlyInvisibles(boolean showFriendlyInvisibles, CallbackInfo ci) {
        this.atomicShowFriendlyInvisibles.set(showFriendlyInvisibles);
    }

    @Inject(method = "setNameTagVisibilityRule", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setNameTagVisibilityRule(VisibilityRule nameTagVisibilityRule, CallbackInfo ci) {
        this.atomicNameTagVisibilityRule.set(nameTagVisibilityRule);
    }

    @Inject(method = "setDeathMessageVisibilityRule", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setDeathMessageVisibilityRule(VisibilityRule deathMessageVisibilityRule, CallbackInfo ci) {
        this.atomicDeathMessageVisibilityRule.set(deathMessageVisibilityRule);
    }

    @Inject(method = "setCollisionRule", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setCollisionRule(CollisionRule collisionRule, CallbackInfo ci) {
        this.atomicCollisionRule.set(collisionRule);
    }

    @Inject(method = "setColor", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/scoreboard/Scoreboard;updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V"))
    public void setColor(Formatting color, CallbackInfo ci) {
        this.atomicColor.set(color);
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public Text getPrefix() {
        return this.atomicPrefix.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public Text getSuffix() {
        return this.atomicSuffix.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public boolean isFriendlyFireAllowed() {
        return this.atomicFriendlyFire.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public boolean shouldShowFriendlyInvisibles() {
        return this.atomicShowFriendlyInvisibles.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
        return this.atomicNameTagVisibilityRule.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public AbstractTeam.VisibilityRule getDeathMessageVisibilityRule() {
        return this.atomicDeathMessageVisibilityRule.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public AbstractTeam.CollisionRule getCollisionRule() {
        return this.atomicCollisionRule.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Override
    @Overwrite
    public Formatting getColor() {
        return this.atomicColor.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public Text getDisplayName() {
        return this.atomicDisplayName.get();
    }


    @Override
    public MutableText decorateName(Text name) {
        MutableText mutableText = Text.empty().append(this.getPrefix()).append(name).append(this.getSuffix());
        Formatting formatting = this.getColor();
        if (formatting != Formatting.RESET) {
            mutableText.formatted(formatting);
        }
        return mutableText;
    }
}
