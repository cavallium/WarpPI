package ar.com.hjg.pngj.pixels;

final public class DeflaterEstimatorHjg {

	/**
	 * This object is stateless, it's thread safe and can be reused
	 */
	public DeflaterEstimatorHjg() {}

	/**
	 * Estimates the length of the compressed bytes, as compressed by Lz4
	 * WARNING: if larger than LZ4_64K_LIMIT it cuts it
	 * in fragments
	 * 
	 * WARNING: if some part of the input is discarded, this should return the
	 * proportional (so that
	 * returnValue/srcLen=compressionRatio)
	 * 
	 * @param src
	 * @param srcOff
	 * @param srcLen
	 * @return length of the compressed bytes
	 */
	public int compressEstim(final byte[] src, int srcOff, final int srcLen) {
		if (srcLen < 10)
			return srcLen; // too small
		int stride = DeflaterEstimatorHjg.LZ4_64K_LIMIT - 1;
		final int segments = (srcLen + stride - 1) / stride;
		stride = srcLen / segments;
		if (stride >= DeflaterEstimatorHjg.LZ4_64K_LIMIT - 1 || stride * segments > srcLen || segments < 1 || stride < 1)
			throw new RuntimeException("?? " + srcLen);
		int bytesIn = 0;
		int bytesOut = 0;
		int len = srcLen;
		while (len > 0) {
			if (len > stride)
				len = stride;
			bytesOut += DeflaterEstimatorHjg.compress64k(src, srcOff, len);
			srcOff += len;
			bytesIn += len;
			len = srcLen - bytesIn;
		}
		final double ratio = bytesOut / (double) bytesIn;
		return bytesIn == srcLen ? bytesOut : (int) (ratio * srcLen + 0.5);
	}

	public int compressEstim(final byte[] src) {
		return compressEstim(src, 0, src.length);
	}

	static final int MEMORY_USAGE = 14;
	static final int NOT_COMPRESSIBLE_DETECTION_LEVEL = 6; // see SKIP_STRENGTH

	static final int MIN_MATCH = 4;

	static final int HASH_LOG = DeflaterEstimatorHjg.MEMORY_USAGE - 2;
	static final int HASH_TABLE_SIZE = 1 << DeflaterEstimatorHjg.HASH_LOG;

	static final int SKIP_STRENGTH = Math.max(DeflaterEstimatorHjg.NOT_COMPRESSIBLE_DETECTION_LEVEL, 2); // 6 findMatchAttempts =
	// 2^SKIP_STRENGTH+3
	static final int COPY_LENGTH = 8;
	static final int LAST_LITERALS = 5;
	static final int MF_LIMIT = DeflaterEstimatorHjg.COPY_LENGTH + DeflaterEstimatorHjg.MIN_MATCH;
	static final int MIN_LENGTH = DeflaterEstimatorHjg.MF_LIMIT + 1;

	static final int MAX_DISTANCE = 1 << 16;

	static final int ML_BITS = 4;
	static final int ML_MASK = (1 << DeflaterEstimatorHjg.ML_BITS) - 1;
	static final int RUN_BITS = 8 - DeflaterEstimatorHjg.ML_BITS;
	static final int RUN_MASK = (1 << DeflaterEstimatorHjg.RUN_BITS) - 1;

	static final int LZ4_64K_LIMIT = (1 << 16) + DeflaterEstimatorHjg.MF_LIMIT - 1;
	static final int HASH_LOG_64K = DeflaterEstimatorHjg.HASH_LOG + 1;
	static final int HASH_TABLE_SIZE_64K = 1 << DeflaterEstimatorHjg.HASH_LOG_64K;

	static final int HASH_LOG_HC = 15;
	static final int HASH_TABLE_SIZE_HC = 1 << DeflaterEstimatorHjg.HASH_LOG_HC;
	static final int OPTIMAL_ML = DeflaterEstimatorHjg.ML_MASK - 1 + DeflaterEstimatorHjg.MIN_MATCH;

