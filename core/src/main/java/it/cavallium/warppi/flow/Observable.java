package it.cavallium.warppi.flow;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

public abstract class Observable<T> implements ObservableSource<T> {

	protected List<Subscriber<? super T>> subscribers = new LinkedList<>();

	public Disposable subscribe() {
		return null;
	}

	public Disposable subscribe(final Action1<? super T> onNext) {
		return subscribe(createSubscriber(onNext));
	}

	protected Observable<T>.DisposableOfSubscriber createDisposable(final Subscriber<? super T> sub) {
		return new DisposableOfSubscriber(sub);
	}

	public Disposable subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
		return subscribe(createSubscriber(onNext, onError));
	}

	public Disposable subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError,
			final Action0 onCompleted) {
		return subscribe(createSubscriber(onNext, onError, onCompleted));
	}

	@Override
	public void subscribe(final Observer<? super T> obs) {
		subscribe(createSubscriber(obs));
	}

	public Disposable subscribe(final Subscriber<? super T> sub) {
		subscribers.add(sub);
		return createDisposable(sub);
	}

	protected Subscriber<T> createSubscriber(final Action1<? super T> onNext) {
		return new Subscriber<T>() {
			@Override
			public void onSubscribe(final Subscription s) {}

			@Override
			public void onNext(final T t) {
				onNext.call(t);
			}
		};
	}

	protected Subscriber<T> createSubscriber(final Action1<? super T> onNext, final Action1<Throwable> onError) {
		return new Subscriber<T>() {
			@Override
			public void onSubscribe(final Subscription s) {}

			@Override
			public void onNext(final T t) {
				onNext.call(t);
			}

			@Override
			public void onError(final Throwable t) {
				onError.call(t);
			}
		};
	}

	protected Subscriber<T> createSubscriber(final Action1<? super T> onNext, final Action1<Throwable> onError,
			final Action0 onCompl) {
		return new Subscriber<T>() {
			@Override
			public void onSubscribe(final Subscription s) {}

			@Override
			public void onNext(final T t) {
				onNext.call(t);
			}

			@Override
			public void onError(final Throwable t) {
				onError.call(t);
			}

			@Override
			public void onComplete() {
				onCompl.call();
			}
		};
	}

	protected Subscriber<T> createSubscriber(final Observer<? super T> obs) {
		return new Subscriber<T>() {
			@Override
			public void onSubscribe(final Subscription s) {}

			@Override
			public void onNext(final T t) {
				obs.onNext(t);
			}

			@Override
			public void onError(final Throwable t) {
				obs.onError(t);
			}

			@Override
			public void onComplete() {
				obs.onComplete();
			}
		};
	}

	public static final <T> Observable<T> merge(final Observable<T> a, final Observable<T> b) {
		return new ObservableMerged<>(a, b);
	}

	@Deprecated
	public static final <T> Observable<T> of(final Observable<T> a) {
		return null;
	}

	public final <U> Observable<U> map(final Function<T, U> f) {
		return new ObservableMap<>(this, f);
	}

	public static final <T, U> Observable<Pair<T, U>> combineLatest(final Observable<T> a, final Observable<U> b) {
		return new ObservableCombinedLatest<>(a, b);
	}

	public static final <T, U> Observable<Pair<T, U>> combineChanged(final Observable<T> a, final Observable<U> b) {
		return new ObservableCombinedChanged<>(a, b);
	}

	public static final <T, U> Observable<Pair<T, U>> zip(final Observable<T> a, final Observable<U> b) {
		return new ObservableZipped<>(a, b);
	}

	public static final Observable<Long> interval(final long interval) {
		return new ObservableInterval(interval);
	}

	protected class DisposableOfSubscriber implements Disposable {

		private final Subscriber<? super T> sub;

		public DisposableOfSubscriber(final Subscriber<? super T> sub) {
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
			if (isDisposed())
				throw new RuntimeException("Already disposed!");
			subscribers.remove(sub);
			Observable.this.onDisposed(sub);
		}

		@Override
		public boolean isDisposed() {
			return !subscribers.contains(sub);
		}

	}

	public Observable<T> doOnNext(final Action1<T> onNext) {
		final Subject<T> onNextSubject = BehaviorSubject.create();
		this.subscribe((val) -> {
			onNext.call(val);
			onNextSubject.onNext(val);
		});
		return onNextSubject;
	}

	public void onDisposed(final Subscriber<? super T> sub) {

	}
}
