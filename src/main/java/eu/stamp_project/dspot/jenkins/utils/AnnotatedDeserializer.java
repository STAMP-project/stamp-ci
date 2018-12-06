package eu.stamp_project.dspot.jenkins.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.google.gson.JsonObject;

/**
 * 
 * @author valentina di giacomo
 * needed to check that the object to be deserialized actually contains all fields annotated as JsonRequired
 * @param <T> the type to be checked
 */
public class AnnotatedDeserializer<T> implements JsonDeserializer<T> {
	
	@Override
	public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {

		try {
			JsonObject jo = JsonObject.class.cast(je);
			T pojo = new Gson().fromJson(je, type);
			Field[] fields = pojo.getClass().getDeclaredFields();
			for (Field f : fields) {
				if (f.getAnnotation(JsonRequired.class) != null) {
					if(jo.get(f.getName())==null)
						throw new JsonParseException("required field "+ f.getName() + " does not exist");
				}
			}
			return pojo;
		} catch (ClassCastException e) {
			throw new JsonParseException("element represents an array or is null");
		}
	}

}