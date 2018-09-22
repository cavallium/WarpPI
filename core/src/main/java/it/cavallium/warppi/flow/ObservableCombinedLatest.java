package it.cavallium.warppi.flow;

import org.apache.commons.lang3.tuple.Pair;

public class ObservableCombinedLatest<T, U> extends Observable<Pair<T, U>> {
	private volatile boolean initialized = false;
	private final Observable<T> a;
	private final Observable<U> b;
	private Disposable disposableA;
	private Disposable disposableB;
	private volatile T lastA;
	private volatile U lastB;
	private volatile boolean didAOneTime;
	private volatile boolean didBOneTime;

	public ObservableCombinedLatest(final Observable<T> a, final Observable<U> b) {
		super();
		this.a = a;
		this.b = b;
	}

	private void initialize() {
		this.disposableA = a.subscribe((t) -> {
			lastA = t;
			didAOneTime = true;
			receivedNext();
		}, (e) -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onComplete();;
		});
		this.disposableB = b.subscribe((t) -> {
			lastB = t;
			didBOneTime = true;
			receivedNext();
		}, (e) -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onComplete();;
		});
	}

	private void receivedNext() {
		if (didAOneTime && didBOneTime)
			subscribers.forEach(sub -> {
				sub.onNext(Pair.of(lastA, lastB));
			});
	}

	private void chechInitialized() {
		if (!initialized) {
			initialized = true;
			initialize();
		}
	}

	@Override
	public Disposable subscribe(final Subscriber<? super Pair<T, U>> sub) {
		final Disposable disp = super.subscribe(sub);
		chechInitialized();
		return disp;
	}

	@Override
	public void onDisposed(final Subscriber<? super Pair<T, U>> sub) {
		super.onDisposed(sub);
		this.disposableA.dispose();
		this.disposableB.dispose();
	}
}
