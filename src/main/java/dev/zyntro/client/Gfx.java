package dev.zyntro.client;

import net.minecraft.client.gui.DrawContext;

/** Small pixel-drawing helpers used by the Zyntro menu. */
final class Gfx {
    private Gfx() {}

    // Grape positions in tenths of a berry radius (inverted triangle bunch).
    private static final int[][] BUNCH = {
            {-18, -16}, {0, -16}, {18, -16},
            {-9, 0}, {9, 0},
            {0, 16}
    };

    static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    static int lerpRgb(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    static void fillCircle(DrawContext c, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0.0, (double) r * r - (double) dy * dy)));
            c.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    static void circleOutline(DrawContext c, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dxo = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            int y = cy + dy;
            if (Math.abs(dy) > r - 1) {
                c.fill(cx - dxo, y, cx + dxo + 1, y + 1, color);
            } else {
                int dxi = (int) Math.round(Math.sqrt((double) (r - 1) * (r - 1) - (double) dy * dy));
                int leftEnd = Math.max(cx - dxi, cx - dxo + 1);
                c.fill(cx - dxo, y, leftEnd, y + 1, color);
                int rightStart = Math.min(cx + dxi + 1, cx + dxo);
                c.fill(rightStart, y, cx + dxo + 1, y + 1, color);
            }
        }
    }

    static void line(DrawContext c, double x1, double y1, double x2, double y2, int th, int color) {
        int steps = (int) Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) steps = 1;
        int off = th / 2;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(x1 + (x2 - x1) * t);
            int y = (int) Math.round(y1 + (y2 - y1) * t);
            c.fill(x - off, y - off, x - off + th, y - off + th, color);
        }
    }

    private static void berry(DrawContext c, int x, int y, int r) {
        fillCircle(c, x, y, r, 0xFF3E1766);
        fillCircle(c, x, y, Math.max(1, r - 1), 0xFF8E44AD);
        int hr = Math.max(1, (int) (r * 0.55));
        fillCircle(c, x - r / 4, y - r / 4, hr, 0xFFAA6AD0);
        int sr = Math.max(1, (int) (r * 0.2));
        fillCircle(c, x - (int) (r * 0.4), y - (int) (r * 0.45), sr, 0xDDFFFFFF);
    }

    /** Full-color purple grape bunch, u = berry radius in pixels. */
    static void grapeBunch(DrawContext c, int cx, int cy, int u) {
        line(c, cx, cy - 2.2 * u, cx + 0.5 * u, cy - 3.5 * u, 2, 0xFF6B4423);
        fillCircle(c, cx + (int) (1.4 * u), cy - (int) (3.2 * u), Math.max(2, (int) (0.7 * u)), 0xFF4CAF50);
        for (int[] p : BUNCH) {
            berry(c, cx + p[0] * u / 10, cy + p[1] * u / 10, u);
        }
    }

    /** Small colorless (outline only) grape bunch. */
    static void grapeOutline(DrawContext c, int cx, int cy, int u, int color) {
        line(c, cx, cy - 2.2 * u, cx + 0.5 * u, cy - 3.4 * u, 1, color);
        for (int[] p : BUNCH) {
            circleOutline(c, cx + p[0] * u / 10, cy + p[1] * u / 10, u, color);
        }
    }

    /** 24x24 icon for a category at (x, y). */
    static void icon(DrawContext c, Modules.Category cat, int x, int y) {
        switch (cat) {
            case COMBAT -> swords(c, x, y);
            case VISUALS -> eye(c, x, y);
            case PERFORMANCE -> bolt(c, x, y);
            case HUD -> gear(c, x, y);
        }
    }

    private static void swords(DrawContext c, int x, int y) {
        // Sword A: hilt bottom-left, tip top-right
        line(c, x + 7, y + 16, x + 4, y + 19, 2, 0xFF8B5A2B);
        line(c, x + 7, y + 16, x + 21, y + 2, 2, 0xFFE6E6F0);
        line(c, x + 4, y + 13, x + 10, y + 19, 2, 0xFFD4A017);
        // Sword B: hilt bottom-right, tip top-left
        line(c, x + 16, y + 16, x + 19, y + 19, 2, 0xFF8B5A2B);
        line(c, x + 16, y + 16, x + 2, y + 2, 2, 0xFFE6E6F0);
        line(c, x + 19, y + 13, x + 13, y + 19, 2, 0xFFD4A017);
    }

    private static void eye(DrawContext c, int x, int y) {
        int cx = x + 12, cy = y + 12;
        int col = 0xFF5CE1E6;
        int prev = 0;
        for (int dx = -11; dx <= 11; dx++) {
            double t = dx / 11.0;
            int hh = (int) Math.round(7 * (1 - t * t));
            int lo = Math.min(hh, prev), hi = Math.max(hh, prev);
            c.fill(cx + dx, cy - hi - 1, cx + dx + 1, cy - lo + 1, col);
            c.fill(cx + dx, cy + lo - 1, cx + dx + 1, cy + hi + 1, col);
            prev = hh;
        }
        fillCircle(c, cx, cy, 5, col);
        fillCircle(c, cx, cy, 2, 0xFF101018);
        c.fill(cx + 1, cy - 3, cx + 3, cy - 1, 0xFFFFFFFF);
    }

    private static void bolt(DrawContext c, int x, int y) {
        int col = 0xFFFFD43B;
        line(c, x + 15, y + 1, x + 7, y + 13, 3, col);
        line(c, x + 7, y + 13, x + 16, y + 12, 3, col);
        line(c, x + 16, y + 12, x + 9, y + 23, 3, col);
    }

    private static void gear(DrawContext c, int x, int y) {
        int cx = x + 12, cy = y + 12;
        int col = 0xFFC9C9D6;
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            int tx = cx + (int) Math.round(Math.cos(a) * 9.5);
            int ty = cy + (int) Math.round(Math.sin(a) * 9.5);
            fillCircle(c, tx, ty, 2, col);
        }
        fillCircle(c, cx, cy, 7, col);
        fillCircle(c, cx, cy, 3, 0xFF140A22);
    }
}
