package elephant.utils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * @author skydu
 *
 */
public class XmlUtil {

	public static Map<String, String> parseXmlMessage(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Map<String, String> result = new HashMap<String, String>();
		try {
			DocumentBuilder db = factory.newDocumentBuilder();
			Document d = db.parse(new InputSource(new StringReader(xml)));
			NodeList nl = d.getDocumentElement().getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String k = n.getNodeName();
				String v = n.getTextContent();
				if(v==null||StringUtil.isEmpty(v.trim())){
					continue;
				}
				result.put(k, v);
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
	//
	public static void main(String[] args) throws Exception {
		String xml="<error><ret>0</ret><message></message><skey>@crypt_e81593ac_42d6f7c43a14d4a1bb6a0c063a2310fc</skey><wxsid>2uIAlPxvmxOhvSGR</wxsid><wxuin>2808561200</wxuin><pass_ticket>iVsWyg1Mu7o6KMquHuaxP2HWJnKshVslcNlPgHjqzi3bkSihppOuvM%2FXRUpORGks</pass_ticket><isgrayscale>1</isgrayscale></error>";
		Map<String, String> map=XmlUtil.parseXmlMessage(xml);
		System.out.println(JSONUtil.dump(map));
	}
}
