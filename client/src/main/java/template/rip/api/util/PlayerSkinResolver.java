package template.rip.api.util;

import java.net.URL;
import java.net.URLConnection;

public class PlayerSkinResolver {

    public static URL resolvePlayerSkin(String playerUUID) {
        try {
            URL url = new URL("https://api.mineatar.io/face/" + playerUUID);
            URLConnection connect = url.openConnection();
            connect.setConnectTimeout(10000);
            connect.connect();
            return url;
        } catch (Exception e) {
            return null;
        }
    }
}
