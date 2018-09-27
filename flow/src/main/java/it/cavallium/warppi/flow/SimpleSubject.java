package it.cavallium.warppi.flow;

public class SimpleSubject<T> extends Subject<T> {

	protected SimpleSubject() {}

	public final static <T> SimpleSubject<T> create() {
		return new SimpleSubject<>();
	}

	@Override
	public void onComplete() {
		for (final Subscriber<? super T> sub : subscribers)
			sub.onComplete();;
	}

	@Override
	public void onError(final Throwable e) {
		for (final Subscriber<? super T> sub : subscribers)
			sub.onError(e);;
	}

	@Override
	public void onNext(final T t) {
		for (final Subscriber<? super T> sub : subscribers)
			sub.onNext(t);;
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
	public void onSubscribe(final Disposable d) {}

}
