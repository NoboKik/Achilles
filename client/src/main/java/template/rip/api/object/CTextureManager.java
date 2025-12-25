package template.rip.api.object;

import com.google.common.collect.Maps;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Map;

public class CTextureManager {

    private final TextureManager textureManager;
    private final Map<String, Integer> dynamicIdCounters = Maps.newHashMap();

    public CTextureManager(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public Identifier registerDynamicTexture(String prefix, NativeImageBackedTexture texture) {
        Integer integer = this.dynamicIdCounters.get(prefix);
        if (integer == null) {
            integer = 1;
        } else {
            integer = integer + 1;
        }
        this.dynamicIdCounters.put(prefix, integer);
        Identifier identifier = Identifier.ofVanilla(String.format(Locale.ROOT, "dynamic/%s_%d", prefix, integer));
        textureManager.registerTexture(identifier, texture);
        return identifier;
    }
}
