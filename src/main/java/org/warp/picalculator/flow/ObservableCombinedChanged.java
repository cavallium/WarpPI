package org.warp.picalculator.flow;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

public class ObservableCombinedChanged<T, U> extends Observable<Pair<T, U>>  {
	private volatile boolean initialized = false;
	private Observable<T> a;
	private Observable<U> b;
	private Disposable disposableA;
	private Disposable disposableB;
	private Subject<Pair<T, U>> pairSubject;
	private Subject<T> mapSubject;
	
	public ObservableCombinedChanged(Observable<T> a, Observable<U> b) {
		super();
		this.a = a;
		this.b = b;
		this.pairSubject = SimpleSubject.create();
	}

	private void initialize() {
		this.disposableA = a.subscribe((t) -> {
			for (Subscriber<? super Pair<T, U>> sub : this.subscribers) {
				sub.onNext(Pair.of(t, null));
			}
		}, (e) -> {
			for (Subscriber<? super Pair<T, U>> sub : this.subscribers) {
				sub.onError(e);
			};
		}, () -> {
			for (Subscriber<? super Pair<T, U>> sub : this.subscribers) {
				sub.onComplete();
			};
		});
		this.disposableB = b.subscribe((t) -> {
			for (Subscriber<? super Pair<T, U>> sub : this.subscribers) {
				sub.onNext(Pair.of(null, t));
			};
		}, (e) -> {
			for (Subscriber<? super Pair<T, U>> sub : this.subscribers) {
				sub.onError(e);
			};
		}, () -> {
			for (Subscriber<? super Pair<T, U>> sub : this.subscribers) {
				sub.onComplete();
			};
		});
	}
	
	private void chechInitialized() {
		if (!initialized) {
			initialized = true;
			initialize();
		}
	}
	
	@Override
	public Disposable subscribe(Subscriber<? super Pair<T,U>> sub) {
		Disposable disp = super.subscribe(sub);
		chechInitialized();
		return disp;
	}

	@Override
	public void onDisposed(Subscriber<? super Pair<T, U>> sub) {
		super.onDisposed(sub);
		this.disposableA.dispose();
		this.disposableB.dispose();
	}
}
