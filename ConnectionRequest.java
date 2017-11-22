import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionRequest extends Thread 
{
	private int peer_id;
	private String hostName;
	private int port;

	public ConnectionRequest(int peer_id,String hostName, int port) {
		this.peer_id = peer_id;
		this.hostName = hostName;
		this.port = port;

	}
	
	@Override
	public void run() 
	{	
		
		boolean connected = false;
		while(!connected){
		/*	try {
				synchronized (peerProcess.debug_file){
				peerProcess.debug_file.write("Connection to "+hostName+":"+ port);
				peerProcess.debug_file.write("\n");
				peerProcess.debug_file.flush();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} */
			
			
			try {
				Socket socket = new Socket(hostName, port);
				
	/*			synchronized (peerProcess.debug_file){
					peerProcess.debug_file.write("Sending handshake  ");
					peerProcess.debug_file.write("\n");
					peerProcess.debug_file.flush();
				}
				*/

				connected = true;
				System.out.println("Established connection with "+ hostName + "at port: "+ port);

			        //send hanshake
				Handshake hand_shake = new Handshake(peer_id);
				hand_shake.sendHandShake(socket);
				
			       //receive handshake from the outgoing connection
				Handshake receive_shake = new Handshake(); 
				byte[] data = receive_shake.receiveHandShake(socket);
				
				String header = new String(data,0,28);
				
				int incoming_peer_id = Integer.parseInt(new String(data,28,4));
				Logger.establish_tcp_connection(incoming_peer_id);
				
				//P2PFILESHARINGPROJ- header
				if(header.equals("P2PFILESHARINGPROJ0000000000"))
				{

						if(peerProcess.process.peers_info().containsKey(incoming_peer_id)){
							System.out.println(" Error condition: Peer already connected");
						}			
						else
						{
							// only info about the peer right now is the socket and id info
							PeerInfo p = new PeerInfo(socket,incoming_peer_id);
							
							peerProcess.process.peers_info().put(incoming_peer_id,p);
						//	peerProcess.process.incrementPeersConnected();
							
							LinkedBlockingQueue<Message> in_q = new LinkedBlockingQueue<Message>();
							peerProcess.process.getinputqueuemap().put(incoming_peer_id, in_q);
							
							LinkedBlockingQueue<Message> out_q = new LinkedBlockingQueue<Message>();
							peerProcess.process.getoutputqueuemap().put(incoming_peer_id, out_q);

							// default assumption is the neigbhors are missing all pieces unless they update it with 
							// their bitfields or have messages
							for(int i=0; i < peerProcess.process.totalPieces();i++)
								peerProcess.process.peers_info().get(incoming_peer_id).getmissingpieces().put(i, true);
							
							//System.out.println("Spawn a thread for handling incoming messages");
							IncomingMessages in = new IncomingMessages(incoming_peer_id,socket);
							in.start();
							
							//System.out.println("Spawn a thread for handling outgoing messages");
							OutgoingMessages out = new OutgoingMessages(incoming_peer_id,socket);
							out.start();
						
							
							//System.out.println("Spawn a thread for processing messages");
							ProcessingMessageThread pthread = new ProcessingMessageThread(incoming_peer_id);
							pthread.start();
							
							// Need to send the current bitfield , could have been updated by already established connections
							int totalPieces = peerProcess.process.totalPieces();
							byte[] pieces = new byte[totalPieces];
							//this should be updated to a readers writer lock
							peerProcess.process.getMissingPiecesLock().lock();
							try{
								for (Map.Entry<Integer, Boolean> entry : peerProcess.process.getMissingpieces().entrySet())
								{
									// key index is from 0 to totalPieces -1, safe for byte indexing
								    pieces[entry.getKey()] = 1;
								    totalPieces--;
								}
		
							} finally{
								peerProcess.process.getMissingPiecesLock().unlock();
							}
							
							if(totalPieces != 0){
								// some are not missing
							//	Logger.write_message("Sending Bitfield with piece count "+ totalPieces+ " to peer "+ incoming_peer_id);
								Message bitfield_message = new BitField(pieces);
								peerProcess.process.getoutputqueuemap().get(incoming_peer_id).put(bitfield_message);
							}
							
							
							
				
						}
						
						
						
						
		
				}
				else 
				{
					System.out.println("Invalid remote peer is trying to connect");
				}
			} 
			catch (UnknownHostException e) {
				System.out.println("Error:Invalid remote host name or port name");
			} catch (IOException e) {
				System.out.println("Connection failed while trying to connect to "+ hostName + " ,waiting and trying again");
				try
				{
					Thread.sleep(2000);//2 seconds
				}
				catch(InterruptedException ie){
					ie.printStackTrace();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(SecurityException e){
				System.out.println("Error:Unable to connect to remote host:"+ hostName+ " - " + port +" due to security issue");
			}
			catch(IllegalArgumentException e){
				System.out.println("Error:Invalid port number:"+ port);
			}
			
		}
		
	//	System.out.println("Exiting the client thread");

	}




}
