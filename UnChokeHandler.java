

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class UnChokeHandler {

	public UnChokeHandler(Integer peer_id) {
		// TODO Auto-generated constructor stub

		// need to identify an interesting piece to request
		int new_requested_piece = -1;
		Logger.unchoke(peer_id);

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
	 
	
				current.setrequestedPiece(new_requested_piece);
				current.incrementpiecesrequestedcount();
				try {
					Logger.write_message("Generating a new request to "+ peer_id + " for piece " + new_requested_piece+ " on receiving unchoke");
					peerProcess.process.getoutputqueuemap().get(peer_id).put(new Request(new_requested_piece));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// add this piece in requested piece list
				ArrayList<Integer> peer_list = new ArrayList<Integer>();
				peer_list.add(peer_id);
				peerProcess.process.getrequested_pieces().put(new_requested_piece, peer_list);

				for (Map.Entry<Integer, PeerInfo> entry : peerProcess.process.peers_info().entrySet())
				{
					PeerInfo peer= peerProcess.process.peers_info().get(entry.getKey());

					if(peer.getinterestingpieces().containsKey(new_requested_piece)){
						peer.getinterestingpieces().remove(new_requested_piece);
					
						peer.incrementpiecesrequestedcount();
						peer_list.add(entry.getKey());

					}
				}


			}
			else{
				//Logger.write_message("Did not Generate a new request to "+ peer_id + " for piece on receiving unchoke as it has no more interesting pieces");
			}

		}

	}

}
