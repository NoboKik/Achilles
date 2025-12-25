package template.rip.gui.screens;

import by.radioegor146.nativeobfuscator.Native;
import net.minecraft.client.texture.TextureManager;
import template.rip.Template;
import template.rip.api.config.ConfigManager;
import template.rip.api.notification.Notification;
import template.rip.api.object.CTextureManager;
import template.rip.api.object.Description;
import template.rip.api.util.*;
import template.rip.deprecated.ServerCrasherModule;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.SearchBar;
import template.rip.module.Module;
import template.rip.module.ModuleManager;
import template.rip.module.modules.blatant.*;
import template.rip.module.modules.client.*;
import template.rip.module.modules.combat.*;
import template.rip.module.modules.crystal.*;
import template.rip.module.modules.legit.*;
import template.rip.module.modules.misc.*;
import template.rip.module.modules.player.*;
import template.rip.module.modules.render.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import static template.rip.Template.EVENTBUS;
import static template.rip.Template.mc;

@Native
public class LoginThread extends Thread {

    private LoginScreen screen;

    public LoginThread(LoginScreen screen) {
        init(screen);
    }

    private void init(LoginScreen screen) {
        this.screen = screen;
        setUncaughtExceptionHandler((t, e) -> {
//            e.printStackTrace(System.err);
            screen().authenticating = false;
        });
        if (screen().authenticating) {
            return;
        }
        screen().authenticating = true;
        start();
    }

    private LoginScreen screen() {
        return screen;
    }

    private void onRun() {
        if (Objects.equals(screen().login.get(), "")) {
            Template.notificationManager().addNotification(new Notification("Failed to authenticate", 5000, "Username cannot be empty"));
            screen().authenticating = false;
            return;
        }

        int port = 800;
        try {
            new Socket("127.0.0.1", port).close();
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException ignored) {
            }
            Template.notificationManager().addNotification(new Notification("Failed to authenticate", 5000, "Incorrect credentials"));
            screen().authenticating = false;
            return;
        } catch (IOException ignored) {}

        String username = screen().login.get();
        String hwid = screen().hwID;

        Template.notificationManager().addNotification(new Notification("Failed to authenticate", 5000, "Incorrect credentials"));
        screen().authenticating = false;

        boolean originalLogin = true;
        /*boolean originalLogin = false;
        Gson GSON = new Gson();
        JsonObject prof = new JsonObject();
        try {
            Path remember = ConfigManager.pathConfigFolder.resolve("login.json");
            if (!Files.isRegularFile(remember)) {
                originalLogin = true;
                throw new RuntimeException();
            }
            String read = Files.readString(remember);
            if (read.isEmpty()) {
                originalLogin = true;
                prof.addProperty(str, true);
                throw new RuntimeException();
            }

            String decode = " ";
            try {
                decode = Template.decrypt(read, "J50n1Mag3");
            } catch (Exception ignored) {
            }
            while (decode.charAt(decode.length() - 1) == '1') {
                decode = decode.substring(0, decode.length() - 1);
            }
            prof = GSON.fromJson(decode, JsonObject.class);

            if (prof == null || !prof.isJsonObject()) {
                prof = new JsonObject();
            }

            if (prof.get(str) == null) {
                originalLogin = true;
                prof.addProperty(str, true);
                throw new RuntimeException();
            }
        } catch (Exception ignored) {
        }*/

