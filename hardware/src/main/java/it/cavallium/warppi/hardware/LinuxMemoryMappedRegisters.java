package it.cavallium.warppi.hardware;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Map;

/** Maps a Linux device through Java 25 FFM and exposes volatile 32-bit registers. */
final class LinuxMemoryMappedRegisters implements GpioRegisters {
	private static final Path GPIO_DEVICE = Path.of("/dev/gpiomem");
	private static final long GPIO_MAP_SIZE = 4_096;
	private static final VarHandle NATIVE_INT = ValueLayout.JAVA_INT
			.withOrder(ByteOrder.nativeOrder())
			.varHandle();

	private Arena arena;
	private volatile MemorySegment registers;

	private LinuxMemoryMappedRegisters(final Arena arena, final MemorySegment registers) {
		this.arena = arena;
		this.registers = registers;
	}

	static LinuxMemoryMappedRegisters openGpio() throws IOException {
		return map(GPIO_DEVICE, GPIO_MAP_SIZE);
	}

	static LinuxMemoryMappedRegisters map(final Path path, final long byteSize) throws IOException {
		if (byteSize <= 0)
			throw new IllegalArgumentException("byteSize must be positive");

		final Arena arena = Arena.ofShared();
		MemorySegment address = null;
		boolean managed = false;
		try (NativeFile file = NativeFile.open(path)) {
			address = Native.mmap(file.descriptor(), byteSize);
			final MemorySegment mapped = address.reinterpret(byteSize, arena,
					segment -> Native.munmapUnchecked(segment, byteSize));
			managed = true;
			return new LinuxMemoryMappedRegisters(arena, mapped);
		} catch (final IOException | RuntimeException | Error failure) {
			if (!managed && address != null)
				unmapAfterFailedSetup(address, byteSize, failure);
			try {
				arena.close();
			} catch (final RuntimeException cleanupFailure) {
				failure.addSuppressed(cleanupFailure);
			}
			throw failure;
		}
	}

	private static void unmapAfterFailedSetup(final MemorySegment address, final long byteSize,
			final Throwable failure) {
		try {
			Native.munmap(address, byteSize);
		} catch (final IOException cleanupFailure) {
			failure.addSuppressed(cleanupFailure);
		}
	}

	@Override
	public int read(final int byteOffset) {
		final MemorySegment currentRegisters = requireRegisters();
		validateOffset(currentRegisters, byteOffset);
		return (int) NATIVE_INT.getVolatile(currentRegisters, (long) byteOffset);
	}

	@Override
	public void write(final int byteOffset, final int value) {
		final MemorySegment currentRegisters = requireRegisters();
		validateOffset(currentRegisters, byteOffset);
		NATIVE_INT.setVolatile(currentRegisters, (long) byteOffset, value);
	}

	@Override
	public synchronized void close() {
		final Arena currentArena = arena;
		if (currentArena == null)
			return;
		registers = null;
		arena = null;
		currentArena.close();
	}

	private MemorySegment requireRegisters() {
		final MemorySegment result = registers;
		if (result == null)
			throw new IllegalStateException("GPIO register mapping is closed");
		return result;
	}

	private static void validateOffset(final MemorySegment registers, final int byteOffset) {
		if (byteOffset < 0 || (byteOffset & (Integer.BYTES - 1)) != 0
				|| byteOffset > registers.byteSize() - Integer.BYTES)
			throw new IndexOutOfBoundsException("Invalid 32-bit register offset " + byteOffset);
	}

	private record NativeFile(int descriptor) implements AutoCloseable {
		static NativeFile open(final Path path) throws IOException {
			return new NativeFile(Native.open(path));
		}

		@Override
		public void close() throws IOException {
			Native.close(descriptor);
		}
	}

	private static final class Native {
		private static final int O_RDWR = 0x0002;
		private static final int PROT_READ = 0x1;
		private static final int PROT_WRITE = 0x2;
		private static final int MAP_SHARED = 0x1;

		private static final Linker LINKER = Linker.nativeLinker();
		private static final Map<String, MemoryLayout> C_LAYOUTS = LINKER.canonicalLayouts();
		private static final ValueLayout.OfInt C_INT = (ValueLayout.OfInt) requiredLayout("int");
		private static final ValueLayout C_LONG = (ValueLayout) requiredLayout("long");
		private static final ValueLayout C_SIZE_T = (ValueLayout) requiredLayout("size_t");
		private static final AddressLayout C_POINTER = (AddressLayout) requiredLayout("void*");
		private static final StructLayout CAPTURE_STATE_LAYOUT = Linker.Option.captureStateLayout();
		private static final VarHandle ERRNO = CAPTURE_STATE_LAYOUT.varHandle(groupElement("errno"));
		private static final Linker.Option CAPTURE_ERRNO = Linker.Option.captureCallState("errno");

