package it.cavallium.warppi.util;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Reimplements map creation methods which are not available on TeaVM.
 */
public class MapFactory {
	private MapFactory() {}

	/**
	 * Returns an unmodifiable map containing keys and values extracted from the given entries.
	 * <p>
	 * This method is equivalent to {@link Map#ofEntries(Map.Entry...)}.
	 *
	 * @param entries <code>Map.Entry</code>s containing the keys and values from which the map is populated
	 * @param <K> the <code>Map</code>'s key type
	 * @param <V> the <code>Map</code>'s value type
	 * @return a Map containing the specified mappings
	 * @see MapFactory#entry(K, V)
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> fromEntries(Map.Entry<? extends K, ? extends V>... entries) {
		HashMap<K, V> map = new HashMap<>();
		for (Map.Entry<? extends K, ? extends V> entry : entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Returns an unmodifiable <code>Map.Entry</code> containing the given key and value.
	 * These entries are suitable for populating Map instances using the {@link MapFactory#fromEntries(Map.Entry...)}
	 * or {@link Map#ofEntries(Map.Entry...)} methods.
	 * <p>
	 * This method can be used as a replacement for {@link Map#entry(K, V)}, if the latter is not available (for example,
	 * when compiling for TeaVM). However, unlike <code>Map.entry</code>, <code>null</code> keys and values are allowed,
	 * and the returned <code>Entry</code> is serializable.
	 *
	 * @param key the key
	 * @param value the value
	 * @param <K> the key's type
	 * @param <V> the value's type
	 * @return an <code>Entry</code> containing the specified key and value
	 */
	public static <K, V> Map.Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleImmutableEntry<>(key, value);
	}
}
