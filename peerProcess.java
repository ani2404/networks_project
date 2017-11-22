import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;




public class peerProcess
{

	//Common.cfg properties 
    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final long fileSize;
    private final long pieceSize;
    private final int totalPieces;
    
    //PeerInfo.cfg variables
    private final int peer_id;
    private final int port;
    
    //private final Boolean hasCompleteFile;
    private final ArrayList<PeerInfoConfig.PeerSocketAddress> peersAddressList;
    
    // 
    private ConcurrentHashMap<Integer,PeerInfo> peers_info = new ConcurrentHashMap<Integer,PeerInfo>();
    
    private FileChannel fc_read,fc_write;
    private RandomAccessFile fd;
    
    // should be a read write lock
    private ConcurrentHashMap<Integer, ArrayList<Integer>>requested_pieces = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
    
    private final Integer numberOfPeers;
    private AtomicInteger peers_download_complete_count = new AtomicInteger();
    
    private ConcurrentHashMap<Integer,Boolean> missing_pieces = new ConcurrentHashMap<Integer,Boolean>();
    private ReentrantLock lock_missing_pieces = new ReentrantLock(); // should be a read write lock
   
   // private ReentrantLock lock_self_interested_list = new ReentrantLock();
   // private ConcurrentHashMap<Integer,Boolean> self_interested_list = new ConcurrentHashMap<Integer,Boolean>();
    
    // list of peers interested in me
   // private ReentrantLock lock_interested_list = new ReentrantLock();
    private ArrayList<InterestedPeerInfo> interested_list = new  ArrayList<InterestedPeerInfo>();
    private ConcurrentHashMap<Integer,InterestedPeerInfo> interestedmap = new ConcurrentHashMap<Integer,InterestedPeerInfo>();
    
    private ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>> inputqueuemap = new ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>>();
   
    private ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>> outputqueuemap = new ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>>();
    
    private ConcurrentHashMap<Integer, Message> request_in_queue = new ConcurrentHashMap<Integer, Message>();
    
    // running will be set to false when the process is closing down
	private AtomicBoolean running = new AtomicBoolean();
    
	private AtomicInteger threadcount = new AtomicInteger();
    
    public static peerProcess process;
    
//    private ByteOrder machine_order = ByteOrder.LITTLE_ENDIAN;
 //   public static FileWriter debug_file,debug_file2;
    
