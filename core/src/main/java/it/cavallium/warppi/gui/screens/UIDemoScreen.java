package it.cavallium.warppi.gui.screens;

import java.io.IOException;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;

/**
 * Rendering and font diagnostics that used to be mixed into the Mario game.
 * Optional external test assets are deliberately best-effort so this screen is
 * still useful in production images where those assets are excluded.
 */
public final class UIDemoScreen extends Screen {

	private static final int BACKGROUND_COLOR = 0xFF18202A;
	private static final String[] TEST_GLYPHS = {
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
			"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "ò"
	};
	private static final int[] PALETTE = {
			0xFF000000, 0xFF800000, 0xFFEEA28E, 0xFFEE7255,
			0xFFEAC0B0, 0xFFF3D8CE, 0xFFFFEDE7
	};
	private static final int[] COLOR_BARS = {
			0xFFE74C3C, 0xFFF39C12, 0xFFF1C40F, 0xFF2ECC71,
			0xFF1ABC9C, 0xFF3498DB, 0xFF9B59B6, 0xFFECF0F1
	};
	private static final float SPRITE_FRAME_SECONDS = 0.04f;
	private static final float GLYPH_SECONDS = 1.5f;
	private static final int SPRITE_FRAME_COUNT = 21;

	private BinaryFont largeTestFont;
	private BinaryFont paletteTestFont;
	private boolean largeAtlas;
	private Skin animatedTestSkin;
	private int spriteFrame;
	private float spriteElapsed;
	private int glyphIndex;
	private float glyphElapsed;

	public UIDemoScreen() {
		historyBehavior = HistoryBehavior.NORMAL;
	}

	@Override
	public void created() {}

	@Override
	public void initialized() {}

	@Override
	public int getBackgroundColor() {
		return BACKGROUND_COLOR;
	}

	@Override
	public void graphicInitialized(final ScreenContext ctx) {
		final GraphicEngine engine = d.display.getGraphicEngine();
		paletteTestFont = loadOptionalFont(engine, "gputest", "gputest2");
		largeTestFont = loadOptionalFont(engine, "gputest", "gputest12");
		largeAtlas = largeTestFont != null;
		if (largeTestFont == null)
			largeTestFont = loadOptionalFont(engine, "gputest", "gputest1");
		animatedTestSkin = loadOptionalSkin(engine, "gputest/font_gputest3.png");
		if (animatedTestSkin == null)
			animatedTestSkin = loadOptionalSkin(engine, "font_gputest3.png");
		largeTestFont = initializeOptionalFont(largeTestFont, d.display);
		paletteTestFont = initializeOptionalFont(paletteTestFont, d.display);
		animatedTestSkin = initializeOptionalSkin(animatedTestSkin, d.display);
	}

	private static BinaryFont loadOptionalFont(final GraphicEngine engine, final String path, final String name) {
		try {
			return engine.loadFont(path, name);
		} catch (final IOException | RuntimeException externalFailure) {
			try {
				return engine.loadFont(name);
			} catch (final IOException | RuntimeException resourceFailure) {
				return null;
			}
		}
	}

	private static Skin loadOptionalSkin(final GraphicEngine engine, final String path) {
		try {
			return engine.loadSkin(path);
		} catch (final IOException | RuntimeException exception) {
			return null;
		}
	}

	private static BinaryFont initializeOptionalFont(final BinaryFont candidate,
			final DisplayOutputDevice display) {
		return (BinaryFont) initializeOptionalSkin(candidate, display);
	}

