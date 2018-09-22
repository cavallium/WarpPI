package it.cavallium.warppi.hardware;

import java.util.concurrent.Semaphore;

public class HardwareSemaphore extends Semaphore implements it.cavallium.warppi.Platform.Semaphore {

	private static final long serialVersionUID = -2362314723921013871L;

	public HardwareSemaphore(final int arg0) {
		super(arg0);
	}

	public HardwareSemaphore(final int permits, final boolean fair) {
		super(permits, fair);
	}
}
