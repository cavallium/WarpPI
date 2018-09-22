package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngjException;

/**
 * sTER chunk.
 * <p>
 * see http://www.libpng.org/pub/png/spec/register/pngext-1.3.0-pdg.html#C.sTER
 */
public class PngChunkSTER extends PngChunkSingle {
	public final static String ID = "sTER";

	// http://www.libpng.org/pub/png/spec/register/pngext-1.3.0-pdg.html#C.sTER
	private byte mode; // 0: cross-fuse layout 1: diverging-fuse layout

	public PngChunkSTER(final ImageInfo info) {
		super(PngChunkSTER.ID, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.BEFORE_IDAT;
	}

	@Override
	public ChunkRaw createRawChunk() {
		final ChunkRaw c = createEmptyChunk(1, true);
		c.data[0] = mode;
		return c;
	}

	@Override
	public void parseFromRaw(final ChunkRaw chunk) {
		if (chunk.len != 1)
			throw new PngjException("bad chunk length " + chunk);
		mode = chunk.data[0];
	}

	/**
	 * 0: cross-fuse layout 1: diverging-fuse layout
	 */
	public byte getMode() {
		return mode;
	}

	/**
	 * 0: cross-fuse layout 1: diverging-fuse layout
	 */
	public void setMode(final byte mode) {
		this.mode = mode;
	}

}
