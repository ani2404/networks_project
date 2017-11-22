

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Handshake {
	
	public byte[] handshake = new byte[32];
	private int peer_id;
	public Handshake(){};
	public Handshake(int peer_id) {
		this.peer_id = peer_id;
		String header = "P2PFILESHARINGPROJ" + "0000000000" + Integer.toString(this.peer_id);
		handshake = header.getBytes();
	}

	public void sendHandShake(Socket socket) {
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(handshake);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	public byte[] receiveHandShake(Socket socket) {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			handshake = (byte[]) in.readObject();
		} catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		}
		
		return handshake;
	}
	
}
