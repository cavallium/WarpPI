package it.cavallium.warppi.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventSubmitter<T> implements EventSubscriber<T>, Submitter<T> {
	private boolean initialized = false;
	private T value;
	private List<Consumer<T>> subscribers = new LinkedList<>();

	public EventSubmitter() {
		value = null;
	}

	public EventSubmitter(T defaultValue) {
		this.value = defaultValue;
		this.initialized = true;
	}

	public static <T> EventSubmitter<T> create() {
		return new EventSubmitter<>();
	}

	public static <T> EventSubmitter<T> create(T value) {
		return new EventSubmitter<>(value);
	}

	@Override
	public T getLastValue() {
		return value;
	}

	@Override
	public void subscribe(Consumer<T> action) {
		T currentValue = value;
		var newSubscribers = new LinkedList<>(subscribers);

		if (initialized) {
			action.accept(currentValue);
		}
		newSubscribers.add(action);

		subscribers = newSubscribers;
	}

	@Override
	public void submit(T value) {
		this.value = value;
		this.initialized = true;

		for (Consumer<T> subscriber : subscribers) {
			subscriber.accept(value);
		}
	}

	@Override
	public <U> EventSubmitter<U> map(Function<T, U> mapFunction) {
		var eventSubmitter = new EventSubmitter<U>();
		map(eventSubmitter, mapFunction);
		return eventSubmitter;
	}
}
