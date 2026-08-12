package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LinuxMemoryMappedRegistersTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void mapsAndUnmapsARegisterPageThroughFfm() throws IOException {
		final Path registerFile = temporaryDirectory.resolve("registers");
		Files.write(registerFile, new byte[4_096]);
		final LinuxMemoryMappedRegisters registers = LinuxMemoryMappedRegisters.map(registerFile, 4_096);

		registers.write(0x34, 0x1234_5678);
		assertEquals(0x1234_5678, registers.read(0x34));
		assertThrows(IndexOutOfBoundsException.class, () -> registers.read(1));

		registers.close();
		assertThrows(IllegalStateException.class, () -> registers.read(0x34));
		registers.close();
	}

	@Test
	void reportsNativeOpenFailuresWithErrno() {
		final Path missingFile = temporaryDirectory.resolve("missing-registers");

		final IOException failure = assertThrows(IOException.class,
				() -> LinuxMemoryMappedRegisters.map(missingFile, 4_096));

		assertTrue(failure.getMessage().contains("open failed"));
		assertTrue(failure.getMessage().contains("errno"));
	}
}
