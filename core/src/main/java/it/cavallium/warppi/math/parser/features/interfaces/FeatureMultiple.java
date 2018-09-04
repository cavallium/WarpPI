package it.cavallium.warppi.math.parser.features.interfaces;

public interface FeatureMultiple extends Feature {
	public Object[] getChildren();

	public Object getChild(int index);

	public int getChildCount();

	public void setChild(int index, Object obj);

	public void setChildren(Object[] objs);

	public void addChild(Object obj);
}
