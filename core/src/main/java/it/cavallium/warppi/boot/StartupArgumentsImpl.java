package it.cavallium.warppi.boot;

public class StartupArgumentsImpl implements StartupArguments {

	StartupArgumentsImpl() {
		isRaspberryModeAllowed = true;
	}

	private boolean isRaspberryModeAllowed;
	private boolean isZoomed;
	private boolean isCPUEngineForced;
	private boolean isGPUEngineForced;
	private boolean isFrameBufferEngineForced;
	private boolean isNoGUIEngineForced;
	private boolean isHTMLEngineForced;
	private boolean isMSDOSModeEnabled;
	private boolean isVerboseLoggingEnabled;
	private boolean isDebugEnabled;
	private boolean isUncached;

	@Override
	public boolean isRaspberryModeAllowed() {
		return isRaspberryModeAllowed;
	}

	@Override
	public boolean isZoomed() {
		return isZoomed;
	}

	@Override
	public boolean isCPUEngineForced() {
		return isCPUEngineForced;
	}

	@Override
	public boolean isGPUEngineForced() {
		return isGPUEngineForced;
	}

	@Override
	public boolean isFrameBufferEngineForced() {
		return isFrameBufferEngineForced;
	}

	@Override
	public boolean isNoGUIEngineForced() {
		return isNoGUIEngineForced;
	}

	@Override
	public boolean isHTMLEngineForced() {
		return isHTMLEngineForced;
	}

	@Override
	public boolean isMSDOSModeEnabled() {
		return isMSDOSModeEnabled;
	}

	@Override
	public boolean isVerboseLoggingEnabled() {
		return isVerboseLoggingEnabled;
	}

	@Override
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	@Override
	public boolean isUncached() {
		return isUncached;
	}

	@Override
	public boolean isEngineForced() {
		return isCPUEngineForced || isFrameBufferEngineForced || isGPUEngineForced || isHTMLEngineForced || isNoGUIEngineForced;
	}

	void setRaspberryModeAllowed(final boolean isRaspberryModeAllowed) {
		this.isRaspberryModeAllowed = isRaspberryModeAllowed;
	}

	void setZoomed(final boolean isZoomed) {
		this.isZoomed = isZoomed;
	}

	void setCPUEngineForced(final boolean isCPUEngineForced) {
		this.isCPUEngineForced = isCPUEngineForced;
	}

	void setGPUEngineForced(final boolean isGPUEngineForced) {
		this.isGPUEngineForced = isGPUEngineForced;
	}

	void setFrameBufferEngineForced(final boolean isFrameBufferEngineForced) {
		this.isFrameBufferEngineForced = isFrameBufferEngineForced;
	}

	void setNoGUIEngineForced(final boolean isNoGUIEngineForced) {
		this.isNoGUIEngineForced = isNoGUIEngineForced;
	}

	void setHTMLEngineForced(final boolean isHTMLEngineForced) {
		this.isHTMLEngineForced = isHTMLEngineForced;
	}

	void setMSDOSModeEnabled(final boolean isMSDOSModeEnabled) {
		this.isMSDOSModeEnabled = isMSDOSModeEnabled;
	}

	void setVerboseLoggingEnabled(final boolean isVerboseLoggingEnabled) {
		this.isVerboseLoggingEnabled = isVerboseLoggingEnabled;
	}

	void setDebugEnabled(final boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

	void setUncached(final boolean isUncached) {
		this.isUncached = isUncached;
	}

	@Override
	public String toString() {
		return "StartupArgumentsImpl [isRaspberryModeAllowed=" + isRaspberryModeAllowed + ", isZoomed=" + isZoomed + ", isCPUEngineForced=" + isCPUEngineForced + ", isGPUEngineForced=" + isGPUEngineForced + ", isFrameBufferEngineForced=" + isFrameBufferEngineForced + ", isNoGUIEngineForced=" + isNoGUIEngineForced + ", isHTMLEngineForced=" + isHTMLEngineForced + ", isMSDOSModeEnabled=" + isMSDOSModeEnabled + ", isVerboseLoggingEnabled=" + isVerboseLoggingEnabled + ", isDebugEnabled=" + isDebugEnabled + ", isUncached=" + isUncached + "]";
	}

}
