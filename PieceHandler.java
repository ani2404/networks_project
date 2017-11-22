

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PieceHandler {

	public PieceHandler(int peer_id, byte[] message_length, byte[] message) throws IOException {
		
		//this would generate further requests if the remote peer has interesting pieces, 
		//also generate Uninterested messages if certain peers have no more interesting pieces
		// also generates have message to all peer

		
		
		int piece_index = ByteBuffer.wrap(message,0,4).getInt();
		int total_pieces = peerProcess.process.totalPieces();
		int piece_size = (int)peerProcess.process.piecesize();
	//	Logger.write_message("Piece:the piece index received is "+ piece_index + " from "+ peer_id);
	
		if(piece_index >=0 && piece_index < total_pieces && (piece_index == peerProcess.process.peers_info().get(peer_id).requestedPiece())){ 
			// Redundant check but a safe one
			// FileWrite, this can be changed to writing into memory and do a file write at configurable intervals
			ByteBuffer data = ByteBuffer.wrap(message,4,message.length-4);
		//	System.out.println("writing at file location"+piece_index*piece_size);
			peerProcess.process.fileWriter().write(data,piece_index*piece_size);
			
			//2. Update the MissingList of the LocalPeer to remove the newly received item
			// Take lock over the missinglist as other threads might be iterating the list to generate bitfield
			// this lock should be a reader writer lock where consecutive readers are not blocked and
			// consecutive writers are not blocked as the write is mutually exclusive
			peerProcess.process.getMissingPiecesLock().lock();
			try{

				peerProcess.process.getMissingpieces().remove(piece_index);
				System.out.println("Downloaded piece count: " + Integer.toString(total_pieces-(peerProcess.process.getMissingpieces().size())));
				Logger.downloadPiece(peer_id, piece_index);
			/*	if(peerProcess.process.getMissingpieces().isEmpty())
				{
					peerProcess.process.incrementPeersDownloadComplete();
					synchronized (peerProcess.debug_file){
					peerProcess.debug_file.write("Piece handler:File downloaded completely");
					peerProcess.debug_file.flush();
					} 
					Logger.downloadComplete();
				} */
			}finally{
				peerProcess.process.getMissingPiecesLock().unlock();
			}
			
			// increment download rate
			synchronized (peerProcess.process.getInterestedmap()) {
				if(peerProcess.process.getInterestedmap().containsKey(peer_id)){
					peerProcess.process.getInterestedmap().get(peer_id).increment_download_rate(); // this needs to be moved out of peerinfo
				}
			}
			
			//3. Identify if some peers are no more interesting
			ArrayList<Integer> peer_ids = peerProcess.process.getrequested_pieces().get(piece_index);
			synchronized (peer_ids) {

				for(int i:peer_ids){
					PeerInfo current = peerProcess.process.peers_info().get(i);
					current.getLock().lock();
					try{

						current.decrementpiecesrequestedcount();

						if(current.getinterestingpieces().isEmpty() && (current.getpiecesrequestedcount() ==0)){
						//	Logger.write_message("Sending not intereseted to "+ i + " on receiving piece index "+ piece_index);
							peerProcess.process.getoutputqueuemap().get(i).put(new NotInterested());
						}

					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						current.getLock().unlock();
					}

				}
			}
			
			// 4. Generate a new request for new piece from the same peer if he has anymore
			// interesting pieces
			
			int new_requested_piece = -1;
			
			synchronized(peerProcess.process.peers_info())
			{
				PeerInfo current = peerProcess.process.peers_info().get(peer_id);
		
				ArrayList<Integer> list = new ArrayList<Integer>();
				for (Map.Entry<Integer, Boolean> entry : current.getinterestingpieces().entrySet())
				{					
					list.add(entry.getKey());	
				}
				if(list.size() != 0){
					Random generator = new Random();
					int random_int = generator.nextInt(list.size());
					//***** NEED TO remove the piece from other peers interesting list and add it to requested piece
					new_requested_piece = list.get(random_int);
					current.getinterestingpieces().remove(new_requested_piece);
				//	peerProcess.debug_file.write(current.getinterestingpieces().size());
					//peerProcess.debug_file.flush();
					current.setrequestedPiece(new_requested_piece);
					current.incrementpiecesrequestedcount();
					try {
						Logger.write_message("Generating a new request to "+ peer_id + " for piece " + new_requested_piece+ " after receiving a piece");
						peerProcess.process.getoutputqueuemap().get(peer_id).put(new Request(new_requested_piece));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// add this piece in requested piece list
					ArrayList<Integer> peer_list = new ArrayList<Integer>();
					peer_list.add(peer_id);
					peerProcess.process.getrequested_pieces().put(new_requested_piece, peer_list);
					
					//5. Generate have to all peers
					for (Map.Entry<Integer, PeerInfo> entry : peerProcess.process.peers_info().entrySet())
					{
						PeerInfo peer= peerProcess.process.peers_info().get(entry.getKey());

						try {
						//	Logger.write_message("Generating a have to "+ entry.getKey() + " for piece index "+ piece_index);
							peerProcess.process.getoutputqueuemap().get(entry.getKey()).put(new Have(piece_index));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(peer.getinterestingpieces().containsKey(new_requested_piece)){
							peer.getinterestingpieces().remove(new_requested_piece);
						//	peerProcess.debug_file.write(peer.getinterestingpieces().size());
						//	peerProcess.debug_file.flush();
							peer.incrementpiecesrequestedcount();
							peer_list.add(entry.getKey());
							
						}
					}
					
					
				}					
				
		}
			
			if(new_requested_piece == -1){
				peerProcess.process.peers_info().get(peer_id).setrequestedPiece(-1);
				//Logger.write_message("Did not Generate a new request to "+ peer_id + " for piece as it has no more interesting pieces");
				//5. Generate have to all peers
				for (Map.Entry<Integer, PeerInfo> entry : peerProcess.process.peers_info().entrySet())
				{
					PeerInfo peer= peerProcess.process.peers_info().get(entry.getKey());

					try {
				//		Logger.write_message("Generating a have to "+ entry.getKey() + " for piece index "+ piece_index);
						peerProcess.process.getoutputqueuemap().get(entry.getKey()).put(new Have(piece_index));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			
				peerProcess.process.getMissingPiecesLock().lock();
			
				if(peerProcess.process.getMissingpieces().isEmpty())
				{
					peerProcess.process.incrementPeersDownloadComplete();
				/*	synchronized (peerProcess.debug_file){
					peerProcess.debug_file.write("Piece handler:File downloaded completely");
					peerProcess.debug_file.flush();
					} */
					Logger.downloadComplete();
				}
			peerProcess.process.getMissingPiecesLock().unlock();
			//System.out.println("peers download complete count is:"+peerProcess.process.PeersDownloadComplete());
			//System.out.println("number of peers is:"+peerProcess.process.numberOfPeers());
			
			if(peerProcess.process.numberOfPeers()== peerProcess.process.PeersDownloadComplete()){
				// All downloads complete
				// close the necessary resources
				
				System.out.println("all processes got the file");
				peerProcess.process.setRunning(false);
			}
			
			
			


			
			
			
			
			// write to the file
		//	new Thread(new FileWrite(peerProcess.process.fileWriter(),piece_index*piece_size,piece_size,message)).start();;
		}
		
		
		
		
		
	}

}
