

import java.util.concurrent.atomic.AtomicInteger;
enum Choke_type{
	Unchoke,
	Choke
};

public class InterestedPeerInfo {
	

	AtomicInteger download_rate = new AtomicInteger();
	Choke_type current_choke_status;
	Integer peer_id;
	public InterestedPeerInfo(Integer peer_id) {
		// TODO Auto-generated constructor stub
		this.peer_id = peer_id;
		this.download_rate.set(0);
		this.current_choke_status = Choke_type.Choke;
	}
	public Integer peer_id() {
		// TODO Auto-generated method stub
		return peer_id;
	}
	public int download_rate() {
		// TODO Auto-generated method stub
		return download_rate.get();
	}
	public Boolean isUnchoked() {
		// TODO Auto-generated method stub
		return (current_choke_status == Choke_type.Unchoke);
	}
	
	public Boolean isChoked() {
		// TODO Auto-generated method stub
		return (current_choke_status == Choke_type.Choke);
	}
	
	public void choke() {
		// TODO Auto-generated method stub
		current_choke_status = Choke_type.Choke;
	}
	
	public void Unchoke() {
		// TODO Auto-generated method stub
		current_choke_status = Choke_type.Unchoke;
	}
	public void reset_download_rate() {
		// TODO Auto-generated method stub
		download_rate.set(0);
	}
	
	public void increment_download_rate() {
		// TODO Auto-generated method stub
		download_rate.incrementAndGet();
	}
	public int getPeerID() {
		// TODO Auto-generated method stub
		return peer_id;
	}
};
