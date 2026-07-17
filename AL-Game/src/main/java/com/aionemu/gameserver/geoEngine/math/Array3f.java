package com.aionemu.gameserver.geoEngine.math;

/**
 * @author MrPoke
 */
public class Array3f {

	public float a = 0;
	public float b = 0;
	public float c = 0;

	public void reset() {
		a = 0;
		b = 0;
		c = 0;
	}

	public static Array3f newInstance() {
		return new Array3f();
	}

	public static void recycle(Array3f instance) {
		// no-op in Java 25
	}
}