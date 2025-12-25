package template.rip.mixin;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.Template;
import template.rip.api.util.PlayerUtils;
import template.rip.gui.screens.LoginScreen;
import template.rip.module.modules.render.NickHiderModule;

import java.net.URL;
import java.util.UUID;

import static template.rip.Template.mc;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    @Unique
    SkinTextures targetTexture, ownTexture;
    @Unique
    boolean loadingTarget, loadingOwn;
    @Unique
    int counter;

    @Inject(method = "getSkinTextures", at = @At(value = "TAIL"), cancellable = true)
    public void a(CallbackInfoReturnable<SkinTextures> cir) {
        NickHiderModule nick = Template.moduleManager.getModule(NickHiderModule.class);
        if (nick == null || !nick.isEnabled()) {
            loadingOwn = false;
            ownTexture = null;
            loadingTarget = false;
            targetTexture = null;
            return;
        }

        if (!nick.changeTargetSkin.isEnabled()) {
            loadingTarget = false;
            targetTexture = null;
        } else {
            UUID str = ((PlayerListEntry) (Object) this).getProfile().getId();
            Entity e = PlayerUtils.findFirstTarget(true);
            if (e instanceof PlayerEntity && ((PlayerEntity) e).getGameProfile().getId().equals(str)) {
                if (targetTexture != null) {
                    cir.setReturnValue(targetTexture);
                } else if (!loadingTarget) {
                    loadingTarget = true;
                    new Thread(() -> {
                        try {
                            URL url = new URL(nick.targetSkinURL.getContent());
                            Identifier targetId = Template.cTextureManager.registerDynamicTexture("targetskin-" + LoginScreen.getInstance().login.get() + '-' + str + '-' + counter++, new NativeImageBackedTexture(NativeImage.read(url.openStream())));
                            targetTexture = new SkinTextures(targetId, null, null, null, cir.getReturnValue().model(), true);
                        } catch (Exception ignored) {
                        }
                    }).start();
                }
            }
        }

        if (!nick.changeOwnSkin.isEnabled()) {
            loadingOwn = false;
            ownTexture = null;
        } else {
            UUID uid = ((PlayerListEntry) (Object) this).getProfile().getId();
            if (mc.player != null && mc.player.getGameProfile().getId().equals(uid)) {
                if (ownTexture != null) {
                    cir.setReturnValue(ownTexture);
                } else if (!loadingOwn) {
                    loadingOwn = true;
                    new Thread(() -> {
                        try {
                            URL url = new URL(nick.ownSkinURL.getContent());
                            Identifier ownId = Template.cTextureManager.registerDynamicTexture("ownskin-" + LoginScreen.getInstance().login.get() + '-' + uid + '-' + counter++, new NativeImageBackedTexture(NativeImage.read(url.openStream())));
                            ownTexture = new SkinTextures(ownId, null, null, null, cir.getReturnValue().model(), true);
                        } catch (Exception ignored) {
                        }
                    }).start();
                }
            }
        }
    }
}
