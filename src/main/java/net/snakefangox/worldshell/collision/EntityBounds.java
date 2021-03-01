package net.snakefangox.worldshell.collision;

import net.minecraft.entity.EntityDimensions;

public class EntityBounds extends EntityDimensions {
	//The X size of the entity
	public final float length;

	public EntityBounds(float length, float height, float width, boolean fixed) {
		super(width, height, fixed);
		this.length = length;
	}

	public float getRoughMaxDist() {
		return length + height + width;
	}

	@Override
	public String toString() {
		return "EntityBounds{" +
				"length=" + length +
				", width=" + width +
				", height=" + height +
				", fixed=" + fixed +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EntityBounds)) return false;

		EntityBounds that = (EntityBounds) o;

		if (Float.compare(that.length, length) != 0) return false;
		if (Float.compare(that.width, width) != 0) return false;
		if (Float.compare(that.height, height) != 0) return false;
		return fixed == that.fixed;
	}

	@Override
	public int hashCode() {
		int result = (length != +0.0f ? Float.floatToIntBits(length) : 0);
		result = 31 * result + (width != +0.0f ? Float.floatToIntBits(width) : 0);
		result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
		result = 31 * result + (fixed ? 1 : 0);
		return result;
	}
}
