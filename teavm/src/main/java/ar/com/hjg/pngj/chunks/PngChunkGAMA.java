package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngHelperInternal;
import ar.com.hjg.pngj.PngjException;

/**
 * gAMA chunk.
 * <p>
 * see http://www.w3.org/TR/PNG/#11gAMA
 */
public class PngChunkGAMA extends PngChunkSingle {
	public final static String ID = ChunkHelper.gAMA;

	// http://www.w3.org/TR/PNG/#11gAMA
	private double gamma;

	public PngChunkGAMA(final ImageInfo info) {
		super(PngChunkGAMA.ID, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT;
	}

	@Override
	public ChunkRaw createRawChunk() {
		final ChunkRaw c = createEmptyChunk(4, true);
		final int g = (int) (gamma * 100000 + 0.5);
		PngHelperInternal.writeInt4tobytes(g, c.data, 0);
		return c;
	}

	@Override
	public void parseFromRaw(final ChunkRaw chunk) {
		if (chunk.len != 4)
			throw new PngjException("bad chunk " + chunk);
		final int g = PngHelperInternal.readInt4fromBytes(chunk.data, 0);
		gamma = g / 100000.0;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(final double gamma) {
		this.gamma = gamma;
	}

}
