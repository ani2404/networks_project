

import java.util.Random;
import java.util.TimerTask;

public class DetermineOptimisticPeer extends TimerTask {

	@Override
	public void run() {
		// TODO Auto-generated method stub

		int i=peerProcess.process.getnumberOfPreferredNeighbors();

		// Select a random choked neighbor if there is any
		synchronized (peerProcess.process.getInterestedmap()) {

			if(i < peerProcess.process.getInterestedlist().size()){
				Random generator = new Random();
				int random_int = generator.nextInt(peerProcess.process.getInterestedlist().size()-i)+i;
				peerProcess.process.getInterestedlist().get(random_int).Unchoke();
				int peer_id = peerProcess.process.getInterestedlist().get(random_int).peer_id();

				try {
					peerProcess.process.getoutputqueuemap().get(peer_id).put(new UnChoke());
					Logger.changeOfOptimisticallyUnchoked(peer_id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


	}

}
