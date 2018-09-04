package it.cavallium.warppi.flow;

public class SimpleSubject<T> extends Subject<T> {

	protected SimpleSubject() {}

	public final static <T> SimpleSubject<T> create() {
		return new SimpleSubject<>();
	}

	@Override
	public void onComplete() {
		for (Subscriber<? super T> sub : this.subscribers) {
			sub.onComplete();
		} ;
	}

	@Override
	public void onError(Throwable e) {
		for (Subscriber<? super T> sub : this.subscribers) {
			sub.onError(e);
		} ;
	}

	@Override
	public void onNext(T t) {
		for (Subscriber<? super T> sub : this.subscribers) {
			sub.onNext(t);
		} ;
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
	public void onSubscribe(Disposable d) {}

}
