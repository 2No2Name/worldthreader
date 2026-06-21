package no2.worldthreader.mixin.threading_compatibility.fixes.dedicated_server_console;

import com.mojang.datafixers.DataFixer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.Proxy;
import java.util.Optional;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServer {


    public DedicatedServerMixin(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes, NotificationManager notificationManager) {
        super(serverThread, storageSource, packRepository, worldStem, gameRules, proxy, fixerUpper, services, levelLoadListener, propagatesCrashes, notificationManager);
    }

    @Redirect(
            method = "handleConsoleInputs",
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/ConsoleInput;source:Lnet/minecraft/commands/CommandSourceStack;")
    )
    private CommandSourceStack getCommandSourceStack(ConsoleInput instance) {
        //noinspection ConstantValue
        if (instance.source == null) {
            return this.createCommandSourceStack();
        }
        return instance.source;
    }
}
