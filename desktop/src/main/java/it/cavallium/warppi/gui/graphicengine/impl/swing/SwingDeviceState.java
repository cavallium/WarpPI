package it.cavallium.warppi.gui.graphicengine.impl.swing;

import it.cavallium.warppi.device.DeviceStateDevice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SwingDeviceState implements DeviceStateDevice {
    private final SwingEngine graphicEngine;
    private final CompletableFuture<Void> exitWait;

    public SwingDeviceState(SwingEngine graphicEngine) {
        this.graphicEngine = graphicEngine;
        this.exitWait = new CompletableFuture<>();
    }

    @Override
    public void initialize() {
        graphicEngine.subscribeExit(() -> {
            exitWait.complete(null);
        });
    }

    @Override
    public Future<?> waitForExit() {
        return exitWait;
    }

    @Override
    public void powerOff() {
        graphicEngine.sendPowerOffSignal();
    }
}
