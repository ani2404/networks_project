import java.io.IOException;
import java.nio.ByteBuffer;

public class Message {
	
	protected byte[] message_length;
	protected byte message_type;
	protected byte[] message;
	//static FileOutputStream file_pointer;
	
	public Message(byte[] message_length, byte message_type,byte[] message) throws IOException{
		
	
		this.message_length = new byte[4];
		for(int i=0;i<4;i++) this.message_length[i] = message_length[i];
		this.message_type = message_type;
		Integer length = ByteBuffer.wrap(message_length,0,4).getInt();
		if(length> 0)
			this.message = new byte[length];
		else
			this.message = null;
	//	System.out.println("Message Length is:" + length);
	//	System.out.println("Message Type of the message is:" + this.message_type);
	//	System.out.println("Message content of the is:");
		for(int i=0;i <length;i++){
			this.message[i] = message[i];
			//System.out.print((byte)this.message[i]);
		}


	}
	
	public Message() {
		// TODO Auto-generated constructor stub
		this.message = null;
		this.message_length = null;
		this.message_type = -1;
	}

	public Message(byte[] message2) {
		// TODO Auto-generated constructor stub
		
		this.message_length = new byte[4];
		int i=0;
		for(;i<4;i++) this.message_length[i] = message2[i];
		this.message_type = message2[i++];
		Integer length = ByteBuffer.wrap(message_length,0,4).getInt();
		if(length> 0)
			this.message = new byte[length];
		else
			this.message = null;
	//	System.out.println("Message Length is:" + length);
	//	System.out.println("Message Type of the message is:" + this.message_type);
	//	System.out.println("Message content of the is:");
		for(int j=0;j <length;j++){
			this.message[j] = message2[i++];
			//System.out.print((byte)this.message[i]);
		}

	}

	public byte[] getmessageLength(){
		return message_length;
	}
	
	public byte getmessagetype(){
		return message_type;
	}
	
	public byte[] getmessage(){
		return message;
	}

}
