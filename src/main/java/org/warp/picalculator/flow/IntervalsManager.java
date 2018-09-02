package org.warp.picalculator.flow;

import java.util.LinkedList;
import java.util.List;

import org.warp.picalculator.PlatformUtils;

public class IntervalsManager {
	private static List<ObservableInterval> intervals = new LinkedList<>();
	
	static {
		startChecker();
	}
	
	private IntervalsManager() {
		
	}

	public static void register(ObservableInterval t) {
		synchronized (intervals) {
			if (!intervals.contains(t)) {
				intervals.add(t);
			}
		}
	}
	
	private static void startChecker() {
		Thread t = new Thread(() -> {
			try {
				while (true) {
					Thread.sleep(1000);
					for (ObservableInterval interval : intervals) {
						if (interval.running) {
							if (interval.subscribers.size() <= 0) {
								interval.stopInterval();
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		PlatformUtils.setDaemon(t);
		PlatformUtils.setThreadName(t, "Intervals Manager");
		t.start();
	}
}
