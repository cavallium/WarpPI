package it.cavallium.warppi.device.display;

public interface BacklightOutputDevice {
	
	/**
	 * Set the brightness level
	 * @param value Value from 0.0 to 1.0
	 */
	void setBrightness(double value);
	
	/**
	 * Turn on or off the backlight
	 * @param value true is ON, false is OFF
	 */
	void setPower(boolean value);
}
