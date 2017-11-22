

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TimerTask;

class compare_download_rate implements Comparator<InterestedPeerInfo>{

	public int compare(InterestedPeerInfo a, InterestedPeerInfo b){
		int rate1 = a.download_rate();
		int rate2 = b.download_rate();
		// return 
		return  rate1 > rate2? -1 : rate1 == rate2 ? 0 : 1;
	}
}

public class DeterminePreferredNeighbors extends TimerTask {


	@Override
	public void run() {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		// TODO Determine the preferred neighbors using the download rate
		boolean changed = false;
		int k = peerProcess.process.getnumberOfPreferredNeighbors();
		synchronized(peerProcess.process.getInterestedmap()){
			peerProcess.process.setInterestedList(new ArrayList<InterestedPeerInfo>());
			ArrayList<InterestedPeerInfo> list = peerProcess.process.getInterestedlist();
			for (Map.Entry<Integer, InterestedPeerInfo> entry : peerProcess.process.getInterestedmap().entrySet()){
				list.add(entry.getValue());
			}

			if(peerProcess.process.getMissingpieces().isEmpty()){
				//shuffle the list of random k preferred neighbors

				Collections.shuffle(list);

			}
			else{
				Collections.sort(list, new compare_download_rate());
			}

			

			// Determine sending of choke and unchoke
			
			for(int i=0; i < list.size();i++){
				int peer_id = list.get(i).peer_id();
				if(list.get(i).isChoked() && (i <k)){
					list.get(i).Unchoke();
					try {
						
						peerProcess.process.getoutputqueuemap().get(peer_id).put(new UnChoke());
					//	Logger.write_message("Sending unchoke to "+ peer_id+ " whose download rate is " + list.get(i).download_rate());
						changed = true;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if(list.get(i).isUnchoked() && (i >= k)){
					list.get(i).choke();
					try {
						peerProcess.process.getoutputqueuemap().get(peer_id).put(new Choke());
					//	Logger.write_message("Sending choke to "+ peer_id+ " whose download rate is " + list.get(i).download_rate());
						/*if(peerProcess.process.request_in_queue().containsKey(peer_id)){							
							Message m = peerProcess.process.request_in_queue().get(peer_id);
							int piece_index = ByteBuffer.wrap(m.getmessage(),0,4).getInt();
						//	Logger.write_message("Removing the request of the peer "+ peer_id+" from the queue due to choke for "+ piece_index);
							peerProcess.process.getinputqueuemap().get(peer_id).remove(m);
							peerProcess.process.request_in_queue().remove(peer_id);
						} */
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				list.get(i).reset_download_rate();
			}
			
			if(changed)
			{
				for(int i=0; i <k && i<peerProcess.process.getInterestedlist().size();i++)
					temp.add(peerProcess.process.getInterestedlist().get(i).getPeerID());
				Logger.changeOfPreferredNeighbours(temp);
			}

		}

	}
	

}
