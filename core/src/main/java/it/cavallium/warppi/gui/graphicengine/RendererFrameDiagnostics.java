package it.cavallium.warppi.gui.graphicengine;

/** Previous-frame renderer work, measured without forcing a GPU synchronization. */
public record RendererFrameDiagnostics(boolean available, String backend, long frameNumber,
		int quads, int batches, int textureSwitches, int uploadedBytes,
		int maxBatchQuads, long cpuSubmissionNanos) {
	public static final RendererFrameDiagnostics UNAVAILABLE = new RendererFrameDiagnostics(
			false, "GENERIC", 0, 0, 0, 0, 0, 0, 0);

	public RendererFrameDiagnostics {
		if (backend == null || backend.isBlank())
			throw new IllegalArgumentException("Renderer backend name is required");
		if (frameNumber < 0 || quads < 0 || batches < 0 || textureSwitches < 0
				|| uploadedBytes < 0 || maxBatchQuads < 0 || cpuSubmissionNanos < 0)
			throw new IllegalArgumentException("Renderer diagnostics cannot contain negative values");
	}

	public double cpuSubmissionMillis() {
		return cpuSubmissionNanos / 1_000_000d;
	}

	public int vertices() {
		return quads * 6;
	}
}