    public peerProcess(Integer peer_id) throws IOException {
    //	 debug_file= new FileWriter("./debug.txt");
    	 
    	running.set(true);
    //	numberOfPeers.set(0);
    	peers_download_complete_count.set(0);;
		// TODO Auto-generated constructor stub
    	CommonConfig config = new CommonConfig();
    	//set the properties for the current peer prKeyIteratorocess p with the properties read from common.cfg
    	numberOfPreferredNeighbors = config.getNumberOfPreferredNeighbors();
    	unchokingInterval = config.getUnchokingInterval();
    	optimisticUnchokingInterval = config.getOptimisticUnchokingInterval();
    	fileName = config.getFileName();
    	fileSize = config.getFileSize();
    	pieceSize = config.getPieceSize();
    	totalPieces = (int) Math.ceil((double)fileSize/pieceSize);
    	
    	

		// Parse the PeerInfoConfig file
    	PeerInfoConfig peerInfo = new PeerInfoConfig(peer_id);
    	
		
		if(peerInfo.valid_process()){
			this.peer_id = peer_id;
			this.port = peerInfo.port();
			this.numberOfPeers = peerInfo.numberOfPeers();
			this.threadcount.set(numberOfPeers()-1);
			//this.hasCompleteFile = peerInfo.hasCompleteFile();
			this.peersAddressList = peerInfo.peersAddressList();
			if(!peerInfo.hasCompleteFile()){
				// all pieces are missing, assuming PieceIndex starts from zero.
				for(int i=0; i < totalPieces;i++)
					missing_pieces.put(i, true);
			}
			else{
				incrementPeersDownloadComplete();
			}
			
	    	
	//		System.out.println("File path is set to :" + "/peer_"+ Integer.toString(this.peer_id) +"/"+ this.fileName);
			// fc needs to be closed once file write/read is complete
			try {
				String s = "./peer_"+ Integer.toString(this.peer_id);
				File file = new File(s);
				file.mkdir();
				s = s + "/"+this.fileName;
				fd = new RandomAccessFile(s, "rw");
		//		System.out.println("Length of the file is :" + fd.length());
				fd.setLength(this.fileSize);
			//	System.out.println("Length of the file after updating is :" + fd.length());
				fc_read = fd.getChannel();
				fc_write = fd.getChannel();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("Error:Actual File could not be created");
			}
			catch(SecurityException e){
				System.out.println("Error:Unable to open the actual file due to security issue in Process:"+ this.peer_id);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error:Unable to set the length of the actual file in Process:"+ this.peer_id);
			}
	    	
			
	      }
		else
		{
			System.out.println("Error:Invalid peerProcess");
			this.port = -1;
			this.peer_id = -1;
		//	this.hasCompleteFile = false;
			this.peersAddressList = null;
			this.numberOfPeers =-1;
			
		}
		
	}
    
	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException
	{
		//Create new instance of PeerProcess
    	peerProcess.process = new peerProcess(Integer.parseInt(args[0]));
    	
    	if(peerProcess.process.peer_id != -1)
    	{
    	
    		Logger.startLogger(peerProcess.process.peer_id);

    		//System.out.println("Spawn a listener thread");
    		// Listen on the port for new connections
    		Listener listen = new Listener(peerProcess.process.port, peerProcess.process.peer_id);
    		listen.start();

    		//System.out.println("Spawn a client thread");
    		// Establish connection with peers that are already in the pool
    		for(PeerInfoConfig.PeerSocketAddress s: peerProcess.process.peersAddressList()) {
    			String hostName = s.hostname();
    			int port = s.port();

    			ConnectionRequest connect = new ConnectionRequest(peerProcess.process.peer_id,hostName,port);
    			connect.start();
    		}

    		//spawn the timer task, identify on whether to schedule as demean thread or user thread
    		//System.out.println("Spawn a timer thread for determining preferred neighbors in Process:"+ peerProcess.process.peer_id());
    		try{
    			TimerTask t = new DeterminePreferredNeighbors();
    			Timer timer = new Timer(true);
    			timer.scheduleAtFixedRate(t, 0, peerProcess.process.unchokingInterval()*1000);
    		}catch(NullPointerException e){
    			System.out.println("Error:Preferred neighbors task is empty in Process:"+ peerProcess.process.peer_id());
    			return;
    		}
    		catch(IllegalArgumentException e){
    			System.out.println("Error:unchokingInterval is negative in Process:"+ peerProcess.process.peer_id());
    			return;
    		}
       		catch(IllegalStateException  e){
    			System.out.println("Error: Unchoking Timer was already scheduled in Process:"+ peerProcess.process.peer_id());
    			return;
    		}

    		try{
    		//	System.out.println("Spawn a timer thread for determining optimistic neighbor");
    			TimerTask t2 = new DetermineOptimisticPeer();
    			Timer timer2 = new Timer(true);
    			timer2.scheduleAtFixedRate(t2, 0,peerProcess.process.optimisticUnchokingInterval()*1000);
    		}catch(NullPointerException e){
    			System.out.println("Optimistic neighbors task is empty in Process:"+ peerProcess.process.peer_id());
    			return;
    		}
    		catch(IllegalArgumentException e){
    			System.out.println("Error:optimisticUnchokingInterval is negative in Process:"+ peerProcess.process.peer_id());
    			return;
    		}
       		catch(IllegalStateException  e){
    			System.out.println("Error:Optimistic Timer was already scheduled in Process:"+ peerProcess.process.peer_id());
    			return;
    		}
    		
    		TimerTask t = new Flush();
			Timer timer = new Timer(true);
			timer.scheduleAtFixedRate(t, 0, 15000);

    		listen.join();
    		//connect.join();
    		
    	}
    	System.out.println("Main thread exiting");
	}

