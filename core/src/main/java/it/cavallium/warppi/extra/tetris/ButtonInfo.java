package it.cavallium.warppi.extra.tetris;

public class ButtonInfo {

	public volatile int pressedCount = 0;
	public volatile int releasedCount = 0;
	public volatile int unreadCount = 0;
	
	public ButtonInfo() {
		
	}
	
	public void press() {
		if (pressedCount <= releasedCount)  {
			pressedCount = releasedCount + 1;
			unreadCount++;
		}
	}
	
	public void release() {
		releasedCount++;
		pressedCount = releasedCount;
	}
	
	public int readPressed() {
		int val = unreadCount;
		unreadCount = 0;
		return val;
	}
	
	public boolean hasUnreadData() {
		return unreadCount > 0;
	}
	
	public boolean isPressedNow() {
		return pressedCount > releasedCount;
	}
}
