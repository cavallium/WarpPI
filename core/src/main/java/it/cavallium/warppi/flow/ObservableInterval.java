package it.cavallium.warppi.flow;

import it.cavallium.warppi.Engine;

public class ObservableInterval extends Observable<Long> {
	private final long interval;
	volatile boolean running;
	volatile Thread timeThread;

	protected ObservableInterval(final long interval) {
		super();
		this.interval = interval;
		try {
			startInterval();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	void stopInterval() {
		if (running) {
			running = false;
			timeThread.interrupt();
		}
	}

	@Override
	public Disposable subscribe(final Subscriber<? super Long> sub) {
		try {
			startInterval();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return super.subscribe(sub);
	}

	void startInterval() throws InterruptedException {
		if (running == false) {
			while (timeThread != null)
				Thread.sleep(100);
			timeThread = new Thread(() -> {
				try {
					while (!Thread.interrupted()) {
						for (final Subscriber<? super Long> sub : subscribers)
							sub.onNext(System.currentTimeMillis());
						Thread.sleep(interval);
					}
				} catch (final InterruptedException e) {}
				timeThread = null;
			});
			Engine.getPlatform().setThreadName(timeThread, "ObservableTimer");
			timeThread.start();
			running = true;
		}
	}

	public static ObservableInterval create(final long l) {
		return new ObservableInterval(l);
	}

	@Override
	public void onDisposed(final Subscriber<? super Long> sub) {
		super.onDisposed(sub);
		stopInterval();
	}
}
