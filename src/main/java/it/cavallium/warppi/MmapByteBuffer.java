package it.cavallium.warppi;

import java.nio.ByteBuffer;

public class MmapByteBuffer {
	private final int fd;
	private final int address;
	private final int length;
	private final ByteBuffer buffer;

	public MmapByteBuffer(int fd, int address, int length, ByteBuffer buffer) {
		this.fd = fd;
		this.address = address;
		this.length = length;
		this.buffer = buffer;
	}

	public int getFd() {
		return fd;
	}

	public int getAddress() {
		return address;
	}

	public int getLength() {
		return length;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
}