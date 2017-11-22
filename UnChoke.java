

import java.nio.ByteBuffer;


public class UnChoke extends Message{
	
	public UnChoke() 
	{
		this.message_length = ByteBuffer.allocate(4).putInt(0).array();
		this.message_type = 1; // type is 0
	
	}
		
}
