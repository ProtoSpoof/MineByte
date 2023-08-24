package dev.protospoof.minebyte.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.protospoof.minebyte.MineByteMod;

@Mixin(MinecraftServer.class)
public class MineByteMixin {
    @Inject(at = @At("HEAD"), method = "loadWorld")
    private void init(CallbackInfo info) {
        // This code is injected into the start of MinecraftServer.loadWorld()V
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", ordinal = 0))
    private void craftmessenger_registerStarting(CallbackInfo ci) {
        MineByteMod.serverStarting((MinecraftServer) (Object) this);
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    private void craftmessenger_registerStopping(CallbackInfo ci) {
        MineByteMod.serverStopped((MinecraftServer) (Object) this);
    }
}