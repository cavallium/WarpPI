package org.warp.picalculator.flow;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

public abstract class Observable<T> implements ObservableSource<T> {

	protected List<Subscriber<? super T>> subscribers = new LinkedList<>();
	
	public Disposable subscribe() {
		return null;
	}
	public Disposable subscribe(Action1<? super T> onNext) {
		return subscribe(createSubscriber(onNext));
	}
	protected Observable<T>.DisposableOfSubscriber createDisposable(Subscriber<? super T> sub) {
		return new DisposableOfSubscriber(sub);
	}
	public Disposable subscribe(Action1<? super T> onNext, Action1<Throwable> onError) {
		return subscribe(createSubscriber(onNext, onError));
	}
	public Disposable subscribe(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
		return subscribe(createSubscriber(onNext, onError, onCompleted));
	}
	public void subscribe(Observer<? super T> obs) {
		subscribe(createSubscriber(obs));
	}
	public Disposable subscribe(Subscriber<? super T> sub) {
		subscribers.add(sub);
		return createDisposable(sub);
	}

	protected Subscriber<T> createSubscriber(Action1<? super T> onNext) {
		return new Subscriber<T>() {
			@Override public void onSubscribe(Subscription s) {}
			public void onNext(T t) {
				onNext.call(t);
			}
		};
	}
	
	protected Subscriber<T> createSubscriber(Action1<? super T> onNext, Action1<Throwable> onError) {
		return new Subscriber<T>() {
			@Override public void onSubscribe(Subscription s) {}
			public void onNext(T t) {
				onNext.call(t);
			}
			@Override
			public void onError(Throwable t) {
				onError.call(t);
			}
		};
	}
	
	protected Subscriber<T> createSubscriber(Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompl) {
		return new Subscriber<T>() {
			@Override public void onSubscribe(Subscription s) {}
			public void onNext(T t) {
				onNext.call(t);
			}
			@Override
			public void onError(Throwable t) {
				onError.call(t);
			}
			@Override
			public void onComplete() {
				onCompl.call();
			}
		};
	}
	
	protected Subscriber<T> createSubscriber(Observer<? super T> obs) {
		return new Subscriber<T>() {
			@Override public void onSubscribe(Subscription s) {}
			public void onNext(T t) {
				obs.onNext(t);
			}
			@Override
			public void onError(Throwable t) {
				obs.onError(t);
			}
			@Override
			public void onComplete() {
				obs.onComplete();
			}
		};
	}
	
	public static final <T> Observable<T> merge(Observable<T> a, Observable<T> b) {
		return new ObservableMerged<>(a, b);
	}
	
	@Deprecated
	public static final <T> Observable<T> of(Observable<T> a) {
		return null;
	}

	public final <U> Observable<U> map(Function<T, U> f) {
		return new ObservableMap<T, U>(this, f);
	}

	public static final <T, U> Observable<Pair<T,U>> combineLatest(Observable<T> a, Observable<U> b) {
		return new ObservableCombinedLatest<>(a, b);
	}

	public static final <T, U> Observable<Pair<T,U>> combineChanged(Observable<T> a, Observable<U> b) {
		return new ObservableCombinedChanged<>(a, b);
	}

	public static final <T, U> Observable<Pair<T,U>> zip(Observable<T> a, Observable<U> b) {
		return new ObservableZipped<>(a, b);
	}
	
	public static final Observable<Long> interval(long interval) {
		return new ObservableInterval(interval);
	}
	
	protected class DisposableOfSubscriber implements Disposable {

		private final Subscriber<? super T> sub;
		
		public DisposableOfSubscriber(Subscriber<? super T> sub) {
			this.sub = sub;
		}

		protected Subscriber<? super T> getSubscriber() {
			return sub;
		}
		
		protected Observable<T> getObservable() {
			return Observable.this;
		}

		@Override
		public void dispose() {
			if (isDisposed()) {
				throw new RuntimeException("Already disposed!");
			}
			subscribers.remove(sub);
			Observable.this.onDisposed(sub);
		}

		@Override
		public boolean isDisposed() {
			return !subscribers.contains(sub);
		}
		
	}

	public Observable<T> doOnNext(Action1<T> onNext) {
		Subject<T> onNextSubject = BehaviorSubject.create();
		this.subscribe((val) -> {
			onNext.call(val);
			onNextSubject.onNext(val);
		});
		return onNextSubject;
	}
	
	public void onDisposed(Subscriber<? super T> sub) {
		
	}
}
