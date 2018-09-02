package org.warp.picalculator.flow;

public interface Action2<T, U> {
	void call(T t, U u);
}
