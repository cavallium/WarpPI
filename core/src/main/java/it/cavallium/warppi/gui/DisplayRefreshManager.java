package it.cavallium.warppi.gui;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DisplayRefreshManager {
	private final Consumer<Integer[]> refreshConsumer;
	private AtomicBoolean ticked = new AtomicBoolean(false);
	private AtomicBoolean sizeSet = new AtomicBoolean(false);
	private volatile Integer[] size;

	public DisplayRefreshManager(Consumer<Integer[]> refreshConsumer) {
		this.refreshConsumer = refreshConsumer;
	}

	public void onTick() {
		ticked.set(true);
		refreshIfNeeded();
	}

	public void onResize(Integer[] newSize) {
		size = newSize;
		sizeSet.set(true);
		refreshIfNeeded();
	}

	private void refreshIfNeeded() {
		if (ticked.get() && sizeSet.get()) {
			refreshConsumer.accept(size);
		}
	}
}
