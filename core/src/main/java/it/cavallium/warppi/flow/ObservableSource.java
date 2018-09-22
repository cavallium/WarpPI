package it.cavallium.warppi.flow;

public interface ObservableSource<T> {
	void subscribe(Observer<? super T> observer);
}
