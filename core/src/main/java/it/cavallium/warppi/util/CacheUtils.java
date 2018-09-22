package it.cavallium.warppi.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CacheUtils {

	private static final Map<String, Object> cache = Collections.synchronizedMap(new HashMap<>());
	private static final Map<String, Long> time = Collections.synchronizedMap(new HashMap<>());

	@SuppressWarnings("unchecked")
	public static <T> T get(final String entryName, final long expireDelta, final Supplier<T> function) {
		CacheUtils.refreshEntry(entryName);
		synchronized (CacheUtils.cache) {
			if (CacheUtils.cache.containsKey(entryName))
				return (T) CacheUtils.cache.get(entryName);
			else {
				CacheUtils.time.put(entryName, System.currentTimeMillis() + expireDelta);
				final T result = function.get();
				CacheUtils.cache.put(entryName, result);
				return result;
			}
		}
	}

	private static void refreshEntry(final String entryName) {
		synchronized (CacheUtils.time) {
			synchronized (CacheUtils.cache) {
				if (CacheUtils.time.containsKey(entryName))
					if (CacheUtils.time.get(entryName) <= System.currentTimeMillis()) {
						CacheUtils.time.remove(entryName);
						CacheUtils.cache.remove(entryName);
					}
			}
		}
	}

}
