package it.cavallium.warppi.flow;

public class BehaviorSubject<T> extends Subject<T> {

	private T lastValue;
	private boolean lastValueSet;

	protected BehaviorSubject() {
		super();
		lastValue = null;
		lastValueSet = false;
	}

	protected BehaviorSubject(final T initialValue) {
		super();
		lastValue = initialValue;
		lastValueSet = true;
	}

	public final static <T> BehaviorSubject<T> create() {
		return new BehaviorSubject<>();
	}

	public final static <T> BehaviorSubject<T> create(final T initialValue) {
		return new BehaviorSubject<>(initialValue);
	}

	@Override
	public void onComplete() {
		for (final Subscriber<? super T> sub : subscribers)
			sub.onComplete();
	}

	@Override
	public void onError(final Throwable e) {
		for (final Subscriber<? super T> sub : subscribers)
			sub.onError(e);;
	}

	@Override
	public void onNext(final T t) {
		lastValue = t;
		lastValueSet = true;
		for (final Subscriber<? super T> sub : subscribers)
			sub.onNext(t);
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
	public void onSubscribe(final Disposable d) {
		@SuppressWarnings("unchecked")
		final DisposableOfSubscriber ds = (DisposableOfSubscriber) d;
		final Subscriber<? super T> s = ds.getSubscriber();
		if (lastValueSet)
			s.onNext(lastValue);
	}

	public T getLastValue() {
		return lastValue;
	}

}
