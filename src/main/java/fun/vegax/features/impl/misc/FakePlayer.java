package fun.vegax.features.impl.misc;

import com.mojang.authlib.GameProfile;
import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.features.module.setting.implement.SliderSettings;
import fun.vegax.features.module.setting.implement.TextSetting;
import fun.vegax.utils.client.managers.event.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.UUID;

public class FakePlayer extends Module {

    private final TextSetting fakeName = new TextSetting("Ник", "Имя фейк-игрока")
            .setText("CodeBOT");

    private final BooleanSetting copyName = new BooleanSetting("Скопировать свой ник", "Использовать ник реального игрока")
            .setValue(true);

    private final BooleanSetting copySkin = new BooleanSetting("Скопировать скин", "Использовать скин реального игрока")
            .setValue(true);

    private final BooleanSetting infiniteTotem = new BooleanSetting("Бесконечные тотемы", "Тотем спасает бесконечно")
            .setValue(false);

    private final SliderSettings totemCount = new SliderSettings("Кол-во тотемов", "Сколько раз спасает (0 = бесконечно)")
            .range(1f, 20f)
            .setValue(3f)
            .visible(() -> !infiniteTotem.isValue());

    private final SliderSettings damagePerHit = new SliderSettings("Урон за удар", "HP снимаемых при каждой атаке")
            .range(0.5f, 20f)
            .setValue(5f);

    private final SliderSettings attackRange = new SliderSettings("Радиус атаки", "На каком расстоянии считается удар")
            .range(1f, 6f)
            .setValue(3.5f);

    // ── Состояние ─────────────────────────────────────────────────────────
    private OtherClientPlayerEntity fakePlayer = null;
    private float fakeHealth = 20f;
    private int   totemSaves = 0;
    private int   hurtCooldown = 0;

    // Отслеживаем swing-состояние каждой сущности чтобы засчитать ровно 1 удар
    private final java.util.Set<Integer> swingingLastTick = new java.util.HashSet<>();

    public FakePlayer() {
        super("FakePlayer", "FakePlayer", ModuleCategory.MISC);
    }

    @Override
    public void activate() {
        if (mc.player == null || mc.world == null) {
            setState(false);
            return;
        }
        fakeHealth = 20f;
        totemSaves = 0;
        hurtCooldown = 0;
        swingingLastTick.clear();
        spawnFakePlayer();
    }

    @Override
    public void deactivate() {
        removeFakePlayer();
        swingingLastTick.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || fakePlayer == null) return;

        // Если фейк пропал из мира — пересоздаём
        if (mc.world.getEntityById(fakePlayer.getId()) == null) {
            spawnFakePlayer();
            return;
        }

        // Тотем всегда в левой руке
        fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (hurtCooldown > 0) hurtCooldown--;

        // ── Детектируем удар по анимации замаха ──────────────────────────
        // Засчитываем урон ровно в момент НАЧАЛА swing (leading edge)
        double range = attackRange.getValue();
        Box searchBox = fakePlayer.getBoundingBox().expand(range);

        List<LivingEntity> nearbyEntities = mc.world.getEntitiesByClass(
                LivingEntity.class,
                searchBox,
                e -> e != fakePlayer
                        && e.isAlive()
                        && e.squaredDistanceTo(fakePlayer) <= range * range
        );

        java.util.Set<Integer> swingingNow = new java.util.HashSet<>();

        for (LivingEntity entity : nearbyEntities) {
            if (entity.handSwinging) {
                swingingNow.add(entity.getId());
                // Только если в прошлом тике этот entity НЕ замахивался — новый удар
                if (!swingingLastTick.contains(entity.getId()) && hurtCooldown <= 0) {
                    dealDamage(damagePerHit.getValue());
                    hurtCooldown = 10; // иммунитет 10 тиков между ударами
                }
            }
        }

        swingingLastTick.clear();
        swingingLastTick.addAll(swingingNow);
    }

    // ── Нанести урон ─────────────────────────────────────────────────────
    private void dealDamage(float amount) {
        if (fakePlayer == null) return;

        fakeHealth -= amount;

        // Анимация получения урона (красное мигание)
        fakePlayer.hurtTime    = 10;
        fakePlayer.maxHurtTime = 10;

        if (fakeHealth <= 0f) {
            triggerTotem();
        }
    }

    // ── Срабатывание тотема ───────────────────────────────────────────────
    private void triggerTotem() {
        if (fakePlayer == null) return;

        int maxSaves = infiniteTotem.isValue() ? Integer.MAX_VALUE : (int) totemCount.getValue();

        if (totemSaves >= maxSaves) {
            removeFakePlayer();
            setState(false);
            return;
        }

        totemSaves++;
        fakeHealth = 20f;

        // Анимация тотема (видна только нам)
        mc.gameRenderer.showFloatingItem(new ItemStack(Items.TOTEM_OF_UNDYING));

        // Эффекты после тотема — как у настоящего
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,    900, 1));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,     2400, 1));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
    }

    // ── Спавн фейк-игрока ─────────────────────────────────────────────────
    private void spawnFakePlayer() {
        if (mc.player == null || mc.world == null) return;

        GameProfile profile;
        if (copySkin.isValue()) {
            GameProfile real = mc.player.getGameProfile();
            String name = copyName.isValue() ? real.getName() : fakeName.getText();
            profile = new GameProfile(UUID.randomUUID(), name);
            real.getProperties().forEach((key, prop) -> profile.getProperties().put(key, prop));
        } else {
            String name = copyName.isValue() ? mc.player.getGameProfile().getName() : fakeName.getText();
            profile = new GameProfile(UUID.randomUUID(), name);
        }

        fakePlayer = new OtherClientPlayerEntity(mc.world, profile);

        // Ставим точно где стоит реальный игрок
        fakePlayer.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        fakePlayer.setHeadYaw(mc.player.getHeadYaw());
        fakePlayer.setBodyYaw(mc.player.getBodyYaw());
        fakePlayer.setPitch(mc.player.getPitch());
        fakePlayer.setYaw(mc.player.getYaw());

        // Копируем броню и предметы в руках
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                fakePlayer.getInventory().setStack(i, stack.copy());
            }
        }

        // Тотем в левую руку
        fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        mc.world.addEntity(fakePlayer);
    }

    // ── Удалить из мира ───────────────────────────────────────────────────
    private void removeFakePlayer() {
        if (fakePlayer != null && mc.world != null) {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
            fakePlayer = null;
        }
    }
}