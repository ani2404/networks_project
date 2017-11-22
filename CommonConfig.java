
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class CommonConfig 
{
	//Common Configuration Properties
	
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private long fileSize;
    private long pieceSize;
    
    //parse the Common.cfg file and set the variables with the respective properties
    public CommonConfig() 
    {
		// Need to make it singleton so only one object is created
		String file_name = "Common.cfg";
		
		
		Properties file = new Properties();
		
		try {
			
			file.load(new BufferedInputStream(new FileInputStream(file_name)));
			numberOfPreferredNeighbors = Integer.parseInt(file.getProperty("NumberOfPreferredNeighbors"));
			unchokingInterval = Integer.parseInt(file.getProperty("UnchokingInterval"));
			optimisticUnchokingInterval = Integer.parseInt(file.getProperty("OptimisticUnchokingInterval"));
			fileName = file.getProperty("FileName");
			fileSize = Long.parseLong(file.getProperty("FileSize"));
			pieceSize = Long.parseLong(file.getProperty("PieceSize"));
			
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Error:No common config file present in the directory");
			System.exit(1);
		} 
		catch (IOException e) 
		{
			System.out.println("Error:I/O error in common config file parsing");
			System.exit(1);
		}
		catch(SecurityException e){
			System.out.println("Error:Unable to open config file due to security issue");
			System.exit(1);
		}
	}

	public int getNumberOfPreferredNeighbors() 
	{
		return numberOfPreferredNeighbors;
	}

	public int getUnchokingInterval() 
	{
		return unchokingInterval;
	}

	public int getOptimisticUnchokingInterval()
	{
		return optimisticUnchokingInterval;
	}

	public String getFileName() 
	{
		return fileName;
	}

	public long getFileSize()
	{
		return fileSize;
	}

	public long getPieceSize()
	{
		return pieceSize;
	}

}
