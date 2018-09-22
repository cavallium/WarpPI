package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngjException;

/**
 * tEXt chunk.
 * <p>
 * see http://www.w3.org/TR/PNG/#11tEXt
 */
public class PngChunkTEXT extends PngChunkTextVar {
	public final static String ID = ChunkHelper.tEXt;

	public PngChunkTEXT(final ImageInfo info) {
		super(PngChunkTEXT.ID, info);
	}

	public PngChunkTEXT(final ImageInfo info, final String key, final String val) {
		super(PngChunkTEXT.ID, info);
		setKeyVal(key, val);
	}

	@Override
	public ChunkRaw createRawChunk() {
		if (key == null || key.trim().length() == 0)
			throw new PngjException("Text chunk key must be non empty");
		final byte[] b = ChunkHelper.toBytes(key + "\0" + val);
		final ChunkRaw chunk = createEmptyChunk(b.length, false);
		chunk.data = b;
		return chunk;
	}

	@Override
	public void parseFromRaw(final ChunkRaw c) {
		int i;
		for (i = 0; i < c.data.length; i++)
			if (c.data[i] == 0)
				break;
		key = ChunkHelper.toString(c.data, 0, i);
		i++;
		val = i < c.data.length ? ChunkHelper.toString(c.data, i, c.data.length - i) : "";
	}

}
