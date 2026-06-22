package fun.vegax.features.impl.render;

import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.VegaXDLC;
import fun.vegax.display.screens.clickgui.MenuScreen;
import org.lwjgl.glfw.GLFW;

public class Menu extends Module {
    public static final Menu INSTANCE = new Menu();

    public Menu() {
        super("Menu", ModuleCategory.RENDER);
        this.setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void activate() {
        if (mc.world == null) return;
        
        // Use old GUI
        if (mc.currentScreen == MenuScreen.INSTANCE) return;
        MenuScreen.INSTANCE.openGui();
    }

    @Override
    public void deactivate() {
        if (mc.currentScreen instanceof MenuScreen) {
            mc.currentScreen.close();
        }
    }
}
