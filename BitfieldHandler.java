import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BitfieldHandler {

	public BitfieldHandler(Integer peer_id, byte[] message_length, byte[] message) {
		// Update remote peer missing list by removing the non-missing pieces identified in the bitfield
		// need to check if the list is empty or not, to determine if the remote peer has completely downloaded the file
		// increment the peers_download_complete_count in case if the list is empty
		// send interested or not interested
		
		int length = ByteBuffer.wrap(message_length,0,4).getInt();
		PeerInfo current = peerProcess.process.peers_info().get(peer_id);
		int count = 0;
		int piece_count =0;
		boolean interested = false;
		for(int i=0; i < length;i++){
		
			byte bitfield = message[i];
			
			for(int j=0; j<8;j++){
				if((bitfield & (1<<(7-j))) != 0){
					piece_count++;
					current.getmissingpieces().remove(count+j);
					if(peerProcess.process.getMissingpieces().containsKey(count+j)){
						// the piece is interesting as i am missing the piece, add it into the interesting list of the remoter peer
						// or add the peer id in the requested queue if the piece is already requested
						if(peerProcess.process.getrequested_pieces().containsKey(count+j)){
							ArrayList<Integer> list = peerProcess.process.getrequested_pieces().get(count+j);
							synchronized (list) {								
								list.add(peer_id);
								current.getLock().lock();
								try{
									current.incrementpiecesrequestedcount();
								}finally{
									current.getLock().unlock();
								}

							}
						}
						else{
							current.getLock().lock();
							try{
								current.getinterestingpieces().put(count+j, true);
							//	peerProcess.debug_file.write(current.getinterestingpieces().size());
							//	peerProcess.debug_file.flush();
							}finally{
								current.getLock().unlock();
							}
							
						}
						interested = true;
					}

				}
				
			}
			count+=8;
			
		}
		
	//	Logger.write_message("Received Bitfield with piece count "+ piece_count+ " from peer "+ peer_id);
		if(current.getmissingpieces().isEmpty()){
			peerProcess.process.incrementPeersDownloadComplete();
		//	Logger.write_message("Received bitfield from "+ peer_id +" with full file");
			
		}
		
		if(interested)
			try {
				
				peerProcess.process.getoutputqueuemap().get(peer_id).put(new Interested());
			//	Logger.write_message("Sending interested to "+ peer_id+ " on receiving bitfield");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			try {
				peerProcess.process.getoutputqueuemap().get(peer_id).put(new NotInterested());
			//	Logger.write_message("Sending not interested to "+ peer_id+ " on receiving bitfield");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	
	}
		
		
		
		
	
}


