package fun.vegax.features.impl.render;

import fun.vegax.events.render.WorldRenderEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.features.module.setting.implement.ColorSetting;
import fun.vegax.features.module.setting.implement.SliderSettings;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.display.color.ColorAssist;
import fun.vegax.utils.display.geometry.Render3D;
import fun.vegax.utils.math.calc.CalcVector;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class HitRange extends Module {

    static final int   SIDES      = 8;
    static final float HIT_RADIUS = 3.32f;
    static final float Y_OFFSET   = 0.02f;

    final ColorSetting color = new ColorSetting("Цвет", "Цвет")
            .setColor(new Color(80, 180, 255, 200).getRGB());

    final SliderSettings thickness = new SliderSettings("Толщина", "Толщина")
            .setValue(0.06f).range(0.01f, 0.5f);

    final SliderSettings rotSpeed = new SliderSettings("Скорость вращения", "Скорость вращения")
            .setValue(0.4f).range(0.05f, 2.0f);

    final BooleanSetting depthTest = new BooleanSetting("Скрывать за блоками", "Скрывать за блоками")
            .setValue(true);

    float rotationAngle = 0f;

    public HitRange() {
        super("HitRange", "Hit Range", ModuleCategory.RENDER);
        setup(color, thickness, rotSpeed, depthTest);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (mc.player == null || mc.world == null) return;

        boolean enemyNear = false;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (p.distanceTo(mc.player) <= HIT_RADIUS) {
                enemyNear = true;
                break;
            }
        }

        rotationAngle = (rotationAngle + rotSpeed.getValue()) % 360f;

        Vec3d origin = CalcVector.lerpPosition(mc.player);
        double y = origin.y + Y_OFFSET;

        int ringColor = enemyNear
                ? new Color(255, 45, 45, 220).getRGB()
                : color.getColor();
        int glowColor = ColorAssist.multAlpha(ringColor, 0.25f);

        double rotRad = Math.toRadians(rotationAngle);
        float half = thickness.getValue() / 2f;
        boolean depth = depthTest.isValue();

        for (int i = 0; i < SIDES; i++) {
            double a1 = rotRad + 2 * Math.PI * i       / SIDES;
            double a2 = rotRad + 2 * Math.PI * (i + 1) / SIDES;

            double outerR = HIT_RADIUS + half;
            double innerR = HIT_RADIUS - half;

            Vec3d o1 = new Vec3d(origin.x + outerR * Math.cos(a1), y, origin.z + outerR * Math.sin(a1));
            Vec3d o2 = new Vec3d(origin.x + outerR * Math.cos(a2), y, origin.z + outerR * Math.sin(a2));
            Vec3d i1 = new Vec3d(origin.x + innerR * Math.cos(a1), y, origin.z + innerR * Math.sin(a1));
            Vec3d i2 = new Vec3d(origin.x + innerR * Math.cos(a2), y, origin.z + innerR * Math.sin(a2));

            Render3D.drawQuad(o1, o2, i2, i1, ringColor, depth);

            double gOuter = HIT_RADIUS + half * 3;
            double gInner = HIT_RADIUS - half * 3;
            Vec3d go1 = new Vec3d(origin.x + gOuter * Math.cos(a1), y, origin.z + gOuter * Math.sin(a1));
            Vec3d go2 = new Vec3d(origin.x + gOuter * Math.cos(a2), y, origin.z + gOuter * Math.sin(a2));
            Vec3d gi1 = new Vec3d(origin.x + gInner * Math.cos(a1), y, origin.z + gInner * Math.sin(a1));
            Vec3d gi2 = new Vec3d(origin.x + gInner * Math.cos(a2), y, origin.z + gInner * Math.sin(a2));
            Render3D.drawQuad(go1, go2, gi2, gi1, glowColor, depth);
        }
    }
}