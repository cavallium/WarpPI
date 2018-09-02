package it.cavallium.warppi.flow;

public abstract class Subject<T> extends Observable<T> implements Observer<T> {
	abstract Throwable getThrowable();

	abstract boolean hasComplete();

	abstract boolean hasObservers();

	abstract boolean hasThrowable();

	abstract Subject<T> toSerialized();

	@Override
	public Disposable subscribe(Action1<? super T> onNext) {
		return subscribe(createSubscriber(onNext));
	}

	@Override
	public Disposable subscribe(Action1<? super T> onNext, Action1<Throwable> onError) {
		return subscribe(createSubscriber(onNext, onError));
	}

	@Override
	public Disposable subscribe(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompl) {
		return subscribe(createSubscriber(onNext, onError, onCompl));
	}

	@Override
	public void subscribe(Observer<? super T> obs) {
		subscribe(createSubscriber(obs));
	}

	@Override
	public Disposable subscribe(Subscriber<? super T> sub) {
		Disposable disp = super.subscribe(sub);
		this.onSubscribe(disp);
		return disp;
	}
}
