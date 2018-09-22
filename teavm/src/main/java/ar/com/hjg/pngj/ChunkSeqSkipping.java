package ar.com.hjg.pngj;

import java.util.ArrayList;
import java.util.List;

import ar.com.hjg.pngj.ChunkReader.ChunkReaderMode;
import ar.com.hjg.pngj.chunks.ChunkRaw;

/**
 * This simple reader skips all chunks contents and stores the chunkRaw in a
 * list. Useful to read chunks structure.
 *
 * Optionally the contents might be processed. This doesn't distinguish IDAT
 * chunks
 */
public class ChunkSeqSkipping extends ChunkSeqReader {

	private final List<ChunkRaw> chunks = new ArrayList<>();
	private boolean skip = true;

	/**
	 * @param skipAll
	 *            if true, contents will be truly skipped, and CRC will not be
	 *            computed
	 */
	public ChunkSeqSkipping(final boolean skipAll) {
		super(true);
		skip = skipAll;
	}

	public ChunkSeqSkipping() {
		this(true);
	}

	@Override
	protected ChunkReader createChunkReaderForNewChunk(final String id, final int len, final long offset,
			final boolean skip) {
		return new ChunkReader(len, id, offset, skip ? ChunkReaderMode.SKIP : ChunkReaderMode.PROCESS) {
			@Override
			protected void chunkDone() {
				postProcessChunk(this);
			}

			@Override
			protected void processData(final int offsetinChhunk, final byte[] buf, final int off, final int len) {
				processChunkContent(getChunkRaw(), offsetinChhunk, buf, off, len);
			}
		};
	}

	protected void processChunkContent(final ChunkRaw chunkRaw, final int offsetinChhunk, final byte[] buf,
			final int off, final int len) {
		// does nothing
	}

	@Override
	protected void postProcessChunk(final ChunkReader chunkR) {
		super.postProcessChunk(chunkR);
		chunks.add(chunkR.getChunkRaw());
	}

	@Override
	protected boolean shouldSkipContent(final int len, final String id) {
		return skip;
	}

	@Override
	protected boolean isIdatKind(final String id) {
		return false;
	}

	public List<ChunkRaw> getChunks() {
		return chunks;
	}

}
