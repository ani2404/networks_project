
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {

	private static int peer_id;
	private static BufferedWriter out;
	private static int piece_count = 0;
	
	public static void startLogger(int peer_ID) {
		
		peer_id = peer_ID;
		String fileName = "log_peer_" + peer_id + ".log";
		File file = new File(fileName);
		
		try {
			out = new BufferedWriter(new FileWriter(file));
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void write_message(String s){
		synchronized (out) {			
			
			try {
				String date = new Date().toString();
				out.append(date+ " : Peer " + peer_id+ " ");
				out.append(s);
				out.newLine();
				out.newLine();
			} catch (IOException e) {
				System.err.println(e);
			}
			}
	} 
	 
	public static void establish_tcp_connection(int neighbor_id) {
		
		synchronized (out) {
			
		
		try {
			String time = new Date().toString();
			String s = time + " : Peer " + peer_id + " makes a connection to Peer " + neighbor_id + ".";
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}
	
	public static void tcp_connection_Established(int peer_ID) {
		synchronized (out) {
		try {
			String time = new Date().toString();
			String s = time + " : Peer " + peer_id + " is connected from Peer " + peer_ID + ".";
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}
	public static void changeOfPreferredNeighbours(ArrayList<Integer> preferredNeighbors)
	{
		synchronized (out) {
		try
		{
			String time = new Date().toString();
			String s = time + " : Peer " +peer_id + " has the preferred neighbors " + preferredNeighbors + ".";
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
			
	}
	
	public static void changeOfOptimisticallyUnchoked(int peer_ID)
	{
		synchronized (out) {
		try
		{
			String date = new Date().toString();
			String s= date + " : Peer " +peer_id  + "has the optimistically unchoked neighbor"+ peer_ID;
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}	
			
		}
	
	public static void unchoke(int peer_ID)
	{
		synchronized (out) {
		try
		{
			String date = new Date().toString();
			String s= date + " : Peer " +peer_id  + "is unchoked by"+ peer_ID;
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
		}
	
	
	public static void choke(int peer_ID)
	{
		synchronized (out) {
		try
		{
			String date = new Date().toString();
			String s= date + " : Peer " +peer_id  + "is choked by"+ peer_ID;
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}
	
	public static void HaveReceived(int peer_ID, int pieceIndex) {
		synchronized (out) {
		try {
			String date = new Date().toString();
			String s = date + " : Peer " + peer_id + " received the 'have' message from Peer " + peer_ID + " for the piece " + pieceIndex + ".";
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}
	
	public static void InterestedReceived(int peer_ID) {
		synchronized (out) {
		try {
			String date = new Date().toString();
			String s = date + " : Peer " + peer_id + " received the 'interested' message from Peer " + peer_ID + ".";
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}
	
	public static void receiveNotInterested(int peer_ID) {
		synchronized (out) {
		try {
			String date = new Date().toString();
			String s = date + " : Peer " + peer_id + " received the 'not interested' message from Peer " + peer_ID + ".";
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}
	
	public static void downloadPiece(int peer_ID, int pieceIndex) {
		synchronized (out) {
		piece_count++;
		try {
			String date = new Date().toString();
			String s = date + " : Peer " + peer_id + " has downloaded the piece " + pieceIndex +" from Peer " + peer_ID + ".";
			out.append(s);
			out.newLine();
			s = "Now  the number of pieces it has is " + piece_count;
			out.append(s);
			out.newLine();
			out.newLine();
		} catch (IOException e) {
			System.err.println(e);
		}
		}
	}	
		
	public static void downloadComplete() {
		synchronized (out) {
			
			try {
				String date = new Date().toString();
				String s = date + " : Peer " + peer_id + " has downloaded the complete file.";
				out.append(s);
				out.newLine();
				out.newLine();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
	
	public static void closeLogger() {
		try {
			synchronized (out) {
				out.close();
			}			
		} catch (IOException e) {
			System.err.println(e);
		} 
	}

	public static void flush() {
		// TODO Auto-generated method stub
		synchronized(out){
			try {
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}