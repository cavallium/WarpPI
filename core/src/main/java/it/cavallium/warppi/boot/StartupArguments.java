package it.cavallium.warppi.boot;

public interface StartupArguments {
	boolean isRaspberryModeAllowed();

	boolean isZoomed();

	boolean isCPUEngineForced();

	boolean isGPUEngineForced();

	boolean isFrameBufferEngineForced();

	boolean isNoGUIEngineForced();

	boolean isHTMLEngineForced();

	boolean isMSDOSModeEnabled();

	boolean isVerboseLoggingEnabled();

	boolean isDebugEnabled();

	boolean isUncached();

	boolean isEngineForced();

}
