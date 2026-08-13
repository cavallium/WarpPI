package it.cavallium.warppi.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.nogui.NoGuiEngine;

class DisplayManagerBrightnessCapabilityTest {

	@Test
	void ignoresBrightnessCommandsWhenTheDisplayHasNoBacklight() {
		final int[] brightnessWrites = {0};
		final BacklightOutputDevice unsupportedBacklight = new BacklightOutputDevice() {
			@Override
			public void setBrightness(final double value) {
				brightnessWrites[0]++;
			}

			@Override
			public void setPower(final boolean value) {
			}
		};
		final DisplayManager manager = manager(unsupportedBacklight);

		assertFalse(manager.supportsBrightnessControl());
		manager.setBrightness(0.5f);
		manager.changeBrightness(0.1f);
		manager.cycleBrightness(false);
		manager.cycleBrightness(true);

		assertEquals(0, brightnessWrites[0]);
		assertEquals(0f, manager.getBrightness());
	}

	private static DisplayManager manager(final BacklightOutputDevice backlight) {
		final GraphicEngine engine = new NoGuiEngine();
		final DisplayOutputDevice display = new DisplayOutputDevice() {
			private final int[] size = {320, 240};

			@Override
			public GraphicEngine getGraphicEngine() {
				return engine;
			}

			@Override
			public int[] getDisplaySize() {
				return size;
			}
		};
		final Semaphore semaphore = new Semaphore() {
			@Override
			public void release() {
			}

			@Override
			public void acquire() {
			}
		};
		return new DisplayManager(display, backlight, new NoOpHud(), null,
				"test", semaphore);
	}

	private static final class NoOpHud extends HUD {
		@Override
		public void created() {
		}

		@Override
		public void graphicInitialized() {
		}

		@Override
		public void initialized() {
		}

		@Override
		public void renderBackground() {
		}

		@Override
		public void render(final RenderContext ctx) {
		}

		@Override
		public void renderTopmostBackground() {
		}

		@Override
		public void renderTopmost(final RenderContext ctx) {
		}

		@Override
		public void beforeRender(final ScreenContext ctx, final float dt) {
		}

		@Override
		public int getMarginLeft() {
			return 0;
		}

		@Override
		public int getMarginTop() {
			return 0;
		}

		@Override
		public int getMarginRight() {
			return 0;
		}

		@Override
		public int getMarginBottom() {
			return 0;
		}
	}
}
