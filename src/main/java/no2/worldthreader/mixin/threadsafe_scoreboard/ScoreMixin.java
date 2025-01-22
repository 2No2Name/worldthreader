package no2.worldthreader.mixin.threadsafe_scoreboard;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.Score;
import no2.worldthreader.common.scoreboard.AtomicArithmeticScore;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Mixin(Score.class)
public abstract class ScoreMixin implements AtomicArithmeticScore {
    @Shadow private int value;
    @Shadow private boolean locked;
    @Shadow private @Nullable net.minecraft.network.chat.numbers.NumberFormat numberFormat;
    @Shadow private @Nullable Component display;
    @Unique
    private final AtomicInteger atomicValue = new AtomicInteger(0);
    @Unique
    private final AtomicBoolean atomicLocked = new AtomicBoolean(true);
    @Unique
    private final AtomicReference<Component> atomicDisplay = new AtomicReference<>();
    @Unique
    private final AtomicReference<NumberFormat> atomicNumberFormat = new AtomicReference<>();


    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void init(CallbackInfo ci) {
        this.atomicValue.set(this.value);
        this.atomicLocked.set(this.locked);
        this.atomicDisplay.set(this.display);
        this.atomicNumberFormat.set(this.numberFormat);
    }

    @Override
    public int worldthreader$addToValueAndGet(int amount) {
        return this.atomicValue.addAndGet(amount);
    }

    @Override
    public int worldthreader$compareExchangeValue(int expectedValue, int newValue) {
        return this.atomicValue.compareAndExchange(expectedValue, newValue);
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public int value() {
        return this.atomicValue.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public void value(int value) {
        this.atomicValue.set(value);
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public boolean isLocked() {
        return this.atomicLocked.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public void setLocked(boolean locked) {
        this.atomicLocked.set(locked);
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public @Nullable Component display() {
        return this.atomicDisplay.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public void display(Component component) {
        this.atomicDisplay.set(component);
    }


    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public @Nullable NumberFormat numberFormat() {
        return this.atomicNumberFormat.get();
    }

    /**
     * @author 2No2Name
     * @reason access atomic fields
     */
    @Overwrite
    public void numberFormat(NumberFormat numberFormat) {
        this.atomicNumberFormat.set(numberFormat);
    }


    @Redirect(
            method = "write", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;value:I", opcode = Opcodes.GETFIELD)
    )
    private int fieldAccess(Score instance) {
        return instance.value();
    }
    @Redirect(
            method = "write", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;locked:Z", opcode = Opcodes.GETFIELD)
    )
    private boolean fieldAccess1(Score instance) {
        return instance.isLocked();
    }
    @Redirect(
            method = "write", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;display:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD), require = 2
    )
    private Component fieldAccess2(Score instance) {
        return instance.display();
    }
    @Redirect(
            method = "write", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;numberFormat:Lnet/minecraft/network/chat/numbers/NumberFormat;", opcode = Opcodes.GETFIELD), require = 2
    )
    private NumberFormat fieldAccess3(Score instance) {
        return instance.numberFormat();
    }

    @Redirect(
            method = "read", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;value:I", opcode = Opcodes.PUTFIELD)
    )
    private static void fieldWrite(Score instance, int value) {
        instance.value(value);
    }
    @Redirect(
            method = "read", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;locked:Z", opcode = Opcodes.PUTFIELD)
    )
    private static void fieldWrite1(Score instance, boolean locked) {
        instance.setLocked(locked);
    }
    @Redirect(
            method = "read", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;display:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.PUTFIELD)
    )
    private static void fieldWrite2(Score instance, Component component) {
        instance.display(component);
    }
    @ModifyArg(
            method = "read", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult;ifSuccess(Ljava/util/function/Consumer;)Lcom/mojang/serialization/DataResult;", remap = false)
    )
    private static Consumer<?> fieldWrite3(Consumer<?> ifSuccess, @Local Score score) {
        return numberFormat -> score.numberFormat((NumberFormat) numberFormat);
    }
}
