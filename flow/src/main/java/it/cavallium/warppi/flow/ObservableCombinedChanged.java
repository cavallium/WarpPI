package it.cavallium.warppi.flow;

import org.apache.commons.lang3.tuple.Pair;

public class ObservableCombinedChanged<T, U> extends Observable<Pair<T, U>> {
	private volatile boolean initialized = false;
	private final Observable<T> a;
	private final Observable<U> b;
	private Disposable disposableA;
	private Disposable disposableB;

	public ObservableCombinedChanged(final Observable<T> a, final Observable<U> b) {
		super();
		this.a = a;
		this.b = b;
	}

	private void initialize() {
		this.disposableA = a.subscribe((t) -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onNext(Pair.of(t, null));
		}, (e) -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onComplete();;
		});
		this.disposableB = b.subscribe((t) -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onNext(Pair.of(null, t));;
		}, (e) -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
				sub.onError(e);;
		}, () -> {
			for (final Subscriber<? super Pair<T, U>> sub : subscribers)
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
