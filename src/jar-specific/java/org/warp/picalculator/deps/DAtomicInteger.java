package org.warp.picalculator.deps;

import java.util.concurrent.atomic.AtomicInteger;

public class DAtomicInteger extends AtomicInteger {
	public DAtomicInteger() {
		super();
	}

	public DAtomicInteger(int i) {
		super(i);
	}

	private static final long serialVersionUID = 2910383978241062566L;
}
