package dev.zyntro.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ZyntroMenu extends Screen {
    private static final int GAP = 6;
    private static final String TITLE = "ZYNTRO CLIENT";

    private Modules.Category open = null;
    private boolean wasMouseDown;
    private boolean wasShiftDown;

    public ZyntroMenu() {
        super(Text.literal("Zyntro Client"));
        long w = window();
        wasMouseDown = GLFW.glfwGetMouseButton(w, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        wasShiftDown = GLFW.glfwGetKey(w, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private static long window() {
        return MinecraftClient.getInstance().getWindow().getHandle();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ---- layout ----
    private int panelW() { return Math.min(250, width * 45 / 100); }
    private int panelX() { return width - panelW() - 14; }
    private int panelTop() { return 46; }

    private int cardH() {
        int avail = height - panelTop() - 24;
        return Math.max(42, Math.min(58, (avail - 3 * GAP) / 4));
    }

    private int cardY(int i) { return panelTop() + i * (cardH() + GAP); }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static final int ROW_H = 28;

    private int rowsTop() { return panelTop() + 40; }

    // ---- input (polled, so it works the same on every 1.21.x version) ----
    private void handleInput(int mx, int my) {
        long w = window();

        boolean shift = GLFW.glfwGetKey(w, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (shift && !wasShiftDown) {
            close();
            return;
        }
        wasShiftDown = shift;

        boolean down = GLFW.glfwGetMouseButton(w, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean clicked = down && !wasMouseDown;
        wasMouseDown = down;
        if (!clicked) return;

        int px = panelX(), pw = panelW();
        if (open == null) {
            Modules.Category[] cats = Modules.Category.values();
            for (int i = 0; i < cats.length; i++) {
                if (inside(mx, my, px, cardY(i), pw, cardH())) {
                    open = cats[i];
                    return;
                }
            }
        } else {
            if (inside(mx, my, px, panelTop(), 54, 18)) {
                open = null;
                return;
            }
            int i = 0;
            for (Modules.Module m : open.modules()) {
                int ry = rowsTop() + i * (ROW_H + 4);
                if (inside(mx, my, px, ry, pw, ROW_H)) {
                    m.enabled = !m.enabled;
                    Modules.save();
                    return;
                }
                i++;
            }
        }
    }

    // ---- drawing ----
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        handleInput(mouseX, mouseY);

        double t = System.currentTimeMillis() / 1000.0;

        // dark purple tinted background
        ctx.fill(0, 0, width, height, 0xC00B0614);

        // grape scene in the free area left of the panel
        int cx = (panelX() - 10) / 2;
        int cy = height / 2 + 6;
        int u = Math.max(6, Math.min(14, height / 22));

        drawShadow(ctx, cx, cy, u);
        drawRings(ctx, cx, cy, u, t, false);
        Gfx.grapeBunch(ctx, cx, cy, u);
        drawRings(ctx, cx, cy, u, t, true);

        drawTitle(ctx);

        if (open == null) {
            drawCategories(ctx, mouseX, mouseY);
        } else {
            drawModuleList(ctx, mouseX, mouseY);
        }

        String hint = "Right Shift or Esc to close";
        ctx.drawTextWithShadow(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 13, 0xFF8A7A9E);
    }

    private void drawTitle(DrawContext ctx) {
        int textW = textRenderer.getWidth(TITLE) * 2;
        int grapeW = 20, gap = 6;
        int startX = (width - (grapeW + gap + textW)) / 2;

        // small colorless grape next to the title
        Gfx.grapeOutline(ctx, startX + grapeW / 2, 19, 3, 0xFFFFFFFF);

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate((float) (startX + grapeW + gap), 9f);
        ctx.getMatrices().scale(2f, 2f);
        ctx.drawTextWithShadow(textRenderer, TITLE, 0, 0, 0xFFE9D5FF);
        ctx.getMatrices().popMatrix();
    }

    /** Shadow trail: dark purple near the grape, lighter purple further away. */
    private void drawShadow(DrawContext ctx, int cx, int cy, int u) {
        int steps = 22;
        for (int i = steps; i >= 0; i--) {
            double k = i / (double) steps;
            int sx = cx + (int) (u * 0.6 + k * u * 5.5);
            int sy = cy + (int) (u * 1.2 + k * u * 2.6);
            int r = Math.max(2, (int) (u * (2.3 - 1.0 * k)));
            int alpha = (int) (150 - 110 * k);
            int rgb = Gfx.lerpRgb(0x2A0A40, 0xC9A0E8, k);
            Gfx.fillCircle(ctx, sx, sy, r, Gfx.argb(alpha, rgb));
        }
    }

    private void drawRings(DrawContext ctx, int cx, int cy, int u, double t, boolean front) {
        double tilt = Math.toRadians(-18);
        ring(ctx, cx, cy, u * 4.6, u * 1.15, tilt, t * 1.1, 0xD9B8F2, front, 2);
        ring(ctx, cx, cy, u * 3.7, u * 0.85, tilt, -t * 1.6, 0xA678D8, front, 2);
    }

    private void ring(DrawContext ctx, int cx, int cy, double a, double b, double tilt,
                      double phase, int rgb, boolean front, int th) {
        double cosT = Math.cos(tilt), sinT = Math.sin(tilt);
        for (double ang = 0; ang < Math.PI * 2; ang += 0.025) {
            double s = Math.sin(ang);
            if ((s >= 0) != front) continue;
            double ex = a * Math.cos(ang), ey = b * s;
            int px = cx + (int) Math.round(ex * cosT - ey * sinT);
            int py = cy + (int) Math.round(ex * sinT + ey * cosT);
            double dash = 0.5 + 0.5 * Math.sin(7 * (ang + phase));
            ctx.fill(px, py, px + th, py + th, Gfx.argb((int) (70 + 185 * dash), rgb));
        }
        // a bright little moon orbiting along the ring so the spin is easy to see
        double m = -phase;
        if ((Math.sin(m) >= 0) == front) {
            double ex = a * Math.cos(m), ey = b * Math.sin(m);
            int px = cx + (int) Math.round(ex * cosT - ey * sinT);
            int py = cy + (int) Math.round(ex * sinT + ey * cosT);
            Gfx.fillCircle(ctx, px, py, 2, 0xFFFFFFFF);
        }
    }

    private void drawCategories(DrawContext ctx, int mx, int my) {
        int px = panelX(), pw = panelW(), ch = cardH();
        Modules.Category[] cats = Modules.Category.values();
        for (int i = 0; i < cats.length; i++) {
            Modules.Category cat = cats[i];
            int y = cardY(i);
            boolean hover = inside(mx, my, px, y, pw, ch);

            ctx.fill(px, y, px + pw, y + ch, hover ? 0xF0301848 : 0xD0180C28);
            int border = hover ? 0xFFB57EDC : 0xFF5E2F8A;
            ctx.fill(px, y, px + pw, y + 1, border);
            ctx.fill(px, y + ch - 1, px + pw, y + ch, border);
            ctx.fill(px, y, px + 1, y + ch, border);
            ctx.fill(px + pw - 1, y, px + pw, y + ch, border);

            int iy = y + (ch - 28) / 2;
            ctx.fill(px + 8, iy, px + 36, iy + 28, 0xFF2A1646);
            Gfx.icon(ctx, cat, px + 10, iy + 2);

            int tx = px + 46;
            ctx.drawTextWithShadow(textRenderer, Text.literal(cat.label).formatted(Formatting.BOLD), tx, y + 6, 0xFFFFFFFF);
            String sub = textRenderer.trimToWidth(cat.subtitle, pw - 46 - 6);
            ctx.drawTextWithShadow(textRenderer, sub, tx, y + 18, 0xFFA99BBF);

            String mods = "Modules: " + cat.total();
            ctx.drawTextWithShadow(textRenderer, mods, tx, y + 30, 0xFFDDDDDD);
            int w1 = textRenderer.getWidth(mods);
            int active = cat.active();
            Gfx.fillCircle(ctx, tx + w1 + 6, y + 34, 2, 0xFF9B6BD3);
            ctx.drawTextWithShadow(textRenderer, "Active: " + active, tx + w1 + 12, y + 30,
                    active > 0 ? 0xFF55FF55 : 0xFF8A8A99);
        }
    }

    private void drawModuleList(DrawContext ctx, int mx, int my) {
        int px = panelX(), pw = panelW(), top = panelTop();
        int count = open.total();
        int panelBottom = rowsTop() + count * (ROW_H + 4);
        ctx.fill(px - 6, top - 6, px + pw + 6, panelBottom + 4, 0xA0100818);

        // back button
        boolean backHover = inside(mx, my, px, top, 54, 18);
        ctx.fill(px, top, px + 54, top + 18, backHover ? 0xF0301848 : 0xD0180C28);
        ctx.drawTextWithShadow(textRenderer, "< Back", px + 8, top + 5, 0xFFE9D5FF);

        // category header
        Gfx.icon(ctx, open, px + 62, top - 3);
        ctx.drawTextWithShadow(textRenderer, Text.literal(open.label).formatted(Formatting.BOLD), px + 92, top + 5, 0xFFFFFFFF);
        String stats = "Modules: " + open.total() + "  Active: " + open.active();
        ctx.drawTextWithShadow(textRenderer, stats, px, top + 24, 0xFFA99BBF);

        int i = 0;
        for (Modules.Module m : open.modules()) {
            int ry = rowsTop() + i * (ROW_H + 4);
            boolean hover = inside(mx, my, px, ry, pw, ROW_H);
            ctx.fill(px, ry, px + pw, ry + ROW_H, hover ? 0xF0301848 : 0xD0180C28);

            ctx.drawTextWithShadow(textRenderer, m.name, px + 10, ry + 5, 0xFFFFFFFF);
            String d = textRenderer.trimToWidth(m.description, pw - 70);
            ctx.drawTextWithShadow(textRenderer, d, px + 10, ry + 16, 0xFF8F82A6);

            // toggle switch
            int sx = px + pw - 40, sy = ry + (ROW_H - 12) / 2;
            ctx.fill(sx, sy, sx + 26, sy + 12, m.enabled ? 0xFF3DDC84 : 0xFF555566);
            int kx = m.enabled ? sx + 15 : sx + 1;
            ctx.fill(kx, sy + 1, kx + 10, sy + 11, 0xFFFFFFFF);
            i++;
        }
    }
}
