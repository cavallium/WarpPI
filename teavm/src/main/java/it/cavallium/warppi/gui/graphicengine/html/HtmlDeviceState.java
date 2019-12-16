package it.cavallium.warppi.gui.graphicengine.html;

import it.cavallium.warppi.device.DeviceStateDevice;

import java.util.concurrent.atomic.AtomicBoolean;

public class HtmlDeviceState implements DeviceStateDevice {
	private final HtmlEngine graphicEngine;
	private final AtomicBoolean exitWait = new AtomicBoolean(false);

	public HtmlDeviceState(HtmlEngine graphicEngine) {
		this.graphicEngine = graphicEngine;
	}

	@Override
	public void initialize() {
		graphicEngine.subscribeExit(() -> {
			exitWait.set(true);
		});
	}

	@Override
	public void waitForExit() {
		try {
			while (!exitWait.get()) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void powerOff() {
		graphicEngine.sendPowerOffSignal();
	}
}
