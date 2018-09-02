package org.warp.picalculator.flow;

public interface Subscription {
	void cancel();

	void request(long n);
}
