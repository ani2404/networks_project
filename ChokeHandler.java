import java.util.ArrayList;

public class ChokeHandler {

	public ChokeHandler(Integer peer_id){
		// reupdate the interesting pieces list of the peers which had the piece requested(if requested) 
		// to this remote peer to include this requested piece,
		// as the request is not served
		Logger.choke(peer_id);
		PeerInfo current = peerProcess.process.peers_info().get(peer_id);
		// the below line does not need lock as the this is updated only by the same thread in a 
		//different scenario
		int piece_index = current.requestedPiece();
		if(piece_index != -1){
			ArrayList<Integer> list =peerProcess.process.getrequested_pieces().get(piece_index);
			synchronized (list) {

				
				for(Integer i:list){
					//****** determine lock
					PeerInfo peer = peerProcess.process.peers_info().get(i);
					peer.getLock().lock();
					try {	

						peerProcess.process.peers_info().get(i).getinterestingpieces().put(piece_index,true);
					//	peerProcess.debug_file.write(peerProcess.process.peers_info().get(i).getinterestingpieces().size());
					//	peerProcess.debug_file.flush();
						peerProcess.process.peers_info().get(i).decrementpiecesrequestedcount();
					}finally{
						peer.getLock().unlock();
					}
				}
				peerProcess.process.getrequested_pieces().remove(piece_index);				
			}
			current.setrequestedPiece(-1);
		}
		
	}

}
