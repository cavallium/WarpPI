package it.cavallium.warppi.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.impl.common.ReboundedRenderer;
import it.cavallium.warppi.gui.graphicengine.impl.nogui.NoGuiEngine;
import it.cavallium.warppi.gui.screens.Screen;

class DisplayManagerScreenTransitionTest {

	@Test
	void clearsWithTheNewScreensBackgroundBeforeRenderingItsUi() {
		final RecordingRenderer renderer = new RecordingRenderer();
		final RecordingEngine engine = new RecordingEngine(renderer);
		final TestScreen oldScreen = new TestScreen(0xFF102030, "old", renderer);
		final TestScreen newScreen = new TestScreen(0xFFF1EFE5, "new", renderer);
		final DisplayManager manager = manager(engine, oldScreen);
		manager.renderer = renderer;

		manager.setScreen(oldScreen);
		manager.refresh(true);
		renderer.events.clear();

		manager.setScreen(newScreen);
		manager.refresh(true);

		assertEquals(List.of("clear:fff1efe5", "render:new"), renderer.events);
	}

	@Test
	void finishesOneCoherentFrameWhenTheScreenChangesDuringGraphicInitialization() {
		final RecordingRenderer renderer = new RecordingRenderer();
		final RecordingEngine engine = new RecordingEngine(renderer);
		final TestScreen replacement = new TestScreen(0xFFF1EFE5, "replacement", renderer);
		final TestScreen transitioning = new TestScreen(0xFF18202A, "transitioning", renderer);
		final DisplayManager manager = manager(engine, transitioning);
		manager.renderer = renderer;
		transitioning.onGraphicInitialization = () -> manager.setScreen(replacement);

		manager.setScreen(transitioning);
		manager.refresh(true);

		assertEquals(List.of("clear:ff18202a", "render:transitioning"), renderer.events);
		renderer.events.clear();
		manager.refresh(true);
		assertEquals(List.of("clear:fff1efe5", "render:replacement"), renderer.events);
	}

	private static DisplayManager manager(final GraphicEngine engine, final Screen initialScreen) {
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
		final BacklightOutputDevice backlight = new BacklightOutputDevice() {
			@Override
			public void setBrightness(final double value) {
			}

			@Override
			public void setPower(final boolean value) {
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
		final CalculatorHUD hud = new CalculatorHUD();
		hud.visible = false;
		final DisplayManager manager = new DisplayManager(display, backlight, hud,
				initialScreen, "test", semaphore);
		manager.fonts = new BinaryFont[1];
		return manager;
	}

	private static final class TestScreen extends Screen {
		private final int backgroundColor;
		private final String name;
		private final RecordingRenderer renderer;
		private Runnable onGraphicInitialization = () -> {};

		private TestScreen(final int backgroundColor, final String name,
				final RecordingRenderer renderer) {
			this.backgroundColor = backgroundColor;
			this.name = name;
			this.renderer = renderer;
		}

		@Override
		public void created() {
		}

		@Override
		public void initialized() {
		}

		@Override
		public void graphicInitialized(final ScreenContext ctx) {
			onGraphicInitialization.run();
		}

		@Override
		public int getBackgroundColor() {
			return backgroundColor;
		}

		@Override
		public void render(final RenderContext ctx) {
			renderer.events.add("render:" + name);
		}

		@Override
		public void beforeRender(final ScreenContext ctx, final float dt) {
		}

		@Override
		public boolean mustBeRefreshed() {
			return true;
		}

		@Override
		public String getSessionTitle() {
			return name;
		}
	}

	private static final class RecordingEngine extends NoGuiEngine {
		private final RecordingRenderer renderer;

		private RecordingEngine(final RecordingRenderer renderer) {
			this.renderer = renderer;
		}

		@Override
		public int getWidth() {
			return 320;
		}

		@Override
		public int getHeight() {
			return 240;
		}

		@Override
		public Renderer getRenderer() {
			return renderer;
		}
	}

	private static final class RecordingRenderer extends ReboundedRenderer {
		private final List<String> events = new ArrayList<>();
		private int clearColor;

		private RecordingRenderer() {
			super(new NoGuiEngine().getRenderer(), 0, 0, 320, 240);
		}

		@Override
		public void glClearColor(final int color) {
			clearColor = color;
		}

		@Override
		public void glClear(final int width, final int height) {
			events.add("clear:" + Integer.toHexString(clearColor));
		}

		@Override
		public int glGetClearColor() {
			return clearColor;
		}
	}
}