	private int peer_id() {
		// TODO Auto-generated method stub
		return peer_id;
	}

	private int optimisticUnchokingInterval() {
		// TODO Auto-generated method stub
		return optimisticUnchokingInterval;
	}

	private int unchokingInterval() {
		// TODO Auto-generated method stub
		return unchokingInterval;
	}

	public Boolean running() {
		// TODO Auto-generated method stub
		
		return running.get();
	}

	public ConcurrentHashMap<Integer, PeerInfo> peers_info() {
		// TODO Auto-generated method stub
		return peers_info;
	}

	/*public void incrementPeersConnected() {
		// TODO Auto-generated method stub
		numberOfPeers.incrementAndGet();
	} */
	
	public int numberOfPeers() {
		// TODO Auto-generated method stub
		return numberOfPeers;
	}
	
	public void incrementPeersDownloadComplete() {
		// TODO Auto-generated method stub
		peers_download_complete_count.incrementAndGet();
	}

	public int PeersDownloadComplete() {
		// TODO Auto-generated method stub
		return peers_download_complete_count.get();
	}
	public long piecesize() {
		// TODO Auto-generated method stub
		return pieceSize;
	}

	public int totalPieces() {
		// TODO Auto-generated method stub
		return totalPieces;
	}

/*	public boolean hasCompleteFile() {
		// TODO Auto-generated method stub
		return hasCompleteFile;
	} */

	public ArrayList<PeerInfoConfig.PeerSocketAddress> peersAddressList() {
		// TODO Auto-generated method stub
		return peersAddressList;
	}
	
	public ReentrantLock getMissingPiecesLock(){
		return lock_missing_pieces;
	}
	
	/*public ReentrantLock getselfinterestedlistlock(){
		return lock_self_interested_list;
	}
	
	public ConcurrentHashMap<Integer, Boolean> getselfinterestedlist(){
		return self_interested_list;
	} */
	
	public ConcurrentHashMap<Integer,Boolean> getMissingpieces(){
		return missing_pieces;
	}

	public long fileSize() {
		// TODO Auto-generated method stub
		return fileSize;
	}

	public FileChannel fileWriter() {
		// TODO Auto-generated method stub
		return fc_write;
	}
	
	public FileChannel fileReader() {
		// TODO Auto-generated method stub
		return fc_read;
	}

	public void setRunning(boolean b) {
		// TODO Auto-generated method stub
		running.set(b);
	}

	public ConcurrentHashMap<Integer, ArrayList<Integer>> getrequested_pieces() {
		// TODO Auto-generated method stub
		return requested_pieces;
	}

	public ConcurrentHashMap<Integer,InterestedPeerInfo> getInterestedmap() {
		// TODO Auto-generated method stub
		return interestedmap;
	}
	
	public ArrayList<InterestedPeerInfo> getInterestedlist() {
		// TODO Auto-generated method stub
		return interested_list;
	}

/*	public ReentrantLock getInterestedListlock() {
		// TODO Auto-generated method stub
		return lock_interested_list;
	}
*/
	public int getnumberOfPreferredNeighbors() {
		// TODO Auto-generated method stub
		return numberOfPreferredNeighbors;
	}
	
	public ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>> getinputqueuemap(){
		return inputqueuemap;
	}
	
	public ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>> getoutputqueuemap(){
		return outputqueuemap;
	}

	public void closeresources() throws IOException {
		// TODO Auto-generated method stub
		fc_read.close();
		fc_write.close();
		fd.close();
		Logger.closeLogger();
		
	}

	public void setInterestedList(ArrayList<InterestedPeerInfo> arrayList) {
		// TODO Auto-generated method stub
		interested_list = arrayList;
	}

	public Integer threadcount() {
		// TODO Auto-generated method stub
		return threadcount.decrementAndGet();
	}

	public void resourcesflush() {
		// TODO Auto-generated method stub
		Logger.flush();
		
	}

	/*public ByteOrder machineorder() {
		// TODO Auto-generated method stub
		return machine_order;
	} */
	public ConcurrentHashMap<Integer, Message> request_in_queue(){
		return request_in_queue;
	}
}
