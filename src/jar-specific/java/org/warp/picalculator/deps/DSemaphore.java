package org.warp.picalculator.deps;

import java.util.concurrent.Semaphore;

public class DSemaphore extends Semaphore {

	private static final long serialVersionUID = -2362314723921013871L;

	public DSemaphore(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public DSemaphore(int permits, boolean fair) {
		super(permits, fair);
		// TODO Auto-generated constructor stub
	}
}