	static Skin initializeOptionalSkin(final Skin candidate, final DisplayOutputDevice display) {
		if (candidate == null || candidate.isInitialized())
			return candidate;
		try {
			candidate.initialize(display);
			return candidate;
		} catch (final RuntimeException exception) {
			System.err.println("Optional UI demo asset disabled: " + exception.getMessage());
			return null;
		}
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {
		final float elapsed = Math.max(0f, Math.min(dt, 0.25f));
		spriteElapsed += elapsed;
		while (spriteElapsed >= SPRITE_FRAME_SECONDS) {
			spriteFrame = (spriteFrame + 1) % SPRITE_FRAME_COUNT;
			spriteElapsed -= SPRITE_FRAME_SECONDS;
		}
		glyphElapsed += elapsed;
		while (glyphElapsed >= GLYPH_SECONDS) {
			glyphIndex = (glyphIndex + 1) % TEST_GLYPHS.length;
			glyphElapsed -= GLYPH_SECONDS;
		}
	}

	@Override
	public void render(final RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		final DisplayOutputDevice display = d.display;

		renderer.glClearSkin();
		renderer.glColor(0xFF263342);
		renderer.glFillColor(8, 28, 176, 100);
		renderer.glColor(0xFF34495E);
		renderer.glFillColor(192, 28, Math.max(0, ctx.getWidth() - 200), 100);
		drawColorBars(renderer, 8, 140, Math.max(1, (ctx.getWidth() - 16) / 8), 22);

		useDefaultFont(display);
		renderer.glColor(0xFFFFFFFF);
		renderer.glDrawStringLeft(10, 10, "UI / GPU DEMO");
		renderer.glDrawStringLeft(16, 42, "LINES");
		renderer.glDrawStringLeft(16, 58, "RECTANGLES");
		renderer.glDrawStringLeft(16, 74, "FONT ATLAS");
		renderer.glColor(0xFF8FD3FF);
		renderer.glDrawLine(16, 94, 168, 94);
		renderer.glColor(0xFFFFFFFF);

		if (largeTestFont != null) {
			final int size = largeAtlas ? 96 : 72;
			final int x = ctx.getWidth() - size - 16;
			final int y = 30;
			renderer.glColor(0xFFFFFFFF);
			renderer.glFillColor(x, y, size, size);
			largeTestFont.use(display);
			renderer.glColor(0xFF101820);
			renderer.glDrawStringCenter(x + size / 2f, y + 8, TEST_GLYPHS[glyphIndex]);
		} else {
			useDefaultFont(display);
			renderer.glColor(0xFFFFC857);
			renderer.glDrawStringRight(ctx.getWidth() - 16, 74, "OPTIONAL LARGE FONT: N/A");
		}

		if (animatedTestSkin != null) {
			animatedTestSkin.use(display);
			renderer.glColor4f(1f, 1f, 1f, 0.7f);
			final int width = Math.min(224, ctx.getWidth() / 2);
			renderer.glFillRect(8, ctx.getHeight() - 128, width, 128,
					spriteFrame * 224f, 0, 224, 128);
		}

		if (paletteTestFont != null) {
			paletteTestFont.use(display);
			for (int i = 0; i < PALETTE.length; i++) {
				renderer.glColor(PALETTE[i]);
				renderer.glDrawStringRight(ctx.getWidth() - i * 14f,
						ctx.getHeight() - paletteTestFont.getCharacterHeight() - 4, String.valueOf((char) ('A' + i)));
			}
		}

		useDefaultFont(display);
		renderer.glColor(0xFFD8E6F3);
		renderer.glDrawStringLeft(10, ctx.getHeight() - 12, "LEFT/RIGHT: GLYPH   BACK: EXIT");
	}

	private void useDefaultFont(final DisplayOutputDevice display) {
		if (d.fonts != null && d.fonts.length > 0 && d.fonts[0] != null)
			d.fonts[0].use(display);
	}

	private static void drawColorBars(final Renderer renderer, final int x, final int y,
			final int width, final int height) {
		for (int i = 0; i < COLOR_BARS.length; i++) {
			renderer.glColor(COLOR_BARS[i]);
			renderer.glFillColor(x + i * width, y, width, height);
		}
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent event) {
		switch (event.getKey()) {
			case LEFT:
				glyphIndex = (glyphIndex + TEST_GLYPHS.length - 1) % TEST_GLYPHS.length;
				glyphElapsed = 0;
				return true;
			case RIGHT:
			case OK:
				glyphIndex = (glyphIndex + 1) % TEST_GLYPHS.length;
				glyphElapsed = 0;
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public String getSessionTitle() {
		return "UI / GPU Demo";
	}
}
