package it.cavallium.warppi.flow;

public interface ObservableSource<T> {
	public void subscribe(Observer<? super T> observer);
}
