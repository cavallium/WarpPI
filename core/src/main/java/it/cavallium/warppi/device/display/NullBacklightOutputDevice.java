package it.cavallium.warppi.device.display;

public class NullBacklightOutputDevice implements BacklightOutputDevice {

	private double brightness;
	private boolean power;

	@Override
	public void setBrightness(double value) {
		this.brightness = value;
	}

	@Override
	public void setPower(boolean value) {
		this.power = value;
	}

}
