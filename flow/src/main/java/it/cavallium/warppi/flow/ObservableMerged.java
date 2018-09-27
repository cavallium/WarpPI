package it.cavallium.warppi.flow;

public class ObservableMerged<T> extends Observable<T> {
	private final Observable<T> originalObservableA;
	private final Observable<T> originalObservableB;
	private volatile boolean initialized = false;
	private Disposable mapDisposableA;
	private Disposable mapDisposableB;

	public ObservableMerged(final Observable<T> originalObservableA, final Observable<T> originalObservableB) {
		super();
		this.originalObservableA = originalObservableA;
		this.originalObservableB = originalObservableB;
	}

	private void initialize() {
		this.mapDisposableA = originalObservableA.subscribe((t) -> {
			for (final Subscriber<? super T> sub : subscribers)
				sub.onNext(t);;
		}, (e) -> {
			for (final Subscriber<? super T> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super T> sub : subscribers)
				sub.onComplete();;
		});
		this.mapDisposableB = originalObservableB.subscribe((t) -> {
			for (final Subscriber<? super T> sub : subscribers)
				sub.onNext(t);;
		}, (e) -> {
			for (final Subscriber<? super T> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super T> sub : subscribers)
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
	public Disposable subscribe(final Subscriber<? super T> sub) {
		final Disposable disp = super.subscribe(sub);
		chechInitialized();
		return disp;
	}

	@Override
	public void onDisposed(final Subscriber<? super T> sub) {
		super.onDisposed(sub);
		this.mapDisposableA.dispose();
		this.mapDisposableB.dispose();
	}
}
