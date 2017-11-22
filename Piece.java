import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Piece extends Message{
	
	public Piece(int index,FileChannel fc) {
		int piece_size = (int)peerProcess.process.piecesize();
		int remaining_piece_size = (int)(peerProcess.process.fileSize()%peerProcess.process.piecesize());
			
		this.message_type = 7; // type is 7
	//	System.out.println("Sendingpiece:the piece index sent is"+ index);
		
		ByteBuffer b= null;
		int payload_length = 0;
		//check if it is not the last piece or if the last piece is full
		if(index != (peerProcess.process.totalPieces()-1) || (remaining_piece_size==0))
		{
			payload_length = piece_size + 4;// extra four bytes for piece index	
		}
		
		else{
			payload_length = remaining_piece_size + 4;// extra four bytes for piece index	
		}
		
		this.message_length = ByteBuffer.allocate(4).putInt(payload_length).array();
		this.message = new byte[payload_length];
		b=  ByteBuffer.allocate(payload_length-4);
		
		b.clear();
		try {
			fc.read(b, index*piece_size);
			b.flip();
			ByteBuffer b2 = ByteBuffer.allocate(4).putInt(index);
			b2.flip();
			b2.get(message,0,4);
			b.get(message,4,message.length-4);
			
			/* FileOutputStream	file_pointer = new FileOutputStream("/home/ani2404/Desktop/piece_transfer.txt");
		     file_pointer.write(message);
		     file_pointer.close();*/
	
			//System.out.println("Size of Piece is :" + this.message.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("I/O Exception while reading the file at index:" + index);
		}
		
	}
	
}
