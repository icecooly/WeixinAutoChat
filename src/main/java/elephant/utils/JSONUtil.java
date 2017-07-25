package elephant.utils;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 
 * @author skydu
 *
 */
public class JSONUtil {
	//
	public static String toJson(Object obj){
		return JSON.toJSONString(obj);
	}
	//
	public static String dump(Object obj){
		return JSON.toJSONString(obj,SerializerFeature.PrettyFormat);
	}
	//
	public static String toJson(Object obj,boolean disableCircularReferenceDetect){
		return JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect);
	}
	//
	public static String toJson(Object obj,SerializerFeature... features){
		return JSON.toJSONString(obj,features);
	}
	//
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String str,Class<?>t){
		return (T) JSON.parseObject(str, t);
	}
	//
	@SuppressWarnings("unchecked")
	public static <T> List<T> fromJsonList(String str,Class<?>t){
		return  (List<T>) JSON.parseArray(str, t);	
	}
}
