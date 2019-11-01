package it.cavallium.warppi.util;

import java.util.function.Consumer;
import java.util.function.Function;

public interface EventSubscriber<T> {

	/**
	 *
	 * @return Last value, or null
	 */
	T getLastValue();

	void subscribe(Consumer<T> action);

	default <U> EventSubscriber<U> map(Function<T, U> mapFunction) {
		var eventSubmitter = new EventSubmitter<U>();
		map(this, eventSubmitter, mapFunction);
		return eventSubmitter;
	}

	default <U> void map(EventSubmitter<U> targetHandler, Function<T, U> mapFunction) {
		map(this, targetHandler, mapFunction);
	}

	static <T, U> void map(EventSubscriber<T> originalEventHandler, EventSubmitter<U> mappedEventHandler, Function<T, U> mapFunction) {
		originalEventHandler.subscribe((value) -> {
			mappedEventHandler.submit(mapFunction.apply(value));
		});
	}
}
