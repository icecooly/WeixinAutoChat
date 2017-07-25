package elephant.api.domain;

/**
 * 
 * @author skydu
 *
 */
public class SyncCheckResp {
	//
	public static final int RETCODE_正常=0;
	public static final int RETCODE_退出=1102;
	public static final int RETCODE_其它地方登陆=1101;
	public static final int RETCODE_移动端退出=1102;
	public static final int RETCODE_未知错误=9999;
	//
	public int retcode;
	
	public int selector;
}
