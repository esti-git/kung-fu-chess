package view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpriteConfigLoader {

    private static final int DEFAULT_FPS = 6;
    private static final Pattern FPS_PATTERN = Pattern.compile("\"frames_per_sec\"\\s*:\\s*([0-9]+(\\.[0-9]+)?)");
    private static final Pattern IS_LOOP_PATTERN = Pattern.compile("\"is_loop\"\\s*:\\s*(true|false)");

    private final Map<String, Integer> frameCountCache = new HashMap<>();
    private final Map<String, Integer> fpsCache = new HashMap<>();
    private final Map<String, Boolean> loopCache = new HashMap<>();

    public int getFrameCount(String folderName, String stateFolder) {
        String key = folderName + "/" + stateFolder;
        Integer cached = frameCountCache.get(key);
        if (cached != null) return cached;

        File spritesDir = new File(String.format("assets/%s/states/%s/sprites", folderName, stateFolder));
        File[] files = spritesDir.listFiles((dir, name) -> name.matches("\\d+\\.png"));
        int count = (files != null && files.length > 0) ? files.length : 1;

        frameCountCache.put(key, count);
        return count;
    }

    public int getFramesPerSec(String folderName, String stateFolder) {
        String key = folderName + "/" + stateFolder;
        Integer cached = fpsCache.get(key);
        if (cached != null) return cached;

        int fps = DEFAULT_FPS;
        String config = readStateConfig(folderName, stateFolder);
        if (config != null) {
            Matcher m = FPS_PATTERN.matcher(config);
            if (m.find()) {
                fps = (int) Math.round(Double.parseDouble(m.group(1)));
            }
        }
        if (fps <= 0) fps = DEFAULT_FPS;

        fpsCache.put(key, fps);
        return fps;
    }

    public boolean isLoop(String folderName, String stateFolder) {
        String key = folderName + "/" + stateFolder;
        Boolean cached = loopCache.get(key);
        if (cached != null) return cached;

        boolean loop = true;
        String config = readStateConfig(folderName, stateFolder);
        if (config != null) {
            Matcher m = IS_LOOP_PATTERN.matcher(config);
            if (m.find()) {
                loop = Boolean.parseBoolean(m.group(1));
            }
        }

        loopCache.put(key, loop);
        return loop;
    }

    private String readStateConfig(String folderName, String stateFolder) {
        File configFile = new File(String.format("assets/%s/states/%s/config.json", folderName, stateFolder));
        if (!configFile.exists()) return null;
        try {
            return new String(Files.readAllBytes(configFile.toPath()));
        } catch (IOException e) {
            return null;
        }
    }
}
