package it.cavallium.warppi.settings;

import java.io.IOException;
import java.util.Objects;

import it.cavallium.warppi.Platform.Settings;

/** Settings backed by a platform-specific persistent key/value store. */
public final class PersistentSettings implements Settings {
	static final String DEBUG_KEY = "debug";
	static final String BRIGHTNESS_KEY = "brightness";
	private static final double DEFAULT_BRIGHTNESS = 0.2d;

	private final SettingsStore store;
	private boolean debug;
	private double brightness;
	private boolean debugDirty;
	private boolean brightnessDirty;

	public PersistentSettings(final SettingsStore store, final boolean defaultDebug) {
		this.store = Objects.requireNonNull(store);
		debug = readBoolean(DEBUG_KEY, defaultDebug);
		brightness = readBrightness();
	}

	@Override
	public synchronized boolean isDebugEnabled() {
		return debug;
	}

	@Override
	public synchronized void setDebugEnabled(final boolean debug) {
		if (this.debug == debug && !debugDirty)
			return;
		this.debug = debug;
		debugDirty = !write(DEBUG_KEY, Boolean.toString(debug));
	}

	@Override
	public synchronized double getBrightness() {
		return brightness;
	}

	@Override
	public synchronized void setBrightness(final double brightness) {
		if (!Double.isFinite(brightness) || brightness < 0d || brightness > 1d)
			throw new IllegalArgumentException("Brightness must be between 0 and 1");
		final double normalizedBrightness = brightness == 0d ? 0d : brightness;
		if (Double.compare(this.brightness, normalizedBrightness) == 0
				&& !brightnessDirty)
			return;
		this.brightness = normalizedBrightness;
		brightnessDirty = !write(BRIGHTNESS_KEY, Double.toString(normalizedBrightness));
	}

	private boolean readBoolean(final String key, final boolean fallback) {
		final String value = read(key);
		return "true".equalsIgnoreCase(value) ? true
				: "false".equalsIgnoreCase(value) ? false : fallback;
	}

	private double readBrightness() {
		final String value = read(BRIGHTNESS_KEY);
		if (value == null)
			return DEFAULT_BRIGHTNESS;
		try {
			final double parsed = Double.parseDouble(value);
			return Double.isFinite(parsed) && parsed >= 0d && parsed <= 1d
					? parsed : DEFAULT_BRIGHTNESS;
		} catch (final NumberFormatException exception) {
			return DEFAULT_BRIGHTNESS;
		}
	}

	private String read(final String key) {
		try {
			return store.get(key);
		} catch (final IOException | RuntimeException exception) {
			System.err.println("Unable to read setting " + key + ": " + exception.getMessage());
			return null;
		}
	}

	private boolean write(final String key, final String value) {
		try {
			store.put(key, value);
			return true;
		} catch (final IOException | RuntimeException exception) {
			System.err.println("Unable to persist setting " + key + ": " + exception.getMessage());
			return false;
		}
	}
}
