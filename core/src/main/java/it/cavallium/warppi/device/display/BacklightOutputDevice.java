package it.cavallium.warppi.device.display;

public interface BacklightOutputDevice {

	/**
	 * Returns whether this device controls a physical display backlight.
	 *
	 * <p>The conservative default keeps brightness controls hidden until a real
	 * backlight implementation explicitly advertises the capability.</p>
	 */
	default boolean supportsBrightnessControl() {
		return false;
	}
	
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
