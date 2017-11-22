import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class Listener extends Thread 
{
	// the process peer id and the listening port
	private final int listenPort;
	private final int peer_id;
	private final int time_out_rate;
	
	
	public Listener(int listenPort, int peer_id) 																																														
	{
		this.listenPort = listenPort;
		this.peer_id = peer_id;
		time_out_rate = 10000;
	}
	
	public void run() {
		
		try {
			 // System.out.println("MY IP address is " + InetAddress.getLocalHost().getHostAddress());
			ServerSocket listener = new ServerSocket(listenPort);
			listener.setSoTimeout(time_out_rate);
			// synchronization needs to be done
			while(peerProcess.process.running())
			{
				
				//waiting for the connection to get established
				try{
					Socket socket = listener.accept();				

					//Incoming Connection request

					//receive handshake from the incoming connection
				//	System.out.println("Waiting for receiving handshake after connection established on the server socket");
					Handshake hand_shake = new Handshake(); 
					byte[] data = hand_shake.receiveHandShake(socket);

					// validate the handshake

					String header = new String(data,0,28);

					int incoming_peer_id = Integer.parseInt(new String(data,28,4));


					//P2PFILESHARINGPROJ- header
					if(header.equals("P2PFILESHARINGPROJ0000000000"))
					{

						if(peerProcess.process.peers_info().containsKey(incoming_peer_id)){
							// Error condition
							System.out.println(" Error condition: Peer already connected");
						}			
						else
						{
							//System.out.println("Received Handshake is valid");
							//send hanshake
							//System.out.println("Sending handshake back");
							Handshake send = new Handshake(peer_id);
							send.sendHandShake(socket);

							PeerInfo p = new PeerInfo(socket,incoming_peer_id);

							//Synchronized
							peerProcess.process.peers_info().put(incoming_peer_id,p);
							//peerProcess.process.incrementPeersConnected();
							LinkedBlockingQueue<Message> in_q = new LinkedBlockingQueue<Message>();
							peerProcess.process.getinputqueuemap().put(incoming_peer_id, in_q);
							
							LinkedBlockingQueue<Message> out_q = new LinkedBlockingQueue<Message>();
							peerProcess.process.getoutputqueuemap().put(incoming_peer_id, out_q);
							
							// default assumption is the neigbhors are missing all pieces unless they update it with 
							// their bitfields or have messages
							for(int i=0; i < peerProcess.process.totalPieces();i++)
								peerProcess.process.peers_info().get(incoming_peer_id).getmissingpieces().put(i, true);

							//System.out.println("Spawing a thread for handling incoming messages");
							IncomingMessages in = new IncomingMessages(incoming_peer_id,socket);
							in.start();

							//System.out.println("Spawing a thread for handling outgoing messages");
							OutgoingMessages out = new OutgoingMessages(incoming_peer_id,socket);
							out.start();


							//System.out.println("Spawing a thread for processing messages");
							ProcessingMessageThread pthread = new ProcessingMessageThread(incoming_peer_id);
							pthread.start();

							// Need to send the current bitfield , could have been updated by already established connections
							int totalPieces = peerProcess.process.totalPieces();
							byte[] pieces = new byte[totalPieces];
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
								//Logger.write_message("Sending Bitfield with piece count "+ totalPieces+ " to peer "+ incoming_peer_id);
								Message bitfield_message = new BitField(pieces);
								peerProcess.process.getoutputqueuemap().get(incoming_peer_id).put(bitfield_message);
							}

							Logger.tcp_connection_Established(incoming_peer_id);
						}


					}
					else 
					{
						System.out.println("Invalid peer");
					}
				}catch(SocketTimeoutException e){
					if(!peerProcess.process.running())
					{
						System.out.println("the process is no more running");
						break;
					}
					else{
						//System.out.println("Conncection accept timeout");
					}
				}
				catch(SecurityException e){
					System.out.println("Error:Unable to accept connection from remote host: due to security issue");
				}
				catch(Exception e){
					System.out.println("Error:unknown exception in listener thread");
				}
			}
		//	System.out.println("Exiting the listener thread");
			listener.close();


		} catch (IOException e) {
			System.err.println(e);
		}
		catch(SecurityException e){
			System.out.println("Error:Unable to accept connection from remote host: due to security issue");
		}
		catch(IllegalArgumentException e){
			System.out.println("Error:Invalid port number:");
		}
	}

	
}
