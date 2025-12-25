package template.rip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.world.GameMode;
import template.rip.api.config.ConfigManager;
import template.rip.api.event.orbit.EventBus;
import template.rip.api.event.orbit.IEventBus;
import template.rip.api.notification.Notification;
import template.rip.api.notification.NotificationManager;
import template.rip.api.object.CTextureManager;
import template.rip.api.rotation.RotationManager;
import template.rip.api.util.*;
import template.rip.gui.ImguiLoader;
import template.rip.gui.screens.LoginScreen;
import template.rip.module.ModuleManager;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ClickSimulationModule;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Template {

    public static String name = "Template";
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static IEventBus EVENTBUS = new EventBus();
    public static Template INSTANCE;
    public static final ArrayList<Packet<?>> noEvent = new ArrayList<>();
    private static float lastWindowW = mc.getWindow().getFramebufferWidth();
    private static float lastWindowH = mc.getWindow().getFramebufferHeight();
    public static CTextureManager cTextureManager;
    public static ModuleManager moduleManager;
    private static ConfigManager configManager;
    private static CrystalUtils crystalUtils;
    private static RotationManager rotationManager;
    private static MouseSimulation mouseSimulation;
    private static NotificationManager notificationManager;
    private static CPSHelper cpsHelper;
    private static BlinkUtil blinkUtil;
    private static BacktrackUtil backtrackUtil;
    public static float[] rots = new float[4];
    public static Float realYaw = null;
    public static float yaw, pitch;
    public static long frame;
//    private ConfigServer configServer;

    public Template() {
        INSTANCE = this;
    }

    /**
     * <img src=https://i.postimg.cc/FKwh79YB/image.png />
     */
    public void init() {
        moduleManager = new ModuleManager();
        notificationManager = new NotificationManager();
        rotationManager = new RotationManager();
        EVENTBUS.registerLambdaFactory(Template.class.getPackage().getName(), (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENTBUS.subscribe(LoginScreen.getInstance());
        EVENTBUS.subscribe(notificationManager);
        EVENTBUS.subscribe(rotationManager);

        ImguiLoader.addRenderable(Template.notificationManager());
    }

    // randomly crashes the game
    public static void funny() {
        try {
            Date expireDate = new SimpleDateFormat("dd/MM/yyyy").parse("1/4/2025");
            Date now = new Date();
            if (!now.after(expireDate))
                return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (Template.configManager().loadingConfig)
            return;

        if (MathUtils.getRandomBoolean())
            return;

        switch (MathUtils.getRandomInt(0, 18)) {
            case 1 -> BlockUtils.loop();
            case 2 -> ColorUtil.memory();
            case 3 -> DamageUtils.formatExc();
            case 4 -> InvUtils.outbounds();
            case 5 -> MathUtils.div0();
            case 6 -> MouseSimulation.death();
            case 7 -> PlayerUtils.npe();
            case 8 -> RenderUtils.n();
            case 9 -> SoundUtils.overflow();
            default -> {
                ExitUtil.ExitType type = Arrays.asList(ExitUtil.ExitType.values()).get(ThreadLocalRandom.current().nextInt(0, 5));
                ExitUtil.exit(type);
            }
        }
    }

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(false);
    }

    public static void sendNoEvent(Packet<?> packet) {
        if (mc.getNetworkHandler() == null)
            return;

        noEvent.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static void attackEntityNoResetCooldown(PlayerEntity player, Entity target) {
        mc.interactionManager.syncSelectedSlot();
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
        if (mc.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            player.attack(target);
        }
    }

    public static boolean isAdvanced() {
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        return asm != null && asm.advancedMode.isEnabled();
    }

    public static boolean isBlatant() {
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        boolean blatant = asm != null && asm.blatantMode.isEnabled();
        if (!blatant) {
            notificationManager().addNotification(new Notification("Blatant Mode Is Disabled", 5000, "Module was not enabled for safety"));
        }
        return blatant;
    }

    public static boolean isClickSim() {
        ClickSimulationModule asm = Template.moduleManager.getModule(ClickSimulationModule.class);
        return asm != null && asm.clickEnabled.isEnabled();
    }

    public static boolean shouldMove() {
        if (moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            lastWindowW = mc.getWindow().getFramebufferWidth();
            lastWindowH = mc.getWindow().getFramebufferHeight();
            return true;
        } else {
            return !(lastWindowW != mc.getWindow().getFramebufferWidth() || lastWindowH != mc.getWindow().getFramebufferHeight());
        }
    }

    public static boolean displayRender() {
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (asm == null) return false;
        if (!asm.noVisuals.isEnabled()) return true;
        if (asm.isEnabled()) return true;
        return mc.currentScreen == null;
    }

    public static void setModuleManager(ModuleManager moduleManager) {
        Template.moduleManager = moduleManager;
    }

    public static void setConfigManager(ConfigManager configManager) {
        Template.configManager = configManager;
    }

    public static ConfigManager configManager() {
        return configManager;
    }

    public void setRotationManager(RotationManager rotationManager) {
        Template.rotationManager = rotationManager;
    }

    public static RotationManager rotationManager() {
        return rotationManager;
    }

    public static void setMouseSimulation(MouseSimulation mouseSimulation) {
        Template.mouseSimulation = mouseSimulation;
    }

    public static MouseSimulation mouseSimulation() {
        return mouseSimulation;
    }

    public static void setNotificationManager(NotificationManager notificationManager) {
        Template.notificationManager = notificationManager;
    }

    public static NotificationManager notificationManager() {
        return notificationManager;
    }

    public static void setCrystalUtils(CrystalUtils crystalUtils) {
        Template.crystalUtils = crystalUtils;
    }

    public static CrystalUtils crystalUtils() {
        return crystalUtils;
    }

    public static void setCPSHelper(CPSHelper cpsHelper) {
        Template.cpsHelper = cpsHelper;
    }

    public static CPSHelper cpsHelper() {
        return cpsHelper;
    }

    public static void setBlinkUtil(BlinkUtil blinkUtil) {
        Template.blinkUtil = blinkUtil;
    }

    public static BlinkUtil blinkUtil() {
        return blinkUtil;
    }

    public static void setBacktrackUtil(BacktrackUtil backtrackUtil) {
        Template.backtrackUtil = backtrackUtil;
    }

    public static BacktrackUtil backtrackUtil() {
        return backtrackUtil;
    }

    //https://github.com/mervick/aes-everywhere :pray:

    private static final byte[] SALTED = "Salted__".getBytes(US_ASCII);

    public static String encrypt(String input, String passphrase) throws Exception {
        return Base64.getEncoder().encodeToString(_encrypt(input.getBytes(UTF_8), passphrase.getBytes(UTF_8)));
    }

    public static byte[] encrypt(byte[] input, byte[] passphrase) throws Exception {
        return Base64.getEncoder().encode(_encrypt(input, passphrase));
    }

    public static String decrypt(String crypted, String passphrase) throws Exception {
        return new String(_decrypt(decode(crypted, NO_WRAP), passphrase.getBytes(UTF_8)), UTF_8);
    }

    private static byte[] _encrypt(byte[] input, byte[] passphrase) throws Exception {
        byte[] salt = (new SecureRandom()).generateSeed(8);
        Object[] keyIv = deriveKeyAndIv(passphrase, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec((byte[]) keyIv[0], "AES"), new IvParameterSpec((byte[]) keyIv[1]));

        byte[] enc = cipher.doFinal(input);
        return concat(concat(SALTED, salt), enc);
    }

    private static byte[] _decrypt(byte[] data, byte[] passphrase) throws Exception {
        byte[] salt = Arrays.copyOfRange(data, 8, 16);

        if (!Arrays.equals(Arrays.copyOfRange(data, 0, 8), SALTED)) {
            throw new IllegalArgumentException("Invalid crypted data");
        }

        Object[] keyIv = deriveKeyAndIv(passphrase, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec((byte[]) keyIv[0], "AES"), new IvParameterSpec((byte[]) keyIv[1]));
        return cipher.doFinal(data, 16, data.length - 16);
    }

    private static Object[] deriveKeyAndIv(byte[] passphrase, byte[] salt) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] passSalt = concat(passphrase, salt);
        byte[] dx = new byte[0];
        byte[] di = new byte[0];

        for (int i = 0; i < 3; i++) {
            di = md5.digest(concat(di, passSalt));
            dx = concat(dx, di);
        }

        return new Object[]{Arrays.copyOfRange(dx, 0, 32), Arrays.copyOfRange(dx, 32, 48)};
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static final int DEFAULT = 0;

    /**
     * Encoder flag bit to omit the padding '=' characters at the end
     * of the output (if any).
     */
    public static final int NO_PADDING = 1;

    /**
     * Encoder flag bit to omit all line terminators (i.e., the output
     * will be on one long line).
     */
    public static final int NO_WRAP = 2;

    /**
     * Encoder flag bit to indicate lines should be terminated with a
     * CRLF pair instead of just an LF.  Has no effect if {@code
     * NO_WRAP} is specified as well.
     */
    public static final int CRLF = 4;

    /**
     * Encoder/decoder flag bit to indicate using the "URL and
     * filename safe" variant of Base64 (see RFC 3548 section 4) where
     * {@code -} and {@code _} are used in place of {@code +} and
     * {@code /}.
     */
    public static final int URL_SAFE = 8;
    public static final int NO_CLOSE = 16;

    //  --------------------------------------------------------
    //  shared code
    //  --------------------------------------------------------

    /* package */
    static abstract class Coder {
        public byte[] output;
        public int op;

        /**
         * @return the maximum number of bytes a call to process()
         * could produce for the given number of input bytes.  This may
         * be an overestimate.
         */
        public abstract int maxOutputSize(int len);
    }

    //  --------------------------------------------------------
    //  decoding
    //  --------------------------------------------------------

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but
     * if any are present, there must be the correct number of them.
     *
     * @param str   the input String to decode, which is converted to
     *              bytes using the default charset
     * @param flags controls certain features of the decoded output.
     *              Pass {@code DEFAULT} to decode standard Base64.
     * @throws IllegalArgumentException if the input contains
     *                                  incorrect padding
     */
    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but
     * if any are present, there must be the correct number of them.
     *
     * @param input the input array to decode
     * @param flags controls certain features of the decoded output.
     *              Pass {@code DEFAULT} to decode standard Base64.
     * @throws IllegalArgumentException if the input contains
     *                                  incorrect padding
     */
    public static byte[] decode(byte[] input, int flags) {
        return decode(input, 0, input.length, flags);
    }

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are considered optional, but
     * if any are present, there must be the correct number of them.
     *
     * @param input  the data to decode
     * @param offset the position within the input array at which to start
     * @param len    the number of bytes of input to decode
     * @param flags  controls certain features of the decoded output.
     *               Pass {@code DEFAULT} to decode standard Base64.
     * @throws IllegalArgumentException if the input contains
     *                                  incorrect padding
     */
    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        // Allocate space for the most data the input could represent.
        // (It could contain less if it contains whitespace, etc.)
        Decoder decoder = new Decoder(flags, new byte[len * 3 / 4]);
        if (!Other.process(decoder, input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        }

        // Maybe we got lucky and allocated exactly enough output space.
        if (decoder.op == decoder.output.length) {
            return decoder.output;
        }

        // Need to shorten the array, so allocate a new one of the
        // right size and copy.
        byte[] temp = new byte[decoder.op];
        System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
        return temp;
    }

    /* package */
    public static class Decoder extends Coder {
        /**
         * Lookup table for turning bytes into their position in the
         * Base64 alphabet.
         */
        private static final int[] DECODE = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
                -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
                -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        /**
         * Decode lookup table for the "web safe" variant (RFC 3548
         * sec. 4) where - and _ replace + and /.
         */
        private static final int[] DECODE_WEBSAFE = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
                -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63,
                -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        };

        /**
         * States 0-3 are reading through the next input tuple.
         * State 4 is having read one '=' and expecting exactly
         * one more.
         * State 5 is expecting no more data or padding characters
         * in the input.
         * State 6 is the error state; an error has been detected
         * in the input and no future input can "fix" it.
         */
        int state;   // state number (0 to 6)
        int value;

        int[] alphabet;

        public Decoder(int flags, byte[] output) {
            this.output = output;

            alphabet = ((flags & URL_SAFE) == 0) ? DECODE : DECODE_WEBSAFE;
            state = 0;
            value = 0;
        }

        /**
         * @return an overestimate for the number of bytes {@code
         * len} bytes could decode to.
         */
        public int maxOutputSize(int len) {
            return len * 3 / 4 + 10;
        }
    }

    //  --------------------------------------------------------
    //  encoding
    //  --------------------------------------------------------

    /**
     * Base64-encode the given data and return a newly allocated
     * String with the result.
     *
     * @param input the data to encode
     * @param flags controls certain features of the encoded output.
     *              Passing {@code DEFAULT} results in output that
     *              adheres to RFC 2045.
     */
    public static String encodeToString(byte[] input, int flags) {
        return new String(encode(input, flags), US_ASCII);
    }

    /**
     * Base64-encode the given data and return a newly allocated
     * String with the result.
     *
     * @param input  the data to encode
     * @param offset the position within the input array at which to
     *               start
     * @param len    the number of bytes of input to encode
     * @param flags  controls certain features of the encoded output.
     *               Passing {@code DEFAULT} results in output that
     *               adheres to RFC 2045.
     */
    public static String encodeToString(byte[] input, int offset, int len, int flags) {
        return new String(encode(input, offset, len, flags), US_ASCII);
    }

    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input the data to encode
     * @param flags controls certain features of the encoded output.
     *              Passing {@code DEFAULT} results in output that
     *              adheres to RFC 2045.
     */
    public static byte[] encode(byte[] input, int flags) {
        return encode(input, 0, input.length, flags);
    }

    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input  the data to encode
     * @param offset the position within the input array at which to
     *               start
     * @param len    the number of bytes of input to encode
     * @param flags  controls certain features of the encoded output.
     *               Passing {@code DEFAULT} results in output that
     *               adheres to RFC 2045.
     */
    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        Encoder encoder = new Encoder(flags, null);

        // Compute the exact length of the array we will produce.
        int output_len = len / 3 * 4;

        // Account for the tail of the data and the padding bytes, if any.
        if (encoder.do_padding) {
            if (len % 3 > 0) {
                output_len += 4;
            }
        } else {
            switch (len % 3) {
                case 0:
                    break;
                case 1:
                    output_len += 2;
                    break;
                case 2:
                    output_len += 3;
                    break;
            }
        }

        // Account for the newlines, if any.
        if (encoder.do_newline && len > 0) {
            output_len += (((len - 1) / (3 * Encoder.LINE_GROUPS)) + 1) *
                    (encoder.do_cr ? 2 : 1);
        }

        encoder.output = new byte[output_len];
        Other.process(encoder, input, offset, len, true);

        assert encoder.op == output_len;

        return encoder.output;
    }

    /* package */
    public static class Encoder extends Coder {
        /**
         * Emit a new line every this many output tuples.  Corresponds to
         * a 76-character line length (the maximum allowable according to
         * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>).
         */
        public static final int LINE_GROUPS = 19;

        /**
         * Lookup table for turning Base64 alphabet positions (6 bits)
         * into output bytes.
         */
        private static final byte[] ENCODE = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
        };

        /**
         * Lookup table for turning Base64 alphabet positions (6 bits)
         * into output bytes.
         */
        private static final byte[] ENCODE_WEBSAFE = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_',
        };

        byte[] tail;
        /* package */
        int tailLen;
        int count;

        public boolean do_padding;
        public boolean do_newline;
        public boolean do_cr;
        byte[] alphabet;

        public Encoder(int flags, byte[] output) {
            this.output = output;

            do_padding = (flags & NO_PADDING) == 0;
            do_newline = (flags & NO_WRAP) == 0;
            do_cr = (flags & CRLF) != 0;
            alphabet = ((flags & URL_SAFE) == 0) ? ENCODE : ENCODE_WEBSAFE;

            tail = new byte[2];
            tailLen = 0;

            count = do_newline ? LINE_GROUPS : -1;
        }

        /**
         * @return an overestimate for the number of bytes {@code
         * len} bytes could encode to.
         */
        public int maxOutputSize(int len) {
            return len * 8 / 5 + 10;
        }
    }

    /**
     * Credit:
     * <a href="https://github.com/gsurma/steganographer/blob/master/Steganographer.java">...</a> *
     */
    private static final int bytesForTextLengthData = 4;
    private static final int bitsInByte = 8;

    // Encode

    public static BufferedImage encode(byte[] image, String withText) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(getStreamForBytes(image));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedImage imageInUserSpace = getImageInUserSpace(originalImage);

        byte[] imageInBytes = getBytesFromImage(imageInUserSpace);
        byte[] textInBytes = withText.getBytes();
        byte[] textLengthInBytes = getBytesFromInt(textInBytes.length);

        encodeImage(imageInBytes, textLengthInBytes, 0);
        encodeImage(imageInBytes, textInBytes, bytesForTextLengthData * bitsInByte);

        return imageInUserSpace;
    }

    private static void encodeImage(byte[] image, byte[] addition, int offset) {
        if (addition.length + offset > image.length) {
            throw new IllegalArgumentException("Image file is not long enough to store provided text");
        }
        for (int additionByte : addition) {
            for (int bit = bitsInByte - 1; bit >= 0; --bit, offset++) {
                int b = (additionByte >>> bit) & 0x1;
                image[offset] = (byte) ((image[offset] & 0xFE) | b);
            }
        }
    }


    // Decode

    public static String decode(InputStream image) throws IOException {
        byte[] decodedHiddenText;

        BufferedImage imageFromPath = ImageIO.read(image);
        BufferedImage imageInUserSpace = getImageInUserSpace(imageFromPath);
        byte[] imageInBytes = getBytesFromImage(imageInUserSpace);
        decodedHiddenText = decodeImage(imageInBytes);
        return new String(decodedHiddenText);
    }

    private static byte[] decodeImage(byte[] image) {
        int length = 0;
        int offset = bytesForTextLengthData * bitsInByte;

        for (int i = 0; i < offset; i++) {
            length = (length << 1) | (image[i] & 0x1);
        }

        byte[] result = new byte[length];

        for (int b = 0; b < result.length; b++) {
            for (int i = 0; i < bitsInByte; i++, offset++) {
                result[b] = (byte) ((result[b] << 1) | (image[offset] & 0x1));
            }
        }
        return result;
    }


    // File I/O methods

    public static InputStream getImageInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        return getStreamForBytes(stream.toByteArray());
    }

    private static InputStream getStreamForBytes(byte[] bytes) {
        return new InputStream() {
            byte[] data = bytes;
            int i = 0;

            @Override
            public int read() {
                if (i == data.length)
                    return -1;
                return data[i++] & 0xFF;
            }
        };
    }


    // Helpers

    private static BufferedImage getImageInUserSpace(BufferedImage image) {
        BufferedImage imageInUserSpace = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = imageInUserSpace.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose();
        return imageInUserSpace;
    }

    private static byte[] getBytesFromImage(BufferedImage image) {
        DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
        return buffer.getData();
    }

    private static byte[] getBytesFromInt(int integer) {
        return ByteBuffer.allocate(bytesForTextLengthData).putInt(integer).array();
    }

}