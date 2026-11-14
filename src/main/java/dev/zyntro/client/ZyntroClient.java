package dev.zyntro.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ZyntroClient implements ClientModInitializer {
    public static final String MOD_ID = "zyntroclient";
    public static final String NAME = "Zyntro Client";

    private static KeyBinding menuKey;
    private static KeyBinding zoomKey;

    private static int hits = 0;
    private static boolean zooming = false;
    private static int savedFov = 70;

    @Override
    public void onInitializeClient() {
        Modules.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyntroclient.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyntroclient.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ZyntroMenu());
                }
            }
            tickZoom(client);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && Modules.HITS.enabled) hits++;
            return ActionResult.PASS;
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(MOD_ID, "hud"),
                ZyntroClient::renderHud);
    }

    private static void tickZoom(MinecraftClient mc) {
        if (mc.options == null) return;
        boolean wantZoom = Modules.ZOOM.enabled && zoomKey.isPressed() && mc.currentScreen == null;
        if (wantZoom) {
            if (!zooming) {
                savedFov = mc.options.getFov().getValue();
                zooming = true;
            }
            mc.options.getFov().setValue(30);
        } else if (zooming) {
            mc.options.getFov().setValue(savedFov);
            zooming = false;
        }
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        boolean any = Modules.SHOW_FPS.enabled || Modules.SHOW_COORDS.enabled
                || Modules.SHOW_DIRECTION.enabled || Modules.SHOW_CLOCK.enabled || Modules.HITS.enabled;
        if (!any) return;

        TextRenderer tr = mc.textRenderer;
        int x = 4, y = 4;

        ctx.drawTextWithShadow(tr, NAME, x, y, 0xFFB57EDC);
        y += 11;

        if (Modules.SHOW_FPS.enabled) {
            ctx.drawTextWithShadow(tr, "FPS: " + mc.getCurrentFps(), x, y, 0xFFFFFFFF);
            y += 11;
        }
        if (Modules.SHOW_COORDS.enabled) {
            ctx.drawTextWithShadow(tr, String.format("XYZ: %d / %d / %d",
                    mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ()), x, y, 0xFFFFFFFF);
            y += 11;
        }
        if (Modules.SHOW_DIRECTION.enabled) {
            String d = mc.player.getHorizontalFacing().asString();
            d = d.substring(0, 1).toUpperCase() + d.substring(1);
            ctx.drawTextWithShadow(tr, "Facing: " + d, x, y, 0xFFFFFFFF);
            y += 11;
        }
        if (Modules.SHOW_CLOCK.enabled) {
            ctx.drawTextWithShadow(tr, "Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), x, y, 0xFFFFFFFF);
            y += 11;
        }
        if (Modules.HITS.enabled) {
            ctx.drawTextWithShadow(tr, "Hits: " + hits, x, y, 0xFFFF8080);
        }
    }
}
