package it.cavallium.warppi.flow;

public interface Subscriber<T> {
	default void onComplete() {}

	default void onError(final Throwable t) {}

	void onNext(T t);

	void onSubscribe(Subscription s);
}
