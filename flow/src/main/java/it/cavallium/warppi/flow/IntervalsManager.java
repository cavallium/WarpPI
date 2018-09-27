package it.cavallium.warppi.flow;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class IntervalsManager {
	private static List<ObservableInterval> intervals = new LinkedList<>();

	static {
		IntervalsManager.startChecker();
	}

	private IntervalsManager() {

	}

	public static void register(final ObservableInterval t) {
		synchronized (IntervalsManager.intervals) {
			if (!IntervalsManager.intervals.contains(t))
				IntervalsManager.intervals.add(t);
		}
	}

	private static void startChecker() {
		final Thread t = new Thread(() -> {
			try {
				while (true) {
					Thread.sleep(1000);
					for (final ObservableInterval interval : IntervalsManager.intervals)
						if (interval.running)
							if (interval.subscribers.size() <= 0)
								interval.stopInterval();
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
		Utils.setThreadDaemon(t, true);
		t.start();
	}
}
