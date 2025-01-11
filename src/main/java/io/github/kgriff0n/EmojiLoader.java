package io.github.kgriff0n;

import io.github.kgriff0n.emoji.Emoji;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class EmojiLoader implements SimpleSynchronousResourceReloadListener {

    public static HashMap<String, Emoji> EMOJIS = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return Identifier.of("emoji");
    }

    @Override
    public void reload(ResourceManager manager) {
        // Clear caches
        EMOJIS.clear();

        for (Identifier id : manager.findResources("emoji", path -> path.getPath().endsWith(".png")).keySet()) {
            String path = id.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String emojiName = ":" + fileName.substring(0, fileName.lastIndexOf('.')) + ":";
            EMOJIS.put(emojiName, new Emoji(id));
        }

        EmojiClient.LOGGER.info("Loaded {} emojis", EMOJIS.size());

        // Refresh chat messages
        MinecraftClient.getInstance().inGameHud.getChatHud().reset();
    }
}
