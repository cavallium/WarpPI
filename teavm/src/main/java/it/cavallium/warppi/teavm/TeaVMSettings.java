package it.cavallium.warppi.teavm;

import java.io.IOException;

import org.teavm.jso.browser.Storage;

import it.cavallium.warppi.Platform.Settings;
import it.cavallium.warppi.settings.PersistentSettings;
import it.cavallium.warppi.settings.SettingsStore;

public final class TeaVMSettings implements Settings {
	private static final String STORAGE_PREFIX = "warppi.settings.";

	private final PersistentSettings settings;

	public TeaVMSettings() {
		settings = new PersistentSettings(new LocalStorageSettingsStore(), false);
	}

	@Override
	public boolean isDebugEnabled() {
		return settings.isDebugEnabled();
	}

	@Override
	public void setDebugEnabled(final boolean debug) {
		settings.setDebugEnabled(debug);
	}

	@Override
	public double getBrightness() {
		return settings.getBrightness();
	}

	@Override
	public void setBrightness(final double brightness) {
		settings.setBrightness(brightness);
	}

	private static final class LocalStorageSettingsStore implements SettingsStore {
		@Override
		public String get(final String key) throws IOException {
			return Storage.getLocalStorage().getItem(STORAGE_PREFIX + key);
		}

		@Override
		public void put(final String key, final String value) throws IOException {
			Storage.getLocalStorage().setItem(STORAGE_PREFIX + key, value);
		}
	}

}
