package yms.tinyvelocity.providers;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import yms.tinyvelocity.ValuesProvider;

/**
 * 
 * Provide values form a POJO to the template engine.
 * 
 * it looks up for properties in Javabean style.
 * 
 * @author Yotam Madem
 *
 */
public class POJOValuesProvider implements ValuesProvider{

	private Object model;

	public POJOValuesProvider(Object model){
		this.model = model;
	}
	
	@Override
	public Object getProperty(Object obj, String propName) {
		if (obj == null){
			return getPOJOProperty(model, propName);
		}
		return getPOJOProperty(obj, propName);
	}

	private Object getPOJOProperty(Object pojo, String propName) {
		try {
			Field f = pojo.getClass().getField(propName);
			Object value = f.get(pojo);
			return value;
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		
		try {
			Method getter = pojo.getClass().getMethod("get"+firstLetterUpcase(propName));
			Object value = getter.invoke(pojo);
			return value;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		
		try {
			Method getter = pojo.getClass().getMethod("get",String.class);
			Object value = getter.invoke(pojo,propName);
			return value;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		
		try {
			Method getter = pojo.getClass().getMethod("get",Object.class);
			Object value = getter.invoke(pojo,propName);
			return value;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		
		return null;
	}

	private String firstLetterUpcase(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}

	@Override
	public String getObjectAsString(Object obj) {
		return obj == null ? "null" : obj.toString();
	}

}
