package no2.worldthreader.mixin.threading_compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * @author 2No2Name
     * @reason That's not threadsafe. So don't do it.
     */
    @Overwrite
    @Nullable
    private ServerLevel getServerLevel() {
        return null;
    }

    /**
     * @author 2No2Name
     * @reason That's not threadsafe. So don't do it.
     */
    @Overwrite
    private Level getLevel() {
        return this.minecraft.level;
    }

    @Redirect(
            method = {"getGameInformation()Ljava/util/List;", "drawGameInformation(Lnet/minecraft/client/gui/GuiGraphics;)V"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getSingleplayerServer()Lnet/minecraft/client/server/IntegratedServer;")
    )
    private IntegratedServer avoidOffthreadAccess(Minecraft instance) {
        return null;
    }
}
