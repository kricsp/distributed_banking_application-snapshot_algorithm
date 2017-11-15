import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class BranchHandler implements Runnable{
	
	Socket socket = null;
	Branch bobj = null;
	String name;
	public BranchHandler(Socket sock, Branch bcin, String in) {
		this.socket = sock;
		this.bobj = bcin;
		this.name = in;
	//	System.out.println(name);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			}
		}
		
}
