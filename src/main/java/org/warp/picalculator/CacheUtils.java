package org.warp.picalculator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class CacheUtils {

	private static final Map<String, Object> cache = Collections.synchronizedMap(new HashMap<>());
	private static final Map<String, Long> time = Collections.synchronizedMap(new HashMap<>());

	@SuppressWarnings("unchecked")
	public static <T> T get(String entryName, long expireDelta, Supplier<T> function) {
		refreshEntry(entryName);
		synchronized (cache) {
			if (cache.containsKey(entryName)) {
				return (T) cache.get(entryName);
			} else {
				time.put(entryName, System.currentTimeMillis() + expireDelta);
				T result = function.get();
				cache.put(entryName, result);
				return result;
			}
		}
	}

	private static void refreshEntry(String entryName) {
		synchronized (time) {
			synchronized (cache) {
				if (time.getOrDefault(entryName, 0L) <= System.currentTimeMillis()) {
					time.remove(entryName);
					cache.remove(entryName);
				}
			}
		}
	}

}
