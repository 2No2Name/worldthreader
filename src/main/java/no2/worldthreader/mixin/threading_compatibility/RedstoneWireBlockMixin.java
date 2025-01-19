package no2.worldthreader.mixin.threading_compatibility;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

@Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
	/**
	 * {@code RedstoneWireBlock#wiresGivePower} is not thread-safe since it's a global flag. To ensure
	 * no interference between threads the field is replaced with this thread local one.
	 *
	 * @see RedStoneWireBlock#isSignalSource(BlockState)
	 */
	@Unique
	private final ThreadLocal<Boolean> shouldSignalThreadLocal = ThreadLocal.withInitial(() -> true);


	@Redirect(
			method = "getBlockSignal", require = 2,
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldSignal:Z")
	)
	private void getReceivedRedstonePowerBefore(RedStoneWireBlock instance, boolean value) {
		this.shouldSignalThreadLocal.set(value);
	}


	@Redirect(
			method = "isSignalSource(Lnet/minecraft/world/level/block/state/BlockState;)Z",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldSignal:Z")
	)
	public boolean shouldSignalSafe(RedStoneWireBlock instance) {
		return this.shouldSignalThreadLocal.get();
	}

	@ModifyExpressionValue(
			method = "getDirectSignal(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldSignal:Z")
	)
	public boolean shouldSignalSafe1(boolean original) {
		return this.shouldSignalThreadLocal.get();
	}

	@ModifyExpressionValue(
			method = "getSignal(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldSignal:Z")
	)
	public boolean shouldSignalSafe2(boolean original) {
		return this.shouldSignalThreadLocal.get();
	}
}
