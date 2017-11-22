

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class OutgoingMessages extends Thread{

	Integer peer_id;
	Socket socket;
	BufferedOutputStream socket_output_stream;
	public OutgoingMessages(Integer peer_id,Socket socket) throws IOException{
		this.peer_id= peer_id;
		this.socket= socket;
		try{
			this.socket_output_stream = new BufferedOutputStream(this.socket.getOutputStream());
			//System.out.print(socket.getSendBufferSize());
		}catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
			System.out.println("Could not open the socket output buffer for peer_id:"+ peer_id);
		}
	}
	
	public void run(){
		
		while(peerProcess.process.running()){
			
			Message m= null;
			try {
				m = peerProcess.process.getoutputqueuemap().get(peer_id).poll(10000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				if(!peerProcess.process.running())
					break;
			}
			
			if(m != null){
				try {
				//	Logger.write_message("Sending message type "+ m.getmessagetype()+" to "+peer_id);
					socket_output_stream.write(m.getmessageLength());
					socket_output_stream.write(m.getmessagetype());
					if(m.getmessage() != null)
						socket_output_stream.write(m.getmessage());
					socket_output_stream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			
		}
		
		try {
			// check if the have messages need to be send before closing down
			while(peerProcess.process.getoutputqueuemap().get(peer_id).peek() != null){
				Message last_message = peerProcess.process.getoutputqueuemap().get(peer_id).poll();
				socket_output_stream.write(last_message.getmessageLength());
				socket_output_stream.write(last_message.getmessagetype());
				if(last_message.getmessage() != null)
					socket_output_stream.write(last_message.getmessage());

				socket_output_stream.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
			//	Thread.sleep(2000);
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(peerProcess.process.threadcount() == 0)
				try {
					peerProcess.process.closeresources();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
	}

}
