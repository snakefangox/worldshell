package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.util.VectorPool;
import oimo.collision.geometry.Aabb;
import oimo.common.Mat3;
import oimo.common.Quat;
import oimo.common.Vec3;

import java.util.Arrays;

public class RotationHelper {

	public static Quat identityQuat() {
		return new Quat(null, null, null, null);
	}

	public static Mat3 identityMat3() {
		return new Mat3(null, null, null, null, null, null, null, null, null);
	}

	public static Vec3 of(Vec3d vec3d) {
		return VectorPool.vec3().init(vec3d.x, vec3d.y, vec3d.z);
	}

	public static Vec3d of(Vec3 vec3) {
		return new Vec3d(vec3.x, vec3.y, vec3.z);
	}

	public static Quaternion quatToMc(Quat quat) {
		return new Quaternion((float) quat.x, (float) quat.y, (float) quat.z, (float) quat.w);
	}

	public static Vec3 rotatePosition(Mat3 mat3, double x, double y, double z) {
		return VectorPool.vec3().init(x, y, z).mulMat3Eq(mat3);
	}

	public static Vec3d rotatePositionMc(Mat3 mat3, double x, double y, double z) {
		Vec3 vec = VectorPool.vec3().init(x, y, z).mulMat3Eq(mat3);
		return new Vec3d(vec.x, vec.y, vec.z);
	}

	public static Aabb of(Box box) {
		Aabb aabb = new Aabb();
		aabb._minX = box.minX;
		aabb._minY = box.minY;
		aabb._minZ = box.minZ;
		aabb._maxX = box.maxX;
		aabb._maxY = box.maxY;
		aabb._maxZ = box.maxZ;
		return aabb;
	}

	public static double[][] matToArray(Mat3 mat3) {
		return new double[][]{{mat3.e00, mat3.e10, mat3.e20},
				{mat3.e01, mat3.e11, mat3.e12},
				{mat3.e20, mat3.e21, mat3.e22}};
	}

	public static Box transformBox(Box box, Mat3 rot, Vec3 trans) {
		double[][] matArray = RotationHelper.matToArray(rot);
		double[] oMin = new double[]{box.minX, box.minY, box.minZ};
		double[] oMax = new double[]{box.maxX, box.maxY, box.maxZ};
		double[] nMin = new double[]{trans.x, trans.y, trans.z};
		double[] nMax = Arrays.copyOf(nMin, nMin.length);

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				double a = matArray[i][j] * oMin[j];
				double b = matArray[i][j] * oMax[j];

				nMin[i] += Math.min(a, b);
				nMax[i] += Math.max(a, b);
			}
		}

		return new Box(nMin[0], nMin[1], nMin[2], nMax[0], nMax[1], nMax[2]);
	}
}
