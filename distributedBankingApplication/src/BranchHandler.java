import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
* 
*
* @author  Surendrakumar Koneti
* @since   2017-11-16
*/

public class BranchHandler implements Runnable{
	
	Socket socket = null;
	Branch bobj = null;
	String name;
	public BranchHandler(Socket sock, Branch bcin, String in) {
		this.socket = sock;
		this.bobj = bcin;
		this.name = in;
	}
	@Override
	public void run() {
		while(true) {
			try {
					InputStream in = socket.getInputStream();
					Bank.BranchMessage incoming = Bank.BranchMessage.parseDelimitedFrom(in);
					bobj.handleRequest(incoming, name);
				}
			 catch (IOException e) {
				e.printStackTrace();
			}				
			}
		}
		
}
