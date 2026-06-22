package fun.vegax.common.animation;

public interface AnimationCalculation {
    default double calculation(double value) {
        return 0;
    }
}
