package it.cavallium.warppi.flow;

public abstract class Subject<T> extends Observable<T> implements Observer<T> {
	abstract Throwable getThrowable();

	abstract boolean hasComplete();

	abstract boolean hasObservers();

	abstract boolean hasThrowable();

	abstract Subject<T> toSerialized();

	@Override
	public Disposable subscribe(final Action1<? super T> onNext) {
		return subscribe(createSubscriber(onNext));
	}

	@Override
	public Disposable subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
		return subscribe(createSubscriber(onNext, onError));
	}

	@Override
	public Disposable subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError,
			final Action0 onCompl) {
		return subscribe(createSubscriber(onNext, onError, onCompl));
	}

	@Override
	public void subscribe(final Observer<? super T> obs) {
		subscribe(createSubscriber(obs));
	}

	@Override
	public Disposable subscribe(final Subscriber<? super T> sub) {
		final Disposable disp = super.subscribe(sub);
		onSubscribe(disp);
		return disp;
	}
}
