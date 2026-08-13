package it.cavallium.warppi.settings;

import java.io.IOException;

/** Minimal key/value storage used by calculator settings on every platform. */
public interface SettingsStore {

	String get(String key) throws IOException;

	void put(String key, String value) throws IOException;
}
