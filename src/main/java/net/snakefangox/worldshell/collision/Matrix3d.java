package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Vec3d;

/**
 * Originally written by Stuff-Stuffs, here until I figure out updating multipart entities
 * https://github.com/Stuff-Stuffs/MultipartEntities
 */
public final class Matrix3d {
	public static final Matrix3d IDENTITY = new Matrix3d(QuaternionD.IDENTITY);
	public static final Vec3d[] IDENTITY_BASIS = IDENTITY.getBasis();
	public final double m00;
	public final double m01;
	public final double m02;
	public final double m10;
	public final double m11;
	public final double m12;
	public final double m20;
	public final double m21;
	public final double m22;

	public Matrix3d(final double m00, final double m01, final double m02, final double m10, final double m11, final double m12, final double m20, final double m21, final double m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
	}

	public Matrix3d(final QuaternionD quaternion) {
		final double x = quaternion.getX();
		final double y = quaternion.getY();
		final double z = quaternion.getZ();
		final double w = quaternion.getW();
		final double x2 = 2.0F * x * x;
		final double y2 = 2.0F * y * y;
		final double z2 = 2.0F * z * z;
		m00 = 1.0F - y2 - z2;
		m11 = 1.0F - z2 - x2;
		m22 = 1.0F - x2 - y2;
		final double xy = x * y;
		final double yz = y * z;
		final double zx = z * x;
		final double xw = x * w;
		final double yw = y * w;
		final double zw = z * w;
		m10 = 2.0F * (xy + zw);
		m01 = 2.0F * (xy - zw);
		m20 = 2.0F * (zx - yw);
		m02 = 2.0F * (zx + yw);
		m21 = 2.0F * (yz + xw);
		m12 = 2.0F * (yz - xw);
	}

	public Matrix3d multiply(final Matrix3d other) {
		final double a00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20;
		final double a01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21;
		final double a02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22;
		final double a10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20;
		final double a11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21;
		final double a12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22;
		final double a20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20;
		final double a21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21;
		final double a22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22;
		return new Matrix3d(a00, a01, a02, a10, a11, a12, a20, a21, a22);
	}

	public Matrix3d invert() {
		final double m00 = this.m00;
		final double m01 = m10;
		final double m02 = m20;
		final double m10 = this.m01;
		final double m11 = this.m11;
		final double m12 = m21;
		final double m20 = this.m02;
		final double m21 = this.m12;
		final double m22 = this.m22;
		return new Matrix3d(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public Vec3d[] getBasis() {
		return new Vec3d[]{new Vec3d(m00, m10, m20), new Vec3d(m01, m11, m21), new Vec3d(m02, m12, m22)};
	}

	public Vec3d transform(final Vec3d v) {
		return new Vec3d(m00 * v.x + m01 * v.y + m02 * v.z, m10 * v.x + m11 * v.y + m12 * v.z, m20 * v.x + m21 * v.y + m22 * v.z);
	}

	public Vec3d transform(final double x, final double y, final double z) {
		return new Vec3d(m00 * x + m01 * y + m02 * z, m10 * x + m11 * y + m12 * z, m20 * x + m21 * y + m22 * z);
	}

	public double transformX(final double x, final double y, final double z) {
		return m00 * x + m01 * y + m02 * z;
	}

	public double transformY(final double x, final double y, final double z) {
		return m10 * x + m11 * y + m12 * z;
	}

	public double transformZ(final double x, final double y, final double z) {
		return m20 * x + m21 * y + m22 * z;
	}
}
