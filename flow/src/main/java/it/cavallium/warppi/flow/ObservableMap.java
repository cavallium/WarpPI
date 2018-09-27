package it.cavallium.warppi.flow;

import java.util.function.Function;

public class ObservableMap<T, U> extends Observable<U> {
	private final Observable<T> originalObservable;
	private final Function<T, U> mapAction;
	private volatile boolean initialized = false;
	private Disposable mapDisposable;

	public ObservableMap(final Observable<T> originalObservable, final Function<T, U> mapAction) {
		super();
		this.originalObservable = originalObservable;
		this.mapAction = mapAction;
	}

	private void initialize() {
		this.mapDisposable = originalObservable.subscribe((t) -> {
			for (final Subscriber<? super U> sub : subscribers)
				sub.onNext(mapAction.apply(t));;
		}, (e) -> {
			for (final Subscriber<? super U> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super U> sub : subscribers)
				sub.onComplete();;
		});
	}

	private void chechInitialized() {
		if (!initialized) {
			initialized = true;
			initialize();
		}
	}

	@Override
	public Disposable subscribe(final Subscriber<? super U> sub) {
		final Disposable disp = super.subscribe(sub);
		chechInitialized();
		return disp;
	}

	@Override
	public void onDisposed(final Subscriber<? super U> sub) {
		super.onDisposed(sub);
		mapDisposable.dispose();
	}
}
