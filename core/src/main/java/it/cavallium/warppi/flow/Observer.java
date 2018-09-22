package it.cavallium.warppi.flow;

public interface Observer<T> {
	void onComplete();

	void onError(Throwable e);

	void onNext(T t);

	void onSubscribe(Disposable d);
}
