package fun.vegax.utils.features.aura.rotations.impl;

import fun.vegax.utils.features.aura.rotations.constructor.RotateConstructor;
import fun.vegax.utils.features.aura.warp.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class CloudAngle extends RotateConstructor {

    private final SecureRandom rng = new SecureRandom();

    private float yaw, pitch;
    private long targetId = -1;

    private float momentumYaw   = 0f;
    private float momentumPitch = 0f;

    private final float[] speedHistory = new float[6];
    private int speedIdx = 0;

    private float driftYaw   = 0f;
    private float driftPitch = 0f;
    private long  driftTimer = 0L;

    private float microYaw   = 0f;
    private float microPitch = 0f;
    private long  microTimer = 0L;

    private float offsetY    = 0.72f;
    private long  offsetTimer = 0L;

    private int    lockTicks = 0;
    private double gcdMod   = 1.0;

    public CloudAngle() {
        super("Cloud");
    }

    @Override
    public Turns limitAngleChange(Turns current, Turns target, Vec3d vec, Entity entity) {
        if (mc.player == null || entity == null || !entity.isAlive()) return current;

        long now = System.currentTimeMillis();

        // ── Смена цели ────────────────────────────────────────────────────
        if (entity.getId() != targetId) {
            yaw      = current.getYaw();
            pitch    = current.getPitch();
            targetId = entity.getId();
            momentumYaw = momentumPitch = 0f;
            lockTicks = 6 + rng.nextInt(4);
            gcdMod   = 0.988 + rng.nextDouble() * 0.024;
            refreshOffset(now);
            refreshDrift(now);
            refreshMicro(now);
            for (int i = 0; i < speedHistory.length; i++) speedHistory[i] = 0f;
        } else {
            if (lockTicks > 0) lockTicks--;
        }

        // ── Предикт позиции цели ──────────────────────────────────────────
        double velX = entity.getX() - entity.prevX;
        double velY = entity.getY() - entity.prevY;
        double velZ = entity.getZ() - entity.prevZ;
        double speed = Math.sqrt(velX * velX + velZ * velZ);

        double predScale = lockTicks > 0
                ? 1.4 + rng.nextDouble() * 0.3
                : MathHelper.clamp(1.1 + speed * 2.2, 0.9, 2.3);

        Vec3d eye = mc.player.getEyePos();
        double aimX = entity.getX() + velX * predScale;
        double aimY = entity.getY() + entity.getEyeHeight(entity.getPose()) * offsetY + velY * predScale * 0.4;
        double aimZ = entity.getZ() + velZ * predScale;

        double dx = aimX - eye.x;
        double dy = aimY - eye.y;
        double dz = aimZ - eye.z;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        float idealYaw   = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float idealPitch = (float) -Math.toDegrees(Math.atan2(dy, horizDist));

        // ── Дельта ────────────────────────────────────────────────────────
        float dYaw   = MathHelper.wrapDegrees(idealYaw   - yaw);
        float dPitch = idealPitch - pitch;
        float total  = (float) Math.hypot(dYaw, dPitch);

        // ── Сила аттрактора (пружина — сильнее когда далеко) ─────────────
        float attraction;
        if      (lockTicks > 0) attraction = 0.55f + rng.nextFloat() * 0.15f;
        else if (total > 35f)   attraction = 0.42f + rng.nextFloat() * 0.12f;
        else if (total > 12f)   attraction = 0.28f + rng.nextFloat() * 0.10f;
        else if (total > 4f)    attraction = 0.16f + rng.nextFloat() * 0.08f;
        else                    attraction = 0.07f + rng.nextFloat() * 0.05f;

        float forceYaw   = dYaw   * attraction;
        float forcePitch = dPitch * attraction;

        // ── Демпфирование импульса (инерция руки) ─────────────────────────
        float friction = computeAdaptiveFriction();
        momentumYaw   = momentumYaw   * friction + forceYaw   * (1f - friction);
        momentumPitch = momentumPitch * friction + forcePitch * (1f - friction);

        // ── Лимит скорости ────────────────────────────────────────────────
        float maxYaw   = lockTicks > 0 ? 65f : (28f + rng.nextFloat() * 10f);
        float maxPitch = lockTicks > 0 ? 42f : (18f + rng.nextFloat() * 7f);
        momentumYaw   = MathHelper.clamp(momentumYaw,   -maxYaw,   maxYaw);
        momentumPitch = MathHelper.clamp(momentumPitch, -maxPitch, maxPitch);

        speedHistory[speedIdx] = Math.abs(momentumYaw) + Math.abs(momentumPitch);
        speedIdx = (speedIdx + 1) % speedHistory.length;

        // ── Применяем движение ────────────────────────────────────────────
        float prevYaw   = yaw;
        float prevPitch = pitch;

        yaw   += momentumYaw;
        pitch += momentumPitch;

        // ── Drift — медленный уход прицела ────────────────────────────────
        if (now - driftTimer > 180 + (long)(rng.nextDouble() * 120)) refreshDrift(now);
        yaw   += driftYaw   * 0.012f;
        pitch += driftPitch * 0.009f;

        // ── Breath — дыхание (две синусоиды разной частоты) ──────────────
        double bp = now * 0.00065;
        yaw   += (float)(Math.sin(bp + 0.7)  * 0.022 + Math.sin(bp * 1.7 + 1.3) * 0.011);
        pitch += (float)(Math.cos(bp + 0.4)  * 0.016 + Math.cos(bp * 1.4 + 2.1) * 0.008);

        // ── Micro — тремор пальцев ────────────────────────────────────────
        if (now - microTimer > 14 + (long)(rng.nextDouble() * 18)) refreshMicro(now);
        yaw   += microYaw;
        pitch += microPitch;

        // ── Overshoot — редкий перелёт мыши рядом с целью ────────────────
        if (total < 8f && rng.nextFloat() < 0.04f) {
            yaw += rng.nextFloat() * 0.4f * Math.signum(dYaw);
        }

        // ── Обновление offset внутри хитбокса ─────────────────────────────
        if (now - offsetTimer > 280 + (long)(rng.nextDouble() * 200)) refreshOffset(now);

        // ── Clamp pitch ───────────────────────────────────────────────────
        pitch = MathHelper.clamp(pitch, -90f, 90f);

        // ── GCD-фикс ─────────────────────────────────────────────────────
        double sens = mc.options.getMouseSensitivity().getValue();
        double f    = sens * 0.6 + 0.2;
        double gcd  = f * f * f * 1.2 * gcdMod;

        float diffY = yaw   - prevYaw;
        float diffP = pitch - prevPitch;

        yaw   = prevYaw   + (float)(Math.round(diffY / gcd) * gcd);
        pitch = prevPitch + (float)(Math.round(diffP / gcd) * gcd);

        if (Math.abs(yaw - prevYaw) < gcd * 0.3 && Math.abs(pitch - prevPitch) < gcd * 0.3) {
            yaw   = prevYaw;
            pitch = prevPitch;
        }

        return new Turns(yaw, pitch);
    }

    private float computeAdaptiveFriction() {
        float avg = 0f;
        for (float s : speedHistory) avg += s;
        avg /= speedHistory.length;
        float base  = 0.52f + rng.nextFloat() * 0.08f;
        float extra = MathHelper.clamp(avg / 30f, 0f, 0.22f);
        return base + extra;
    }

    private void refreshDrift(long now) {
        driftYaw   = (float)(rng.nextGaussian() * 0.8);
        driftPitch = (float)(rng.nextGaussian() * 0.5);
        driftTimer = now;
    }

    private void refreshMicro(long now) {
        microYaw   = (float)(rng.nextGaussian() * 0.06);
        microPitch = (float)(rng.nextGaussian() * 0.04);
        microTimer = now;
    }

    private void refreshOffset(long now) {
        offsetY     = 0.55f + rng.nextFloat() * 0.30f;
        offsetTimer = now;
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(
                rng.nextGaussian() * 0.025,
                rng.nextGaussian() * 0.018,
                rng.nextGaussian() * 0.025
        );
    }
}