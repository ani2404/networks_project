

import java.nio.ByteBuffer;

public class NotInterested extends Message{
	
	public NotInterested() {
		this.message_length = ByteBuffer.allocate(4).putInt(0).array();
		
		this.message_type = 3; // type is 3
		
	}
	
}
