package fun.vegax.utils.features.aura.rotations.impl;

import fun.vegax.VegaXDLC;
import fun.vegax.features.impl.combat.Aura;
import fun.vegax.utils.features.aura.rotations.constructor.RotateConstructor;
import fun.vegax.utils.features.aura.striking.StrikeManager;
import fun.vegax.utils.features.aura.utils.MathAngle;
import fun.vegax.utils.features.aura.warp.Turns;
import fun.vegax.utils.math.time.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class MatrixAngle extends RotateConstructor {
    public MatrixAngle() {
        super("CakeWorld TEST");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        StrikeManager attackHandler = VegaXDLC.getInstance().getAttackPerpetrator().getAttackHandler();
        Aura aura = Aura.getInstance();
        StopWatch attackTimer = attackHandler.getAttackTimer();

        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw(), pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean canAttack = entity != null && attackHandler.canAttack(aura.getConfig(), 0);

        float distanceToTarget = 0;
        if (entity != null) {
            distanceToTarget = (float) mc.player.distanceTo(entity);
        }

        float baseSpeed = canAttack ? 0.87F : 0.56F;

        float speed = baseSpeed;
        if (distanceToTarget > 0 && distanceToTarget < 0.66F) {
            float closeRangeSpeed = MathHelper.clamp(distanceToTarget / 1.5F * 0.35F, 0.1F, 0.6F);
            speed = canAttack ? 0.85f : Math.min(speed, closeRangeSpeed);
        }
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
        float jitterYaw = canAttack ? 0 : (float) (randomLerp(18, 27) * Math.sin(System.currentTimeMillis() / 50D));
        float jitterPitch = canAttack ? 0 : (float) (randomLerp(15, 22) * Math.sin(System.currentTimeMillis() / 13D));

        if ((!aura.isState() || aura.getTarget() == null) && attackHandler.getAttackTimer().finished(1000)) {
            baseSpeed = 0.35F;
            jitterYaw = 0;
            jitterPitch = 0;
        }
        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(baseSpeed, currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(MathHelper.lerp(baseSpeed, currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);

        return moveAngle;
    }

    public static float lerp(float delta, float start, float end) {
        return end;
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.01, 0.07, 0.02);
    }
}