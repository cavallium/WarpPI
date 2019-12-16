package it.cavallium.warppi.gui.graphicengine.impl.swing;

import it.cavallium.warppi.device.DeviceStateDevice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class SwingDeviceState implements DeviceStateDevice {
    private final SwingEngine graphicEngine;
    private final AtomicBoolean exitWait = new AtomicBoolean(false);

    public SwingDeviceState(SwingEngine graphicEngine) {
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
