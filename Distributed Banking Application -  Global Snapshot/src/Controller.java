import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Controller{
	
	public static int snapshotId = 0;
	public static List<Integer> InitiatedSnaps = new ArrayList<Integer>();

	public static void main(String[] args){

		if(args.length != 2){
			System.out.println("Usage: ./Controller 4000 branch.txt");
			System.exit(0);
		}

		HashMap<String,Socket> socklist = new HashMap<String,Socket>();
		
		int totalBalance = 0;
		int getSnapShot = 0;
		FileProcessor fp = new FileProcessor(args[1]);
		totalBalance = Integer.parseInt(args[0]);

		List<String> branch = new ArrayList<>();
		List<String> ip = new ArrayList<>();
		List<Integer> port = new ArrayList<>();

		while(true){
			String value = fp.readLine();
			if(value == null){
				break;	
			}
			String[] splitValue;
			splitValue = value.split(" ");
			branch.add(splitValue[0]);
			ip.add(splitValue[1]);
			port.add(Integer.parseInt(splitValue[2]));
		}

		Socket socket;
		int noBranches = branch.size();
		int balance = totalBalance/noBranches;
		Bank.InitBranch.Builder bank = Bank.InitBranch.newBuilder();
		Bank.InitBranch.Branch.Builder bankBranch;
		Bank.BranchMessage.Builder branchMessage = Bank.BranchMessage.newBuilder();
		
		bank.setBalance(balance);
		for (int i = 0; i < branch.size(); i++) {
			
			bankBranch = Bank.InitBranch.Branch.newBuilder();
			bankBranch.setName(branch.get(i));
			bankBranch.setIp(ip.get(i));
			bankBranch.setPort(port.get(i));
			bank.addAllBranches(bankBranch);		
		}
		System.out.println("Sending InitBranch message");
		for(int i = 0; i < bank.getAllBranchesCount(); i++) {
			try {
				socket = new Socket(bank.getAllBranches(i).getIp(),bank.getAllBranches(i).getPort());	
				OutputStream output = socket.getOutputStream();
				branchMessage.setInitBranch(bank.build());
			    branchMessage.build().writeDelimitedTo(output);
			    output.flush();
			    socklist.put(bank.getAllBranches(i).getName(), socket);
			}
			catch(IOException e) {
				System.out.println(e);
			}
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		Map<String, Socket> treeMap = new TreeMap<String, Socket>(socklist);
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
/*		
			Bank.BranchMessage.Builder branchMessage1 = Bank.BranchMessage.newBuilder();
			Bank.InitSnapshot.Builder snapshot = Bank.InitSnapshot.newBuilder() ;
			snapshot.setSnapshotId(snapshotId);
			try {
				Socket send;
				send = socklist.get(bank.getAllBranches(1).getName());
		//		send = new Socket(bank.getAllBranches(2).getIp(), bank.getAllBranches(2).getPort());
				OutputStream out = send.getOutputStream();
				branchMessage1.setInitSnapshot(snapshot.build());
				System.out.println("Initiating SnapShot");
				branchMessage1.build().writeDelimitedTo(out);
				out.flush();
				InitiatedSnaps.add(snapshotId);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
*/	
		
		Thread snap = new Thread(new Runnable() {
						
			public void run() {
				
				while(true) {
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							Bank.BranchMessage.Builder branchMessage1 = Bank.BranchMessage.newBuilder();
							Bank.InitSnapshot.Builder snapshot = Bank.InitSnapshot.newBuilder() ;
							snapshot.setSnapshotId(snapshotId);
							try {
								Random rand = new Random();
								List<String> keys = new ArrayList<String>(socklist.keySet());
								String randomKey = keys.get(rand.nextInt(keys.size()));
								Socket sock;
								//System.out.println(randomKey);
								sock = socklist.get(randomKey);
							//	send = socklist.get(bank.getAllBranches(1).getName());
								OutputStream out = sock.getOutputStream();
								branchMessage1.setInitSnapshot(snapshot.build());
								System.out.println();
								System.out.println("Initiating SnapShot to " + randomKey);
								branchMessage1.build().writeDelimitedTo(out);
								out.flush();
								InitiatedSnaps.add(snapshotId);
								snapshotId += 1;
								} catch (UnknownHostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}		
				}
			}
		});
	
		snap.start();
	
		Collections.sort(branch);
			
		while(true) {			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println();
			Bank.BranchMessage.Builder brMessage = Bank.BranchMessage.newBuilder();
			Bank.RetrieveSnapshot.Builder retreive = Bank.RetrieveSnapshot.newBuilder();
			//int temp = getSnapShot;
			int id = InitiatedSnaps.get(getSnapShot);
			retreive.setSnapshotId(id);
			System.out.println("Retreiving SnapShot " + id);
			System.out.println("Snapshot_id: " + id);
			for (String br : treeMap.keySet()) {
				Socket sock = treeMap.get(br);
				OutputStream out;
				try {
					out = sock.getOutputStream();
					brMessage.setRetrieveSnapshot(retreive.build());
				//	System.out.println("Retreiving SnapShots " + br);
					brMessage.build().writeDelimitedTo(out);
					out.flush();
					InputStream in = sock.getInputStream();
					Bank.BranchMessage incoming = Bank.BranchMessage.parseDelimitedFrom(in);
					if(incoming != null) {
						//	System.out.println("Return message received");
						//	int idNo = incoming.getReturnSnapshot().getLocalSnapshot().getSnapshotId();
						int bal = incoming.getReturnSnapshot().getLocalSnapshot().getBalance();
						int ch = incoming.getReturnSnapshot().getLocalSnapshot().getChannelStateCount();
						System.out.println();
						System.out.print(br+":" +bal+ ", ");
						int j = 0;
						for(int i = 0; i < branch.size(); i++) {
							if(!(branch.get(i).equals(br))){
								System.out.print(branch.get(i) + "-->" + br+": ");
								System.out.print(incoming.getReturnSnapshot().getLocalSnapshot().getChannelState(j) + " ");
								j++;
								if(j != ch){
									System.out.print(", ");
								}
							}
						}
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
			getSnapShot++;
			if(getSnapShot > InitiatedSnaps.size()) {
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
