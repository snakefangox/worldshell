package net.snakefangox.worldshell;

public class WorldShellConfig {

	/** No idea why you'd need to go this big but if you do you only get 360,000 entities at a time */
	private static final int BUFFER_MAX = 100000;
	private static int bufferSpace = 1000;

	/**
	 * By default the maximum size of a complex WorldShellEntity is {@value bufferSpace} along each horizontal axis.
	 * This will allow a maximum of 3,600,000,000 complex entities to exist at once.
	 * Should you need a bigger max size you can request an increase with this function but doing so will decrease the capacity.
	 * Note that once a world has been created the max size of its entities is fixed.
	 *
	 * @param requestedSize the new max size you want to request
	 */
	public static void requestLargerBuffer(int requestedSize) {
		if (requestedSize > bufferSpace && requestedSize < BUFFER_MAX) bufferSpace = requestedSize;
	}

	public static int getBufferSpace() {
		return bufferSpace;
	}
}
