package org.warp.picalculator.flow;

public interface Observer<T> {
	public void onComplete();
	public void onError(Throwable e);
	public void onNext(T t);
	public void onSubscribe(Disposable d);
}
