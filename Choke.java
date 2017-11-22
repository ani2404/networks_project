

import java.nio.ByteBuffer;


public class Choke extends Message
{
	
	public Choke() 
	{
		this.message_length = ByteBuffer.allocate(4).putInt(0).array();
		
		this.message_type = 0; // type is 0
	
	}
		
}
