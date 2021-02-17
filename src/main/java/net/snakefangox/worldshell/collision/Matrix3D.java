package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

/**
 * A custom 3D Matrix implementation because Minecraft's is missing things we need and has functions stripped on the server
 */
public class Matrix3D {

	public double m00, m10, m20;
	public double m01, m11, m21;
	public double m02, m12, m22;

	/*	OldRep
	public double m00, m01, m02;
	public double m10, m11, m12;
	public double m20, m21, m22;
	*/

	public Matrix3D() {
	}

	public Matrix3D(double n00, double n01, double n02, double n10, double n11, double n12, double n20, double n21, double n22) {
		m00 = n00;
		m01 = n01;
		m02 = n02;
		m10 = n10;
		m11 = n11;
		m12 = n12;
		m20 = n20;
		m21 = n21;
		m22 = n22;
	}

	public Matrix3D(Quaternion quaternion) {
		fromQuaternion(quaternion);
	}

	public void fromQuaternion(Quaternion quaternion) {
		float x = quaternion.getX();
		float y = quaternion.getY();
		float z = quaternion.getZ();
		float w = quaternion.getW();
		double i = 2.0F * x * x;
		double j = 2.0F * y * y;
		double k = 2.0F * z * z;
		m00 = 1.0F - j - k;
		m11 = 1.0F - k - i;
		m22 = 1.0F - i - j;
		double q = y * w;
		double r = z * w;
		double s = y * z;
		double t = x * y;
		double u = z * x;
		double v = x * w;
		m10 = 2.0F * (t + r);
		m01 = 2.0F * (t - r);
		m20 = 2.0F * (u - q);
		m02 = 2.0F * (u + q);
		m21 = 2.0F * (s + v);
		m12 = 2.0F * (s - v);
	}

	public Matrix3D copy() {
		return new Matrix3D(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public Vec3d rotate(Vec3d v) {
		return new Vec3d(m00 * v.x + m01 * v.y + m02 * v.z, m10 * v.x + m11 * v.y + m12 * v.z, m20 * v.x + m21 * v.y + m22 * v.z);
	}

	public void rotate(double x, double y, double z, ShellCollisionHull.Vec3dM vec) {
		vec.setAll(m00 * x + m01 * y + m02 * z, m10 * x + m11 * y + m12 * z, m20 * x + m21 * y + m22 * z);
	}

	public void rotate(ShellCollisionHull.Vec3dM vec) {
		vec.setAll(m00 * vec.x + m01 * vec.y + m02 * vec.z, m10 * vec.x + m11 * vec.y + m12 * vec.z, m20 * vec.x + m21 * vec.y + m22 * vec.z);
	}

}
