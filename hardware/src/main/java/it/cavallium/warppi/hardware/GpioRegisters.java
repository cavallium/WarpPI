package it.cavallium.warppi.hardware;

interface GpioRegisters extends AutoCloseable {
	int read(int byteOffset);

	void write(int byteOffset, int value);

	@Override
	void close();
}
