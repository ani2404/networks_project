

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
// Class not used
public class FileWrite implements Runnable{
	int position;
	int size;
	ByteBuffer data;
	FileChannel fwrite;
	
	public FileWrite(FileChannel fwrite,int position,int size, byte[] data){
		this.fwrite = fwrite;
		this.position = position;
		this.size = size;
		this.data = ByteBuffer.wrap(data,4,data.length);// start at offset 4 as the first four bytes is the piece index field
	}
	
	public void run(){
		
		try {
			fwrite.write(this.data,position);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
