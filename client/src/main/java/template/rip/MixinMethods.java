package template.rip;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import template.rip.api.event.events.*;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.*;
import template.rip.gui.screens.LoginScreen;
import template.rip.gui.windowgui.ConfigMenu;
import template.rip.gui.windowgui.LegitModulesMenu;
import template.rip.gui.windowgui.MainMenu;
import template.rip.gui.windowgui.ModulesMenu;
import template.rip.module.modules.blatant.NoSlowdownModule;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ClientSpooferModule;
import template.rip.module.modules.combat.ReachModule;
import template.rip.module.modules.crystal.AutoCrystalRecodeModule;
import template.rip.module.modules.legit.HurtCamModule;
import template.rip.module.modules.legit.WorldChangerModule;
import template.rip.module.modules.misc.CoordSpooferModule;
import template.rip.module.modules.misc.NoPushModule;
import template.rip.module.modules.misc.NoRotateModule;
import template.rip.module.modules.player.BabyPlayerModule;
import template.rip.module.modules.player.KeepSprintModule;
import template.rip.module.modules.player.SnapTapModule;
import template.rip.module.modules.render.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static template.rip.Template.mc;
import static template.rip.api.util.PlayerUtils.particleDisabled;
import static template.rip.api.util.PlayerUtils.soundDisabled;
import static template.rip.gui.ImguiLoader.imGuiGlfw;

public class MixinMethods {

    private static final Executor clickPoolExecutor = Executors.newSingleThreadExecutor();
    public static double xoffset, yoffset, zoffset;
    public static long tickCount = 0;
    private static int taskThread = 1;
    private static boolean cancelNext;
    private static TracersModule cache = null;
    static HashMap<Entity, Vec3d> prev;
    static Box b;

