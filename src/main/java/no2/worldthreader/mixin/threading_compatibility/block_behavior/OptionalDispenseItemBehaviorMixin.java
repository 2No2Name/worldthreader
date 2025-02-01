package no2.worldthreader.mixin.threading_compatibility.block_behavior;

import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OptionalDispenseItemBehavior.class)
public class OptionalDispenseItemBehaviorMixin {
    //Note: This mixin modifies the behavior of the bug in the scute brushing dispenser behavior where the failure sound
    // is played forever instead of a success sound after the first failed attempt.

    @Unique
    private final ThreadLocal<Boolean> successThreadlocal = ThreadLocal.withInitial(() -> true);

    /**
     * @author 2No2Name
     * @reason Thread-safety
     */
    @Overwrite
   	public boolean isSuccess() {
		return this.successThreadlocal.get();
	}

    /**
     * @author 2No2Name
     * @reason Thread-safety
     */
    @Overwrite
	public void setSuccess(boolean bl) {
		this.successThreadlocal.set(bl);
	}

}
