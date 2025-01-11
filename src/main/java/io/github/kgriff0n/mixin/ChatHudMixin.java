package io.github.kgriff0n.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kgriff0n.emoji.TextWithEmoji;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@Unique
	private final List<TextWithEmoji> hudMessages = new ArrayList<>();

	@Shadow public abstract int getWidth();

	@Shadow public abstract double getChatScale();

	@Unique
	private int s;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"
	), method = "render")
	private void capture(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci, @Local(ordinal = 13) int s) {
		this.s = s;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
	private int render(DrawContext instance, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
		return hudMessages.get(s).render(instance, textRenderer, x, y, color);
	}

	@Inject(at = @At("HEAD"), method = "addVisibleMessage")
	private void add(ChatHudLine message, CallbackInfo ci) {
		int i = MathHelper.floor(getWidth() / getChatScale());
		MessageIndicator.Icon icon = message.getIcon();
		if (icon != null) {
			i -= icon.width + 4 + 2;
		}
		for (Text text : breakRenderedChatMessageLines(message.content(), i, MinecraftClient.getInstance().textRenderer)) {
			hudMessages.addFirst(new TextWithEmoji(text));
		}
		while (hudMessages.size() > 100) {
			hudMessages.removeLast();
		}
	}

	@Inject(at = @At("HEAD"), method = "clear")
	private void clear(boolean clearHistory, CallbackInfo ci) {
		hudMessages.clear();
	}

	@Inject(at = @At("HEAD"), method = "refresh")
	private void refresh(CallbackInfo ci) {
		hudMessages.clear();
		/* messages are added by calling addVisibleMessage in the original method */
	}

	/**
     * @see net.minecraft.client.util.ChatMessages#breakRenderedChatMessageLines(StringVisitable, int, TextRenderer)
	 */
	@Unique
	private static List<Text> breakRenderedChatMessageLines(Text message, int width, TextRenderer textRenderer) {
		TextCollector textCollector = new TextCollector();
		message.visit((style, messagex) -> {
			textCollector.add(StringVisitable.styled(getRenderedChatMessage(messagex), style));
			return Optional.empty();
		}, Style.EMPTY);
		List<Text> list = new ArrayList<>();
		textRenderer.getTextHandler().wrapLines(textCollector.getCombined(), width, Style.EMPTY, (text, lastLineWrapped) -> {
			MutableText newText = Text.empty();
			if (lastLineWrapped) {
				newText.append(" ");
			}
			text.visit((style, asString) -> {
				newText.append(Text.literal(asString).setStyle(style));
				return Optional.empty();
			}, Style.EMPTY);
			list.add(newText);
		});
		return list;
	}

	/**
	 * @see net.minecraft.client.util.ChatMessages#getRenderedChatMessage(String)
	 */
	@Unique
	private static String getRenderedChatMessage(String message) {
		return MinecraftClient.getInstance().options.getChatColors().getValue() ? message : Formatting.strip(message);
	}
}