    public static void abs(BlockState blockState, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new BlockCollisionEvent(blockState, pos, entity)).isCancelled())
            ci.cancel();
    }

    public static void c(Camera camera) {
        Template.EVENTBUS.post(new CameraEvent(camera));
    }

    @SuppressWarnings("unchecked")
    public static void cc1(Packet<?> packet, PacketListener listener, CallbackInfo callback) {
        if (listener instanceof ClientPlayNetworkHandler && Template.EVENTBUS.post(new PacketEvent.Receive((Packet<ClientPlayNetworkHandler>) packet)).isCancelled()) {
            callback.cancel();
        }
    }

    public static void cc2(Packet<?> packet, NetworkSide side, CallbackInfo callback) {
        if (Template.noEvent.contains(packet)) {
            Template.noEvent.remove(packet);
            return;
        }
        if (side == NetworkSide.CLIENTBOUND && Template.EVENTBUS.post(new PacketEvent.Send(packet)).isCancelled()) {
            callback.cancel();
        }
    }
    
    @SuppressWarnings("unused")
    public static void cc3(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSTART OF LOG");
        sb.append("\nInternal Exception: ").append(ex);
        sb.append("\nStackTrace: ");
        Arrays.stream(ex.getStackTrace()).toList().forEach(e -> sb.append("\n").append(e));
        sb.append("\nCause: ").append(ex.getCause());
        sb.append("\nSuppressed: ");
        Arrays.stream(ex.getSuppressed()).toList().forEach(e -> sb.append("\n").append(e));
        sb.append("\nMessage: ").append(ex.getMessage());
        sb.append("\nEND OF LOG\n");
        // used to debug the backtrack issue (turned out to be a playerutil issue), very
        // useful if having internal exceptions, uncomment this to enable it
        // System.out.println(sb);
    }

    public static void cpe1(CallbackInfoReturnable<Boolean> cir, Object obj) {
        if (obj == Template.mc.player) {
            NoSlowdownModule noSlowdownModule = Template.moduleManager.getModule(NoSlowdownModule.class);
            if (noSlowdownModule != null && noSlowdownModule.isEnabled() && noSlowdownModule.sprint.isEnabled() && noSlowdownModule.check() && noSlowdownModule.slowDown()) {
                cir.setReturnValue(true);
            }
        }
    }

    public static void cpe2(CallbackInfoReturnable<Boolean> cir, Object object) {
        if (object == mc.player) {
            WalkingForwardEvent eventIsWalkingForward = new WalkingForwardEvent(cir.getReturnValue());
            Template.EVENTBUS.post(eventIsWalkingForward);
            if (eventIsWalkingForward.forward) {
                cir.setReturnValue(true);
            } else {
                NoSlowdownModule noSlowdownModule = Template.moduleManager.getModule(NoSlowdownModule.class);
                if (noSlowdownModule != null && noSlowdownModule.isEnabled() && noSlowdownModule.sprint.isEnabled() && noSlowdownModule.check() && noSlowdownModule.slowDown()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    public static void cpe3(Object object) {
        if (object == mc.player) {
            NoSlowdownModule noSlowdownModule = Template.moduleManager.getModule(NoSlowdownModule.class);
            assert mc.player != null;
            if (noSlowdownModule != null && noSlowdownModule.isEnabled() && noSlowdownModule.check()
                    && noSlowdownModule.slowDown() && mc.player.isUsingItem() && !mc.player.hasVehicle()) {
                float l = 0.0f;
                float f = 0.0f;
                if (mc.player.input.playerInput.right() && !mc.player.input.playerInput.left()) {
                    l = -noSlowdownModule.slowdownSpeed.getFValue();
                }
                if (!mc.player.input.playerInput.right() && mc.player.input.playerInput.left()) {
                    l = noSlowdownModule.slowdownSpeed.getFValue();
                }
                if (mc.player.input.playerInput.forward() && !mc.player.input.playerInput.backward()) {
                    f = noSlowdownModule.slowdownSpeed.getFValue();
                }
                if (!mc.player.input.playerInput.forward() && mc.player.input.playerInput.backward()) {
                    f = -noSlowdownModule.slowdownSpeed.getFValue();
                }
                mc.player.input.movementSideways = l;
                mc.player.input.movementForward = f;
            }
        }
    }

    public static void cpe4(Object obj, CallbackInfo ci) {
        if (obj == mc.player && Template.EVENTBUS.post(new PlayerTickEvent.Pre()).isCancelled()) {
            assert mc.player != null;
            mc.player.lastAttackedTicks++;
            ci.cancel();
        }
    }

    public static void cpe5(Object object) {
        if (object == mc.player)
            Template.EVENTBUS.post(new PlayerTickEvent.PrePacket());
    }

    public static void cpe6(Object object) {
        if (object == mc.player)
            Template.EVENTBUS.post(new PlayerTickEvent.Post());
    }

    public static void cpe7(Object object) {
        if (object == mc.player)
            Template.EVENTBUS.post(new SendMovementPacketEvent.Pre());
    }

    public static void cpe8(Object object) {
        if (object == mc.player)
            Template.EVENTBUS.post(new SendMovementPacketEvent.Post());
    }

    public static boolean cpe9(Object object) {
        if (object == mc.player) {
            assert mc.player != null;
            SafeWalkEvent swe = new SafeWalkEvent(mc.player.isSneaking());
            Template.EVENTBUS.post(swe);
            return swe.safe;
        }
        return false;
    }

    public static boolean cpe10(Object object) {
        if (object == mc.player) {
            NoRenderModule norender = Template.moduleManager.getModule(NoRenderModule.class);
            assert mc.player != null;
            if (norender != null && norender.shieldDelay.isEnabled() && norender.isEnabled() && mc.player.isUsingItem() && mc.player.getActiveItem().isOf(Items.SHIELD)) {
                return mc.player.getActiveItem().getItem().getMaxUseTime(mc.player.getActiveItem(), mc.player) - mc.player.getItemUseTimeLeft() >= 0;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static double cpi1() {
        return PlayerUtils.getReachDistance();
    }

    public static void cpi2(Entity target) {
        Template.EVENTBUS.post(new AttackEntityEvent.Pre(target));
    }

    public static void cpi3(Entity target) {
        Template.EVENTBUS.post(new AttackEntityEvent.Post(target));
    }

    public static void cpi4(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        Template.EVENTBUS.post(new InteractBlockEvent.Pre(player, hand, hitResult));
    }

    public static void cpi5(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Template.EVENTBUS.post(new InteractBlockEvent.Post(player, hand, hitResult, cir.getReturnValue()));
    }

    public static void cpi6() {
        Template.EVENTBUS.post(new SendMovementPacketEvent.Pre());
    }

    public static void cpi7() {
        Template.EVENTBUS.post(new SendMovementPacketEvent.Post());
    }

    public static void cpn1(EntityVelocityUpdateS2CPacket p, CallbackInfo ci) {
        assert mc.world != null;
        Entity entity = mc.world.getEntityById(p.getEntityId());
        if (entity == null) {
            return;
        }
        if (Template.EVENTBUS.post(new VelocityEvent.Pre(entity, p.getVelocityX() / 8000.0, p.getVelocityY() / 8000.0, p.getVelocityZ() / 8000.0)).isCancelled()) {
            ci.cancel();
        }
    }

    public static void cpn2(EntityVelocityUpdateS2CPacket p) {
        assert mc.world != null;
        Entity entity = mc.world.getEntityById(p.getEntityId());
        if (entity == null) {
            return;
        }
        Template.EVENTBUS.post(new VelocityEvent.Post(entity, p.getVelocityX() / 8000.0, p.getVelocityY() / 8000.0, p.getVelocityZ() / 8000.0));
    }

    public static void cpn3(Text text, boolean overlay, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new ChatEvent.Game(text, overlay)).isCancelled())
            ci.cancel();
    }

    public static void cpn4() {
        Template.EVENTBUS.post(new GameJoinedEvent());
    }

    public static void cpn5() {
        if (mc.player != null)
            Template.EVENTBUS.post(new SendMovementPacketEvent.Pre());
    }

    public static void cpn6(TitleS2CPacket packet, CallbackInfo info) {
        if (Template.EVENTBUS.post(new TitleEvent(packet.text())).isCancelled())
            info.cancel();
    }

    public static void cpn7(SubtitleS2CPacket packet, CallbackInfo info) {
        if (Template.EVENTBUS.post(new SubTitleEvent(packet.text())).isCancelled())
            info.cancel();
    }

    public static void cpn8() {
        if (mc.player != null)
            Template.EVENTBUS.post(new SendMovementPacketEvent.Post());
    }

    public static void cpn9(ChunkDataS2CPacket packet) {
        assert mc.world != null;
        Template.EVENTBUS.post(new ChunkDataEvent(mc.world.getChunk(packet.chunkX, packet.chunkZ)));
    }

    public static void cpn10(PlayerPositionLookS2CPacket packet, CallbackInfo ci, PlayerEntity playerEntity) {
        if (AchillesMenu.isClientEnabled() && Template.moduleManager.isModuleEnabled(NoRotateModule.class) && !(mc.currentScreen instanceof DownloadingTerrainScreen)) {
            mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(packet.teleportId()));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), packet.change().yaw(), packet.change().pitch(), false, false));
            playerEntity.setYaw(playerEntity.prevYaw + 0.000001f);
            playerEntity.setPitch(playerEntity.prevPitch + 0.000001f);
            ci.cancel();
        }
    }

    public static void cpn11(CallbackInfo ci) {
        if (AchillesMenu.isClientEnabled() && Template.moduleManager.isModuleEnabled(NoRotateModule.class) && !(mc.currentScreen instanceof DownloadingTerrainScreen)) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, false));
            mc.player.setYaw(mc.player.prevYaw + 0.000001f);
            mc.player.setPitch(mc.player.prevPitch + 0.000001f);
            ci.cancel();
        }
    }

    public static String cpn12() {
        if (Template.INSTANCE != null && Template.moduleManager != null) {
            ClientSpooferModule clientSpooferModule = Template.moduleManager.getModule(ClientSpooferModule.class);
            if (clientSpooferModule != null && clientSpooferModule.isEnabled()) {
                return clientSpooferModule.brand.getContent();
            }
        }
        return ClientBrandRetriever.getClientModName();
    }

    public static boolean cpn13(LivingEntity damaged, DamageSource source) {
        DamageEvent event = new DamageEvent(damaged, source);
        Template.EVENTBUS.post(event);
        return event.isCancelled();
    }

    public static void cw2(int entityId) {
        Template.EVENTBUS.post(new EntityRemoveEvent(mc.world.getEntityById(entityId)));
    }

    public static void cw4(CallbackInfo ci, ClientWorld.Properties clientWorldProperties) {
        WorldChangerModule wc = Template.moduleManager.getModule(WorldChangerModule.class);

        if (wc == null || !wc.isEnabled() || !wc.modifyTime.isEnabled())
            return;

        ci.cancel();

        int time = wc.time.getIValue();
        if (time < 0L)
            time = -time;

        clientWorldProperties.setTimeOfDay(time);
    }

    public static void eci(CallbackInfoReturnable<ActionResult> cir) {
        AutoCrystalRecodeModule autoCrystal = Template.moduleManager.getModule(AutoCrystalRecodeModule.class);
        if (autoCrystal != null && autoCrystal.isEnabled() && autoCrystal.noCountGlitch.isEnabled() && !mc.isInSingleplayer()) {
            cir.cancel();
            cir.setReturnValue(mc.world.isClient ? ActionResult.SUCCESS : ActionResult.CONSUME);
        }
    }

    public static void e2(Object object, CallbackInfo ci, float speed, Vec3d movementInput) {
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (asm == null)
            return;
        AchillesSettingsModule.moveFixModeEnum mf = asm.moveFixMode.getMode();
        if (object == mc.player && Template.rotationManager().isEnabled()) {
            ci.cancel();
            float yaw = ((mf == AchillesSettingsModule.moveFixModeEnum.Silent
                    || mf == AchillesSettingsModule.moveFixModeEnum.Direct))
                    ? Template.rotationManager().getClientRotation().fyaw()
                    : Template.rotationManager().yyyaw;
            Vec3d vec3d = Entity.movementInputToVelocity(movementInput, speed, yaw);
            mc.player.setVelocity(mc.player.getVelocity().add(vec3d));
        }
    }

    public static void e3(Object object, Entity entity, CallbackInfo ci) {
        if (object == mc.player && PlayerUtils.isClientSided(entity) || Template.moduleManager.isModuleEnabled(NoPushModule.class)) {
            ci.cancel();
        }
    }

    public static void e5(Object object, CallbackInfoReturnable<Vec3d> cir) {
        Rotation fakeRotation = Template.rotationManager().getClientRotation();
        if (object == mc.player && Template.rotationManager().isEnabled()) {
            cir.setReturnValue(RotationUtils.getPlayerLookVec(fakeRotation.fyaw(), fakeRotation.fpitch()));
        }
    }

    public static void e6(PlayerEntity player, Object object, CallbackInfoReturnable<Boolean> cir) {
        if (player == mc.player) {
            InvisibleEvent ie = new InvisibleEvent((Entity) object, cir.getReturnValue());
            Template.EVENTBUS.post(ie);
            cir.setReturnValue(ie.invisible);
        }
    }

    public static void e7(Object object, CallbackInfoReturnable<Boolean> cir) {
        if (object instanceof AbstractMinecartEntity) {
            NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
            if (noRender != null && noRender.isEnabled() && noRender.mineCart.isEnabled()) {
                cir.setReturnValue(false);
            }
        }
        if (object instanceof FishingBobberEntity) {
            NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
            if (noRender != null && noRender.isEnabled() && noRender.fishBobber.isEnabled()) {
                cir.setReturnValue(false);
            }
        }
    }

    public static void erd1(Entity entity) {
        if (AchillesMenu.isClientEnabled()) {
            ReachModule reach = Template.moduleManager.getModule(ReachModule.class);
            if (reach != null && reach.isEnabled()) {
                b = entity.getBoundingBox();
                entity.setBoundingBox(entity.getBoundingBox().expand(reach.getRenderHitboxSize(entity)));
            }
        }
    }

    public static void erd2(Entity entity) {
        if (AchillesMenu.isClientEnabled()) {
            ReachModule reach = Template.moduleManager.getModule(ReachModule.class);
            if (reach != null && reach.isEnabled() && b != null) {
                entity.setBoundingBox(b);
                b = null;
            }
        }
    }

    public static void er(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new NameTagRenderEvent(entity, yaw, tickDelta, matrices, vertexConsumers, light)).isCancelled())
            ci.cancel();
    }

    public static double gr1(double d) {
        ReachModule reachModule = Template.moduleManager.getModule(ReachModule.class);
        if (reachModule != null && reachModule.isEnabled())
            return reachModule.entityReach.getValue();

        return d;
    }

    public static double gr2(double d) {
        ReachModule reachModule = Template.moduleManager.getModule(ReachModule.class);
        if (reachModule != null && reachModule.isEnabled())
            return reachModule.entityReach.getValue() * reachModule.entityReach.getValue();

        return d;
    }

    public static void gr3(float tickDelta) {
        Template.EVENTBUS.post(new UpdateCrosshairEvent(tickDelta));
    }

    public static void gr4(CallbackInfo ci) {
        HurtCamModule hurt = Template.moduleManager.getModule(HurtCamModule.class);
        if (hurt != null && hurt.isEnabled() && hurt.disableHurtcam.isEnabled())
            ci.cancel();
    }

    public static void gr5(ItemStack stack, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new FloatingItemEvent(stack)).isCancelled())
            ci.cancel();
    }

    public static void hi1(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new HeldItemRenderEvent.Swing(matrices, arm, swingProgress)).isCancelled())
            ci.cancel();
    }

    public static void hi2(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new HeldItemRenderEvent.Equip(matrices, arm, equipProgress)).isCancelled())
            ci.cancel();
    }

    public static void hi3(ClientPlayerEntity player) {
        if (!Template.rotationManager().isEnabled())
            return;

        Template.rots[0] = player.lastRenderPitch;
        Template.rots[1] = player.renderPitch;
        Template.rots[2] = player.lastRenderYaw;
        Template.rots[3] = player.renderYaw;

        player.lastRenderPitch = player.getPitch();
        player.renderPitch = player.getPitch();
        player.lastRenderYaw = player.getYaw();
        player.renderYaw = player.getYaw();
    }

    public static void hi4(ClientPlayerEntity player) {
        if (!Template.rotationManager().isEnabled())
            return;

        player.lastRenderPitch = Template.rots[0];
        player.renderPitch = Template.rots[1];
        player.lastRenderYaw = Template.rots[2];
        player.renderYaw = Template.rots[3];
    }

    public static void hi5(Hand hand, AbstractClientPlayerEntity player, ItemStack item, CallbackInfo ci) {
        if (Template.moduleManager.isModuleEnabled(SwingAnimationsModule.class)
                && Template.moduleManager.getModule(SwingAnimationsModule.class).hideShield.isEnabled()
                && hand == Hand.OFF_HAND && item.getItem() instanceof ShieldItem
                && !player.getStackInHand(Hand.MAIN_HAND).isEmpty()
                && player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof SwordItem) {
            ci.cancel();
        }
    }

    public static boolean hi6(ItemStack instance) {
        return Template.moduleManager.isModuleEnabled(SwingAnimationsModule.class)
                && Template.moduleManager.getModule(SwingAnimationsModule.class).isAllowed()
                && instance.getItem() instanceof SwordItem
                && Template.moduleManager.isModuleEnabled(SwingAnimationsModule.class)
                && Template.moduleManager.getModule(SwingAnimationsModule.class).mode.is(SwingAnimationsModule.modeEnum.К1$7);

    }

    public static boolean hi7(AbstractClientPlayerEntity instance) {
        SwingAnimationsModule swing = Template.moduleManager.getModule(SwingAnimationsModule.class);
        return swing != null && swing.isEnabled() && swing.isAllowed() && instance.getMainHandStack().getItem() instanceof SwordItem && swing.mode.is(SwingAnimationsModule.modeEnum.К1$7);
    }

    public static boolean hi8(AbstractClientPlayerEntity instance) {
        SwingAnimationsModule swing = Template.moduleManager.getModule(SwingAnimationsModule.class);
        return swing != null && swing.isEnabled() && swing.isAllowed() && instance.getMainHandStack().getItem() instanceof SwordItem && swing.mode.is(SwingAnimationsModule.modeEnum.К1$7);
    }

    public static boolean hi9(AbstractClientPlayerEntity instance) {
        SwingAnimationsModule swing = Template.moduleManager.getModule(SwingAnimationsModule.class);
        return swing != null && swing.isEnabled() && swing.isAllowed() && instance.getMainHandStack().getItem() instanceof SwordItem && swing.mode.is(SwingAnimationsModule.modeEnum.К1$7);
    }

    public static boolean hi10() {
        return Template.moduleManager.isModuleEnabled(SwingAnimationsModule.class) && Template.moduleManager.getModule(SwingAnimationsModule.class).isAllowed()
                && Template.moduleManager.getModule(SwingAnimationsModule.class).mode.is(SwingAnimationsModule.modeEnum.К1$7);
    }

    public static void hi11(AbstractClientPlayerEntity player, Hand hand, ItemStack item, float swingProgress,
                            MatrixStack matrices) {
        if (item.getItem() instanceof SwordItem && Template.moduleManager.isModuleEnabled(SwingAnimationsModule.class)) {
            Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            if (Objects.requireNonNull(Template.moduleManager.getModule(SwingAnimationsModule.class)).isAllowed()
                    && Template.moduleManager.getModule(SwingAnimationsModule.class).mode.is(SwingAnimationsModule.modeEnum.К1$7)
                    && item.getItem() instanceof SwordItem) {
                matrices.translate(arm == Arm.RIGHT ? -0.1f : 0.1f, 0.1f, 0.0f);
                applySwingOffset2(matrices, arm, swingProgress * 0.9f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f));
                matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365f));
                matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05f));
            }
        }
    }

    private static void applySwingOffset2(MatrixStack matrices, Arm arm, float swingProgress) {
        int armSide = (arm == Arm.RIGHT ? 1 : -1);
        float f = MathHelper.sin((float) (swingProgress * swingProgress * Math.PI));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide * (45.0f + f * -20.0f)));
        float g = MathHelper.sin((float) (MathHelper.sqrt(swingProgress) * Math.PI));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armSide * g * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide * -45.0f));
    }

    public static void ig1(DrawContext context, float tickDelta) {
        Template.EVENTBUS.post(new HudRenderEvent(context, tickDelta));
    }

    public static void ig2(Identifier texture, CallbackInfo ci) {
        NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled()) {
            if (texture.equals(Identifier.of("textures/misc/pumpkinblur.png")) && noRender.pumpkin.isEnabled())
                ci.cancel();

            if (texture.equals(Identifier.of("textures/misc/powder_snow_outline.png")) && noRender.powderSnow.isEnabled())
                ci.cancel();
        }
    }

    public static void ig3(DrawContext drawContext, ScoreboardObjective scoreboardObjective, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new ScoreboardEvent(drawContext, scoreboardObjective)).isCancelled())
            ci.cancel();
    }

    public static void igo(CallbackInfo ci) {
        NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled() && noRender.fire.isEnabled())
            ci.cancel();
    }

    public static void i(Object object, CallbackInfoReturnable<Boolean> cir) {
        if (Template.mc.player == null || object != Template.mc.player.input)
            return;

        WalkingForwardEvent eventIsWalkingForward = new WalkingForwardEvent(cir.getReturnValue());
        Template.EVENTBUS.post(eventIsWalkingForward);
        cir.setReturnValue(eventIsWalkingForward.forward);
    }

    public static void is(Object object, CallbackInfoReturnable<Integer> cir) {
        if (((ItemStack) object).isOf(Items.END_CRYSTAL)) {
            AutoCrystalRecodeModule autoCrystal = Template.moduleManager.getModule(AutoCrystalRecodeModule.class);
            if (autoCrystal != null && autoCrystal.isEnabled() && autoCrystal.noBounce.isEnabled())
                cir.setReturnValue(0);
        }
    }

    public static void ki(Input input) {
        Template.EVENTBUS.post(new InputEvent(input));
    }

    private static final Executor pressPoolExecutor = Executors.newSingleThreadExecutor();

    public static void k1(long window, int key, int scancode, int action) {
        if (key == -1 || scancode == 172 || action == GLFW.GLFW_REPEAT)
            return;

        pressPoolExecutor.execute(() -> Template.EVENTBUS.post(new KeyPressEvent(key, scancode, action, window)));

        if ((KeyUtils.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_TAB) ||
                (KeyUtils.isKeyPressed(GLFW.GLFW_KEY_TAB) && key == GLFW.GLFW_KEY_LEFT_CONTROL)) return;

        imGuiGlfw.keyCallback(window, key, scancode, action, 0);
    }

    public static void k2(long window, int codePoint, int modifiers) {
        if (codePoint == -1)
            return;

        Template.EVENTBUS.post(new CharPressEvent(Character.toChars(codePoint), window));
    }

    public static void le1(Entity entity) {
        AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (asm == null)
            return;

        AchillesSettingsModule.moveFixModeEnum mf = asm.moveFixMode.getMode();
        if (entity == mc.player) {
            WalkingForwardEvent wfe = new WalkingForwardEvent(false);
            Template.EVENTBUS.post(wfe);
            if (wfe.forward) {
                Template.realYaw = entity.getYaw();
                entity.setYaw((float) PlayerUtils.getMoveDirection());
            } else if (Template.rotationManager().isEnabled() && (mf == AchillesSettingsModule.moveFixModeEnum.Silent
                    || mf == AchillesSettingsModule.moveFixModeEnum.Direct)) {
                Template.realYaw = entity.getYaw();
                entity.setYaw((float) Template.rotationManager().getClientRotation().yaw());
            }
        }
    }

    public static void le2(Entity entity) {
        if (entity == mc.player && Template.realYaw != null) {
            entity.setYaw(Template.realYaw);
            Template.realYaw = null;
        }
    }

    public static void le3(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        BabyPlayerModule babyPlayer = Template.moduleManager.getModule(BabyPlayerModule.class);
        if (mc.player != null && entity == mc.player && babyPlayer != null && babyPlayer.isEnabled()
                && (mc.player.isSneaking() || !babyPlayer.onlyWhileSneaking.isEnabled())) {
            cir.setReturnValue(true);
        }
    }

    public static void le4(CallbackInfoReturnable<Integer> cir) {
        SwingAnimationsModule swingAnimations = Template.moduleManager.getModule(SwingAnimationsModule.class);
        if (swingAnimations != null && swingAnimations.isEnabled()) {
            float var = swingAnimations.swingDuration.getFValue();
            cir.setReturnValue((int) (cir.getReturnValue() * var));
        }
    }

    public static void le5(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule noRender = Template.moduleManager.getModule(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled()) {
            if (noRender.blindness.isEnabled() && effect == StatusEffects.BLINDNESS) {
                cir.setReturnValue(false);
            }
            if (noRender.nausea.isEnabled() && effect == StatusEffects.NAUSEA) {
                cir.setReturnValue(false);
            }
        }
    }

    private static boolean check(LivingEntity entity) {
        return entity == mc.player && Template.rotationManager().isEnabled() && !(mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof CreativeInventoryScreen);
    }

    public static void ler1(LivingEntity livingEntity) {
        if (check(livingEntity)) {
            Template.yaw = livingEntity.getYaw();
            Template.pitch = livingEntity.getPitch();
            livingEntity.setYaw(Template.rotationManager().getClientRotation().fyaw());
            livingEntity.setPitch(Template.rotationManager().getClientRotation().fpitch());
        }
    }

    public static void ler2(LivingEntity livingEntity) {
        if (check(livingEntity)) {
            if (Template.yaw != 0 && Template.pitch != 0) {
                livingEntity.setYaw(Template.yaw);
                livingEntity.setPitch(Template.pitch);
                Template.yaw = 0;
                Template.pitch = 0;
            }
        }
    }

    public static void mg(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
        if (Template.EVENTBUS.post(new ChatEvent.Chat(decorated, message, sender, params, receptionTimestamp)).isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    public static void mc1(CallbackInfo ci) {
        tickCount++;
        if (Template.EVENTBUS.post(new TickEvent.Pre()).isCancelled())
            ci.cancel();
    }

    public static void mc2() {
        Template.EVENTBUS.post(new TickEvent.Post());
    }

    public static void mc3(CallbackInfoReturnable<Boolean> cir) {
        if (MouseSimulation.cancelLeft) {
            return;
        }
        if (Template.EVENTBUS.post(new AttackEvent.Pre()).isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    public static void mc4(CallbackInfoReturnable<Boolean> cir) {
        if (Template.EVENTBUS.post(new AttackEvent.Post()).isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    public static void mc5(CallbackInfo ci) {
        if (MouseSimulation.cancelRight) {
            return;
        }
        if (Template.EVENTBUS.post(new ItemUseEvent.Pre()).isCancelled())
            ci.cancel();
    }

    public static void mc6(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new ItemUseEvent.Post()).isCancelled())
            ci.cancel();
    }

    public static void mc7(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new ItemUseEvent.Return()).isCancelled())
            ci.cancel();
    }

    public static void mc8(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new BlockBreakEvent.Pre()).isCancelled())
            ci.cancel();
    }

    public static void mc9(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new BlockBreakEvent.Post()).isCancelled())
            ci.cancel();
    }

    public static void mc10(CallbackInfo ci) {
        Template.EVENTBUS.post(new SwitchSlotEvent()).apply();

        if (Template.EVENTBUS.post(new HandleInputEvent.Pre()).isCancelled())
            ci.cancel();
    }

    public static void mc11(Screen theScreen, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new SetScreenEvent(theScreen)).isCancelled())
            ci.cancel();
    }

    public static void mc12() {
        Template.EVENTBUS.post(new HandleInputEvent.Post());
    }

    public static void mc13() {
        if (Template.INSTANCE == null) {
            new Template();
            Template.INSTANCE.init();
        }
    }

    public static void mc14() {
        Template.configManager().saveDefault();
        /*
         * try {
         * Template.configServer().stop();
         * } catch (Exception ignored) {
         * }
         */
    }

    public static void mc15() {
        safeLoopRunnable(() -> Template.EVENTBUS.post(new FastTickEvent()));
        safeLoopRunnable(() -> {
            if (mc.player == null || mc.world == null)
                return;
             Template.rotationManager().tick();
        });

        safeLoopRunnable(() -> Template.EVENTBUS.post(new BlockESPTicker()));
        safeLoopRunnable(() -> Template.EVENTBUS.post(new ArrayListTicker()));
        safeLoopRunnable(() -> {
            double preX = 0;
            double preY = 0;
            double preZ = 0;
            if (Template.moduleManager == null) {
                return;
            }
            FreecamModule freecam = Template.moduleManager.getModule(FreecamModule.class);
            if (freecam != null && freecam.isEnabled() && freecam.showCoords.isEnabled()) {
                Vec3d v = freecam.position;
                Entity e = mc.player;
                if (e != null) {
                    preX += v.x - e.getX();
                    preY += v.y - e.getY();
                    preZ += v.z - e.getZ();
                }
            }
            CoordSpooferModule coordSpoofer = Template.moduleManager.getModule(CoordSpooferModule.class);
            if (coordSpoofer != null && coordSpoofer.isEnabled()) {
                preX += coordSpoofer.xPosOffset.value;
                preY += coordSpoofer.yPosOffset.value;
                preZ += coordSpoofer.zPosOffset.value;
            }
            xoffset = preX;
            yoffset = preY;
            zoffset = preZ;
        });

        safeLoopRunnable(() -> Template.EVENTBUS.post(new TwoDeePosTicker()), 5);
        safeLoopRunnable(PlayerUtils::processTargets, 5);
    }

    private static void safeLoopRunnable(Runnable runnable) {
        safeLoopRunnable(runnable, 10);
    }

    private static void safeLoopRunnable(Runnable runnable, long wait) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(wait);
                    runnable.run();
                } catch (Throwable ignored) {}
            }
        });
        thread.setName("Task Thread ".concat(String.valueOf(taskThread++)));
        thread.setUncaughtExceptionHandler((t, e) -> {
//            System.err.println("Error in thread: ".concat(t.getName()));
//            e.printStackTrace(System.err);
        });
        thread.start();
    }

    public static void mc16(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cache == null) {
            if (Template.moduleManager != null) {
                cache = Template.moduleManager.getModule(TracersModule.class);
            }
        } else if (entity instanceof PlayerEntity && cache.isEnabled() && cache.glowESP.isEnabled()) {// quick class check rather than full targets check
            cir.setReturnValue(true);
        }
    }

    public static void mc17() {
        Template.frame++;
    }

    public static void mc18() {
        Template.EVENTBUS.post(new RenderEvent());
    }

    public static void mc19(Object object, CallbackInfo ci) {
        if (object == mc.player) {
            if (Template.EVENTBUS.post(new MovementTickEvent()).isCancelled()) {
                ci.cancel();
            }
        }
    }

    public static void mc20(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new ClickSimulationEvent.Left()).isCancelled()) {
            ci.cancel();
        }
    }

    public static void mc21(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new ClickSimulationEvent.Right()).isCancelled()) {
            ci.cancel();
        }
    }

    public static Pair<Double, Double> m2(double cursorDeltaX, double cursorDeltaY) {
        MouseDeltaEvent mde = new MouseDeltaEvent(cursorDeltaX, cursorDeltaY);
        Template.EVENTBUS.post(mde);
        return new Pair<>(mde.deltaX, mde.deltaY);
    }

    public static void m3(CallbackInfo ci) {
        if (Template.EVENTBUS.post(new MouseUpdateEvent.Pre()).isCancelled())
            ci.cancel();
    }

    public static void m4() {
        Template.EVENTBUS.post(new MouseUpdateEvent.Post());
    }

    public static void m5(double vertical, CallbackInfo ci) {
        AchillesSettingsModule sm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (sm != null && sm.isEnabled()) {
            float scrollY = (float) (vertical * 30f);

            if (ImguiLoader.isRendered(ModulesMenu.getInstance())) {
                ModulesMenu.getInstance().scrollUntil -= scrollY;
            } else if (ImguiLoader.isRendered(AchillesMenu.getInstance())) {
                AchillesMenu.getInstance().tabs.forEach(categoryTab -> categoryTab.scrollUntil -= categoryTab.isWindowHovered() ? scrollY : 0);
            }

            if (ImguiLoader.isRendered(LegitModules.getInstance()) && LegitMenu.getInstance().isOn) {
                LegitModules.getInstance().scrollUntil -= scrollY;
            }
            if (ImguiLoader.isRendered(ConfigChild.getInstance()) && ConfigParent.getInstance().isOn) {
                ConfigChild.getInstance().scrollUntil -= scrollY;
            }

            if (MainMenu.getInstance().selectedSection.contains("Mod Menu")) {
                LegitModulesMenu.getInstance().scrollUntil -= scrollY;
            }

            if (MainMenu.getInstance().selectedSection.contains("Config")) {
                ConfigMenu.getInstance().scrollUntil -= scrollY;
            }

            ci.cancel();
        }
    }

    public static void m6(int button, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_REPEAT) {
            return;
        }
        if (MouseSimulation.wasSimulatedPressed(action == GLFW.GLFW_PRESS, button)) {
            if (action == GLFW.GLFW_PRESS && mc.currentScreen == null && mc.getOverlay() == null && !mc.mouse.isCursorLocked()) {
                cancelNext = true;
            }
            return;
        }
        AchillesSettingsModule sm = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (sm != null && sm.isEnabled())
            ci.cancel();
        LoginScreen login = LoginScreen.getInstance();
        if (login != null && login.isRendered())
            ci.cancel();
        clickPoolExecutor.execute(() -> Template.EVENTBUS.post(new MousePressEvent(button, action)));
    }

    public static void m7(CallbackInfo ci) {
        if (cancelNext) {
            cancelNext = false;
            ci.cancel();
        }
    }

    public static void pm(Particle particle, CallbackInfo ci) {
        if (particleDisabled)
            ci.cancel();

        if (particle instanceof ExplosionLargeParticle && Template.EVENTBUS.post(new ParticleEvent(particle)).isCancelled())
            ci.cancel();
    }

    public static void pe1() {
        KeepSprintModule keepSprintModule = Template.moduleManager.getModule(KeepSprintModule.class);
        if (keepSprintModule != null && keepSprintModule.isEnabled() && mc.player != null) {
            float multiplier = 0.6f + 0.4f * keepSprintModule.motion.getFValue();
            mc.player.setVelocity(mc.player.getVelocity().x / 0.6 * multiplier, mc.player.getVelocity().y, mc.player.getVelocity().z / 0.6 * multiplier);
            mc.player.setSprinting(true);
        }
    }

    public static void pe2(CallbackInfoReturnable<Float> cir) {
        HurtCamModule hurt = Template.moduleManager.getModule(HurtCamModule.class);
        if (hurt != null && hurt.isEnabled() && hurt.oldHurtcam.isEnabled())
            cir.setReturnValue(0f);
    }

    public static void pe3(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue((double) PlayerUtils.getReachDistance());
    }

    public static void pe4(CallbackInfoReturnable<Double> cir) {
        ReachModule reachModule = Template.moduleManager.getModule(ReachModule.class);
        if (reachModule != null && reachModule.isEnabled())
            cir.setReturnValue(reachModule.entityReach.getValue());
    }

    public static Box pu(Entity ent) {
        ReachModule reach = Template.moduleManager.getModule(ReachModule.class);
        if (AchillesMenu.isClientEnabled() && reach != null && reach.isEnabled()) {
            return ent.getBoundingBox().expand(ent.getTargetingMargin() + reach.getHitboxSize(ent));
        }
        return ent.getBoundingBox().expand(ent.getTargetingMargin());
    }

    public static void ss1(CallbackInfo ci) {
        if (soundDisabled)
            ci.cancel();
    }

    public static void ss2(SoundInstance sound, CallbackInfo ci) {
        if (Template.EVENTBUS.post(new PlaySoundEvent(sound)).isCancelled() || soundDisabled)
            ci.cancel();
    }

    public static void tf(String text, int startIndex, Style style, CharacterVisitor visitor, CallbackInfoReturnable<Boolean> cir) {
        if (text != null && mc.getNetworkHandler() != null && mc.player != null) {
            NickHiderModule nhm = Template.moduleManager.getModule(NickHiderModule.class);
            if (nhm != null && nhm.isEnabled()) {
                text = text.replace(mc.player.getGameProfile().getName(), nhm.ownNick.getContent());

                Entity le = PlayerUtils.findFirstTarget(true);
                PlayerEntity player = null;
                if (le instanceof PlayerEntity)
                    player = ((PlayerEntity) le);

                ArrayList<GameProfile> others = new ArrayList<>();
                for (PlayerListEntry pl : mc.getNetworkHandler().getPlayerList()) {
                    others.add(pl.getProfile());
                }
                others.sort(Comparator.comparing(pr -> pr.getName().length()));
                int i = 0;
                int j = 0;
                for (GameProfile str : others) {
                    if (str.getName().equals(mc.player.getGameProfile().getName()))
                        continue;

                    if (PlayerUtils.isFriend(str.getId()) && nhm.obfFriends.isEnabled()) {
                        String template = nhm.obfFriendsTemplate.getContent();
                        int index = template.indexOf('#');
                        j++;
                        String replacement;
                        if (index == -1) {
                            replacement = template + j;
                        } else {
                            replacement = template.replace("#", String.valueOf(j));
                        }
                        text = text.replace(str.getName(), replacement);
                    } else if (player != null && player.getGameProfile().getName() == str.getName()
                            && nhm.obfTarget.isEnabled()) {
                        text = text.replace(str.getName(), nhm.targetNick.getContent());
                    } else if (nhm.obfOthers.isEnabled()) {
                        i++;
                        text = text.replace(str.getName(), "Player" + i);
                    }
                }
            }
            cir.setReturnValue(TextVisitFactory.visitFormatted(text, startIndex, style, style, visitor));
        }
    }

    public static void it(long handle, GLFWKeyCallbackI keyCallback, GLFWCharModsCallbackI charModsCallback, CallbackInfo ci) {
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                if (key == mc.options.leftKey.boundKey.getCode()) {
                    SnapTapModule.lastLeft = System.currentTimeMillis();
                } else if (key == mc.options.rightKey.boundKey.getCode()) {
                    SnapTapModule.lastRight = System.currentTimeMillis();
                } else if (key == mc.options.forwardKey.boundKey.getCode()) {
                    SnapTapModule.lastForward = System.currentTimeMillis();
                } else if (key == mc.options.backKey.boundKey.getCode()) {
                    SnapTapModule.lastBackwards = System.currentTimeMillis();
                }
            }

            keyCallback.invoke(window, key, scancode, action, mods);
        });
        GLFW.glfwSetCharModsCallback(handle, charModsCallback);
        ci.cancel();
    }

    public static void w(long handle) {
        ImguiLoader.onGlfwInit(handle);
    }

    public static void wr(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (Template.INSTANCE != null) {
            MisPlaceModule mpm = Template.moduleManager.getModule(MisPlaceModule.class);
            if (mpm != null && mpm.isEnabled() && mc.player != null && PlayerUtils.findTargets().contains(entity)) {
                if (prev == null) {
                    prev = new HashMap<>();
                }
                ci.cancel();
                double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - cameraX;
                double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - cameraY;
                double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - cameraZ;
                float yaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());

                Vec3d us = MathUtils.Vec3dWithY(mc.player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)), entity.getY());
                Rotation rot = RotationUtils.getRotations(us, entity.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)));
                Vec3d newPos = RotationUtils.forwardVector(rot).multiply(Math.max(us.distanceTo(entity.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false))) - mpm.blocksCloser.value, 0));
                newPos = new Vec3d(newPos.x, entity.getY(), newPos.z);

                Vec3d last = newPos;
                if (prev.containsKey(entity))
                    last = prev.get(entity);

                newPos = MathUtils.smoothVec3d(last, newPos, mc.getRenderTickCounter().getTickDelta(false));

                newPos = new Vec3d(x, 0.0, z).add(entity.adjustMovementForCollisions(new Vec3d(newPos.x - x, 0.0, newPos.z - z)));
                x = newPos.x;
                z = newPos.z;
                prev.put(entity, new Vec3d(newPos.x, entity.getY(), newPos.z));

                mc.getEntityRenderDispatcher().render(entity, x, y, z, tickDelta, matrices, vertexConsumers, mc.getEntityRenderDispatcher().getLight(entity, tickDelta));
            }
        }
    }

    public static class ClickSim {
        public static void skb(InputUtil.Key key, boolean bl) {
            if (!bl) {
                return;
            }
            if (!MouseSimulation.wasSimulatedPressed(true, key.getCode())) {
                return;
            }
            KeyBinding keybind = KeyBinding.KEY_TO_BINDINGS.get(key);
            if (keybind != null) {
                keybind.pressed = false;
            }
        }

        //spoof isPressed() for non minecraft methods (hud mods)
        public static void ikb(Object thiss, CallbackInfoReturnable<Boolean> cir) {
            KeyBinding me = ((KeyBinding) thiss);

            if (MouseSimulation.wasExperimentalPressed(me.boundKey.getCode())) {
                StackTraceElement[] full = Thread.currentThread().getStackTrace();
                boolean isMc = true;
                for (int i = 2; i < full.length - 1; i++) {
                    StackTraceElement ste = full[i];
                    if (!ste.getClassName().startsWith("net.minecraft") && !ste.getClassName().startsWith("net.fabricmc")) {
                        isMc = false;
                        break;
                    }
                }
                if (!isMc) {
                    cir.setReturnValue(true);
                }
            } else {
                cir.setReturnValue(me.pressed);
            }
        }

        public static void okb(InputUtil.Key key) {
            if (!MouseSimulation.wasSimulatedPressed(true, key.getCode())) {
                return;
            }
            KeyBinding keybind = KeyBinding.KEY_TO_BINDINGS.get(key);
            if (keybind != null) {
                keybind.timesPressed--;
            }
        }

        public static void osp(String s1, CallbackInfo ci) {
            switch (s1) {
                case "mouseClicked event handler" -> {
                    if (MouseSimulation.wasSimulatedPressed(true, MouseSimulation.lastPressSimKey)) {
                        ci.cancel();
                    }
                }
                case "mouseReleased event handler" -> {
                    if (MouseSimulation.wasSimulatedPressed(false, MouseSimulation.lastReleaseSimKey)) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
