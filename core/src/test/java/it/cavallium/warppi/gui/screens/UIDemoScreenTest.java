package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.Skin;

class UIDemoScreenTest {

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
