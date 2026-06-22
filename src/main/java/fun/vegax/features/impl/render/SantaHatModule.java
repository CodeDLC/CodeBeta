package fun.vegax.features.impl.render;

import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;

public class SantaHatModule extends Module {
   private static SantaHatModule instance;

   public SantaHatModule() {
      super("SantaHat", "Santa Hat", ModuleCategory.RENDER);
      instance = this;
   }

   public static SantaHatModule getInstance() {
      return instance;
   }

   public void activate() {
      super.activate();
   }

   public void deactivate() {
      super.deactivate();
   }
}
