package it.cavallium.warppi.device.display;

public class NullBacklightOutputDevice implements BacklightOutputDevice {

	@Override
	public boolean supportsBrightnessControl() {
		return false;
	}

	@Override
	public void setBrightness(double value) {
		// No physical backlight is available on this platform.
	}

	@Override
	public void setPower(boolean value) {
		// No physical backlight is available on this platform.
	}

}
