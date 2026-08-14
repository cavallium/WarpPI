package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.Skin;

class UIDemoScreenTest {

	@Test
	void computesStableRollingFrameStatistics() {
		final UIDemoScreen.FrameStats stats = UIDemoScreen.computeFrameStats(
				new float[] { 10f, 10f, 40f, 999f }, 3);

		assertEquals(3, stats.samples());
		assertEquals(50d, stats.fps(), 0.0001d);
		assertEquals(20d, stats.averageMillis(), 0.0001d);
		assertEquals(10d, stats.minimumMillis(), 0.0001d);
		assertEquals(40d, stats.maximumMillis(), 0.0001d);
		assertEquals(100d / 3d, stats.jankPercent(), 0.0001d);
	}

	@Test
	void exposesPredictableBatchStressLevels() {
		assertEquals(40, UIDemoScreen.stressQuadCount(0));
		assertEquals(96, UIDemoScreen.stressQuadCount(1));
		assertEquals(192, UIDemoScreen.stressQuadCount(2));
		assertEquals(480, UIDemoScreen.stressQuadCount(3));
		assertThrows(IllegalArgumentException.class, () -> UIDemoScreen.stressQuadCount(-1));
		assertThrows(IllegalArgumentException.class, () -> UIDemoScreen.stressQuadCount(4));
	}

	@Test
	void wrapsDiagnosticPagesInBothDirections() {
		assertEquals(UIDemoScreen.Page.PRIMITIVES, UIDemoScreen.Page.OVERVIEW.next());
		assertEquals(UIDemoScreen.Page.FONT_GPUTEST12, UIDemoScreen.Page.OVERVIEW.previous());
		assertEquals(UIDemoScreen.Page.OVERVIEW, UIDemoScreen.Page.FONT_GPUTEST12.next());
	}

	@Test
	void keepsEachHistoricalGpuTestOnItsOwnPage() {
		assertArrayEquals(new UIDemoScreen.Page[] {
				UIDemoScreen.Page.OVERVIEW,
				UIDemoScreen.Page.PRIMITIVES,
				UIDemoScreen.Page.TEXTURES_FONTS,
				UIDemoScreen.Page.STRESS,
				UIDemoScreen.Page.FONT_GPUTEST1,
				UIDemoScreen.Page.FONT_GPUTEST2,
				UIDemoScreen.Page.FONT_GPUTEST3,
				UIDemoScreen.Page.FONT_GPUTEST12
		}, UIDemoScreen.Page.values());
		assertEquals(UIDemoScreen.Page.FONT_GPUTEST2, UIDemoScreen.Page.FONT_GPUTEST1.next());
		assertEquals(UIDemoScreen.Page.FONT_GPUTEST3, UIDemoScreen.Page.FONT_GPUTEST2.next());
		assertEquals(UIDemoScreen.Page.FONT_GPUTEST12, UIDemoScreen.Page.FONT_GPUTEST3.next());
	}

	@Test
	void preservesHistoricalGpuAssetDimensionsAndCadence() {
		assertEquals(256, UIDemoScreen.GPUTEST1_GLYPH_SIZE);
		assertEquals(32, UIDemoScreen.GPUTEST2_GLYPH_SIZE);
		assertEquals(224, UIDemoScreen.GPUTEST3_FRAME_WIDTH);
		assertEquals(128, UIDemoScreen.GPUTEST3_FRAME_HEIGHT);
		assertEquals(512, UIDemoScreen.GPUTEST12_GLYPH_SIZE);
		assertEquals(21, UIDemoScreen.SPRITE_FRAME_COUNT);
		assertEquals(0.04f, UIDemoScreen.SPRITE_FRAME_SECONDS);
		assertEquals(1.5f, UIDemoScreen.GLYPH_SECONDS);
	}

	@Test
	void addressesBothTheOriginalSpriteStripAndTheRaspberryTiledAtlas() {
		assertEquals(4_480, UIDemoScreen.spriteSourceX(20, 4_928));
		assertEquals(0, UIDemoScreen.spriteSourceY(20, 4_928));
		assertEquals(1_792, UIDemoScreen.spriteSourceX(8, 2_048));
		assertEquals(0, UIDemoScreen.spriteSourceY(8, 2_048));
		assertEquals(0, UIDemoScreen.spriteSourceX(9, 2_048));
		assertEquals(128, UIDemoScreen.spriteSourceY(9, 2_048));
		assertEquals(448, UIDemoScreen.spriteSourceX(20, 2_048));
		assertEquals(256, UIDemoScreen.spriteSourceY(20, 2_048));
	}

	@Test
	void disablesOptionalSkinWhenDeferredInitializationFails() {
		final Skin missingOptionalSkin = new Skin() {
			@Override
			public void load(final String file) {}

			@Override
			public void initialize(final DisplayOutputDevice display) {
				throw new IllegalStateException("missing optional texture");
			}

			@Override
			public boolean isInitialized() {
				return false;
			}

			@Override
			public void use(final DisplayOutputDevice display) {}

			@Override
			public int getSkinWidth() {
				return 0;
			}

			@Override
			public int getSkinHeight() {
				return 0;
			}
		};

		assertNull(UIDemoScreen.initializeOptionalSkin(missingOptionalSkin, null));
	}
}
