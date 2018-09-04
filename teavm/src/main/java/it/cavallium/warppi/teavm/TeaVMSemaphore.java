package it.cavallium.warppi.teavm;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class TeaVMSemaphore implements it.cavallium.warppi.deps.Platform.Semaphore {

	private Queue<Object> q;
	
	private int freePermits = 0;
	
	public TeaVMSemaphore(int i) {
		q = new LinkedList<Object>();
		freePermits = i;
	}
	
	@Override
	public void release() {
		if (q.peek() == null) {
			q.poll();
		} else {
			freePermits++;
		}
	}

	@Override
	public void acquire() throws InterruptedException {
		if (freePermits > 0) {
			freePermits--;
		} else {
			Object thiz = new Object();
			q.offer(thiz);
			while(q.contains(thiz)) {
				Thread.sleep(500);
			}
		}
	}
}
