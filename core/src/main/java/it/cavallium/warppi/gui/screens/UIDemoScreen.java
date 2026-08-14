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
import it.cavallium.warppi.gui.graphicengine.RendererFrameDiagnostics;
import it.cavallium.warppi.gui.graphicengine.Skin;

/**
 * Interactive visual and renderer diagnostic. The pages isolate frame pacing,
 * primitives/clipping, texture/font resources, and batch throughput so a visual
 * defect can be narrowed to a specific part of the rendering path.
 */
public final class UIDemoScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0xFF131A22;
	private static final int HEADER_COLOR = 0xFF202B36;
	private static final int HEADER_TEXT_COLOR = 0xFFF4F7FA;
	private static final int PANEL_COLOR = 0xFF253340;
	private static final int PANEL_ALT_COLOR = 0xFF2C3C4B;
	private static final int BORDER_COLOR = 0xFF516779;
	private static final int TEXT_COLOR = 0xFFE6EEF5;
	private static final int MUTED_TEXT_COLOR = 0xFF9FB2C2;
	private static final int ACCENT_COLOR = 0xFF63C7FF;
	private static final int OK_COLOR = 0xFF63D58B;
	private static final int WARNING_COLOR = 0xFFFFC857;
	private static final int ERROR_COLOR = 0xFFFF726D;
	private static final int HEADER_HEIGHT = 31;
	private static final int FOOTER_HEIGHT = 14;
	private static final int FRAME_SAMPLE_COUNT = 120;

	private static final String[] TEST_GLYPHS = {
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
			"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "ò"
	};
	private static final int[] LEGACY_FONT_PALETTE = {
			0xFF000000, 0xFF800000, 0xFFEEA28E, 0xFFEE7255,
			0xFFEAC0B0, 0xFFF3D8CE, 0xFFFFEDE7
	};
	private static final int[] COLOR_BARS = {
			0xFFE74C3C, 0xFFF39C12, 0xFFF1C40F, 0xFF2ECC71,
			0xFF1ABC9C, 0xFF3498DB, 0xFF9B59B6, 0xFFECF0F1
	};
	private static final int[] STRESS_COLUMNS = { 8, 12, 16, 24 };
	private static final int[] STRESS_ROWS = { 5, 8, 12, 20 };
	static final int GPUTEST1_GLYPH_SIZE = 256;
	static final int GPUTEST2_GLYPH_SIZE = 32;
	static final int GPUTEST3_FRAME_WIDTH = 224;
	static final int GPUTEST3_FRAME_HEIGHT = 128;
	static final int GPUTEST12_GLYPH_SIZE = 512;
	static final int SPRITE_FRAME_COUNT = 21;
	static final float SPRITE_FRAME_SECONDS = 0.04f;
	static final float GLYPH_SECONDS = 1.5f;

	private final float[] frameMillis = new float[FRAME_SAMPLE_COUNT];
	private int frameSampleCursor;
	private int frameSamples;
	private Page page = Page.OVERVIEW;
	private int stressLevel = 1;
	private boolean paused;
	private boolean mustRefresh = true;
	private float animationSeconds;
	private int spriteFrame;
	private float spriteElapsed;
	private int glyphIndex;
	private float glyphElapsed;

	private BinaryFont gpuTest1Font;
	private BinaryFont gpuTest2Font;
	private Skin gpuTest3Skin;
	private BinaryFont gpuTest12Font;

	public UIDemoScreen() {
		historyBehavior = HistoryBehavior.DONT_KEEP_IN_HISTORY;
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
		gpuTest1Font = initializeOptionalFont(loadOptionalFont(engine, "gputest", "gputest1"), d.display);
		gpuTest2Font = initializeOptionalFont(loadOptionalFont(engine, "gputest", "gputest2"), d.display);
		gpuTest12Font = initializeOptionalFont(loadOptionalFont(engine, "gputest", "gputest12"), d.display);
		gpuTest3Skin = loadOptionalSkin(engine, "gputest/font_gputest3.png");
		if (gpuTest3Skin == null)
			gpuTest3Skin = loadOptionalSkin(engine, "font_gputest3.png");
		gpuTest3Skin = initializeOptionalSkin(gpuTest3Skin, d.display);
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
		if (dt > 0f && dt <= 0.1f && Float.isFinite(dt))
			recordFrameMillis(dt * 1_000f);
		if (paused)
			return;

		final float elapsed = dt > 0f && Float.isFinite(dt) ? Math.min(dt, 0.1f) : 0f;
		animationSeconds = (animationSeconds + elapsed) % 60f;
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

	private void recordFrameMillis(final float millis) {
		frameMillis[frameSampleCursor] = millis;
		frameSampleCursor = (frameSampleCursor + 1) % frameMillis.length;
		if (frameSamples < frameMillis.length)
			frameSamples++;
	}

	@Override
	public void render(final RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		renderer.glClearSkin();
		useDefaultFont();

		switch (page) {
			case OVERVIEW -> drawOverview(renderer, ctx.getWidth(), ctx.getHeight());
			case PRIMITIVES -> drawPrimitives(renderer, ctx.getWidth(), ctx.getHeight());
			case TEXTURES_FONTS -> drawTexturesAndFonts(renderer, ctx.getWidth(), ctx.getHeight());
			case STRESS -> drawStress(renderer, ctx.getWidth(), ctx.getHeight());
			case FONT_GPUTEST1 -> drawLargeFontGpuTest(renderer, ctx.getWidth(), ctx.getHeight(),
					gpuTest1Font, "FONT_GPUTEST1", GPUTEST1_GLYPH_SIZE);
			case FONT_GPUTEST2 -> drawPaletteFontGpuTest(renderer, ctx.getWidth(), ctx.getHeight());
			case FONT_GPUTEST3 -> drawSpriteGpuTest(renderer, ctx.getWidth(), ctx.getHeight());
			case FONT_GPUTEST12 -> drawLargeFontGpuTest(renderer, ctx.getWidth(), ctx.getHeight(),
					gpuTest12Font, "FONT_GPUTEST12", GPUTEST12_GLYPH_SIZE);
		}

		renderer.glClearSkin();
		useDefaultFont();
		drawHeader(renderer, ctx.getWidth());
		renderer.glClearSkin();
		useDefaultFont();
		drawFooter(renderer, ctx.getWidth(), ctx.getHeight());
		mustRefresh = false;
	}

	private void drawHeader(final Renderer renderer, final int width) {
		renderer.glColor(HEADER_COLOR);
		renderer.glFillColor(0, 0, width, HEADER_HEIGHT);
		renderer.glColor(HEADER_TEXT_COLOR);
		renderer.glDrawStringLeft(4, 2, "UI / GPU DIAGNOSTIC");
		renderer.glColor(paused ? WARNING_COLOR : OK_COLOR);
		renderer.glDrawStringRight(width - 4, 2,
				(paused ? "PAUSED" : "LIVE") + "  " + (page.ordinal() + 1) + "/" + Page.values().length);
		renderer.glColor(ACCENT_COLOR);
		renderer.glFillColor(1, 16, width - 2, 14);
		renderer.glColor(0xFF10202C);
		renderer.glDrawStringCenter(width / 2f, 18, "<  " + page.label + "  >");
	}

	private void drawOverview(final Renderer renderer, final int width, final int height) {
		final int top = HEADER_HEIGHT + 5;
		final int gap = 6;
		final int panelWidth = (width - gap * 3) / 2;
		final int panelHeight = Math.min(128, Math.max(96, height / 2 - 20));
		final FrameStats stats = computeFrameStats(frameMillis, frameSamples);
		final RendererFrameDiagnostics diagnostics = renderer.getFrameDiagnostics();

		drawPanel(renderer, gap, top, panelWidth, panelHeight, "FRAME PACING");
		int y = top + 20;
		drawValue(renderer, gap + 7, y, "FPS", stats.samples() == 0 ? "--" : integer(stats.fps()), OK_COLOR);
		y += 14;
		drawValue(renderer, gap + 7, y, "AVG", stats.samples() == 0 ? "--" : tenths(stats.averageMillis()) + " ms", TEXT_COLOR);
		y += 14;
		drawValue(renderer, gap + 7, y, "MIN", stats.samples() == 0 ? "--" : tenths(stats.minimumMillis()) + " ms", TEXT_COLOR);
		y += 14;
		drawValue(renderer, gap + 7, y, "MAX", stats.samples() == 0 ? "--" : tenths(stats.maximumMillis()) + " ms",
				stats.maximumMillis() > 33.4 ? ERROR_COLOR : TEXT_COLOR);
		y += 14;
		drawValue(renderer, gap + 7, y, "JANK", stats.samples() == 0 ? "--" : integer(stats.jankPercent()) + "%",
				stats.jankPercent() > 10 ? WARNING_COLOR : TEXT_COLOR);

		final int rightX = gap * 2 + panelWidth;
		drawPanel(renderer, rightX, top, panelWidth, panelHeight, "RENDERER / PREVIOUS FRAME");
		y = top + 20;
		renderer.glColor(diagnostics.available() ? OK_COLOR : WARNING_COLOR);
		renderer.glDrawStringLeft(rightX + 7, y,
				diagnostics.available() ? diagnostics.backend() : "COUNTERS UNAVAILABLE");
		y += 14;
		if (diagnostics.available()) {
			drawValue(renderer, rightX + 7, y, "QUADS", Integer.toString(diagnostics.quads()), TEXT_COLOR);
			y += 14;
			drawValue(renderer, rightX + 7, y, "BATCHES", Integer.toString(diagnostics.batches()), TEXT_COLOR);
			y += 14;
			drawValue(renderer, rightX + 7, y, "TEX SW", Integer.toString(diagnostics.textureSwitches()), TEXT_COLOR);
			y += 14;
			drawValue(renderer, rightX + 7, y, "UPLOAD", kibibytes(diagnostics.uploadedBytes()), TEXT_COLOR);
			y += 14;
			drawValue(renderer, rightX + 7, y, "CPU SUBMIT", tenths(diagnostics.cpuSubmissionMillis()) + " ms", TEXT_COLOR);
		}

		final int graphTop = top + panelHeight + gap;
		final int graphHeight = height - FOOTER_HEIGHT - graphTop - 4;
		drawPanel(renderer, gap, graphTop, width - gap * 2, graphHeight, "ROLLING FRAME TIME / 120 SAMPLES");
		drawFrameGraph(renderer, gap + 5, graphTop + 18, width - gap * 2 - 10, graphHeight - 23, stats);
	}

	private void drawFrameGraph(final Renderer renderer, final int x, final int y, final int width,
			final int height, final FrameStats stats) {
		if (frameSamples == 0 || width <= 0 || height <= 0)
			return;
		final float scaleMax = (float) Math.max(33.4, stats.maximumMillis());
		final int samples = Math.min(frameSamples, width / 2);
		final int first = (frameSampleCursor - samples + frameMillis.length) % frameMillis.length;
		for (int index = 0; index < samples; index++) {
			final float millis = frameMillis[(first + index) % frameMillis.length];
			final int barHeight = Math.max(1, Math.min(height, Math.round(millis / scaleMax * height)));
			renderer.glColor(millis > 33.4f ? ERROR_COLOR : millis > 20f ? WARNING_COLOR : ACCENT_COLOR);
			renderer.glFillColor(x + index * 2, y + height - barHeight, 1, barHeight);
		}
	}

	private void drawPrimitives(final Renderer renderer, final int width, final int height) {
		final int contentTop = HEADER_HEIGHT + 5;
		final int contentBottom = height - FOOTER_HEIGHT - 4;
		drawPanel(renderer, 6, contentTop, width - 12, 43, "COLOR / PIXEL ALIGNMENT");
		drawColorBars(renderer, 12, contentTop + 19, Math.max(1, (width - 24) / COLOR_BARS.length), 17);

		final int top = contentTop + 49;
		final int gap = 6;
		final int panelWidth = (width - gap * 4) / 3;
		final int panelHeight = contentBottom - top;
		drawPanel(renderer, gap, top, panelWidth, panelHeight, "ALPHA BLEND");
		drawAlphaTest(renderer, gap + 8, top + 22, panelWidth - 16, panelHeight - 28);

		final int clipX = gap * 2 + panelWidth;
		drawPanel(renderer, clipX, top, panelWidth, panelHeight, "CLIPPED VIEWPORT");
		drawClippingTest(renderer, clipX + 8, top + 22, panelWidth - 16, panelHeight - 30);

		final int motionX = gap * 3 + panelWidth * 2;
		drawPanel(renderer, motionX, top, panelWidth, panelHeight, "MOTION / LINES");
		drawMotionTest(renderer, motionX + 8, top + 22, panelWidth - 16, panelHeight - 30);
	}

	private void drawAlphaTest(final Renderer renderer, final int x, final int y, final int width, final int height) {
		renderer.glColor(0xFF123B57);
		renderer.glFillColor(x, y, width, height);
		final int size = Math.max(20, Math.min(width, height) * 2 / 3);
		renderer.glColor4i(255, 80, 70, 160);
		renderer.glFillColor(x + 5, y + 7, size, size);
		renderer.glColor4i(70, 210, 255, 160);
		renderer.glFillColor(x + width - size - 5, y + height - size - 7, size, size);
	}

	private void drawClippingTest(final Renderer renderer, final int x, final int y, final int width, final int height) {
		renderer.glColor(0xFF17222C);
		renderer.glFillColor(x, y, width, height);
		final Renderer clipped = renderer.getClippedInstance(x + 4, y + 4, width - 8, height - 8);
		clipped.glColor(0xFFEA5A71);
		clipped.glFillColor(-18, 10, width / 2, 28);
		clipped.glColor(0xFF50C9A4);
		clipped.glFillColor(width / 2, height - 34, width, 25);
		clipped.glColor(TEXT_COLOR);
		clipped.glDrawStringLeft(-12, height / 2 - 4, "CLIPPED TEXT WINDOW");
	}

	private void drawMotionTest(final Renderer renderer, final int x, final int y, final int width, final int height) {
		renderer.glColor(0xFF17222C);
		renderer.glFillColor(x, y, width, height);
		final int travel = Math.max(1, width - 20);
		final int movingX = x + Math.round(animationSeconds * 45f) % travel;
		renderer.glColor(ACCENT_COLOR);
		renderer.glFillColor(movingX, y + 8, 18, 9);
		for (int line = 0; line < 7; line++) {
			renderer.glColor(line % 2 == 0 ? OK_COLOR : WARNING_COLOR);
			renderer.glDrawLine(x + 4, y + 28 + line * 12, x + width - 5, y + 28 + line * 12);
		}
	}

	private void drawTexturesAndFonts(final Renderer renderer, final int width, final int height) {
		final int top = HEADER_HEIGHT + 5;
		final int gap = 6;
		final int leftWidth = width * 3 / 5 - gap * 2;
		final int rightX = gap * 2 + leftWidth;
		final int rightWidth = width - rightX - gap;
		final int contentHeight = height - FOOTER_HEIGHT - top - 4;

		drawPanel(renderer, gap, top, leftWidth, 82, "BUILT-IN UI ATLAS / UV TILES");
		drawBuiltInAtlas(renderer, gap + 7, top + 22, leftWidth - 14);
		useDefaultFont();
		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(gap + 7, top + 66,
				d.display.getGraphicEngine().usesProductionTextureAtlases() ? "PACKED PRODUCTION ATLAS" : "PNG / STANDARD ATLAS");

		drawPanel(renderer, gap, top + 88, leftWidth, contentHeight - 88, "FONT RASTERIZATION");
		drawFontSamples(renderer, gap + 8, top + 109, leftWidth - 16, contentHeight - 116);

		drawPanel(renderer, rightX, top, rightWidth, contentHeight, "OPTIONAL GPU ASSETS");
		drawOptionalAssets(renderer, rightX + 7, top + 21, rightWidth - 14, contentHeight - 28);
	}

	private void drawBuiltInAtlas(final Renderer renderer, final int x, final int y, final int width) {
		if (d.guiSkin == null) {
			useDefaultFont();
			renderer.glColor(WARNING_COLOR);
			renderer.glDrawStringLeft(x, y, "UI ATLAS UNAVAILABLE");
			return;
		}
		d.guiSkin.use(d.display);
		final int tileCount = Math.max(1, Math.min(14, width / 20));
		for (int tile = 0; tile < tileCount; tile++) {
			renderer.glColor4i(255, 255, 255, tile % 3 == 0 ? 150 : 255);
			renderer.glFillRect(x + tile * 20, y, 18, 18, tile * 16f, 0, 16, 16);
		}
		renderer.glClearSkin();
	}

	private void drawFontSamples(final Renderer renderer, final int x, final int y, final int width, final int height) {
		int textY = y;
		if (d.fonts != null) {
			for (int fontIndex = 0; fontIndex < Math.min(3, d.fonts.length); fontIndex++) {
				final BinaryFont font = d.fonts[fontIndex];
				if (font == null)
					continue;
				font.use(d.display);
				renderer.glColor(fontIndex == 0 ? TEXT_COLOR : fontIndex == 1 ? ACCENT_COLOR : WARNING_COLOR);
				renderer.glDrawStringLeft(x, textY, "FONT " + fontIndex + "  Aa09 +-*/ ()[]");
				textY += font.getCharacterHeight() + 5;
			}
		}
		useDefaultFont();
		if (textY + 12 < y + height) {
			renderer.glColor(MUTED_TEXT_COLOR);
			renderer.glDrawStringLeft(x, textY, "BASELINE");
			renderer.glColor(BORDER_COLOR);
			renderer.glFillColor(x, textY + 11, width, 1);
		}
	}

	private void drawOptionalAssets(final Renderer renderer, final int x, final int y, final int width, final int height) {
		useDefaultFont();
		drawAssetLine(renderer, x, y, "GPUTEST1", gpuTest1Font, "256x256 GLYPH");
		drawAssetLine(renderer, x, y + 18, "GPUTEST2", gpuTest2Font, "32x32 GLYPH");
		drawAssetLine(renderer, x, y + 36, "GPUTEST3", gpuTest3Skin, "21 x 224x128");
		drawAssetLine(renderer, x, y + 54, "GPUTEST12", gpuTest12Font, "512x512 GLYPH");
		if (height >= 94) {
			renderer.glColor(MUTED_TEXT_COLOR);
			renderer.glDrawStringLeft(x, y + 76, "DEDICATED PAGES RUN");
			renderer.glDrawStringLeft(x, y + 89, "THE ORIGINAL NATIVE TESTS");
		}
	}

	private static void drawAssetLine(final Renderer renderer, final int x, final int y,
			final String name, final Skin asset, final String dimensions) {
		renderer.glColor(asset == null ? WARNING_COLOR : OK_COLOR);
		renderer.glDrawStringLeft(x, y, name + "  " + (asset == null ? "N/A" : "READY"));
		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x + 8, y + 9, dimensions);
	}

	private void drawLargeFontGpuTest(final Renderer renderer, final int width, final int height,
			final BinaryFont font, final String assetName, final int expectedSize) {
		final int canvasY = Math.max(0, height / 2 - expectedSize / 2);
		final int canvasX = width - expectedSize;
		renderer.glColor(0xFFF4F4F1);
		renderer.glFillColor(canvasX, canvasY, expectedSize, expectedSize);

		if (font != null) {
			font.use(d.display);
			renderer.glColor(0xFF090B0D);
			// Keep the old right-aligned native-size path: this deliberately exercises
			// the 256/512-pixel glyph quad and lets the GPU clip it to the viewport.
			renderer.glDrawStringRight(width, canvasY, TEST_GLYPHS[glyphIndex]);
		}

		renderer.glClearSkin();
		useDefaultFont();
		final int infoY = HEADER_HEIGHT + 5;
		renderer.glColor(PANEL_ALT_COLOR);
		renderer.glFillColor(5, infoY, width - 10, 29);
		renderer.glColor(font == null ? WARNING_COLOR : OK_COLOR);
		renderer.glDrawStringLeft(10, infoY + 4,
				assetName + "  " + (font == null ? "ASSET UNAVAILABLE" : "NATIVE "
						+ font.getCharacterWidth() + "x" + font.getCharacterHeight()));
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(10, infoY + 17, "GLYPH " + TEST_GLYPHS[glyphIndex] + "  AUTO 1.5 s");
		drawPreviousFrameCounters(renderer, width - 8, infoY + 17);
	}

	private void drawPaletteFontGpuTest(final Renderer renderer, final int width, final int height) {
		final int top = HEADER_HEIGHT + 5;
		final int gap = 6;
		final int panelWidth = (width - gap * 3) / 2;
		final int panelHeight = height - FOOTER_HEIGHT - top - 4;
		drawPanel(renderer, gap, top, panelWidth, panelHeight, "ORIGINAL SAME-ORIGIN OVERDRAW");
		drawPanel(renderer, gap * 2 + panelWidth, top, panelWidth, panelHeight, "SEPARATED A-G / ORDER CHECK");

		if (gpuTest2Font == null) {
			drawMissingAsset(renderer, width, height, "FONT_GPUTEST2");
			return;
		}

		final int charWidth = gpuTest2Font.getCharacterWidth();
		final int charHeight = gpuTest2Font.getCharacterHeight();
		final int overlapX = gap + (panelWidth - charWidth) / 2;
		final int glyphY = top + 43;
		renderer.glColor(0xFFF7F4ED);
		renderer.glFillColor(overlapX - 12, glyphY - 12, charWidth + 24, charHeight + 24);

		gpuTest2Font.use(d.display);
		for (int index = 0; index < LEGACY_FONT_PALETTE.length; index++) {
			renderer.glColor(LEGACY_FONT_PALETTE[index]);
			renderer.glDrawStringLeft(overlapX, glyphY, String.valueOf((char) ('A' + index)));
		}

		final int sequenceX = gap * 2 + panelWidth + 8;
		final int availableWidth = panelWidth - 16;
		final int step = Math.max(1, availableWidth / LEGACY_FONT_PALETTE.length);
		for (int index = 0; index < LEGACY_FONT_PALETTE.length; index++) {
			renderer.glColor(LEGACY_FONT_PALETTE[index]);
			renderer.glDrawStringCenter(sequenceX + step * index + step / 2f, glyphY,
					String.valueOf((char) ('A' + index)));
		}

		renderer.glClearSkin();
		useDefaultFont();
		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringCenter(gap + panelWidth / 2f, glyphY + charHeight + 18, "A-G SHARE ONE ORIGIN");
		renderer.glDrawStringCenter(gap * 2 + panelWidth + panelWidth / 2f,
				glyphY + charHeight + 18, "7 COLORS / 7 GLYPHS");
		drawPreviousFrameCounters(renderer, width - 8, height - FOOTER_HEIGHT - 17);
	}

	private void drawSpriteGpuTest(final Renderer renderer, final int width, final int height) {
		final int top = HEADER_HEIGHT + 5;
		final int spriteWidth = GPUTEST3_FRAME_WIDTH;
		final int spriteHeight = GPUTEST3_FRAME_HEIGHT;
		final int spriteX = (width - spriteWidth) / 2;
		final int spriteY = top + 27;
		drawCheckerboard(renderer, spriteX, spriteY, spriteWidth, spriteHeight, 16);

		if (gpuTest3Skin != null) {
			gpuTest3Skin.use(d.display);
			renderer.glColor4f(1f, 1f, 1f, 0.7f);
			renderer.glFillRect(spriteX, spriteY, spriteWidth, spriteHeight,
					spriteFrame * spriteWidth, 0, spriteWidth, spriteHeight);

			final int stripX = 6;
			final int stripWidth = width - 12;
			final int thumbnailWidth = Math.max(1, stripWidth / SPRITE_FRAME_COUNT);
			final int thumbnailHeight = Math.max(8, Math.round(thumbnailWidth * 128f / 224f));
			final int stripY = Math.min(height - FOOTER_HEIGHT - thumbnailHeight - 24,
					spriteY + spriteHeight + 12);
			for (int frame = 0; frame < SPRITE_FRAME_COUNT; frame++) {
				renderer.glColor4f(1f, 1f, 1f, frame == spriteFrame ? 1f : 0.45f);
				renderer.glFillRect(stripX + frame * thumbnailWidth, stripY,
						Math.max(1, thumbnailWidth - 1), thumbnailHeight,
						frame * spriteWidth, 0, spriteWidth, spriteHeight);
			}
		}

		renderer.glClearSkin();
		useDefaultFont();
		renderer.glColor(gpuTest3Skin == null ? WARNING_COLOR : OK_COLOR);
		renderer.glDrawStringLeft(7, top + 3,
				"FONT_GPUTEST3  " + (gpuTest3Skin == null ? "ASSET UNAVAILABLE" : "224x128 @ 25 FPS"));
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringRight(width - 7, top + 3,
				"FRAME " + (spriteFrame + 1) + "/" + SPRITE_FRAME_COUNT + "  ALPHA 70%");
		if (gpuTest3Skin == null)
			drawMissingAsset(renderer, width, height, "FONT_GPUTEST3.PNG");
		drawPreviousFrameCounters(renderer, width - 8, top + 16);
	}

	private static void drawCheckerboard(final Renderer renderer, final int x, final int y,
			final int width, final int height, final int cellSize) {
		for (int rowY = 0; rowY < height; rowY += cellSize) {
			for (int columnX = 0; columnX < width; columnX += cellSize) {
				renderer.glColor(((rowY / cellSize + columnX / cellSize) & 1) == 0
						? 0xFF394957 : 0xFF637585);
				renderer.glFillColor(x + columnX, y + rowY,
						Math.min(cellSize, width - columnX), Math.min(cellSize, height - rowY));
			}
		}
	}

	private void drawPreviousFrameCounters(final Renderer renderer, final int rightX, final int y) {
		final RendererFrameDiagnostics diagnostics = renderer.getFrameDiagnostics();
		if (!diagnostics.available())
			return;
		renderer.glColor(ACCENT_COLOR);
		renderer.glDrawStringRight(rightX, y,
				"LAST " + diagnostics.quads() + " Q / " + diagnostics.batches() + " B / "
						+ diagnostics.textureSwitches() + " T");
	}

	private void drawMissingAsset(final Renderer renderer, final int width, final int height, final String assetName) {
		useDefaultFont();
		renderer.glColor(WARNING_COLOR);
		renderer.glDrawStringCenter(width / 2f, height / 2f, assetName + " IS NOT AVAILABLE");
	}

	private void drawStress(final Renderer renderer, final int width, final int height) {
		final RendererFrameDiagnostics diagnostics = renderer.getFrameDiagnostics();
		final int top = HEADER_HEIGHT + 5;
		final int bottom = height - FOOTER_HEIGHT - 4;
		final int columns = STRESS_COLUMNS[stressLevel];
		final int rows = STRESS_ROWS[stressLevel];
		final int expectedQuads = columns * rows;
		useDefaultFont();
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(7, top, "LOAD " + (stressLevel + 1) + "/4  GRID " + columns + "x" + rows
				+ "  TEST QUADS " + expectedQuads);
		if (diagnostics.available()) {
			renderer.glColor(ACCENT_COLOR);
			renderer.glDrawStringRight(width - 7, top, "LAST: " + diagnostics.quads() + " Q / "
					+ diagnostics.batches() + " B / " + kibibytes(diagnostics.uploadedBytes()));
		}

		final int gridX = 6;
		final int gridY = top + 16;
		final int gridWidth = width - 12;
		final int gridHeight = bottom - gridY;
		final float cellWidth = (float) gridWidth / columns;
		final float cellHeight = (float) gridHeight / rows;
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				final int palette = (row * 5 + column * 3) & 7;
				renderer.glColor(COLOR_BARS[palette]);
				renderer.glFillColor(gridX + column * cellWidth, gridY + row * cellHeight,
						Math.max(1, cellWidth - 1), Math.max(1, cellHeight - 1));
			}
		}
		final int scanColumn = Math.round(animationSeconds * 30f) % Math.max(1, gridWidth);
		renderer.glColor4i(255, 255, 255, 110);
		renderer.glFillColor(gridX + scanColumn, gridY, 3, gridHeight);
	}

	private void drawFooter(final Renderer renderer, final int width, final int height) {
		final int y = height - FOOTER_HEIGHT;
		renderer.glColor(HEADER_COLOR);
		renderer.glFillColor(0, y, width, FOOTER_HEIGHT);
		renderer.glColor(HEADER_TEXT_COLOR);
		renderer.glDrawStringLeft(4, y + 2, "LEFT/RIGHT: PAGE");
		renderer.glDrawStringCenter(width / 2f, y + 2, page.controlHint);
		renderer.glDrawStringRight(width - 4, y + 2, "F1: RESET  BACK: EXIT");
	}

	private void useDefaultFont() {
		if (d.fonts != null && d.fonts.length > 0 && d.fonts[0] != null)
			d.fonts[0].use(d.display);
	}

	private static void drawColorBars(final Renderer renderer, final int x, final int y,
			final int width, final int height) {
		for (int index = 0; index < COLOR_BARS.length; index++) {
			renderer.glColor(COLOR_BARS[index]);
			renderer.glFillColor(x + index * width, y, width, height);
		}
	}

	private static void drawPanel(final Renderer renderer, final int x, final int y, final int width,
			final int height, final String title) {
		if (width <= 0 || height <= 0)
			return;
		renderer.glColor(PANEL_COLOR);
		renderer.glFillColor(x, y, width, height);
		renderer.glColor(BORDER_COLOR);
		drawBorder(renderer, x, y, width, height);
		renderer.glColor(PANEL_ALT_COLOR);
		renderer.glFillColor(x + 1, y + 1, width - 2, 15);
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, y + 3, title);
	}

	private static void drawValue(final Renderer renderer, final int x, final int y, final String label,
			final String value, final int valueColor) {
		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x, y, label);
		renderer.glColor(valueColor);
		renderer.glDrawStringRight(x + 190, y, value);
	}

	private static void drawBorder(final Renderer renderer, final int x, final int y, final int width,
			final int height) {
		renderer.glDrawLine(x, y, x + width, y);
		renderer.glDrawLine(x + width, y, x + width, y + height);
		renderer.glDrawLine(x + width, y + height, x, y + height);
		renderer.glDrawLine(x, y + height, x, y);
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent event) {
		switch (event.getKey()) {
			case BACK, SETTINGS -> d.closeScreen();
			case LEFT -> page = page.previous();
			case RIGHT -> page = page.next();
			case UP -> adjustSelection(1);
			case DOWN -> adjustSelection(-1);
			case OK -> paused = !paused;
			case F1 -> resetFrameSamples();
			default -> {
				return true;
			}
		}
		mustRefresh = true;
		return true;
	}

	private void adjustSelection(final int direction) {
		switch (page) {
			case STRESS -> stressLevel = Math.max(0,
					Math.min(STRESS_COLUMNS.length - 1, stressLevel + direction));
			case FONT_GPUTEST1, FONT_GPUTEST12 -> {
				glyphIndex = Math.floorMod(glyphIndex + direction, TEST_GLYPHS.length);
				glyphElapsed = 0;
			}
			case FONT_GPUTEST3 -> {
				spriteFrame = Math.floorMod(spriteFrame + direction, SPRITE_FRAME_COUNT);
				spriteElapsed = 0;
			}
			default -> { }
		}
	}

	private void resetFrameSamples() {
		frameSampleCursor = 0;
		frameSamples = 0;
		for (int index = 0; index < frameMillis.length; index++)
			frameMillis[index] = 0;
		animationSeconds = 0;
		spriteFrame = 0;
		spriteElapsed = 0;
		glyphIndex = 0;
		glyphElapsed = 0;
		stressLevel = 1;
	}

	@Override
	public boolean mustBeRefreshed() {
		return !paused || mustRefresh;
	}

	@Override
	public String getSessionTitle() {
		return "UI / GPU diagnostic";
	}

	static int stressQuadCount(final int level) {
		if (level < 0 || level >= STRESS_COLUMNS.length)
			throw new IllegalArgumentException("Stress level must be between 0 and 3");
		return STRESS_COLUMNS[level] * STRESS_ROWS[level];
	}

	static FrameStats computeFrameStats(final float[] samples, final int sampleCount) {
		if (samples == null)
			throw new NullPointerException("samples");
		final int count = Math.max(0, Math.min(sampleCount, samples.length));
		if (count == 0)
			return new FrameStats(0, 0, 0, 0, 0, 0);
		double sum = 0;
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = 0;
		for (int index = 0; index < count; index++) {
			final double sample = Math.max(0, samples[index]);
			sum += sample;
			minimum = Math.min(minimum, sample);
			maximum = Math.max(maximum, sample);
		}
		final double average = sum / count;
		int jank = 0;
		final double jankThreshold = Math.max(20, average * 1.5);
		for (int index = 0; index < count; index++)
			if (samples[index] > jankThreshold)
				jank++;
		return new FrameStats(count, average == 0 ? 0 : 1_000d / average,
				average, minimum, maximum, jank * 100d / count);
	}

	private static String tenths(final double value) {
		final long tenths = Math.round(Math.max(0, value) * 10d);
		return tenths / 10 + "." + tenths % 10;
	}

	private static String integer(final double value) {
		return Long.toString(Math.round(Math.max(0, value)));
	}

	private static String kibibytes(final int bytes) {
		return tenths(bytes / 1024d) + " KiB";
	}

	enum Page {
		OVERVIEW("OVERVIEW", "OK: PAUSE"),
		PRIMITIVES("PRIMITIVES", "OK: PAUSE"),
		TEXTURES_FONTS("TEXTURES / FONTS", "OK: PAUSE"),
		STRESS("STRESS", "UP/DOWN: LOAD  OK: PAUSE"),
		FONT_GPUTEST1("FONT_GPUTEST1", "UP/DOWN: GLYPH  OK: PAUSE"),
		FONT_GPUTEST2("FONT_GPUTEST2", "OK: PAUSE"),
		FONT_GPUTEST3("FONT_GPUTEST3", "UP/DOWN: FRAME  OK: PAUSE"),
		FONT_GPUTEST12("FONT_GPUTEST12", "UP/DOWN: GLYPH  OK: PAUSE");

		private final String label;
		private final String controlHint;

		Page(final String label, final String controlHint) {
			this.label = label;
			this.controlHint = controlHint;
		}

		Page next() {
			return values()[(ordinal() + 1) % values().length];
		}

		Page previous() {
			return values()[(ordinal() + values().length - 1) % values().length];
		}
	}

	record FrameStats(int samples, double fps, double averageMillis, double minimumMillis,
			double maximumMillis, double jankPercent) {}
}
