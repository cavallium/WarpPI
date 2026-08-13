package it.cavallium.warppi.hardware;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.zip.CRC32;

import it.cavallium.warppi.Platform.Settings;

/**
 * Raspberry settings stored as one compact, fixed-size binary snapshot.
 *
 * <p>The 12-byte format contains a magic/version field, flags, the exact
 * BCM2835 PWM duty cycle, and a CRC-32. It is deliberately hardware-specific:
 * startup requires one small read and updates replace one small file
 * atomically.</p>
 */
public final class HardwareSettings implements Settings {
	private static final String SETTINGS_FILE_PROPERTY = "warppi.settings.file";
	private static final Path DEFAULT_SETTINGS_FILE =
			Paths.get("/var/lib/warppi/settings.bin");
	private static final int MAGIC = 0x5750_5331; // WPS1
	private static final int FORMAT_SIZE = 12;
	private static final int CHECKSUM_OFFSET = 8;
	private static final int DEBUG_FLAG = 1;
	private static final int BRIGHTNESS_SCALE = HardwareBacklightOutputDevice.PWM_RANGE;
	private static final int DEFAULT_BRIGHTNESS_UNITS = 205;

	private final Path file;
	private boolean debug;
	private int brightnessUnits;
	private boolean persistenceDirty;

	public HardwareSettings() {
		this(settingsFile());
	}

	HardwareSettings(final Path file) {
		this.file = Objects.requireNonNull(file).toAbsolutePath().normalize();
		final Snapshot snapshot = readSnapshot();
		debug = snapshot.debug();
		brightnessUnits = snapshot.brightnessUnits();
	}

	@Override
	public synchronized boolean isDebugEnabled() {
		return debug;
	}

	@Override
	public synchronized void setDebugEnabled(final boolean debug) {
		if (this.debug == debug && !persistenceDirty)
			return;
		this.debug = debug;
		persistenceDirty = !persist();
	}

	@Override
	public synchronized double getBrightness() {
		return brightnessUnits / (double) BRIGHTNESS_SCALE;
	}

	@Override
	public synchronized void setBrightness(final double brightness) {
		final int normalizedUnits = brightnessUnits(brightness);
		if (brightnessUnits == normalizedUnits && !persistenceDirty)
			return;
		brightnessUnits = normalizedUnits;
		persistenceDirty = !persist();
	}

	private Snapshot readSnapshot() {
		if (!Files.isRegularFile(file))
			return defaultSnapshot();
		try {
			return decode(readFormatBytes());
		} catch (final IOException | RuntimeException failure) {
			System.err.println("Unable to read hardware settings " + file + ": "
					+ failure.getMessage());
			return defaultSnapshot();
		}
	}

	private byte[] readFormatBytes() throws IOException {
		try (InputStream input = Files.newInputStream(file)) {
			return input.readNBytes(FORMAT_SIZE + 1);
		}
	}

	private boolean persist() {
		try {
			writeAtomically(encode(new Snapshot(debug, brightnessUnits)));
			return true;
		} catch (final IOException | RuntimeException failure) {
			System.err.println("Unable to persist hardware settings " + file + ": "
					+ failure.getMessage());
			return false;
		}
	}

	private void writeAtomically(final byte[] bytes) throws IOException {
		final Path directory = file.getParent();
		Files.createDirectories(directory);
		final Path temporary = Files.createTempFile(directory, ".warppi-settings-", ".tmp");
		try {
			try (FileChannel channel = FileChannel.open(temporary,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				final ByteBuffer buffer = ByteBuffer.wrap(bytes);
				while (buffer.hasRemaining())
					channel.write(buffer);
				channel.force(true);
			}
			try {
				Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (final AtomicMoveNotSupportedException unsupported) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
			try (FileChannel directoryChannel = FileChannel.open(directory,
					StandardOpenOption.READ)) {
				directoryChannel.force(true);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static byte[] encode(final Snapshot snapshot) {
		final byte[] bytes = new byte[FORMAT_SIZE];
		final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(MAGIC);
		buffer.put((byte) (snapshot.debug() ? DEBUG_FLAG : 0));
		buffer.put((byte) 0);
		buffer.putShort((short) snapshot.brightnessUnits());
		buffer.putInt(checksum(bytes));
		return bytes;
	}

	private static Snapshot decode(final byte[] bytes) {
		if (bytes.length != FORMAT_SIZE)
			throw new IllegalArgumentException("expected " + FORMAT_SIZE
					+ " bytes, found " + bytes.length);
		final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
		if (buffer.getInt() != MAGIC)
			throw new IllegalArgumentException("unsupported settings format");
		final int flags = Byte.toUnsignedInt(buffer.get());
		if ((flags & ~DEBUG_FLAG) != 0)
			throw new IllegalArgumentException("unsupported settings flags");
		if (buffer.get() != 0)
			throw new IllegalArgumentException("invalid reserved byte");
		final int brightnessUnits = Short.toUnsignedInt(buffer.getShort());
		if (brightnessUnits > BRIGHTNESS_SCALE)
			throw new IllegalArgumentException("invalid brightness value");
		final int storedChecksum = buffer.getInt();
		if (storedChecksum != checksum(bytes))
			throw new IllegalArgumentException("settings checksum mismatch");
		return new Snapshot((flags & DEBUG_FLAG) != 0, brightnessUnits);
	}

	private static int checksum(final byte[] bytes) {
		final CRC32 checksum = new CRC32();
		checksum.update(bytes, 0, CHECKSUM_OFFSET);
		return (int) checksum.getValue();
	}

	private static int brightnessUnits(final double brightness) {
		if (!Double.isFinite(brightness) || brightness < 0d || brightness > 1d)
			throw new IllegalArgumentException("Brightness must be between 0 and 1");
		return (int) Math.ceil(brightness * BRIGHTNESS_SCALE);
	}

	private static Snapshot defaultSnapshot() {
		return new Snapshot(false, DEFAULT_BRIGHTNESS_UNITS);
	}

	private static Path settingsFile() {
		final String override = System.getProperty(SETTINGS_FILE_PROPERTY);
		return override != null && !override.isBlank()
				? Paths.get(override)
				: DEFAULT_SETTINGS_FILE;
	}

	private record Snapshot(boolean debug, int brightnessUnits) {
	}
}