        if (originalLogin) {
            if (screen().state.equals(LoginScreen.LS.Butter)) {
                ConfigManager configManager = new ConfigManager();
                CrystalUtils crystalUtils = new CrystalUtils();
                MouseSimulation mouseSimulation = new MouseSimulation();
                CPSHelper cpsHelper = new CPSHelper();
                BlinkUtil blinkUtil = new BlinkUtil();
                BacktrackUtil backtrackUtil = new BacktrackUtil();
                ModuleManager bruh = new ModuleManager();

                Template.setConfigManager(configManager);
                Template.setCrystalUtils(crystalUtils);
                Template.setMouseSimulation(mouseSimulation);
                Template.setCPSHelper(cpsHelper);
                Template.setBlinkUtil(blinkUtil);
                Template.setBacktrackUtil(backtrackUtil);
                Template.setModuleManager(bruh);
                Template.cTextureManager = new CTextureManager(mc.getTextureManager());
                //this.configServer = new ConfigServer(6669);

                EVENTBUS.subscribe(bruh);
                EVENTBUS.subscribe(crystalUtils);
                EVENTBUS.subscribe(mouseSimulation);
                EVENTBUS.subscribe(SearchBar.getInstance());
                EVENTBUS.subscribe(cpsHelper);
                EVENTBUS.subscribe(blinkUtil);
                EVENTBUS.subscribe(backtrackUtil);
                EVENTBUS.subscribe(DamageUtils.class);
                EVENTBUS.subscribe(configManager);

                screen().state = LoginScreen.LS.Cheese;
                ImguiLoader.queueRemove(LoginScreen.getInstance());

                if (screen().rememberMe.get()) {
                    //prof.addProperty("true", screen().login.get());
                }

                ModuleManager moduleManager = Template.moduleManager;
                moduleManager.addModule(new AntiVoidModule(Module.Category.BLATANT, Description.of("Prevents you from falling into the void"), "AntiVoid"));
                moduleManager.addModule(new CriticalsModule(Module.Category.BLATANT, Description.of("Forces/increases critical hits"), "Criticals"));
                moduleManager.addModule(new FlightModule(Module.Category.BLATANT, Description.of("Allows you to fly!"), "Flight"));
                moduleManager.addModule(new ElytraCircleModule(Module.Category.BLATANT, Description.of("Circles around your first target using an elytra"), "ElytraCircle"));
                moduleManager.addModule(new FireballJumpModule(Module.Category.BLATANT, Description.of("Allows you to fly using fireballs"), "FireballJump"));
                moduleManager.addModule(new InfiniteAuraModule(Module.Category.BLATANT, Description.of("WIP! Teleports to and from targets to bypass vanilla's 6 block range limit"), "InfiniteAura"));
                moduleManager.addModule(new InventoryMoveModule(Module.Category.BLATANT, Description.of("Allows you to move while having your inventory open"), "InventoryMove"));
                moduleManager.addModule(new LegitAimBotModule(Module.Category.BLATANT, Description.of("An alternative aiming solution for those who don't care about the client controlling their rotation\nBypasses anticheats extremely well!"), "LegitAimBot"));
                moduleManager.addModule(new MultiTaskModule(Module.Category.BLATANT, Description.of("Allows the utilisation of both hands at once"), "MultiTask"));
                moduleManager.addModule(new NoFallModule(Module.Category.BLATANT, Description.of("Negates all fall damage."), "NoFall"));
                moduleManager.addModule(new NoSlowdownModule(Module.Category.BLATANT, Description.of("Reduces/removes item use slowdown"), "NoSlowdown"));
                moduleManager.addModule(new NukerModule(Module.Category.BLATANT, Description.of("Breaks certain blocks around you"), "Nuker"));
                moduleManager.addModule(new ScaffoldModule(Module.Category.BLATANT, Description.of("Automatically bridges for you"), "Scaffold"));
                moduleManager.addModule(new ScaffoldRecodeModule(Module.Category.BLATANT, Description.of("Automatically bridges for you but recode"), "ScaffoldRecode"));
                moduleManager.addModule(new SpeedModule(Module.Category.BLATANT, Description.of("Increases your speed"), "Speed"));
//              moduleManager.addModule(new TargetStrafeModule());
                moduleManager.addModule(new SpinAuraModule(Module.Category.BLATANT, Description.of("Spins around when you're on attack cooldown!"), "SpinAura"));
                moduleManager.addModule(new TickBaseModule(Module.Category.BLATANT, Description.of("Skips ticks to use them later to your advantage"), "TickBase"));
                moduleManager.addModule(new TimerModule(Module.Category.BLATANT, Description.of("Speeds up your game"), "Timer"));
                moduleManager.addModule(new VelocityModule(Module.Category.BLATANT, Description.of("Modifies the velocity sent by the server"), "Velocity"));

                moduleManager.addModule(new AchillesSettingsModule(Module.Category.CLIENT, Description.of("Achilles's settings"), "Achilles"));
                moduleManager.addModule(new BallsModule(Module.Category.CLIENT, Description.of("Renders balls"), "Balls"));
                moduleManager.addModule(new ClickSimulationModule(Module.Category.CLIENT, Description.of("Manages Achilles' Click Simulation"), "ClickSimulation"));
                moduleManager.addModule(new ClientDestructModule(Module.Category.CLIENT, Description.of("Removes traces"), "SelfDestruct"));
                moduleManager.addModule(new ConfigModule(Module.Category.CLIENT, Description.of("Achilles's configuration"), "Config"));
                moduleManager.addModule(new DisablerModule(Module.Category.CLIENT, Description.of("Partially disables certain anticheat checks"), "Disabler"));
                moduleManager.addModule(new MiddleClickFocusTargetModule(Module.Category.CLIENT, Description.of("Adds player into focus target list on middle click."), "MiddleClickFocus"));
                moduleManager.addModule(new MiddleClickFriendModule(Module.Category.CLIENT, Description.of("Adds player into friends list on middle click."), "MiddleClickFriend"));
                moduleManager.addModule(new TargetsModule(Module.Category.CLIENT, Description.of("Manages target priorities for the entire client\nAll Entity render AND Combat modules are dependent on this!"), "Targets"));
                moduleManager.addModule(new ToggleNotifyModule(Module.Category.CLIENT, Description.of("Notifies you when you toggle a module"), "ToggleNotify"));
                moduleManager.addModule(new ModMenuModule(Module.Category.CLIENT, Description.of("Opens the legit mods menu"), "ModMenu"));
                moduleManager.addModule(new InterfaceModule(Module.Category.CLIENT, Description.of("Manages interface"), "Interface"));

                moduleManager.addModule(new AimAssistModule(Module.Category.COMBAT, Description.of("Automatically aims at players"), "AimAssist"));
                moduleManager.addModule(new AutoBowModule(Module.Category.COMBAT, Description.of("Aids in the efficient use of (cross-)bows"), "AutoBow"));
                moduleManager.addModule(new AutoDrainModule(Module.Category.COMBAT, Description.of("Automatically steals the other player's liquids\nIt is recommended to bind this module to a 'Hold' keybind rather than toggle!"), "AutoDrain"));
                moduleManager.addModule(new AutoEatModule(Module.Category.COMBAT, Description.of("Automatically eats food for you"), "AutoEat"));
                moduleManager.addModule(new AutoHealModule(Module.Category.COMBAT, Description.of("Automatically uses common healing items"), "AutoPotHeal"));
                moduleManager.addModule(new BacktrackModule(Module.Category.COMBAT, Description.of("Delays your incoming packets to give you a reach advantage"), "Backtrack"));
                moduleManager.addModule(new BlinkModule(Module.Category.COMBAT, Description.of("Delays outgoing packets"), "Blink"));
                moduleManager.addModule(new CartAssistModule(Module.Category.COMBAT, Description.of("Fully automates the use of TNT Minecarts"), "CartAssist"));
                moduleManager.addModule(new MaceSwapModule(Module.Category.COMBAT, Description.of("Swaps from the sword to the mace when you attack"), "MaceSwap"));
                moduleManager.addModule(new NeverMissModule(Module.Category.COMBAT, Description.of("Only allows successful attacks"), "NeverMiss"));
                moduleManager.addModule(new ObiTrapMacroModule(Module.Category.COMBAT, Description.of("Automatically traps enemies in obsidian on keybind"), "ObiTrapMacro"));
                moduleManager.addModule(new ReachModule(Module.Category.COMBAT, Description.of("Extends player's reach and increase enemy's boxes"), "Reach"));
                moduleManager.addModule(new ProjectileAimbotModule(Module.Category.COMBAT, Description.of("Predicts the enemy's future position and aims there"), "ProjectileAimbot"));
                moduleManager.addModule(new SnowballThrowModule(Module.Category.COMBAT, Description.of("Throws snowballs/eggs silently"), "SnowballThrow"));
                moduleManager.addModule(new TriggerBotModule(Module.Category.COMBAT, Description.of("Automatically hits your crosshair entity"), "TriggerBot"));
                moduleManager.addModule(new WebMacroRecodeModule(Module.Category.COMBAT, Description.of("Automatically switches to, and places a web"), "WebMacro"));
                moduleManager.addModule(new WindMacroModule(Module.Category.COMBAT, Description.of("Automatically switches to a wind charge and throws it"), "WindMacro"));
                moduleManager.addModule(new WTapRecodeModule(Module.Category.COMBAT, Description.of("Resets your sprint after an attack"), "WTap"));

                moduleManager.addModule(new AirAnchorModule(Module.Category.CRYSTAL, Description.of("Makes MINDBLOWING Minecraft Crystal PVP Method easier!"), "AirAnchor"));
                moduleManager.addModule(new AutoAnchorRewriteModule(Module.Category.CRYSTAL, Description.of("A rewrite of a rewrite of the original AutoAnchor\nAirPlace in the module relies on settings and enabled state of the AirPlace module!"), "AutoAnchor"));
                moduleManager.addModule(new AutoCrystalRecodeModule(Module.Category.CRYSTAL, Description.of("Auto purple spinny thing placer and breaker (but super high tektonikal)"), "AutoCrystal"));
                moduleManager.addModule(new AutoDisableModule(Module.Category.CRYSTAL, Description.of("Automagically disables other player's shields with an axe"), "AutoDisable"));
                moduleManager.addModule(new AutoDoubleHandModule(Module.Category.CRYSTAL, Description.of("Automatically does double hand"), "AutoDoubleHand"));
                moduleManager.addModule(new AutoHitCrystalModule(Module.Category.CRYSTAL, Description.of("Automatically hit crystals opponents\nThis module disables FastPlace when active and relies on AutoCrystal logic!"), "AutoHitCrystal"));
                moduleManager.addModule(new AutoHitCrystalRewriteModule(Module.Category.CRYSTAL, Description.of("A full rewrite of the original AutoHitCrystal\nDesigned to fix flaws in the previous implementation"), "AutoHitCrystalRewrite"));
                moduleManager.addModule(new AutoShortPearlModule(Module.Category.CRYSTAL, Description.of("Automatically pearls when you're attacked to reduce knockback"), "AutoShortPearl"));
                moduleManager.addModule(new ElytraMacroModule(Module.Category.CRYSTAL, Description.of("Automatically switches to your elytra and uses a firework\nThis does not work with AutoArmor!"), "ElytraSwap"));
                moduleManager.addModule(new HotbarRefillModule(Module.Category.CRYSTAL, Description.of("Automatically refills your hotbar with potions/totems"), "PotTotemRefill"));
                moduleManager.addModule(new LootHelperModule(Module.Category.CRYSTAL, Description.of("Throws out totems and junk so you can pick up loot!"), "LootHelper"));
                moduleManager.addModule(new MainHandTotemModule(Module.Category.CRYSTAL, Description.of("Automatically fetches a MainHand totem"), "MainHandTotem"));
                moduleManager.addModule(new OffhandModule(Module.Category.CRYSTAL, Description.of("Manages your offhand slot (AutoTotem but better)"), "OffhandTotem"));
                moduleManager.addModule(new PearlMacroModule(Module.Category.CRYSTAL, Description.of("Automatically switches to, and throws a pearl"), "PearlMacro"));
                moduleManager.addModule(new TotemHitModule(Module.Category.CRYSTAL, Description.of("Guarantees sprint hits"), "TotemHit"));

                moduleManager.addModule(new ArmorCalculatorModule(Module.Category.MISC, Description.of("Replaces fake serverside armor durability values with corrected once"), "ArmorDeobfuscator"));
                moduleManager.addModule(new AutoArmorModule(Module.Category.MISC, Description.of("Automatically equips the best armor from inventory"), "AutoArmor"));
                moduleManager.addModule(new AutoCraftModule(Module.Category.MISC, Description.of("Automatically crafts better armor"), "AutoArmorCraft"));
                moduleManager.addModule(new BowBoostModule(Module.Category.MISC, Description.of("Automatically does a bow boost shot"), "BowBoostMacro"));
                moduleManager.addModule(new ChestStealerModule(Module.Category.MISC, Description.of("Automatically steals loot from chest"), "ChestStealer"));
                moduleManager.addModule(new ClientSpooferModule(Module.Category.CLIENT, Description.of("Spoofs your client brand"), "ClientSpoofer"));
                moduleManager.addModule(new CoordSpooferModule(Module.Category.MISC, Description.of("Spoofs your world coordinates in the F3 HUD"), "CoordSpoofer"));
                moduleManager.addModule(new FlagDetectorModule(Module.Category.MISC, Description.of("Detects flags and toggles dangerous modules."), "FlagDetector"));
                moduleManager.addModule(new HealthCalculatorModule(Module.Category.MISC, Description.of("Replaces fake serverside health values with corrected once"), "HealthDeobfuscator"));
                moduleManager.addModule(new InventoryCleanerModule(Module.Category.MISC, Description.of("Automatically throws junk from your inventory"), "InventoryCleaner"));
                moduleManager.addModule(new KillInsultsModule(Module.Category.MISC, Description.of("Insults players after you kill them"), "KillInsults"));
                moduleManager.addModule(new LeftClickerModule(Module.Category.MISC, Description.of("Mashes left click for you, aimed towards 1.8 pvp"), "LeftClicker"));
                moduleManager.addModule(new LightningDetectModule(Module.Category.MISC, Description.of("Prints the coordinates of lightning strikes"), "LightningDetect"));
                moduleManager.addModule(new NoInteractModule(Module.Category.MISC, Description.of("Prevents you from interacting with blocks in multiple ways"), "NoInteract"));
                moduleManager.addModule(new NoPushModule(Module.Category.MISC, Description.of("Prevents entities from pushing you around"), "NoPush"));
                moduleManager.addModule(new NoRotateModule(Module.Category.MISC, Description.of("Prevents server from changing your yaw and pitch"), "NoRotate"));
                moduleManager.addModule(new PingSpoofModule(Module.Category.MISC, Description.of("Spoofs your ping by delaying certain packets"), "PingSpoof"));
                moduleManager.addModule(new ResourcePackSpooferModule(Module.Category.MISC, Description.of("Fools the server into thinking you accepted the texture pack"), "ResourcePackSpoofer"));
                moduleManager.addModule(new RocketModule(Module.Category.MISC, Description.of("Uses a rocket. Useful when using an elytra."), "Rocket"));
                moduleManager.addModule(new SwordBlockModule(Module.Category.MISC, Description.of("Allows the user to block with swords on 1.8 servers"), "SwordBlock"));
                moduleManager.addModule(new ServerCrasherModule(Module.Category.MISC, Description.of("Crash servers"), "ServerCrasher"));
                moduleManager.addModule(new TransactionsModule(Module.Category.CLIENT, Description.of("Reads the transactions the server sends to you"), "Transactions"));

                moduleManager.addModule(new AirStuckModule(Module.Category.PLAYER, Description.of("Freezes you mid air"), "AirStuck"));
                moduleManager.addModule(new AutoFishModule(Module.Category.PLAYER, Description.of("A extremely over engineered auto-fishing solution"), "AutoFish"));
                moduleManager.addModule(new AutoHeadHitterModule(Module.Category.PLAYER, Description.of("Automatically crushes your skull by mashing it against the ceiling."), "AutoHeadHitter"));
                moduleManager.addModule(new AutoJumpReset(Module.Category.PLAYER, Description.of("Automatically jump resets"), "AutoJumpReset"));
                moduleManager.addModule(new AutoParkourModule(Module.Category.PLAYER, Description.of("Automatically jumps on the edge of blocks"), "AutoParkour"));
                moduleManager.addModule(new AutoToolModule(Module.Category.PLAYER, Description.of("Automatically switches to the best tool for the job"), "AutoTool"));
                moduleManager.addModule(new AutowalkModule(Module.Category.PLAYER, Description.of("Holds down keys for you"), "AutoWalk"));
                moduleManager.addModule(new BabyPlayerModule(Module.Category.PLAYER, Description.of("Makes you short and insufferable"), "BabyPlayer"));
                moduleManager.addModule(new BridgeAssistModule(Module.Category.PLAYER, Description.of("Automatically shifts on the edge of a block to avoid falling off"), "BridgeAssist"));
                moduleManager.addModule(new FakePlayerModule(Module.Category.PLAYER, Description.of("Spawns a fake player for testing combat modules"), "FakePlayer"));
                moduleManager.addModule(new FastPlaceModule(Module.Category.PLAYER, Description.of("Removes/reduces place delay for items"), "FastPlace"));
                moduleManager.addModule(new KeepSprintModule(Module.Category.PLAYER, Description.of("Prevents attack slowdown from attacking"), "KeepSprint"));
                moduleManager.addModule(new MovementRecorderModule(Module.Category.PLAYER, Description.of("Records and plays back your movement"), "MovementRecorder"));
                moduleManager.addModule(new NoBreakDelayModule(Module.Category.PLAYER, Description.of("Removes the delay from breaking subsequent blocks"), "NoBreakDelay"));
                moduleManager.addModule(new NoJumpCooldownModule(Module.Category.PLAYER, Description.of("Removes the player's jump cooldown"), "NoJumpCooldown"));
                moduleManager.addModule(new OptimalEatModule(Module.Category.PLAYER, Description.of("Reduces eating slowdown legitimately"), "OptimalEat"));
                moduleManager.addModule(new SnapTapModule(Module.Category.PLAYER, Description.of("Makes movement more responsive legitimately"), "SnapTap"));
                moduleManager.addModule(new SprintModule(Module.Category.PLAYER, Description.of("No client is complete without a sprint module!!!"), "Sprint"));

                moduleManager.addModule(new ArrayListModule(Module.Category.RENDER, Description.of("Shows array list"), "ArrayList"));
                moduleManager.addModule(new WatermarkModule(Module.Category.RENDER, Description.of("Shows watermark"), "Watermark"));
                moduleManager.addModule(new BaseESP(Module.Category.RENDER, Description.of("Highlights certain plant blocks that have grown to a certain stage to predict bases at the bottom of the plant blocks"), "BaseESP"));
                moduleManager.addModule(new BlockESPModule(Module.Category.RENDER, Description.of("Renders blocks through walls"), "BlockESP"));
                moduleManager.addModule(new BlockIndicatorModule(Module.Category.RENDER, Description.of("Shows your block count."), "BlockIndicator"));
                moduleManager.addModule(new BlinkIndicatorModule(Module.Category.RENDER, Description.of("Shows your blink time."), "BlinkIndicator"));
                moduleManager.addModule(new ESP2DModule(Module.Category.RENDER, Description.of("Extrasensory perception of other entities"), "2DESP"));
                moduleManager.addModule(new FreecamModule(Module.Category.RENDER, Description.of("Allows you to view the world freely"), "FreeCam"));
                moduleManager.addModule(new KeybindsHUDModule(Module.Category.RENDER, Description.of("Shows all your keybinds"), "KeybindsHUD"));
                moduleManager.addModule(new MisPlaceModule(Module.Category.RENDER, Description.of("Renders targets closer to you, useful for hiding reach"), "MisPlace"));
                moduleManager.addModule(new NewNameTagsModule(Module.Category.RENDER, Description.of("Shows nametags."), "NewNameTags"));
                moduleManager.addModule(new NickHiderModule(Module.Category.RENDER, Description.of("Protects your own and other's nicknames"), "NickHider"));
                moduleManager.addModule(new NoRenderModule(Module.Category.RENDER, Description.of("Prevents rendering annoying visuals"), "NoRender"));
                moduleManager.addModule(new PotionsHUDModule(Module.Category.RENDER, Description.of("Shows all active potion effects"), "PotionsHUD"));
                moduleManager.addModule(new SwingAnimationsModule(Module.Category.RENDER, Description.of("Modifies your sword swinging animation"), "SwingAnimations"));
                moduleManager.addModule(new TargetCircleModule(Module.Category.RENDER, Description.of("Renders circles ontop of your first target"), "TargetCircle"));
                moduleManager.addModule(new TargetHUDModule(Module.Category.RENDER, Description.of("Renders a HUD with your target's information"), "TargetHUD"));
                moduleManager.addModule(new TracersModule(Module.Category.RENDER, Description.of("Draws lines to entities"), "Tracers"));
                moduleManager.addModule(new TrajectoriesModule(Module.Category.RENDER, Description.of("Predicts the paths of projectiles"), "Trajectories"));

                moduleManager.addModule(new ArrowCountModule(Module.Category.LEGIT, Description.of("Shows your amount of arrows."), "Arrow Count"));
                moduleManager.addModule(new CoordsModule(Module.Category.LEGIT, Description.of("Shows your coordinates."), "Coords"));
                moduleManager.addModule(new CPSModule(Module.Category.LEGIT, Description.of("Shows your CPS."), "CPS"));
                moduleManager.addModule(new FPSModule(Module.Category.LEGIT, Description.of("Shows your FPS."), "FPS"));
//              moduleManager.addModule(new FreelookModule());
                moduleManager.addModule(new HurtCamModule(Module.Category.LEGIT, Description.of("Change the hurtcam."), "Hurt Cam"));
                moduleManager.addModule(new KeystrokesModule(Module.Category.LEGIT, Description.of("Shows your keystrokes."), "Keystrokes"));
                moduleManager.addModule(new PingModule(Module.Category.LEGIT, Description.of("Shows your ping."), "Ping"));
                moduleManager.addModule(new PotCountModule(Module.Category.LEGIT, Description.of("Shows your amount of pots."), "Pot Count"));
//              moduleManager.addModule(new PotionStatusModule());
                moduleManager.addModule(new ReachDisplayModule(Module.Category.LEGIT, Description.of("Shows your reach."), "Reach Display"));
                moduleManager.addModule(new ServerIPModule(Module.Category.LEGIT, Description.of("Shows your Server's IP."), "Server IP"));
                moduleManager.addModule(new TimeModule(Module.Category.LEGIT, Description.of("Shows your time."), "UserTime"));
                moduleManager.addModule(new TotemCountModule(Module.Category.LEGIT, Description.of("Shows your amount of totems."), "Totem Count"));
                moduleManager.addModule(new WorldChangerModule(Module.Category.LEGIT, Description.of("Modifies the weather and time of the world."), "WorldChanger"));
                moduleManager.addModule(new FullBrightModule(Module.Category.LEGIT, Description.of("Makes everything bright."), "FullBright"));

                Template.configManager().loadDefault();

                /*try {
                    StringBuilder toEncode = new StringBuilder(GSON.toJson(prof));
//                                        System.out.println(toEncode);
                    while (toEncode.toString().getBytes(StandardCharsets.UTF_16).length < 8192) {
                        toEncode.append("1");
                    }
//                                        System.out.println(toEncode);
                    Files.write(ConfigManager.pathConfigFolder.resolve("login.json"), Template.encrypt(toEncode.toString(), "J50n1Mag3").getBytes(StandardCharsets.UTF_8));
                } catch (Exception ignored) {
                }*/
            }
            screen().authenticating = false;
            return;
        }
        screen().authenticating = false;
        Template.notificationManager().addNotification(new Notification("Failed to authenticate", 5000, "Incorrect credentials"));
    }

    @Override
    public void run() {
        onRun();
    }
}
