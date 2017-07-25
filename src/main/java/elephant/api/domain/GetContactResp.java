package elephant.api.domain;

import java.util.List;

/**
 * 
 * @author skydu
 *
 */
public class GetContactResp {

	public BaseResponse BaseResponse;
	
	public int MemberCount;
	
	public List<Contact> MemberList;
	
	public int Seq;
}
