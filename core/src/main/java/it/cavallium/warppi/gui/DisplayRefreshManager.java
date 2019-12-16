package it.cavallium.warppi.gui;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DisplayRefreshManager {
	private final Consumer<Integer[]> refreshConsumer;
	private volatile Integer[] size;

	public DisplayRefreshManager(Consumer<Integer[]> refreshConsumer) {
		this.refreshConsumer = refreshConsumer;
	}

	public void onTick() {
		refreshConsumer.accept(size);
	}

	public void onResize(Integer[] newSize) {
		var oldSize = size;
		if (oldSize == null || !Arrays.equals(oldSize, newSize)) {
			size = newSize;
			refreshConsumer.accept(size);
		}
	}
}
