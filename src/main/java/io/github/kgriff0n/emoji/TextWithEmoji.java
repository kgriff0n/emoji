package io.github.kgriff0n.emoji;

import io.github.kgriff0n.EmojiLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextWithEmoji {

    private static final Pattern EMOJI_PATTERN = Pattern.compile(":[a-zA-Z0-9-_]*:");

    private final List<Text> text;
    private final List<Emoji> emojis;

    public TextWithEmoji(Text text) {
        this.text = new ArrayList<>();
        this.emojis = new ArrayList<>();

        text.visit((style, content) -> {
            parseEmoji(content, style);
            return Optional.empty();
        }, Style.EMPTY);
    }

    public int render(DrawContext context, TextRenderer textRenderer, int x, int y, int color) {
        int newX = x;

        for (int i = 0; i < emojis.size(); i++) {
            context.drawTextWithShadow(textRenderer, text.get(i), newX, y, color);
            newX += textRenderer.getWidth(text.get(i));
            Emoji emoji = emojis.get(i);
            if (emoji != null) {
                emoji.render(context, textRenderer, newX, y, color);
                newX += textRenderer.fontHeight;
            }
        }
        return newX;
    }

    private void parseEmoji(String string, Style style) {
        Matcher matcher = EMOJI_PATTERN.matcher(string);

        int end = 0;
        while (matcher.find()) {
            String before = string.substring(end, matcher.start());
            String emoji = matcher.group();
            if (EmojiLoader.EMOJIS.containsKey(emoji)) {
                this.text.add(Text.literal(before).setStyle(style));
                this.emojis.add(EmojiLoader.EMOJIS.get(emoji));
            } else { // Invalid emoji, put back in the text
                this.text.add(Text.literal(before + emoji).setStyle(style));
                this.emojis.add(null);
            }
            end = matcher.end();
        }

        if (end < string.length()) {
            this.text.add(Text.literal(string.substring(end)).setStyle(style)); // end of text
            this.emojis.add(null); // empty emoji
        }
    }
}
