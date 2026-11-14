package dev.zyntro.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Modules {
    private Modules() {}

    public enum Category {
        COMBAT("Combat", "Fight, Hits, Sounds, Kills, Effects"),
        VISUALS("Visuals", "Hands, Crosshair, Zoom, Screen effect"),
        PERFORMANCE("Performance", "Boost FPS, No lag, Smooth"),
        HUD("HUD", "On-Screen Huds");

        public final String label;
        public final String subtitle;

        Category(String label, String subtitle) {
            this.label = label;
            this.subtitle = subtitle;
        }

        public List<Module> modules() {
            List<Module> out = new ArrayList<>();
            for (Module m : ALL) if (m.category == this) out.add(m);
            return out;
        }

        public int total() { return modules().size(); }

        public int active() {
            int n = 0;
            for (Module m : modules()) if (m.enabled) n++;
            return n;
        }
    }

    public static final class Module {
        public final Category category;
        public final String id;
        public final String name;
        public final String description;
        public boolean enabled;

        Module(Category category, String id, String name, String description, boolean enabled) {
            this.category = category;
            this.id = id;
            this.name = name;
            this.description = description;
            this.enabled = enabled;
        }
    }

    public static final List<Module> ALL = new ArrayList<>();

    private static Module add(Category c, String id, String name, String desc, boolean def) {
        Module m = new Module(c, id, name, desc, def);
        ALL.add(m);
        return m;
    }

    // Combat
    public static final Module FIGHT = add(Category.COMBAT, "fight", "Fight", "Combat helpers (coming soon)", false);
    public static final Module HITS = add(Category.COMBAT, "hits", "Hits", "Shows your hit counter on screen", false);
    public static final Module SOUNDS = add(Category.COMBAT, "sounds", "Sounds", "Combat sounds (coming soon)", false);
    public static final Module KILLS = add(Category.COMBAT, "kills", "Kills", "Kill counter (coming soon)", false);
    public static final Module EFFECTS = add(Category.COMBAT, "effects", "Effects", "Hit effects (coming soon)", false);

    // Visuals
    public static final Module HANDS = add(Category.VISUALS, "hands", "Hands", "Hand and item look (coming soon)", false);
    public static final Module CROSSHAIR = add(Category.VISUALS, "crosshair", "Crosshair", "Custom crosshair (coming soon)", false);
    public static final Module ZOOM = add(Category.VISUALS, "zoom", "Zoom", "Hold C to zoom in", true);
    public static final Module SCREEN_EFFECT = add(Category.VISUALS, "screen_effect", "Screen Effect", "Screen effects (coming soon)", false);

    // Performance
    public static final Module BOOST_FPS = add(Category.PERFORMANCE, "boost_fps", "Boost FPS", "More frames (coming soon)", false);
    public static final Module NO_LAG = add(Category.PERFORMANCE, "no_lag", "No Lag", "Reduce lag (coming soon)", false);
    public static final Module SMOOTH = add(Category.PERFORMANCE, "smooth", "Smooth", "Smoother gameplay (coming soon)", false);

    // HUD
    public static final Module SHOW_FPS = add(Category.HUD, "show_fps", "FPS", "Shows your FPS", true);
    public static final Module SHOW_COORDS = add(Category.HUD, "show_coords", "Coordinates", "Shows your XYZ position", true);
    public static final Module SHOW_DIRECTION = add(Category.HUD, "show_direction", "Direction", "Shows the way you face", false);
    public static final Module SHOW_CLOCK = add(Category.HUD, "show_clock", "Clock", "Shows the real time", false);

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("zyntroclient.properties");
    }

    public static void load() {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file())) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Module m : ALL) {
            String v = p.getProperty(m.id);
            if (v != null) m.enabled = Boolean.parseBoolean(v);
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : ALL) p.setProperty(m.id, Boolean.toString(m.enabled));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Zyntro Client modules");
        } catch (IOException ignored) {
        }
    }
}
