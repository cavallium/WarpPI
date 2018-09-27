package it.cavallium.warppi.flow;

public interface Subscription {
	void cancel();

	void request(long n);
}
