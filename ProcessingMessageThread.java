

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

enum message_types{
	Choke,
	UnChoke,
	Interested,
	NotInterested,
	Have,
	BitField,
	Request,
	Piece
};

public class ProcessingMessageThread extends Thread {
	Integer peer_id;
	
	ProcessingMessageThread(Integer peer_id){
		this.peer_id= peer_id;
	}
	
	
	public void run(){
		while(peerProcess.process.running()){
		
			Message m=null;
			try {
				m = peerProcess.process.getinputqueuemap().get(peer_id).poll(10000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				if(!peerProcess.process.running())
					break;
			}
			if(validate(m)){
				byte[]  message_length = m.getmessageLength();
				byte message_type = m.getmessagetype();
				byte[] message = m.getmessage();
				
				if (message_type == 0)
					new ChokeHandler(peer_id);
				else if (message_type == 1)
					new UnChokeHandler(peer_id);
				else if (message_type == 2)
					new InterestedHandler(peer_id);
				else if (message_type == 3)
					new NotInterestedHandler(peer_id);
				else if (message_type == 4)
					new HaveHandler(peer_id,message);
				else if (message_type == 5)
					new BitfieldHandler(peer_id,message_length,message);
				else if (message_type == 6)
					new RequestHandler(peer_id,message);
				else if (message_type == 7)
					try {
						new PieceHandler(peer_id,message_length,message);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
			}
		//	else
			//	System.out.println("Invalid Message");
		}
		
	}
	
	
	private boolean validate(Message m){
		boolean result = false;
		if(m == null) return result;
		
		if(m.getmessagetype() > 7) return result;
		
		if((m.getmessagetype()==message_types.Choke.ordinal() ||
				m.getmessagetype()==message_types.UnChoke.ordinal() ||
				m.getmessagetype()==message_types.Interested.ordinal()||
				m.getmessagetype()==message_types.NotInterested.ordinal()) &&
				ByteBuffer.wrap(m.getmessageLength(),0,4).getInt() != 0){
			// Invalid message as the length should be zero
	//		Logger.write_message("Invalid message received from "+ peer_id);
			return result;
		}
		
		if(((m.getmessagetype() == message_types.Have.ordinal()) || 
				(m.getmessagetype() == message_types.Request.ordinal())) && 
				((ByteBuffer.wrap(m.getmessageLength(),0,4).getInt() != 4) || 
						(ByteBuffer.wrap(m.getmessage(),0,4).getInt() >= peerProcess.process.totalPieces()))){
		//	Logger.write_message("Invalid have or request message received from "+ peer_id);
			return result;
		}
		
		// Also need to validate piece and bitfield messages
		
		return true;
	}
}
