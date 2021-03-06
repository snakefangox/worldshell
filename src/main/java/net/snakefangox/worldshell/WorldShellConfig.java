package net.snakefangox.worldshell;

public class WorldShellConfig {

	private static final int BUFFER_MAX = 100000;
	private static int bufferSpace = 5000;

	public static void requestLargerBuffer(int requestedSize) {
		if (requestedSize > bufferSpace && requestedSize < BUFFER_MAX) bufferSpace = requestedSize;
	}

	public static int getBufferSpace() {
		return bufferSpace;
	}
}