		private static final MethodHandle OPEN = downcall("open",
				FunctionDescriptor.of(C_INT, C_POINTER, C_INT),
				Linker.Option.firstVariadicArg(2), CAPTURE_ERRNO);
		private static final MethodHandle MMAP = downcall("mmap",
				FunctionDescriptor.of(C_POINTER, C_POINTER, C_SIZE_T, C_INT, C_INT, C_INT, C_LONG),
				CAPTURE_ERRNO);
		private static final MethodHandle MUNMAP = downcall("munmap",
				FunctionDescriptor.of(C_INT, C_POINTER, C_SIZE_T), CAPTURE_ERRNO);
		private static final MethodHandle CLOSE = downcall("close",
				FunctionDescriptor.of(C_INT, C_INT), CAPTURE_ERRNO);

		private Native() {
		}

		static int open(final Path path) throws IOException {
			try (Arena callArena = Arena.ofConfined()) {
				final MemorySegment capturedState = callArena.allocate(CAPTURE_STATE_LAYOUT);
				final MemorySegment nativePath = callArena.allocateFrom(path.toString());
				final int descriptor = (int) OPEN.invokeWithArguments(capturedState, nativePath, O_RDWR);
				if (descriptor < 0)
					throw failure("open", path.toString(), capturedState);
				return descriptor;
			} catch (final IOException failure) {
				throw failure;
			} catch (final Throwable failure) {
				throw invocationFailure("open", path.toString(), failure);
			}
		}

		static MemorySegment mmap(final int descriptor, final long byteSize) throws IOException {
			try (Arena callArena = Arena.ofConfined()) {
				final MemorySegment capturedState = callArena.allocate(CAPTURE_STATE_LAYOUT);
				final MemorySegment address = (MemorySegment) MMAP.invokeWithArguments(
						capturedState,
						MemorySegment.NULL,
						canonicalInteger(C_SIZE_T, byteSize),
						PROT_READ | PROT_WRITE,
						MAP_SHARED,
						descriptor,
						canonicalInteger(C_LONG, 0));
				if (isMapFailed(address))
					throw failure("mmap", "shared GPIO register page", capturedState);
				return address;
			} catch (final IOException failure) {
				throw failure;
			} catch (final Throwable failure) {
				throw invocationFailure("mmap", "shared GPIO register page", failure);
			}
		}

		static void munmap(final MemorySegment address, final long byteSize) throws IOException {
			try (Arena callArena = Arena.ofConfined()) {
				final MemorySegment capturedState = callArena.allocate(CAPTURE_STATE_LAYOUT);
				final int result = (int) MUNMAP.invokeWithArguments(
						capturedState, address, canonicalInteger(C_SIZE_T, byteSize));
				if (result != 0)
					throw failure("munmap", "GPIO register page", capturedState);
			} catch (final IOException failure) {
				throw failure;
			} catch (final Throwable failure) {
				throw invocationFailure("munmap", "GPIO register page", failure);
			}
		}

		static void munmapUnchecked(final MemorySegment address, final long byteSize) {
			try {
				munmap(address, byteSize);
			} catch (final IOException failure) {
				throw new UncheckedIOException(failure);
			}
		}

		static void close(final int descriptor) throws IOException {
			try (Arena callArena = Arena.ofConfined()) {
				final MemorySegment capturedState = callArena.allocate(CAPTURE_STATE_LAYOUT);
				final int result = (int) CLOSE.invokeWithArguments(capturedState, descriptor);
				if (result != 0)
					throw failure("close", "file descriptor " + descriptor, capturedState);
			} catch (final IOException failure) {
				throw failure;
			} catch (final Throwable failure) {
				throw invocationFailure("close", "file descriptor " + descriptor, failure);
			}
		}

		private static MethodHandle downcall(final String symbol, final FunctionDescriptor descriptor,
				final Linker.Option... options) {
			return LINKER.downcallHandle(LINKER.defaultLookup().findOrThrow(symbol), descriptor, options);
		}

		private static MemoryLayout requiredLayout(final String name) {
			final MemoryLayout layout = C_LAYOUTS.get(name);
			if (layout == null)
				throw new ExceptionInInitializerError("Native ABI does not define C layout " + name);
			return layout;
		}

		private static Object canonicalInteger(final ValueLayout layout, final long value) {
			if (layout.carrier() == int.class)
				return Math.toIntExact(value);
			if (layout.carrier() == long.class)
				return value;
			throw new IllegalStateException("Unsupported native integer layout " + layout);
		}

		private static boolean isMapFailed(final MemorySegment address) {
			if (C_POINTER.byteSize() == Integer.BYTES)
				return (address.address() & 0xffff_ffffL) == 0xffff_ffffL;
			return address.address() == -1L;
		}

		private static IOException failure(final String operation, final String target,
				final MemorySegment capturedState) {
			final int errno = (int) ERRNO.get(capturedState, 0L);
			return new IOException(operation + " failed for " + target + " (errno " + errno + ")");
		}

		private static IOException invocationFailure(final String operation, final String target,
				final Throwable cause) {
			return new IOException("Unable to invoke " + operation + " for " + target, cause);
		}
	}
}
