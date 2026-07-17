package com.aionemu.commons.utils.internal.chmv8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.aionemu.commons.utils.SystemPropertyUtil;

import sun.misc.Unsafe;

/**
 * The {@link PlatformDependent} operations which requires access to
 * {@code sun.misc.*}.
 */
final class PlatformDependent0 {

	private static final Unsafe UNSAFE;
	private static final long ADDRESS_FIELD_OFFSET;
	/**
	 * {@code true} if and only if the platform supports unaligned access.
	 */
	private static final boolean UNALIGNED;

	static {
		Field addressField;
		try {
			addressField = Buffer.class.getDeclaredField("address");
			addressField.setAccessible(true);
			if (addressField.getLong(ByteBuffer.allocate(1)) != 0) {
				addressField = null;
			} else {
				ByteBuffer direct = ByteBuffer.allocateDirect(1);
				if (addressField.getLong(direct) == 0) {
					addressField = null;
				}
			}
		} catch (Throwable t) {
			addressField = null;
		}

		Unsafe unsafe;
		if (addressField != null) {
			try {
				Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				unsafe = (Unsafe) unsafeField.get(null);
				unsafe.getClass().getDeclaredMethod("copyMemory",
						new Class[] { Object.class, long.class, Object.class, long.class, long.class });
			} catch (Throwable cause) {
				unsafe = null;
			}
		} else {
			unsafe = null;
		}
		UNSAFE = unsafe;

		if (unsafe == null) {
			ADDRESS_FIELD_OFFSET = -1;
			UNALIGNED = false;
		} else {
			ADDRESS_FIELD_OFFSET = objectFieldOffset(addressField);

			boolean unaligned;
			try {
				Class<?> bitsClass = Class.forName("java.nio.Bits", false, ClassLoader.getSystemClassLoader());
				Method unalignedMethod = bitsClass.getDeclaredMethod("unaligned");
				unalignedMethod.setAccessible(true);
				unaligned = Boolean.TRUE.equals(unalignedMethod.invoke(null));
			} catch (Throwable t) {
				String arch = SystemPropertyUtil.get("os.arch", "");
				unaligned = arch.matches("^(i[3-6]86|x86(_64)?|x64|amd64)$");
			}
			UNALIGNED = unaligned;
		}
	}

	static boolean hasUnsafe() {
		return UNSAFE != null;
	}

	static void throwException(Throwable t) {
		UNSAFE.throwException(t);
	}

	static void freeDirectBuffer(ByteBuffer buffer) {
		// In Java 9+, Cleaner is no longer accessible via sun.misc.
		// The JDK handles direct buffer cleanup automatically.
		// This method is kept for API compatibility but does nothing.
	}

	static long directBufferAddress(ByteBuffer buffer) {
		return getLong(buffer, ADDRESS_FIELD_OFFSET);
	}

	static long arrayBaseOffset() {
		return UNSAFE.arrayBaseOffset(byte[].class);
	}

	static Object getObject(Object object, long fieldOffset) {
		return UNSAFE.getObject(object, fieldOffset);
	}

	static int getInt(Object object, long fieldOffset) {
		return UNSAFE.getInt(object, fieldOffset);
	}

	private static long getLong(Object object, long fieldOffset) {
		return UNSAFE.getLong(object, fieldOffset);
	}

	static long objectFieldOffset(Field field) {
		return UNSAFE.objectFieldOffset(field);
	}

	static byte getByte(long address) {
		return UNSAFE.getByte(address);
	}

	static short getShort(long address) {
		if (UNALIGNED) {
			return UNSAFE.getShort(address);
		} else {
			return (short) (getByte(address) << 8 | getByte(address + 1) & 0xff);
		}
	}

	static int getInt(long address) {
		if (UNALIGNED) {
			return UNSAFE.getInt(address);
		} else {
			return getByte(address) << 24 | (getByte(address + 1) & 0xff) << 16 | (getByte(address + 2) & 0xff) << 8
					| getByte(address + 3) & 0xff;
		}
	}

	static long getLong(long address) {
		if (UNALIGNED) {
			return UNSAFE.getLong(address);
		} else {
			return (long) getByte(address) << 56 | ((long) getByte(address + 1) & 0xff) << 48
					| ((long) getByte(address + 2) & 0xff) << 40 | ((long) getByte(address + 3) & 0xff) << 32
					| ((long) getByte(address + 4) & 0xff) << 24 | ((long) getByte(address + 5) & 0xff) << 16
					| ((long) getByte(address + 6) & 0xff) << 8 | (long) getByte(address + 7) & 0xff;
		}
	}

	static void putByte(long address, byte value) {
		UNSAFE.putByte(address, value);
	}

	static void putShort(long address, short value) {
		if (UNALIGNED) {
			UNSAFE.putShort(address, value);
		} else {
			putByte(address, (byte) (value >>> 8));
			putByte(address + 1, (byte) value);
		}
	}

	static void putInt(long address, int value) {
		if (UNALIGNED) {
			UNSAFE.putInt(address, value);
		} else {
			putByte(address, (byte) (value >>> 24));
			putByte(address + 1, (byte) (value >>> 16));
			putByte(address + 2, (byte) (value >>> 8));
			putByte(address + 3, (byte) value);
		}
	}

	static void putLong(long address, long value) {
		if (UNALIGNED) {
			UNSAFE.putLong(address, value);
		} else {
			putByte(address, (byte) (value >>> 56));
			putByte(address + 1, (byte) (value >>> 48));
			putByte(address + 2, (byte) (value >>> 40));
			putByte(address + 3, (byte) (value >>> 32));
			putByte(address + 4, (byte) (value >>> 24));
			putByte(address + 5, (byte) (value >>> 16));
			putByte(address + 6, (byte) (value >>> 8));
			putByte(address + 7, (byte) value);
		}
	}

	static void copyMemory(long srcAddr, long dstAddr, long length) {
		UNSAFE.copyMemory(srcAddr, dstAddr, length);
	}

	static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length) {
		UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, length);
	}

	private PlatformDependent0() {
	}
}