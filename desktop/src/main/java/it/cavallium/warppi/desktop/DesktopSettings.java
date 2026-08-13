package it.cavallium.warppi.desktop;

import java.nio.file.Path;
import java.nio.file.Paths;

import it.cavallium.warppi.Platform.Settings;
import it.cavallium.warppi.settings.PersistentSettings;
import it.cavallium.warppi.settings.PropertiesSettingsStore;

public final class DesktopSettings implements Settings {
	private static final String SETTINGS_FILE_PROPERTY = "warppi.settings.file";

	private final PersistentSettings settings;

	public DesktopSettings() {
		settings = new PersistentSettings(new PropertiesSettingsStore(settingsFile()), true);
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
		if (override != null && !override.isBlank())
			return Paths.get(override);
		final String configHome = System.getenv("XDG_CONFIG_HOME");
		if (configHome != null && !configHome.isBlank())
			return Paths.get(configHome, "warppi", "settings.properties");
		final String userHome = System.getProperty("user.home");
		if (userHome != null && !userHome.isBlank())
			return Paths.get(userHome, ".config", "warppi", "settings.properties");
		return Paths.get("warppi-settings.properties");
	}

}