	static int compress64k(final byte[] src, final int srcOff, final int srcLen) {
		final int srcEnd = srcOff + srcLen;
		final int srcLimit = srcEnd - DeflaterEstimatorHjg.LAST_LITERALS;
		final int mflimit = srcEnd - DeflaterEstimatorHjg.MF_LIMIT;

		int sOff = srcOff, dOff = 0;

		int anchor = sOff;

		if (srcLen >= DeflaterEstimatorHjg.MIN_LENGTH) {

			final short[] hashTable = new short[DeflaterEstimatorHjg.HASH_TABLE_SIZE_64K];

			++sOff;

			main: while (true) {

				// find a match
				int forwardOff = sOff;

				int ref;
				int findMatchAttempts1 = (1 << DeflaterEstimatorHjg.SKIP_STRENGTH) + 3; // 64+3=67
				do {
					sOff = forwardOff;
					forwardOff += findMatchAttempts1++ >>> DeflaterEstimatorHjg.SKIP_STRENGTH;

					if (forwardOff > mflimit)
						break main; // ends all

					final int h = DeflaterEstimatorHjg.hash64k(DeflaterEstimatorHjg.readInt(src, sOff));
					ref = srcOff + DeflaterEstimatorHjg.readShort(hashTable, h);
					DeflaterEstimatorHjg.writeShort(hashTable, h, sOff - srcOff);
				} while (!DeflaterEstimatorHjg.readIntEquals(src, ref, sOff));

				// catch up
				final int excess = DeflaterEstimatorHjg.commonBytesBackward(src, ref, sOff, srcOff, anchor);
				sOff -= excess;
				ref -= excess;
				// sequence == refsequence
				final int runLen = sOff - anchor;
				dOff++;

				if (runLen >= DeflaterEstimatorHjg.RUN_MASK) {
					if (runLen > DeflaterEstimatorHjg.RUN_MASK)
						dOff += (runLen - DeflaterEstimatorHjg.RUN_MASK) / 0xFF;
					dOff++;
				}
				dOff += runLen;
				while (true) {
					// encode offset
					dOff += 2;
					// count nb matches
					sOff += DeflaterEstimatorHjg.MIN_MATCH;
					ref += DeflaterEstimatorHjg.MIN_MATCH;
					final int matchLen = DeflaterEstimatorHjg.commonBytes(src, ref, sOff, srcLimit);
					sOff += matchLen;
					// encode match len
					if (matchLen >= DeflaterEstimatorHjg.ML_MASK) {
						if (matchLen >= DeflaterEstimatorHjg.ML_MASK + 0xFF)
							dOff += (matchLen - DeflaterEstimatorHjg.ML_MASK) / 0xFF;
						dOff++;
					}
					// test end of chunk
					if (sOff > mflimit) {
						anchor = sOff;
						break main;
					}
					// fill table
					DeflaterEstimatorHjg.writeShort(hashTable, DeflaterEstimatorHjg.hash64k(DeflaterEstimatorHjg.readInt(src, sOff - 2)), sOff - 2 - srcOff);
					// test next position
					final int h = DeflaterEstimatorHjg.hash64k(DeflaterEstimatorHjg.readInt(src, sOff));
					ref = srcOff + DeflaterEstimatorHjg.readShort(hashTable, h);
					DeflaterEstimatorHjg.writeShort(hashTable, h, sOff - srcOff);
					if (!DeflaterEstimatorHjg.readIntEquals(src, sOff, ref))
						break;
					dOff++;
				}
				// prepare next loop
				anchor = sOff++;
			}
		}
		final int runLen = srcEnd - anchor;
		if (runLen >= DeflaterEstimatorHjg.RUN_MASK + 0xFF)
			dOff += (runLen - DeflaterEstimatorHjg.RUN_MASK) / 0xFF;
		dOff++;
		dOff += runLen;
		return dOff;
	}

	static int maxCompressedLength(final int length) {
		if (length < 0)
			throw new IllegalArgumentException("length must be >= 0, got " + length);
		return length + length / 255 + 16;
	}

	static int hash(final int i) {
		return i * -1640531535 >>> DeflaterEstimatorHjg.MIN_MATCH * 8 - DeflaterEstimatorHjg.HASH_LOG;
	}

	static int hash64k(final int i) {
		return i * -1640531535 >>> DeflaterEstimatorHjg.MIN_MATCH * 8 - DeflaterEstimatorHjg.HASH_LOG_64K;
	}

	static int readShortLittleEndian(final byte[] buf, final int i) {
		return buf[i] & 0xFF | (buf[i + 1] & 0xFF) << 8;
	}

	static boolean readIntEquals(final byte[] buf, final int i, final int j) {
		return buf[i] == buf[j] && buf[i + 1] == buf[j + 1] && buf[i + 2] == buf[j + 2] && buf[i + 3] == buf[j + 3];
	}

	static int commonBytes(final byte[] b, int o1, int o2, final int limit) {
		int count = 0;
		while (o2 < limit && b[o1++] == b[o2++])
			++count;
		return count;
	}

	static int commonBytesBackward(final byte[] b, int o1, int o2, final int l1, final int l2) {
		int count = 0;
		while (o1 > l1 && o2 > l2 && b[--o1] == b[--o2])
			++count;
		return count;
	}

	static int readShort(final short[] buf, final int off) {
		return buf[off] & 0xFFFF;
	}

	static byte readByte(final byte[] buf, final int i) {
		return buf[i];
	}

	static void checkRange(final byte[] buf, final int off) {
		if (off < 0 || off >= buf.length)
			throw new ArrayIndexOutOfBoundsException(off);
	}

	static void checkRange(final byte[] buf, final int off, final int len) {
		DeflaterEstimatorHjg.checkLength(len);
		if (len > 0) {
			DeflaterEstimatorHjg.checkRange(buf, off);
			DeflaterEstimatorHjg.checkRange(buf, off + len - 1);
		}
	}

	static void checkLength(final int len) {
		if (len < 0)
			throw new IllegalArgumentException("lengths must be >= 0");
	}

	static int readIntBE(final byte[] buf, final int i) {
		return (buf[i] & 0xFF) << 24 | (buf[i + 1] & 0xFF) << 16 | (buf[i + 2] & 0xFF) << 8 | buf[i + 3] & 0xFF;
	}

	static int readIntLE(final byte[] buf, final int i) {
		return buf[i] & 0xFF | (buf[i + 1] & 0xFF) << 8 | (buf[i + 2] & 0xFF) << 16 | (buf[i + 3] & 0xFF) << 24;
	}

	static int readInt(final byte[] buf, final int i) {
		return DeflaterEstimatorHjg.readIntBE(buf, i);
	}

	static void writeShort(final short[] buf, final int off, final int v) {
		buf[off] = (short) v;
	}

}
