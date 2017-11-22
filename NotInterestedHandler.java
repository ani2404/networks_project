

public class NotInterestedHandler {

	public NotInterestedHandler(Integer peer_id) {
		Logger.receiveNotInterested(peer_id);
		// Update the interested list of the local peer to remove this remote peer and 
		//reset his download rate to zero
		synchronized(peerProcess.process.getInterestedmap()){
			if(peerProcess.process.getInterestedmap().containsKey(peer_id)){
				peerProcess.process.getInterestedmap().remove(peer_id);
			}
		}


	}

}
