package it.cavallium.warppi.hardware;

import java.nio.file.Path;
import java.nio.file.Paths;

import it.cavallium.warppi.Platform.Settings;
import it.cavallium.warppi.settings.PersistentSettings;
import it.cavallium.warppi.settings.PropertiesSettingsStore;

public final class HardwareSettings implements Settings {
	private static final String SETTINGS_FILE_PROPERTY = "warppi.settings.file";

	private final PersistentSettings settings;

	public HardwareSettings() {
		settings = new PersistentSettings(new PropertiesSettingsStore(settingsFile()), false);
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

	private static Path settingsFile() {
		final String override = System.getProperty(SETTINGS_FILE_PROPERTY);
		return override != null && !override.isBlank()
				? Paths.get(override)
				: Paths.get("/var/lib/warppi/settings.properties");
	}

}
