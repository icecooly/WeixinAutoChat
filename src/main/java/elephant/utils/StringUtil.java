package elephant.utils;

import java.util.Random;

/**
 * 
 * @author icecooly
 *
 */
public class StringUtil {

	public static boolean isEmpty(String input){
		if(input==null||input.length()==0){
			return true;
		}
		return false;
	}
	
	/**左边补0*/
	public static String lpad(int number,int length){
		return String.format("%0"+length+"d", number);   
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	public static String randomNumbers(int length){
		Random random=new Random();
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<length;i++){
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}
}
