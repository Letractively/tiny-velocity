package yms.tinyvelocity;

/**
 * Interface for providing variables values.
 * 
 * @author Yotam Madem
 * 
 */
public interface ValuesProvider {
	/**
	 * Should return the value of property 'propName' of object: 'obj' if
	 * obj is null, it should return the root property 'propName'.
	 * 
	 * if this method will return null the template engine will infer that
	 * the variable/property does not exists.
	 * 
	 * @param obj (can be null)
	 * @param propName
	 * @return
	 */
	public Object getProperty(Object obj, String propName);

	/**
	 * Should return the string representation of an object in the formatted
	 * output.
	 * 
	 * @param obj - the Object to be formatted
	 * @return
	 */
	public String getObjectAsString(Object obj);

}
