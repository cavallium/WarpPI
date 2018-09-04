package it.cavallium.warppi.desktop;

import java.util.concurrent.Semaphore;

public class DesktopSemaphore extends Semaphore implements it.cavallium.warppi.deps.Platform.Semaphore {

	private static final long serialVersionUID = -2362314723921013871L;

	public DesktopSemaphore(int arg0) {
		super(arg0);
	}

	public DesktopSemaphore(int permits, boolean fair) {
		super(permits, fair);
	}
}
