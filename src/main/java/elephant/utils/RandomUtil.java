package elephant.utils;

import java.util.Random;

/**
 * 
 * @author skydu
 *
 */
public class RandomUtil {
	//
	private static final Random random = new Random();
	//
	public static int randomInt(int minValue, int maxValue){
		if(maxValue<minValue){
			return 0;
		}else if(maxValue==minValue){
			return minValue;
		}
		int randomNumber=minValue+random.nextInt(maxValue-minValue+1);
		return randomNumber;
	}
	//
	public static int randomInt(int maxNumber){
		return random.nextInt(maxNumber);
	}
}