package org.warp.picalculator.boot;

public class StartupArgumentsImpl implements StartupArguments {

	StartupArgumentsImpl() {
		isRaspberryModeAllowed = true;
	}
	
	private boolean isRaspberryModeAllowed;
	private boolean isZoomed;
	private boolean isHeadlessEngineForced;
	private boolean isHeadless8EngineForced;
	private boolean isHeadless256EngineForced;
	private boolean isHeadless24bitEngineForced;
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
	public boolean isHeadlessEngineForced() {
		return isHeadlessEngineForced ||
				isHeadless8EngineForced ||
				isHeadless256EngineForced ||
				isHeadless24bitEngineForced;
	}

	@Override
	public boolean isHeadless8EngineForced() {
		return isHeadless8EngineForced;
	}

	@Override
	public boolean isHeadless256EngineForced() {
		return isHeadless256EngineForced;
	}

	@Override
	public boolean isHeadless24bitEngineForced() {
		return isHeadless24bitEngineForced;
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
		return isCPUEngineForced
				|| isFrameBufferEngineForced
				|| isGPUEngineForced
				|| isHeadless24bitEngineForced
				|| isHeadless256EngineForced
				|| isHeadless8EngineForced
				|| isHTMLEngineForced
				|| isNoGUIEngineForced;
	}
	
	void setRaspberryModeAllowed(boolean isRaspberryModeAllowed) {
		this.isRaspberryModeAllowed = isRaspberryModeAllowed;
	}

	void setZoomed(boolean isZoomed) {
		this.isZoomed = isZoomed;
	}

	void setHeadlessEngineForced(boolean isHeadlessEngineForced) {
		this.isHeadlessEngineForced = isHeadlessEngineForced;
	}

	void setHeadless8EngineForced(boolean isHeadless8EngineForced) {
		this.isHeadless8EngineForced = isHeadless8EngineForced;
	}

	void setHeadless256EngineForced(boolean isHeadless256EngineForced) {
		this.isHeadless256EngineForced = isHeadless256EngineForced;
	}

	void setHeadless24bitEngineForced(boolean isHeadless24bitEngineForced) {
		this.isHeadless24bitEngineForced = isHeadless24bitEngineForced;
	}

	void setCPUEngineForced(boolean isCPUEngineForced) {
		this.isCPUEngineForced = isCPUEngineForced;
	}

	void setGPUEngineForced(boolean isGPUEngineForced) {
		this.isGPUEngineForced = isGPUEngineForced;
	}

	void setFrameBufferEngineForced(boolean isFrameBufferEngineForced) {
		this.isFrameBufferEngineForced = isFrameBufferEngineForced;
	}

	void setNoGUIEngineForced(boolean isNoGUIEngineForced) {
		this.isNoGUIEngineForced = isNoGUIEngineForced;
	}

	void setHTMLEngineForced(boolean isHTMLEngineForced) {
		this.isHTMLEngineForced = isHTMLEngineForced;
	}

	void setMSDOSModeEnabled(boolean isMSDOSModeEnabled) {
		this.isMSDOSModeEnabled = isMSDOSModeEnabled;
	}

	void setVerboseLoggingEnabled(boolean isVerboseLoggingEnabled) {
		this.isVerboseLoggingEnabled = isVerboseLoggingEnabled;
	}

	void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

	void setUncached(boolean isUncached) {
		this.isUncached = isUncached;
	}

}
