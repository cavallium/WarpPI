package it.cavallium.warppi.hardware;

import java.util.concurrent.Semaphore;

public class HardwareSemaphore extends Semaphore implements it.cavallium.warppi.deps.Platform.Semaphore {

	private static final long serialVersionUID = -2362314723921013871L;

	public HardwareSemaphore(int arg0) {
		super(arg0);
	}

	public HardwareSemaphore(int permits, boolean fair) {
		super(permits, fair);
	}
}
