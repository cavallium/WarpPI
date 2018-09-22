package ar.com.hjg.pngj;

/**
 * Image Line factory.
 */
public interface IImageLineFactory<T extends IImageLine> {
	T createImageLine(ImageInfo iminfo);
}
