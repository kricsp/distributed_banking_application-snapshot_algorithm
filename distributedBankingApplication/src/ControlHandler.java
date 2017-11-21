import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
* 
*
* @author  Surendrakumar Koneti
* @since   2017-11-16
*/

public class ControlHandler implements Runnable{
	
	Socket socket = null;
	Branch bobj = null;
	String name;
	
	public ControlHandler(Socket sock, Branch bcin, String in) {
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
					if(incoming != null) {
					if(incoming.hasInitSnapshot()) {
						
						int snapid = incoming.getInitSnapshot().getSnapshotId();
						System.out.println("InitSnapshot Message received " + snapid);
						bobj.initSnapshot(snapid);
					}
					if(incoming.hasRetrieveSnapshot()) {
						int id = incoming.getRetrieveSnapshot().getSnapshotId();
						
						if(Branch.snapshotList.containsKey(id)) {
							if(Branch.snapshotList.get(id).checkStatus() == 1) {
								int balance = Branch.snapshotList.get(id).getBalance();
								int snapshotid = id;
								Bank.BranchMessage.Builder brmessage = Bank.BranchMessage.newBuilder();
								Bank.ReturnSnapshot.Builder rr = Bank.ReturnSnapshot.newBuilder();
								Bank.ReturnSnapshot.LocalSnapshot.Builder ls = Bank.ReturnSnapshot.LocalSnapshot.newBuilder();
								ls.setBalance(balance);
								ls.setSnapshotId(id);
								for(String temp: Branch.snapshotList.get(id).treeMap.keySet()) {
									int value = Branch.snapshotList.get(id).treeMap.get(temp).getMessages();
									ls.addChannelState(value);
								}
								rr.setLocalSnapshot(ls.build());
								brmessage.setReturnSnapshot(rr.build());
								OutputStream output = socket.getOutputStream();
							    brmessage.build().writeDelimitedTo(output);
							    output.flush();
							}
							
						}
						
						
						
					}
					}
				}
			 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			}
		}
		
}
