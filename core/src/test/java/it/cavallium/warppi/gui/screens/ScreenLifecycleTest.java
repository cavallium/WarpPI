package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;

class ScreenLifecycleTest {

	@Test
	void publishesLifecycleStagesOnlyAfterTheirCallbacksComplete() throws InterruptedException {
		final RecordingScreen screen = new RecordingScreen();

		screen.create();
		assertTrue(screen.created);
		assertFalse(screen.isReadyForInput());

		screen.initialize();
		assertTrue(screen.initialized);
		assertFalse(screen.isReadyForInput());

		screen.initializeGraphic(null);
		assertTrue(screen.graphicInitialized);
		assertTrue(screen.isReadyForInput());

		screen.create();
		screen.initialize();
		screen.initializeGraphic(null);
		assertEquals(1, screen.createdCalls);
		assertEquals(1, screen.initializedCalls);
		assertEquals(1, screen.graphicInitializedCalls);
	}

	@Test
	void doesNotPublishFailedGraphicInitialization() throws InterruptedException {
		final RecordingScreen screen = new RecordingScreen();
		screen.create();
		screen.initialize();
		screen.failGraphicInitialization = true;

		assertThrows(IllegalStateException.class, () -> screen.initializeGraphic(null));
		assertFalse(screen.graphicInitialized);
		assertFalse(screen.isReadyForInput());
	}

	private static final class RecordingScreen extends Screen {
		private int createdCalls;
		private int initializedCalls;
		private int graphicInitializedCalls;
		private boolean failGraphicInitialization;

		@Override
		public void created() {
			assertFalse(created);
			createdCalls++;
		}

		@Override
		public void initialized() {
			assertFalse(initialized);
			initializedCalls++;
		}

		@Override
		public void graphicInitialized(final ScreenContext ctx) {
			assertFalse(graphicInitialized);
			graphicInitializedCalls++;
			if (failGraphicInitialization)
				throw new IllegalStateException("graphic initialization failed");
		}

		@Override
		public void render(final RenderContext ctx) {
		}

		@Override
		public void beforeRender(final ScreenContext ctx, final float dt) {
		}

		@Override
		public boolean mustBeRefreshed() {
			return false;
		}

		@Override
		public String getSessionTitle() {
			return "test";
		}
	}
}
