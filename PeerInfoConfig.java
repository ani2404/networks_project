

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class PeerInfoConfig {
	public class PeerSocketAddress{
		String hostname;
		Integer port;
		PeerSocketAddress(String hostname,Integer port){
			this.hostname = hostname;
			this.port = port;
		}
		public String hostname() {
			// TODO Auto-generated method stub
			return hostname;
		}
		public int port() {
			// TODO Auto-generated method stub
			return port;
		}
	};

	
	private ArrayList<PeerSocketAddress> peersAddressList = new  ArrayList<PeerSocketAddress>();
	private Boolean hasCompleteFile;
	private Integer port;
	private Boolean valid_process;
	private Integer numberOfPeers;
	public PeerInfoConfig(int peer_id) {
		valid_process= false;
		numberOfPeers=0;
		BufferedReader reader = null;
		try {
			
			reader = new BufferedReader(new FileReader("PeerInfo.cfg"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				int current_peer_id = Integer.parseInt(parts[0]);
				numberOfPeers++;
				if(peer_id == current_peer_id){
					if(parts[3].equals("1"))
						hasCompleteFile = true;
					else
						hasCompleteFile = false;
					valid_process = true;
					this.port = Integer.parseInt(parts[2]);
				}
				else if(!valid_process){			
					String hostName = parts[1];

					int port = Integer.parseInt(parts[2]);
					peersAddressList.add(new PeerSocketAddress(hostName,port));
				}	
				
					
			}
			
			reader.close();
		
		} catch (FileNotFoundException e) {
			System.out.println("Error:No peer config file present in the directory");
		} catch (IOException e) {
			System.out.println("Error:I/O error in peer config file parsing");

		}
	}
	public boolean valid_process() {
		// TODO Auto-generated method stub
		return valid_process;
	}
	public int port() {
		// TODO Auto-generated method stub
		return port;
	}
	public boolean hasCompleteFile() {
		// TODO Auto-generated method stub
		return hasCompleteFile;
	}
	public ArrayList<PeerSocketAddress> peersAddressList() {
		// TODO Auto-generated method stub
		return peersAddressList;
	}
	public Integer numberOfPeers(){
		return numberOfPeers;
	}

}
