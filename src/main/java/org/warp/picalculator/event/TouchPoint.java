package org.warp.picalculator.event;

public class TouchPoint {
	private final float radiusX;
	private final float radiusY;
	private final float rotationAngle;
	private final float force;
	private final float x;
	private final float y;
	private final long id;

	public TouchPoint(long id, float x, float y, float radiusX, float radiusY, float force, float rotationAngle) {
		super();
		this.id = id;
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.rotationAngle = rotationAngle;
		this.force = force;
		this.x = x;
		this.y = y;
	}

	public long getID() {
		return id;
	}

	public float getRadiusX() {
		return radiusX;
	}

	public float getRadiusY() {
		return radiusY;
	}

	public float getRotationAngle() {
		return rotationAngle;
	}

	public float getForce() {
		return force;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(force);
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + Float.floatToIntBits(radiusX);
		result = prime * result + Float.floatToIntBits(radiusY);
		result = prime * result + Float.floatToIntBits(rotationAngle);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TouchPoint other = (TouchPoint) obj;
		if (Float.floatToIntBits(force) != Float.floatToIntBits(other.force))
			return false;
		if (id != other.id)
			return false;
		if (Float.floatToIntBits(radiusX) != Float.floatToIntBits(other.radiusX))
			return false;
		if (Float.floatToIntBits(radiusY) != Float.floatToIntBits(other.radiusY))
			return false;
		if (Float.floatToIntBits(rotationAngle) != Float.floatToIntBits(other.rotationAngle))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TouchPoint [id=" + id + ", radiusX=" + radiusX + ", radiusY=" + radiusY + ", rotationAngle=" + rotationAngle + ", force=" + force + ", x=" + x + ", y=" + y + "]";
	}

}
