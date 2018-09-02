package org.warp.picalculator.flow;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.warp.picalculator.PlatformUtils;

public class ObservableInterval extends Observable<Long>  {
	private final long interval;
	volatile boolean running;
	volatile Thread timeThread;
	
	protected ObservableInterval(long interval) {
		super();
		this.interval = interval;
		try {
			startInterval();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	void stopInterval() {
		if (running) {
			running = false;
			this.timeThread.interrupt();
		}
	}
	
	@Override
	public Disposable subscribe(Subscriber<? super Long> sub) {
		try {
			startInterval();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return super.subscribe(sub);
	}

	void startInterval() throws InterruptedException {
		if (running == false) {
			while (timeThread != null) {
				Thread.sleep(100);
			}
			timeThread = new Thread(() -> {
				try {
					while(!Thread.interrupted()) {
						for (Subscriber<? super Long> sub : this.subscribers) {
							sub.onNext(System.currentTimeMillis());
						}
						Thread.sleep(interval);
					}
				} catch (InterruptedException e) {}
				timeThread = null;
			});
			PlatformUtils.setThreadName(timeThread, "ObservableTimer");
			timeThread.start();
			running = true;
		}
	}

	public static ObservableInterval create(long l) {
		return new ObservableInterval(l);
	}
	
	@Override
	public void onDisposed(Subscriber<? super Long> sub) {
		super.onDisposed(sub);
		stopInterval();
	}
}
