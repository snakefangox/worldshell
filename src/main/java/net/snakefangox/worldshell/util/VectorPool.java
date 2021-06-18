package net.snakefangox.worldshell.util;

import oimo.common.*;

/**
 * Just a static helper for Oimo's Pool class.
 * You don't *need* to use it, but it's nice.
 */
public class VectorPool {
	private static final Pool pool = new Pool();

	public static Vec3 vec3() {
		return pool.vec3();
	}

	public static Mat3 mat3() {
		return pool.mat3();
	}

	public static Mat4 mat4() {
		return pool.mat4();
	}

	public static Quat quat() {
		return pool.quat();
	}

	public static void disposeVec3(Vec3 v) {
		pool.disposeVec3(v);
	}

	public static void disposeMat3(Mat3 m) {
		pool.disposeMat3(m);
	}

	public static void disposeMat4(Mat4 m) {
		pool.disposeMat4(m);
	}

	public static void disposeQuat(Quat q) {
		pool.disposeQuat(q);
	}
}
