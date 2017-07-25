package elephant;

/**
 * 
 * @author skydu
 *
 */
public class WeixinAutoChatTest {
	//
	public static void main(String[] args) throws Exception {
		MessageHandler handler = new MessageHandler();
		WeixinAutoChat chat = new WeixinAutoChat(handler);
		handler.chat = chat;
		chat.login();
	}
}
