package org.warp.picalculator.boot;

public interface StartupArguments {
	public boolean isRaspberryModeAllowed();

	public boolean isZoomed();

	public boolean isHeadlessEngineForced();

	public boolean isHeadless8EngineForced();

	public boolean isHeadless256EngineForced();

	public boolean isHeadless24bitEngineForced();

	public boolean isCPUEngineForced();

	public boolean isGPUEngineForced();

	public boolean isFrameBufferEngineForced();

	public boolean isNoGUIEngineForced();

	public boolean isHTMLEngineForced();

	public boolean isMSDOSModeEnabled();

	public boolean isVerboseLoggingEnabled();

	public boolean isDebugEnabled();

	public boolean isUncached();

	public boolean isEngineForced();

}
