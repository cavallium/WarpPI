package org.warp.picalculator.deps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.teavm.classlib.java.util.TArrayList;
import org.teavm.classlib.java.util.TQueue;

public class DSemaphore {

	private Queue<Object> q;
	
	private int freePermits = 0;
	
	public DSemaphore(int i) {
		q = new LinkedList<Object>();
		freePermits = i;
	}

	public void release() {
		if (q.peek() == null) {
			q.poll();
		} else {
			freePermits++;
		}
	}

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
