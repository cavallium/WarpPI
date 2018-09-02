package org.warp.picalculator.flow;

public class BehaviorSubject<T> extends Subject<T> {

	private T lastValue;
	private boolean lastValueSet;

	protected BehaviorSubject() {
		super();
		lastValue = null;
		lastValueSet = false;
	}

	protected BehaviorSubject(T initialValue) {
		super();
		lastValue = initialValue;
		lastValueSet = true;
	}

	public final static <T> BehaviorSubject<T> create() {
		return new BehaviorSubject<>();
	}

	public final static <T> BehaviorSubject<T> create(T initialValue) {
		return new BehaviorSubject<T>(initialValue);
	}

	@Override
	public void onComplete() {
		for (Subscriber<? super T> sub : this.subscribers) {
			sub.onComplete();
		}
	}

	@Override
	public void onError(Throwable e) {
		for (Subscriber<? super T> sub : this.subscribers) {
			sub.onError(e);
		} ;
	}

	@Override
	public void onNext(T t) {
		lastValue = t;
		lastValueSet = true;
		for (Subscriber<? super T> sub : this.subscribers) {
			sub.onNext(t);
		}
	}

	@Override
	Throwable getThrowable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean hasComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	boolean hasObservers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	boolean hasThrowable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	Subject<T> toSerialized() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSubscribe(Disposable d) {
		@SuppressWarnings("unchecked")
		DisposableOfSubscriber ds = (DisposableOfSubscriber) d;
		Subscriber<? super T> s = ds.getSubscriber();
		if (lastValueSet) {
			s.onNext(lastValue);
		}
	}

	public T getLastValue() {
		return lastValue;
	}

}
