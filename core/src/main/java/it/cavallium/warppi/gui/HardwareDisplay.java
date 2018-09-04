package it.cavallium.warppi.gui;

public interface HardwareDisplay {
	public void initialize();

	public void shutdown();

	public void setBrightness(double value);
}
