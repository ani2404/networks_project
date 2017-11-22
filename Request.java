

import java.nio.ByteBuffer;

public class Request extends Message{	
	
	public Request(int index) {
		this.message_length = ByteBuffer.allocate(4).putInt(4).array();
		this.message_type = 6;
		this.message = ByteBuffer.allocate(4).putInt(index).array();
	//	Logger.write_message("SENDINGREQUEST:the piece index requested is"+ index);
		
	}
	
}
