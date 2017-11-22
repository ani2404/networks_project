import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// This class implements interface to access/update the information regarding individual peer
public class PeerInfo {
	
	private final Socket socket;
	//private final Integer peer_id;
	private HashMap<Integer,Boolean> missing_pieces;
	private ConcurrentHashMap<Integer,Boolean> interesting_pieces;
	private Integer current_requested_piece;
	private AtomicInteger pieces_requested_count= new AtomicInteger();
	private ReentrantLock lockPeerInterestInfo = new ReentrantLock();
/*	private LinkedBlockingQueue<Message> outgoing_messages;
	private LinkedBlockingQueue<Message> incoming_messages; */
/*	Integer download_rate;
	Choke_type current_choke_status; */
	

	public PeerInfo(Socket socket,Integer peer_id){
		this.socket = socket;
		this.missing_pieces = new HashMap<>();
		this.interesting_pieces = new ConcurrentHashMap<>();
		this.current_requested_piece = -1;
	//	this.peer_id = peer_id;
	/*	this.outgoing_messages = new LinkedBlockingQueue<>();
		this.incoming_messages = new LinkedBlockingQueue<>();
		download_rate = 0;
		current_choke_status = Choke_type.Choke;*/
		pieces_requested_count.set(0);
	}
	// Getter and setter functions
	
	public Socket getSocket() {
		return socket;
	}

	public HashMap<Integer,Boolean> getmissingpieces() {
		return missing_pieces;
	}
	
	public ConcurrentHashMap<Integer,Boolean> getinterestingpieces() {
		return interesting_pieces;
	}
	
	public Integer getcurrentrequestedpiece() {
		return current_requested_piece;
	}
	
/*	public LinkedBlockingQueue<Message> getinputQueue(){
		return incoming_messages;
	}
	
	public LinkedBlockingQueue<Message> getOutputQueue(){
		return outgoing_messages;
	}
	*/
	public ReentrantLock getLock(){
		return lockPeerInterestInfo;
	}

	public int getpiecesrequestedcount() {
		// TODO Auto-generated method stub
		return pieces_requested_count.get();
	}

	public void decrementpiecesrequestedcount() {
		// TODO Auto-generated method stub
		pieces_requested_count.decrementAndGet();
	}
	
	public void incrementpiecesrequestedcount() {
		// TODO Auto-generated method stub
		pieces_requested_count.incrementAndGet();
	}

/*	public Boolean isUnchoked() {
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

	public void incrementDownloadRate() {
		// TODO Auto-generated method stub
		download_rate++;
	} */

	public int requestedPiece() {
		// TODO Auto-generated method stub
		return current_requested_piece;
	}

	public void setrequestedPiece(Integer piece_index) {
		// TODO Auto-generated method stub
		current_requested_piece = piece_index;
	}

/*	public int download_rate() {
		// TODO Auto-generated method stub
		return download_rate;
	}

	public void resetdownloadrate() {
		// TODO Auto-generated method stub
		download_rate = 0;
	}*/
}
