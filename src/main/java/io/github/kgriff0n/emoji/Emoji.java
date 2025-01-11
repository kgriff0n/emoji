package io.github.kgriff0n.emoji;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class Emoji {

    private final Identifier identifier;

    public Emoji(Identifier identifier) {
        this.identifier = identifier;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int color) {
        context.drawTexture(RenderLayer::getGuiTextured, identifier, x, y - textRenderer.fontHeight / 8, 0, 0, textRenderer.fontHeight, textRenderer.fontHeight, textRenderer.fontHeight, textRenderer.fontHeight, color);
    }
}
