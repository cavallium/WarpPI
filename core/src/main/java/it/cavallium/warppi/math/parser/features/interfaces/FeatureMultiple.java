package it.cavallium.warppi.math.parser.features.interfaces;

public interface FeatureMultiple extends Feature {
	Object[] getChildren();

	Object getChild(int index);

	int getChildCount();

	void setChild(int index, Object obj);

	void setChildren(Object[] objs);

	void addChild(Object obj);
}
