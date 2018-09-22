package it.cavallium.warppi.gui;

public interface HardwareDisplay {
	void initialize();

	void shutdown();

	void setBrightness(double value);
}
