package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import no2.worldthreader.common.scoreboard.ThreadsafeScoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Objective.class)
public abstract class ObjectiveMixin {

    @Shadow @Final private Scoreboard scoreboard;

    @Unique
    private void ensureSafe() {
        if (this.scoreboard instanceof ThreadsafeScoreboard threadsafeScoreboard) {
            threadsafeScoreboard.worldthreader$ensureExclusiveScoreboardAccess();
        }
    }


    @Inject(
            method = {
                    "setDisplayName(Lnet/minecraft/network/chat/Component;)V",
                    "setRenderType(Lnet/minecraft/world/scores/criteria/ObjectiveCriteria$RenderType;)V",
                    "setDisplayAutoUpdate(Z)V",
                    "setNumberFormat(Lnet/minecraft/network/chat/numbers/NumberFormat;)V"
            },
            at = @At("HEAD")
    )
    private void ensureSafe1(CallbackInfo ci) {
        this.ensureSafe();
    }
}
