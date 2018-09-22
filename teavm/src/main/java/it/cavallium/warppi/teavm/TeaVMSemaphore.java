package it.cavallium.warppi.teavm;

import java.util.LinkedList;
import java.util.Queue;

public class TeaVMSemaphore implements it.cavallium.warppi.Platform.Semaphore {

	private final Queue<Object> q;

	private int freePermits = 0;

	public TeaVMSemaphore(final int i) {
		q = new LinkedList<>();
		freePermits = i;
	}

	@Override
	public void release() {
		if (q.peek() == null)
			q.poll();
		else
			freePermits++;
	}

	@Override
	public void acquire() throws InterruptedException {
		if (freePermits > 0)
			freePermits--;
		else {
			final Object thiz = new Object();
			q.offer(thiz);
			while (q.contains(thiz))
				Thread.sleep(500);
		}
	}
}
