package fun.vegax.utils.features.aura.rotations.impl;

import fun.vegax.utils.features.aura.rotations.constructor.RotateConstructor;
import fun.vegax.utils.features.aura.warp.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public class SnapAngle extends RotateConstructor {
    
    // Параметры снэпов (360° мгновенные повороты)
    private float tick = 0;
    private float lastYaw = 0;
    private float lastPitch = 0;
    private long targetId = -1;
    private boolean snapped = false;
    
    // Константы из TestRotation
    private static final float MIN_PITCH = -90.0f;
    private static final float MAX_PITCH = 90.0f;
    private static final float CRIT_COOLDOWN_THRESHOLD = 0.9f;

    public SnapAngle() { 
        super("Snap"); 
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        if (mc.player == null || entity == null || !entity.isAlive()) {
            reset();
            return currentAngle;
        }

        // Инициализация при смене цели
        if (entity.getId() != targetId) {
            lastYaw = currentAngle.getYaw();
            lastPitch = currentAngle.getPitch();
            targetId = entity.getId();
            tick = 0;
            snapped = false;
        }

        LivingEntity target = (LivingEntity) entity;
        
        // Проверяем готовность атаки (для снэпов)
        boolean isAttack = mc.player.getAttackCooldownProgress(0.5f) >= CRIT_COOLDOWN_THRESHOLD;
        float attackDistance = mc.player.distanceTo(target);
        
        // ====== СНЭП ЛОГИКА ======
        // Если готовы к атаке и еще не сделали снэп - делаем 360° мгновенный поворот
        if (isAttack && !snapped && tick == 0) {
            tick = 4; // 4 тика для снэпа
            snapped = true;
        }

        // Обработка тиков снэпа
        boolean inSnapMode = tick > 0;
        if (inSnapMode) {
            tick--;
        } else {
            snapped = false;
        }

        // ====== РАСЧЕТ НАПРАВЛЕНИЯ ======
        Vec3d eyePos = mc.player.getEyePos();
        
        // Осцилляции (колебания) для естественности
        float oscillY = (float) Math.cos(System.currentTimeMillis() / 200L);
        float offsetY = 0.053F * oscillY;

        float oscillZ = (float) Math.cos(System.currentTimeMillis() / 170L);
        float offsetZ = 0.064F * oscillZ;
        
        // Временные вариации для динамики
        float timeVar = (float) Math.cos(System.currentTimeMillis() / 820L);
        float addyVact = 0.12F * timeVar;
        float timeVarZ = (float) Math.cos(System.currentTimeMillis() / 1010L);
        float addyVacZ = 0.18F * timeVarZ;
        float timeVarX = (float) Math.cos(System.currentTimeMillis() / 750L);
        float addyVacX = 0.21F * timeVarX;
        
        // Расчет точки цели с учетом смещений
        Vec3d targetPos = target.getPos()
                .add(addyVacZ, MathHelper.clamp(eyePos.y - target.getY(), 0.0F, 1F + addyVact), addyVacX);
        
        // Направление к цели
        Vec3d directionVec = targetPos.subtract(eyePos).normalize();
        
        // ====== СНЭП 360° ПОВОРОТ ======
        if (inSnapMode) {
            // Мгновенный снэп - игнорируем интерполяцию
            float snapYaw = (float) Math.toDegrees(Math.atan2(-directionVec.x, directionVec.z));
            float snapPitch = (float) MathHelper.clamp(
                    -Math.toDegrees(Math.atan2(directionVec.y, Math.hypot(directionVec.x, directionVec.z))),
                    MIN_PITCH, MAX_PITCH
            );
            
            // Добавляем случайное смещение при снэпе
            float randomAttackShift = ThreadLocalRandom.current().nextFloat() * 3f - 1f;
            snapYaw += randomAttackShift;
            snapPitch += randomAttackShift * 0.5f;
            
            lastYaw = snapYaw;
            lastPitch = snapPitch;
            
            return new Turns(snapYaw, snapPitch);
        }

        // ====== ОБЫЧНАЯ НАВОДКА (не снэп) ======
        float baseYaw = (float) Math.toDegrees(Math.atan2(-directionVec.x, directionVec.z));
        float basePitch = (float) MathHelper.clamp(
                -Math.toDegrees(Math.atan2(directionVec.y, Math.hypot(directionVec.x, directionVec.z))),
                MIN_PITCH, MAX_PITCH
        );
        
        // Волновые колебания для естественности
        float waveA = (float) Math.cos(System.currentTimeMillis() / 60D);
        float waveB = (float) Math.sin(System.currentTimeMillis() / 90D);
        
        float randomAttackShift = 0;
        if (isAttack) {
            randomAttackShift = ThreadLocalRandom.current().nextInt(-2, 4);
        }
        
        // Джиттер (случайные отклонения)
        float yawJitter = ((float) (Math.random() * 3) * waveB) + waveA * (float) (Math.random() * 3);
        float pitchJitter = ((float) (Math.random() * 1) * waveA) + waveB * smoothRandom(1, 3, 1);
        
        // Финальные углы с учетом джиттера
        float finalYaw = baseYaw + yawJitter + randomAttackShift;
        float finalPitch = basePitch + pitchJitter + randomAttackShift;
        
        // ====== СКОРОСТИ ПОВОРОТА ======
        // Разные скорости для плавности
        float pitchChangeSpeed = randomValue(5, 10);
        float yawChangeSpeed = randomValue(40, 55);
        
        // Интерполяция к целевым углам
        float yawDiff = MathHelper.wrapDegrees(finalYaw - lastYaw);
        float pitchDiff = finalPitch - lastPitch;
        
        // Применяем скорости
        float newYaw = lastYaw + MathHelper.clamp(yawDiff * (yawChangeSpeed / 100f), -yawChangeSpeed, yawChangeSpeed);
        float newPitch = lastPitch + MathHelper.clamp(pitchDiff * (pitchChangeSpeed / 100f), -pitchChangeSpeed, pitchChangeSpeed);
        
        // GCD фикс
        newYaw = applyGcd(newYaw, lastYaw);
        newPitch = applyGcd(newPitch, lastPitch);
        newPitch = MathHelper.clamp(newPitch, MIN_PITCH, MAX_PITCH);
        
        lastYaw = newYaw;
        lastPitch = newPitch;
        
        return new Turns(lastYaw, lastPitch);
    }
    
    // ====== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ======
    
    private float applyGcd(float angle, float lastAngle) {
        double sensitivity = mc.options.getMouseSensitivity().getValue();
        double factor = sensitivity * 0.6 + 0.2;
        double gcd = factor * factor * factor * 1.2;
        
        float diff = angle - lastAngle;
        return lastAngle + (float) (Math.round(diff / gcd) * gcd);
    }
    
    private float smoothRandom(int min, int max, int factor) {
        return min + (float) Math.random() * (max - min) / factor;
    }
    
    private float randomValue(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }
    
    public void reset() {
        targetId = -1;
        tick = 0;
        snapped = false;
        lastYaw = 0;
        lastPitch = 0;
    }
    
    @Override
    public Vec3d randomValue() {
        return new Vec3d(
                (Math.random() - 0.5) * 0.004,
                (Math.random() - 0.5) * 0.004,
                (Math.random() - 0.5) * 0.004
        );
    }
}
