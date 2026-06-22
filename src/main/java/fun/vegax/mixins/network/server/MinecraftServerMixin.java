package fun.vegax.mixins.network.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.vegax.VegaXDLC;
import fun.vegax.utils.client.managers.file.exception.FileProcessingException;
import fun.vegax.utils.client.logs.Logger;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo ci) {
        if (VegaXDLC.getInstance().isInitialized()) {
            try {
                VegaXDLC.getInstance().getFileController().saveFiles();
            } catch (FileProcessingException e) {
                Logger.error("Error occurred while saving files: " + e.getMessage());
            }
        }
    }
}
