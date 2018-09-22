package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngHelperInternal;

/**
 * acTL chunk. For APGN, not PGN standard
 * <p>
 * see
 * https://wiki.mozilla.org/APNG_Specification#.60acTL.60:_The_Animation_Control_Chunk
 * <p>
 */
public class PngChunkACTL extends PngChunkSingle {
	public final static String ID = "acTL";
	private int numFrames;
	private int numPlays;

	public PngChunkACTL(final ImageInfo info) {
		super(PngChunkACTL.ID, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.BEFORE_IDAT;
	}

	@Override
	public ChunkRaw createRawChunk() {
		final ChunkRaw c = createEmptyChunk(8, true);
		PngHelperInternal.writeInt4tobytes(numFrames, c.data, 0);
		PngHelperInternal.writeInt4tobytes(numPlays, c.data, 4);
		return c;
	}

	@Override
	public void parseFromRaw(final ChunkRaw chunk) {
		numFrames = PngHelperInternal.readInt4fromBytes(chunk.data, 0);
		numPlays = PngHelperInternal.readInt4fromBytes(chunk.data, 4);
	}

	public int getNumFrames() {
		return numFrames;
	}

	public void setNumFrames(final int numFrames) {
		this.numFrames = numFrames;
	}

	public int getNumPlays() {
		return numPlays;
	}

	public void setNumPlays(final int numPlays) {
		this.numPlays = numPlays;
	}

}
