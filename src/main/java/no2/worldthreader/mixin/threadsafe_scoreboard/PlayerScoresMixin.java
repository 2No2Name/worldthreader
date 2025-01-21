package no2.worldthreader.mixin.threadsafe_scoreboard;

import it.unimi.dsi.fastutil.objects.Reference2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScores;
import net.minecraft.world.scores.Score;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Mixin(PlayerScores.class)
public class PlayerScoresMixin {

    @Unique
    private final ConcurrentHashMap<Objective, Score> scoresThreadsafe = new ConcurrentHashMap<>(16, 0.5F);

    @Redirect(
            method = "get", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false)
    )
    private <V> V getThreadSafe(Reference2ObjectOpenHashMap<Objective, V> instance, Object k) {
        //noinspection unchecked,SuspiciousMethodCalls
        return (V) this.scoresThreadsafe.get(k);
    }
    @Redirect(
            method = "remove", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;remove(Ljava/lang/Object;)Ljava/lang/Object;", remap = false)
    )
    private <V> V removeThreadSafe(Reference2ObjectOpenHashMap<Objective, V> instance, Object k) {
        //noinspection unchecked,SuspiciousMethodCalls
        return (V) this.scoresThreadsafe.remove(k);
    }
    @Redirect(
            method = "setScore", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false)
    )
    private <K, V> V putThreadSafe(Reference2ObjectOpenHashMap<K, V> instance, K k, V v) {
        //noinspection unchecked
        return (V) this.scoresThreadsafe.put((Objective) k, (Score) v);
    }
    @Redirect(
            method = "hasScores", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;isEmpty()Z", remap = false)
    )
    private boolean isEmptyThreadsafe(Reference2ObjectOpenHashMap<?, ?> instance) {
        return this.scoresThreadsafe.isEmpty();
    }

    @Redirect(
            method = "getOrCreate", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;computeIfAbsent(Ljava/lang/Object;Lit/unimi/dsi/fastutil/objects/Reference2ObjectFunction;)Ljava/lang/Object;", remap = false)
    )
    private <K,V> V useThreadsafeMap(Reference2ObjectOpenHashMap<K, V> instance, K key, Reference2ObjectFunction<? super K, ? extends V> mappingFunction){
        //noinspection unchecked
        return (V) this.scoresThreadsafe.computeIfAbsent((Objective) key, obj -> (Score) mappingFunction.get(obj));
    }

    @Redirect(
            method = "listScores", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;forEach(Ljava/util/function/BiConsumer;)V", remap = false)
    )
    private <K,V> void useThreadsafeMap(Reference2ObjectOpenHashMap<K,V> instance, BiConsumer<K,V> biConsumer){
        //noinspection unchecked
        this.scoresThreadsafe.forEach((objective, score) -> biConsumer.accept((K) objective, (V) score));
    }
    @ModifyArg(
            method = "listRawScores", at = @At(value = "INVOKE", target = "Ljava/util/Collections;unmodifiableMap(Ljava/util/Map;)Ljava/util/Map;")
    )
    private <K,V> Map<? extends K, ? extends V> useThreadsafeMap(Map<? extends K, ? extends V> m){
        //noinspection unchecked
        return (Map<? extends K, ? extends V>) this.scoresThreadsafe;
    }

}
