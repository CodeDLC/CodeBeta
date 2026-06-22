package fun.vegax.display.hud;

import fun.vegax.features.impl.render.Hud;
import fun.vegax.utils.client.managers.api.draggable.AbstractDraggable;
import fun.vegax.utils.display.font.Fonts;
import fun.vegax.utils.display.font.FontRenderer;
import fun.vegax.utils.display.shape.ShapeProperties;
import fun.vegax.utils.display.color.ColorAssist;
import fun.vegax.utils.display.geometry.Render2D;
import fun.vegax.utils.math.calc.Calculate;
import fun.vegax.utils.interactions.simulate.Simulations;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WaterMark extends AbstractDraggable {

    public WaterMark() {
        super("Вотермарка", 10, 10, 200, 35, true);
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().isState()
                && Hud.getInstance().interfaceSettings.isSelected("Вотермарка");
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (mc.player == null || mc.world == null) return;

        var matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(16, Fonts.Type.DEFAULT);

        String time = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String fps = mc.getCurrentFps() + " Fps";
        String user = mc.getSession().getUsername();

        String bps = Calculate.round(
                Simulations.getSpeedSqrt(mc.player) * 20.0F, 1
        ) + " Bps";

        String coords =
                mc.player.getBlockX() + " "
                        + mc.player.getBlockY() + " "
                        + mc.player.getBlockZ();

        float x = getX();
        float y = getY();

        float h = 15;
        float gap = 6;
        float pad = 10;

        float w1 = font.getStringWidth("CodeDLC") + pad * 2;

        drawBeautifulPlate(context, x, y, w1, h);

        font.drawString(matrix, "CodeDLC", x + pad, y + 5.5f, -1);

        float userW = font.getStringWidth(user);
        float fpsW = font.getStringWidth(fps);
        float timeW = font.getStringWidth(time);

        float w2 = userW + fpsW + timeW + pad * 2 + gap * 2;
        float x2 = x + w1 + gap;

        drawBeautifulPlate(context, x2, y, w2, h);

        font.drawString(matrix, user, x2 + pad, y + 5.5f, -1);

        drawSeparator(context, x2 + pad + userW + gap * 0.5f, y + 4, 8);

        font.drawString(matrix, fps, x2 + pad + userW + gap, y + 5.5f, -1);

        drawSeparator(context, x2 + pad + userW + fpsW + gap * 1.5f, y + 4, 8);

        font.drawString(matrix, time, x2 + pad + userW + fpsW + gap * 2, y + 5.5f, -1);

        float row2Y = y + 19;

        float bpsW = font.getStringWidth(bps);

        drawBeautifulPlate(context, x, row2Y, bpsW + pad * 2, h);

        font.drawString(matrix, bps, x + pad, row2Y + 5.5f, -1);

        float crdW = font.getStringWidth(coords);
        float x4 = x + bpsW + pad * 2 + gap;

        drawBeautifulPlate(context, x4, row2Y, crdW + pad * 2, h);

        font.drawString(matrix, coords, x4 + pad, row2Y + 5.5f, -1);

        setWidth((int)(x2 + w2 - x));
        setHeight(40);
    }

    private void drawBeautifulPlate(
            DrawContext context,
            float x,
            float y,
            float w,
            float h
    ) {

        var ms = context.getMatrices();

        blur.render(
                ShapeProperties.create(ms, x, y, w, h)
                        .round(6)
                        .softness(5)
                        .color(ColorAssist.getRect(0.5f))
                        .build()
        );

        Render2D.rectangle.render(
                ShapeProperties.create(ms, x, y, w, h)
                        .round(6)
                        .color(new Color(10, 10, 10, 215).getRGB())
                        .build()
        );
    }

    private void drawSeparator(
            DrawContext context,
            float x,
            float y,
            float height
    ) {

        Render2D.rectangle.render(
                ShapeProperties.create(
                        context.getMatrices(),
                        x,
                        y,
                        0.8f,
                        height
                )
                .color(new Color(255, 255, 255, 40).getRGB())
                .build()
        );
    }
}