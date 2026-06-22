package fun.vegax.utils.display.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import fun.vegax.VegaXDLC;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Fonts {

    private static final Map<FontKey, FontRenderer> fontCache = new HashMap<>();

    @SneakyThrows
    public static FontRenderer create(float size, String name) {
        String path = "assets/minecraft/fonts/" + name + ".ttf";

        InputStream inputStream = VegaXDLC.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            System.err.println("[Fonts] Font not found: " + path);
            return null;
        }

        try (InputStream is = inputStream) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size / 2f);
            return new FontRenderer(font, size / 2f);
        }
    }

    public static void init() {
        for (Type type : Type.values()) {
            for (int size = 4; size <= 32; size++) {
                FontRenderer renderer = create(size, type.getType());
                if (renderer != null) {
                    fontCache.put(new FontKey(size, type), renderer);
                }
            }
        }
    }

    public static FontRenderer getSize(int size) {
        return getSize(size, Type.INST);
    }

    public static FontRenderer getSize(int size, Type type) {
        return fontCache.computeIfAbsent(
                new FontKey(size, type),
                k -> create(size, type.getType())
        );
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        DEFAULT("sf_medium"),
        REGULAR("sf_regular"),
        SEMI("sf_semibold"),
        BOLD("sf_bold"),
        BOLDED("bold"),
        MANROPEEXTRABOLD("manropeextrabold"),
        MANROPEBOLD("manropebold"),
        VegaXDLCREGULAR("vortex_regular"),
        ICONVegaXDLCREG("iconvortexreg"),
        LOGO("space_age"),
        INST("suisseintl"),
        ICONS("icons"),
        ICONSTYPENEW("icon2"),
        GUIICONS("categoryicons"),
        ICONSCATEGORY("categoryicons"),
        VEGAXDLC_TITLE("VortexClient");

        private final String type;
    }

    private record FontKey(int size, Type type) {}
}
