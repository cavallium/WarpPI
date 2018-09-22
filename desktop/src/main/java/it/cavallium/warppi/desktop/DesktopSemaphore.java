package it.cavallium.warppi.desktop;

import java.util.concurrent.Semaphore;

public class DesktopSemaphore extends Semaphore implements it.cavallium.warppi.Platform.Semaphore {

	private static final long serialVersionUID = -2362314723921013871L;

	public DesktopSemaphore(final int arg0) {
		super(arg0);
	}

	public DesktopSemaphore(final int permits, final boolean fair) {
		super(permits, fair);
	}
}
