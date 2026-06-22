package fun.vegax.mixins.player.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import fun.vegax.utils.client.managers.event.EventManager;
import fun.vegax.utils.display.interfaces.QuickImports;
import fun.vegax.events.player.FireworkEvent;
import fun.vegax.utils.features.aura.warp.TurnsConnection;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin implements QuickImports {

    @Shadow @Nullable private LivingEntity shooter;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d getRotationVectorHook(LivingEntity instance, Operation<Vec3d> original) {
        if (shooter == mc.player) return TurnsConnection.INSTANCE.getMoveRotation().toVector();
        return original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
    public Vec3d getVelocityHook(LivingEntity instance, Operation<Vec3d> original) {
        if (shooter == mc.player) {
            FireworkEvent event = new FireworkEvent(original.call(instance));
            EventManager.callEvent(event);
            return event.getVector();
        }
        return original.call(instance);
    }
}
