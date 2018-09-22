package ar.com.hjg.pngj;

/**
 * This loads the png as a plain sequence of chunks, buffering all
 *
 * Useful to do things like insert or delete a ancilllary chunk. This does not
 * distinguish IDAT from others
 **/
public class ChunkSeqBuffering extends ChunkSeqReader {
	protected boolean checkCrc = true;

	public ChunkSeqBuffering() {
		super();
	}

	@Override
	protected boolean isIdatKind(final String id) {
		return false;
	}

	@Override
	protected boolean shouldCheckCrc(final int len, final String id) {
		return checkCrc;
	}

	public void setCheckCrc(final boolean checkCrc) {
		this.checkCrc = checkCrc;
	}

}
