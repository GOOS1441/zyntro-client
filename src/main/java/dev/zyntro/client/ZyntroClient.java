package dev.zyntro.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ZyntroClient implements ClientModInitializer {
    public static final String MOD_ID = "zyntroclient";
    public static final String NAME = "Zyntro Client";

    private static boolean hudEnabled = true;
    private static KeyBinding toggleHudKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyntroclient.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.wasPressed()) {
                hudEnabled = !hudEnabled;
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(MOD_ID, "hud"),
                ZyntroClient::renderHud);
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!hudEnabled || mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;
        int x = 4, y = 4;

        ctx.drawTextWithShadow(tr, NAME, x, y, 0xFF55FFFF);
        y += 11;
        ctx.drawTextWithShadow(tr, "FPS: " + mc.getCurrentFps(), x, y, 0xFFFFFFFF);
        y += 11;
        ctx.drawTextWithShadow(tr, String.format("XYZ: %d / %d / %d",
                mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ()),
                x, y, 0xFFFFFFFF);
    }
}
