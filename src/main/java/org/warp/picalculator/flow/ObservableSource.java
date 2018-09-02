package org.warp.picalculator.flow;

public interface ObservableSource<T> {
	public void subscribe(Observer<? super T> observer);
}
