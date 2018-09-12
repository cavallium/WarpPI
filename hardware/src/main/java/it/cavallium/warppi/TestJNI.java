package it.cavallium.warppi;

public class TestJNI {
	public TestJNI() {

	}

	static {
		System.load("/boot/libpicalc.so");
	}

	private native MmapByteBuffer getDisplayBuffer();

	private native void disposeDisplayBuffer();

	public MmapByteBuffer retrieveBuffer() {
		return getDisplayBuffer();
	}

	public void deleteBuffer() {
		disposeDisplayBuffer();
	}
}