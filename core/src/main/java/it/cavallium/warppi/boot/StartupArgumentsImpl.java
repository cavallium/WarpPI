package it.cavallium.warppi.boot;

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
		return isHeadlessEngineForced || isHeadless8EngineForced || isHeadless256EngineForced || isHeadless24bitEngineForced;
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
		return isCPUEngineForced || isFrameBufferEngineForced || isGPUEngineForced || isHeadless24bitEngineForced || isHeadless256EngineForced || isHeadless8EngineForced || isHTMLEngineForced || isNoGUIEngineForced;
	}

	void setRaspberryModeAllowed(final boolean isRaspberryModeAllowed) {
		this.isRaspberryModeAllowed = isRaspberryModeAllowed;
	}

	void setZoomed(final boolean isZoomed) {
		this.isZoomed = isZoomed;
	}

	void setHeadlessEngineForced(final boolean isHeadlessEngineForced) {
		this.isHeadlessEngineForced = isHeadlessEngineForced;
	}

	void setHeadless8EngineForced(final boolean isHeadless8EngineForced) {
		this.isHeadless8EngineForced = isHeadless8EngineForced;
	}

	void setHeadless256EngineForced(final boolean isHeadless256EngineForced) {
		this.isHeadless256EngineForced = isHeadless256EngineForced;
	}

	void setHeadless24bitEngineForced(final boolean isHeadless24bitEngineForced) {
		this.isHeadless24bitEngineForced = isHeadless24bitEngineForced;
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
		return "StartupArguments = {\n\t\"isRaspberryModeAllowed\": \"" + isRaspberryModeAllowed + "\",\n\tisZoomed\": \"" + isZoomed + "\",\n\tisHeadlessEngineForced\": \"" + isHeadlessEngineForced + "\",\n\tisHeadless8EngineForced\": \"" + isHeadless8EngineForced + "\",\n\tisHeadless256EngineForced\": \"" + isHeadless256EngineForced + "\",\n\tisHeadless24bitEngineForced\": \"" + isHeadless24bitEngineForced + "\",\n\tisCPUEngineForced\": \"" + isCPUEngineForced + "\",\n\tisGPUEngineForced\": \"" + isGPUEngineForced + "\",\n\tisFrameBufferEngineForced\": \"" + isFrameBufferEngineForced + "\",\n\tisNoGUIEngineForced\": \"" + isNoGUIEngineForced + "\",\n\tisHTMLEngineForced\": \"" + isHTMLEngineForced + "\",\n\tisMSDOSModeEnabled\": \"" + isMSDOSModeEnabled + "\",\n\tisVerboseLoggingEnabled\": \"" + isVerboseLoggingEnabled + "\",\n\tisDebugEnabled\": \"" + isDebugEnabled + "\",\n\tisUncached\": \"" + isUncached + "\"\n}";
	}

}
