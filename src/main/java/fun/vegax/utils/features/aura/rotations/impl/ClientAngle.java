package fun.vegax.utils.features.aura.rotations.impl;

import fun.vegax.utils.features.aura.rotations.constructor.RotateConstructor;
import fun.vegax.utils.features.aura.warp.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public class ClientAngle extends RotateConstructor {
    private float lastYaw, lastPitch;
    private long lastTargetId = -1;
    private float tick = 0;
    private boolean isAttack = false;
    private long lastUpdateTime = 0;
    private float reactionDelay = 0;
    private float humanImperfection = 0;
    private long patternStartTime = 0;

    private static final float MIN_PITCH = -89.0F;
    private static final float MAX_PITCH = 89.0F;

    public ClientAngle() {
        super("PolarAC");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        if (mc.player == null || entity == null || !entity.isAlive()) return currentAngle;

        if (entity.getId() != lastTargetId) {
            lastYaw = currentAngle.getYaw();
            lastPitch = currentAngle.getPitch();
            lastTargetId = entity.getId();
            patternStartTime = System.currentTimeMillis();
        }

        Vec3d eyePos = mc.player.getEyePos();

        // Target torso (middle of hitbox) - not head or legs
        double torsoY = entity.getY() + entity.getHeight() * 0.5;
        double dx = entity.getX() - mc.player.getX();
        double dy = torsoY - mc.player.getEyeY();
        double dz = entity.getZ() - mc.player.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);

        // Calculate exact angles to torso
        float baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float basePitch = (float) -Math.toDegrees(Math.atan2(dy, distance));

        // Infinity/figure-8 pattern (Lissajous curve)
        long time = System.currentTimeMillis() - patternStartTime;
        double t = time / 500.0; // Speed of pattern

        // Figure-8 pattern using sine and cosine
        double patternYaw = Math.sin(t) * 3.0; // Horizontal movement
        double patternPitch = Math.sin(t * 2) * 2.0; // Vertical movement (double frequency for figure-8)

        // Smooth the pattern transitions
        float targetYaw = baseYaw + (float) patternYaw;
        float targetPitch = basePitch + (float) patternPitch;

        // Very subtle jitter for realism
        float microJitter = (float) (Math.random() - 0.5) * 0.05f;
        targetYaw += microJitter;
        targetPitch += microJitter * 0.5f;

        // Fast rotation speed for consistent hits
        float yawSpeed = 180;
        float pitchSpeed = 120;

        // Direct rotation to target (no reaction delay)
        float yawDiff = MathHelper.wrapDegrees(targetYaw - lastYaw);
        float pitchDiff = targetPitch - lastPitch;

        // Apply fast smoothing
        float smoothing = 0.8f;
        lastYaw += yawDiff * smoothing;
        lastPitch += pitchDiff * smoothing;

        // Clamp pitch
        lastPitch = MathHelper.clamp(lastPitch, MIN_PITCH, MAX_PITCH);

        return new Turns(lastYaw, lastPitch);
    }

    private float smoothRandom(float min, float max, float smooth) {
        float random = (float) Math.random();
        return min + (max - min) * (float) Math.pow(random, smooth);
    }

    private float randomValue(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    @Override
    public Vec3d randomValue() {
        return Vec3d.ZERO;
    }

    public void reset() {
        lastTargetId = -1;
        tick = 0;
        isAttack = false;
        humanImperfection = 0;
        reactionDelay = 0;
    }

    public void setAttack(boolean attack) {
        this.isAttack = attack;
    }
}
