package ar.com.hjg.pngj;

/**
 * This interface is just for the sake of unifying some methods of
 * {@link ImageLineHelper} that can use both
 * {@link ImageLineInt} or {@link ImageLineByte}. It's not very useful outside
 * that, and the user should not rely much
 * on this.
 */
public interface IImageLineArray {
	ImageInfo getImageInfo();

	FilterType getFilterType();

	/**
	 * length of array (should correspond to samples)
	 */
	int getSize();

	/**
	 * Get i-th element of array (for 0 to size-1). The meaning of this is type
	 * dependent. For ImageLineInt and
	 * ImageLineByte is the sample value.
	 */
	int getElem(int i);
}
