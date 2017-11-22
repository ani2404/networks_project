import java.nio.ByteBuffer;

public class RequestHandler {

	public RequestHandler(Integer peer_id, byte[] message){
		// TODO Auto-generated constructor stub
		
		int piece_index = ByteBuffer.wrap(message,0,4).getInt();
//		Logger.write_message("Request received for "+ piece_index + " from"+ peer_id);
		// need to add lock
		synchronized (peerProcess.process.getInterestedmap()) {
			if(peerProcess.process.getInterestedmap().get(peer_id).isUnchoked()){
				try {
				//	Logger.write_message("Sending a piece "+ piece_index + " for "+ peer_id);
					peerProcess.process.getoutputqueuemap().get(peer_id).put(new Piece(piece_index,peerProcess.process.fileReader()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
			//	Logger.write_message("Not sending a piece "+ piece_index + " for "+ peer_id);
			}
		}
	}

}
