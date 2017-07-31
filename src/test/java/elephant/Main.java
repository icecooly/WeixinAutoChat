package elephant;

/**
 * 
 * @author skydu
 *
 */
public class Main {
	//
	public static void main(String[] args) throws Exception {
		RobotMessageHandler handler = new RobotMessageHandler();
		WeixinAutoChat chat = new WeixinAutoChat(handler);
		handler.chat = chat;
		chat.login();
	}
}
