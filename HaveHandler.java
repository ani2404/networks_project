import java.nio.ByteBuffer;
import java.util.ArrayList;

public class HaveHandler {

	//int count = 0;
	public HaveHandler(Integer peer_id, byte[] message){
	
		
		// Update remote peer missing list by removing the new message
		
		// need to check if the list is empty or not to determine if the remote peer has completely downloaded the file
		// increment the peers_download_complete_count in case if the list is empty
		
		// check if the peers_download_complete_count and the peers_connected count is same
		// clean the resources and shut down the process as all peers have completely downloaded the file 
		
		int piece_index = ByteBuffer.wrap(message,0,4).getInt();
		Logger.HaveReceived(peer_id, piece_index);
	//	Logger.write_message("Have:the piece index received is"+ piece_index);
		/*try {
			synchronized (peerProcess.debug_file){
			peerProcess.debug_file.write("Peer["+Integer.toString(peer_id) +"]:");
			peerProcess.debug_file.write("received have for index"+Integer.toString(piece_index));
			peerProcess.debug_file.write("\n");
			peerProcess.debug_file.flush();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} */

		boolean piece_missing = false;
		peerProcess.process.getMissingPiecesLock().lock();
		try{
			piece_missing = peerProcess.process.getMissingpieces().containsKey(piece_index);
		}finally{
			peerProcess.process.getMissingPiecesLock().unlock();
		}
		
		PeerInfo current = peerProcess.process.peers_info().get(peer_id);
		
		if(piece_missing){
			try {
				peerProcess.process.getoutputqueuemap().get(peer_id).put(new Interested());
			//	Logger.write_message("Sending interested to "+ peer_id+ " for piece : " + piece_index);

				if(peerProcess.process.getrequested_pieces().containsKey(piece_index)){
					ArrayList<Integer> list = peerProcess.process.getrequested_pieces().get(piece_index);
					synchronized(list){
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
						current.getinterestingpieces().put(piece_index, true);
				//		peerProcess.debug_file.write(current.getinterestingpieces().size());
				//		peerProcess.debug_file.flush();
					} finally{
						current.getLock().unlock();
					}
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			current.getLock().lock();
			try{
				if(current.getinterestingpieces().isEmpty() && (current.getpiecesrequestedcount()==0)){


					try {
					//	Logger.write_message("Sending not interested to "+ peer_id);
						peerProcess.process.getoutputqueuemap().get(peer_id).put(new NotInterested());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}finally{
				current.getLock().unlock();
			}
		}
		
		
		current.getmissingpieces().remove(piece_index);
		
		
		if(current.getmissingpieces().isEmpty()){
			peerProcess.process.incrementPeersDownloadComplete();
		//	Logger.write_message("Peerid: ["+ peer_id +"]  has downloaded the complete file");
		/*	try {
				synchronized (peerProcess.debug_file){
				peerProcess.debug_file.write("Have handler:File downloaded completely for"+Integer.toString(peer_id));
				peerProcess.debug_file.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
			//System.out.println("peers download complete count is:"+peerProcess.process.PeersDownloadComplete());
			//System.out.println("number of peers is:"+peerProcess.process.numberOfPeers());
			if(peerProcess.process.numberOfPeers() == peerProcess.process.PeersDownloadComplete()){
				// All downloads complete
				// close the necessary resources
				System.out.println("all processes got the file");
				peerProcess.process.setRunning(false);
				return;
			}
			
		}
		
		
		
		
	
	
	}

}
