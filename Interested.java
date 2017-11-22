

import java.nio.ByteBuffer;

public class Interested extends Message{
	
	public Interested() {
		this.message_length = ByteBuffer.allocate(4).putInt(0).array();
		this.message_type = 2;
		
	}
	
}
