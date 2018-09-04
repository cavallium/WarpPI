package it.cavallium.warppi.gui.expression;

public class Caret {

	private int pos;
	private int remaining;
	private CaretState state;
	private final int[] lastSize;
	private final int[] lastLocation;

	public Caret(CaretState state, int pos) {
		this(state, pos, new int[] { 0, 0 }, new int[] { 2, 5 });
	}

	public Caret(CaretState state, int pos, int[] lastLocation, int[] lastSize) {
		this.state = state;
		this.pos = pos;
		remaining = pos;
		this.lastLocation = lastLocation;
		this.lastSize = lastSize;
	}

	public void skip(int i) {
		remaining -= i;
	}

	public int getPosition() {
		return pos;
	}

	public int getRemaining() {
		return remaining;
	}

	public CaretState getState() {
		return state;
	}

	public void flipState() {
		if (state == CaretState.VISIBLE_ON) {
			state = CaretState.VISIBLE_OFF;
		} else if (state == CaretState.VISIBLE_OFF) {
			state = CaretState.VISIBLE_ON;
		}
	}

	public void turnOn() {
		if (state == CaretState.VISIBLE_OFF) {
			state = CaretState.VISIBLE_ON;
		}
	}

	public void setPosition(int i) {
		pos = i;
	}

	public void resetRemaining() {
		remaining = pos;
	}

	public void setLastLocation(int x, int y) {
		lastLocation[0] = x;
		lastLocation[1] = y;
	}

	public int[] getLastLocation() {
		return new int[] { lastLocation[0], lastLocation[1] };
	}

	public void setLastSize(int w, int h) {
		lastSize[0] = w;
		lastSize[1] = h;
	}

	public int[] getLastSize() {
		return new int[] { lastSize[0], lastSize[1] };
	}

}
