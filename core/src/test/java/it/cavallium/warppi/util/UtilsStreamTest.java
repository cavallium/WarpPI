package it.cavallium.warppi.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class UtilsStreamTest {
	@Test
	void readsToEofAndClosesStreamsWithNoAvailableBytes() throws IOException {
		final byte[] expected = new byte[16 * 1024 + 3];
		for (int index = 0; index < expected.length; index++)
			expected[index] = (byte) (index * 17);
		final ZeroAvailableInputStream input = new ZeroAvailableInputStream(expected);

		assertArrayEquals(expected, Utils.convertStreamToByteArray(input));
		assertTrue(input.closed);
	}

	private static final class ZeroAvailableInputStream extends ByteArrayInputStream {
		private boolean closed;

		private ZeroAvailableInputStream(final byte[] bytes) {
			super(bytes);
		}

		@Override
		public int available() {
			return 0;
		}

		@Override
		public void close() throws IOException {
			closed = true;
			super.close();
		}
	}
}
