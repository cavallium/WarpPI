package it.cavallium.warppi.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.event.TouchStartEvent;
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

	@Test
	void keepsTheStatusBarBackgroundWhilePaintingTheNewScreenViewportImmediately() {
		final RecordingRenderer renderer = new RecordingRenderer();
		final RecordingEngine engine = new RecordingEngine(renderer);
		final TestScreen oldScreen = new TestScreen(0xFF102030, "old", renderer);
		final TestScreen newScreen = new TestScreen(0xFFF1EFE5, "new", renderer);
		final DisplayManager manager = manager(engine, oldScreen, true);
		manager.renderer = renderer;

		manager.setScreen(oldScreen);
		manager.refresh(true);
		renderer.events.clear();

		manager.setScreen(newScreen);
		manager.refresh(true);

		assertEquals(List.of(
				"clear:ffabcdef",
				"fill:fff1efe5:0,20,320,220",
				"hud:background",
				"render:new",
				"hud:render",
				"hud:top-background",
				"hud:top"), renderer.events);
	}

	@Test
	void suppressesTouchEventsWhenTheActiveScreenBlocksUserInput() {
		final RecordingRenderer renderer = new RecordingRenderer();
		final RecordingEngine engine = new RecordingEngine(renderer);
		final TestScreen screen = new TestScreen(0xFF102030, "blocked", renderer);
		screen.userInputAllowed = false;
		final DisplayManager manager = manager(engine, screen);
		manager.renderer = renderer;
		manager.setScreen(screen);
		manager.refresh(true);

		manager.getTouchEventListener().accept(new TouchStartEvent(List.of(), List.of()));
		assertEquals(0, screen.touchStarts);

		screen.userInputAllowed = true;
		manager.getTouchEventListener().accept(new TouchStartEvent(List.of(), List.of()));
		assertEquals(1, screen.touchStarts);
	}

	private static DisplayManager manager(final GraphicEngine engine, final Screen initialScreen) {
		return manager(engine, initialScreen, false);
	}

	private static DisplayManager manager(final GraphicEngine engine, final Screen initialScreen,
			final boolean hudVisible) {
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
		final RecordingHud hud = new RecordingHud(((RecordingEngine) engine).renderer.events);
		hud.visible = hudVisible;
		final DisplayManager manager = new DisplayManager(display, backlight, hud,
				initialScreen, "test", semaphore);
		manager.fonts = new BinaryFont[1];
		return manager;
	}

	private static final class RecordingHud extends HUD {
		private final List<String> events;

		private RecordingHud(final List<String> events) {
			this.events = events;
		}

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
			events.add("hud:background");
		}

		@Override
		public void render(final RenderContext ctx) {
			events.add("hud:render");
		}

		@Override
		public void renderTopmostBackground() {
			events.add("hud:top-background");
		}

		@Override
		public int getBackgroundColor() {
			return 0xFFABCDEF;
		}

		@Override
		public void renderTopmost(final RenderContext ctx) {
			events.add("hud:top");
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
			return 20;
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

	private static final class TestScreen extends Screen {
		private final int backgroundColor;
		private final String name;
		private final RecordingRenderer renderer;
		private Runnable onGraphicInitialization = () -> {};
		private boolean userInputAllowed = true;
		private int touchStarts;

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
		public boolean allowsUserInput() {
			return userInputAllowed;
		}

		@Override
		public boolean onTouchStart(final TouchStartEvent event) {
			touchStarts++;
			return true;
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
		private int color;

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
		public void glColor(final int color) {
			this.color = color;
		}

		@Override
		public void glFillColor(final float x, final float y,
				final float width, final float height) {
			events.add("fill:" + Integer.toHexString(color) + ":"
					+ Math.round(x) + "," + Math.round(y) + ","
					+ Math.round(width) + "," + Math.round(height));
		}

		@Override
		public int glGetClearColor() {
			return clearColor;
		}
	}
}
