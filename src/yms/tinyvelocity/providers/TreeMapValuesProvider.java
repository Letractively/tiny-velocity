package yms.tinyvelocity.providers;
import java.util.TreeMap;

import yms.tinyvelocity.ValuesProvider;

/**
 * Provides values from treemap.
 * 
 * This provider assumes that all the objects that have properties are treemaps.
 * it can be used in GWT client side, because GWT does not have reflection but it does have
 * treemap.
 * 
 * @author Yotam Madem
 *
 */
public class TreeMapValuesProvider implements ValuesProvider{

	private TreeMap<String, Object> model;

	public TreeMapValuesProvider(TreeMap<String, Object> model){
		this.model = model;
	}
	
	@Override
	public Object getProperty(Object obj, String propName) {
		if (obj == null){
			return model.get(propName);
		}
		@SuppressWarnings("unchecked")
		TreeMap<String, Object> map = (TreeMap<String, Object>) obj;
		return map.get(propName);
	}

	@Override
	public String getObjectAsString(Object obj) {
		return obj == null ? "null" : obj.toString();
	}

}
