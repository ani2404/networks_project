import java.nio.ByteBuffer;


public class BitField extends Message
{
	
	
	public BitField(byte[] pieces){
		int payload_length = (int) Math.ceil((double)pieces.length/8);
		this.message_length = ByteBuffer.allocate(4).putInt(payload_length).array();
		this.message = new byte[payload_length];
		this.message_type = 5; // message type
		int count = 0;
		for(int i=0; i < payload_length;i++){
			int j=0;
			for(;j<8 && (count+j < pieces.length);j++)
			{
				this.message[i] |= ((pieces[count+j])<<(7-j));  
			}
			// need to perform bitwise complement as '1' in piece indicates missing piece and '0' a valid piece
			this.message[i] = (byte) ~this.message[i];
			count +=8;
		}	
	
	}

	
}


