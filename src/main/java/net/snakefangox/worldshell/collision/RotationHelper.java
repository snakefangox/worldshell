package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Vec3d;
import oimo.common.Mat3;
import oimo.common.Quat;
import oimo.common.Vec3;

public class RotationHelper {

	public static Quat identityQuat() {
		return new Quat(null, null, null, null);
	}

	public static Mat3 identityMat3() {
		return new Mat3(null, null, null, null, null, null, null, null, null);
	}

	public static Vec3 zeroVec3() {
		return new Vec3(null, null, null);
	}

	public static Vec3 rotatePosition(Mat3 mat3, double x, double y, double z) {
		return new Vec3(x, y, z).mulMat3Eq(mat3);
	}

	public static Vec3d rotatePositionMc(Mat3 mat3, double x, double y, double z) {
		Vec3 vec = new Vec3(x, y, z).mulMat3Eq(mat3);
		return new Vec3d(vec.x, vec.y, vec.z);
	}
}
