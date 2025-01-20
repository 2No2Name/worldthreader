package no2.worldthreader.mixin.threadsafe_scoreboard;

import net.minecraft.world.scores.Score;
import no2.worldthreader.common.scoreboard.ScoreboardScoreAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Score.class)
public abstract class ScoreMixin implements ScoreboardScoreAccess {
//    private final AtomicInteger atomicScore = new AtomicInteger();
//    private final AtomicBoolean atomicLocked = new AtomicBoolean();
//    private final AtomicBoolean atomicForceUpdate = new AtomicBoolean();
//    @Shadow
//    private boolean locked;
//    @Shadow
//    private boolean forceUpdate;
//    @Shadow
//    private int value;
//
//    @Inject(
//            method = "<init>",
//            at = @At("RETURN")
//    )
//    private void init(CallbackInfo ci) {
//        this.atomicScore.set(this.value);
//        this.atomicLocked.set(this.locked);
//        this.atomicForceUpdate.set(this.forceUpdate);
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public int getValue() {
//        return this.atomicScore.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public void setValue(int value) {
//        this.value = value;
//        int oldScore = this.atomicScore.getAndSet(value);
//        if (oldScore != value || this.forceUpdate) {
//            this.forceUpdate = false;
//            this.atomicForceUpdate.set(false);
//            this.getScoreboard().onScoreChanged((ScoreboardScore) (Object) this);
//        }
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public boolean isLocked() {
//        return this.atomicLocked.get();
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public void setLocked(boolean locked) {
//        this.locked = locked;
//        this.atomicLocked.set(locked);
//    }
//
//    /**
//     * @author 2No2Name
//     * @reason access atomic fields
//     */
//    @Overwrite
//    public void incrementScore(int amount) {
//        if (Objects.requireNonNull(this.getObjective()).getCriteria().isReadOnly()) {
//            throw new IllegalStateException("Cannot modify read-only score");
//        }
//
//        this.forceAddScore(amount);
//    }
//
//    @Override
//    public void forceAddScore(int amount) {
//        this.value = this.atomicScore.addAndGet(amount);
//        if (amount != 0 || this.forceUpdate) {
//            this.forceUpdate = false;
//            this.atomicForceUpdate.set(false);
//            this.getScoreboard().onScoreChanged((ScoreboardScore) (Object) this);
//        }
//    }
}
