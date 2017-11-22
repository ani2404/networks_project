import java.util.TimerTask;

public class Flush extends TimerTask {

	
	public void run() {
		// TODO Auto-generated method stub
		
		peerProcess.process.resourcesflush();

	}

}
