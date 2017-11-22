import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

class IncomingMessages extends Thread {

	Integer peer_id;
	Socket socket;
	BufferedInputStream socket_input_stream;
	IncomingMessages(Integer peer_id,Socket socket){
		this.peer_id= peer_id;
		this.socket= socket;
		// let the buffer capacity be twice the piece size as no more than that could be incoming
		try {
			socket_input_stream = new BufferedInputStream((socket.getInputStream()));
		//	System.out.println(socket.getReceiveBufferSize());
		//	System.out.println("Incoming setup for peer id:"+ this.peer_id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
			System.out.println("Could not open the socket input buffer for peer_id:"+ peer_id);
		}
		catch(Exception e){
			System.out.println("Incoming message exception");
		}
	}

	public void run(){

		byte[] final_buffer = new byte[0];
		byte[] static_buffer = new byte[(int)peerProcess.process.piecesize()];
		int received_bytes = -1;
		int remaining = 0;
		int offset = 0;
		boolean first_time = true;
			//socket.setSoTimeout(10000);
			while(peerProcess.process.running()){


			try{
				while((received_bytes = socket_input_stream.read(static_buffer, 0, static_buffer.length)) > -1) {
					byte[] temp_buff = new byte[final_buffer.length + received_bytes]; 
					System.arraycopy(final_buffer, 0, temp_buff, 0, final_buffer.length);
					System.arraycopy(static_buffer, 0, temp_buff, final_buffer.length, received_bytes);
					final_buffer = temp_buff;
					offset = final_buffer.length;
					while(offset >= 5){
						
						if(first_time){
							remaining = ByteBuffer.wrap(final_buffer,0,4).getInt();
							first_time = false;
							received_bytes= final_buffer.length - 5;
					//		Logger.write_message("Received Message Length is:" + remaining);
					//		Logger.write_message("Message Type of the message is:" + final_buffer[4]);
						}

						offset = remaining -received_bytes< 0?received_bytes-remaining:0;
						remaining =remaining -received_bytes< 0?0:remaining-received_bytes;

						if(remaining == 0){
							byte[] message = final_buffer;							
							
							Message m = new Message(final_buffer);
							
							
							peerProcess.process.getinputqueuemap().get(peer_id).put(m);
															

							final_buffer = new byte[offset];
							for(int i=0;i<offset;i++){
								final_buffer[i] = message[message.length-offset+i];									
							}
							first_time = true;
						}

					}

					

				}

			//	System.out.println("No more messages left");
			
				//}finally{
				/*	if(!peerProcess.process.running())
						break;
				}

			}

		} catch (IOException e) {
			System.err.println(e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		} */
			}
			catch(Exception e){
			//	System.out.println("k value is "+ k);
			} 
	}
	}

	}
