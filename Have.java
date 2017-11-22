

import java.nio.ByteBuffer;

public class Have extends Message{

	
	public Have(int index) {
		this.message_length = ByteBuffer.allocate(4).putInt(4).array();
		this.message_type = 4;
		this.message = ByteBuffer.allocate(4).putInt(index).array();
	//	System.out.println("SendingHave:the piece index sending is"+ index);
		
	}
	
}
