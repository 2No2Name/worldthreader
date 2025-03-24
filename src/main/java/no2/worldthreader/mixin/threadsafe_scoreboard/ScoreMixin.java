package no2.worldthreader.mixin.threadsafe_scoreboard;

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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
            method = {"<init>(IZLjava/util/Optional;Ljava/util/Optional;)V", "<init>()V"},
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
            method = "method_67452", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;display:Lnet/minecraft/network/chat/Component;", opcode = Opcodes.GETFIELD)
    )
    private static Component getAtomicDisplay(Score instance) {
        return instance.display();
    }
    @Redirect(
            method = "method_67451", at = @At(value = "FIELD", target = "Lnet/minecraft/world/scores/Score;numberFormat:Lnet/minecraft/network/chat/numbers/NumberFormat;", opcode = Opcodes.GETFIELD)
    )
    private static NumberFormat getAtomicNumberFormat(Score instance) {
        return instance.numberFormat();
    }
}
