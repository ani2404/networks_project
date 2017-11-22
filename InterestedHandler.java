

public class InterestedHandler {

	public InterestedHandler(Integer peer_id) {
		Logger.InterestedReceived(peer_id);
		// if already in interested list, do nothing, 
		// else add the remote peer in interested list and reset the download rate to zero
		synchronized(peerProcess.process.getInterestedmap()){
			if(!peerProcess.process.getInterestedmap().containsKey(peer_id)){
				peerProcess.process.getInterestedmap().put(peer_id,new InterestedPeerInfo(peer_id));
			}
		}


	}

}
