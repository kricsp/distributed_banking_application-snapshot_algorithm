import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
* 
*
* @author  Surendrakumar Koneti
* @since   2017-11-16
*/

public class Branch{
	
	public static Branch brobj = new Branch();
	public ServerSocket server = null;
	public static BranchContext bc;
	public static HashMap<Integer,Snapshot> snapshotList = new HashMap<Integer,Snapshot>();
	
	public synchronized void initSnapshot(int id) {
		Snapshot sc = new Snapshot(id);
		for (String br : bc.socket.keySet()) {
			MarkerState bc1 = new MarkerState(br);
			sc.channels.put(br, bc1);
		}
		snapshotList.put(id, sc);
		sc.InitSnapState();
		sc.setInitialBalance(bc.getBalance());
	
		Bank.BranchMessage.Builder branchMessage = Bank.BranchMessage.newBuilder();
		Bank.Marker.Builder marker = Bank.Marker.newBuilder();
		marker.setSnapshotId(id);
	
		for(String sockfd : bc.socket.keySet()) {
			Socket sock = bc.socket.get(sockfd);
			OutputStream output;
			try {
				output = sock.getOutputStream();
				branchMessage.setMarker(marker.build());
				branchMessage.build().writeDelimitedTo(output);
				output.flush();
			//	System.out.println("Markers Sent");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		for(String temp : sc.channels.keySet()) {
			sc.channels.get(temp).setStartState();
		}
		
	}
	
	public synchronized void handleRequest(Bank.BranchMessage incoming, String name) {
		
		if(incoming.hasTransfer()) {
			int amount = incoming.getTransfer().getMoney();
			Branch.bc.updateMoney(amount);
			if(Branch.snapshotList.size() > 0) {
			for(Integer sp: Branch.snapshotList.keySet()) {
				if(Branch.snapshotList.get(sp).channels.get(name).getChannelState().compareToIgnoreCase("initial") == 0){
					Branch.snapshotList.get(sp).channels.get(name).recordChannel(amount);
				}
			}
			}
		//	System.out.println("Amount Received " + amount + " " + name + " " + bc.getBalance());
		}
		if(incoming.hasMarker()) {
		//	System.out.println("Received Marker from " + name );
			int marker = incoming.getMarker().getSnapshotId();
			MarkerRequest(marker,name);
		}
			
	}
	
	public synchronized void MarkerRequest(int markerIn, String name){
		
		if(!Branch.snapshotList.containsKey(markerIn)) {
		//	System.out.println("First Markers received " + name);
			int id = markerIn;
			Snapshot sc = new Snapshot(id);
			for (String br : bc.socket.keySet()) {
				MarkerState bc1 = new MarkerState(br);
				sc.channels.put(br, bc1);
			}
			snapshotList.put(id, sc);
			sc.InitSnapState();
			sc.setInitialBalance(bc.getBalance());
			
			Bank.BranchMessage.Builder branchMessage = Bank.BranchMessage.newBuilder();
			Bank.Marker.Builder marker = Bank.Marker.newBuilder();
			marker.setSnapshotId(id);
		
			for(String temp : bc.socket.keySet()) {
				Socket sock = bc.socket.get(temp);
				OutputStream output;

				try {
					output = sock.getOutputStream();
					branchMessage.setMarker(marker.build());
					branchMessage.build().writeDelimitedTo(output);
					output.flush();
		//			System.out.println("Markers Sent "+ temp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			for(String temp : sc.channels.keySet()) {
				if(temp.compareToIgnoreCase(name) == 0) {
					sc.channels.get(temp).clearChannel();
				}
				else {
					sc.channels.get(temp).setStartState();
				}
			}
			
			
		}
		else {
		//	System.out.println(name);
		//	System.out.println(Branch.snapshotList.get(marker));
		//	System.out.println("Second Marker " + name);
			Branch.snapshotList.get(markerIn).channels.get(name).setFinalState();		
		}
			
	}
	
	public static void sendMoney(int money) {
		
		Random rand = new Random();
		List<String> keys = new ArrayList<String>(bc.socket.keySet());
		String randomKey = keys.get(rand.nextInt(keys.size()));
		Socket sock;
		//System.out.println(randomKey);
		sock = bc.socket.get(randomKey);
		Bank.BranchMessage.Builder branchMessage = Bank.BranchMessage.newBuilder();
	    Bank.Transfer.Builder transfer = Bank.Transfer.newBuilder();
	    transfer.setMoney(money);            
	    bc.deductBalance(money);
	    try {
			OutputStream output = sock.getOutputStream();
			branchMessage.setTransfer(transfer.build());
			branchMessage.build().writeDelimitedTo(output);
			output.flush();
    	
		}catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	    
	}
	
	public static void main(String[] args){

		if(args.length != 2){
			System.out.println("Usage: ./branch branch1 9090\n");
			System.exit(0);
		}

		bc = new BranchContext(args[0],Integer.parseInt(args[1]));
	//	ServerSocket server = null;
		
		try {
		brobj.server = new ServerSocket(bc.getPort());
		}
		catch(IOException i) {
			System.out.println(i);
		}
		Socket request = null;
		try {
			request = brobj.server.accept();
			InputStream in = request.getInputStream();
			System.out.println("InitBranch");
			Bank.BranchMessage incoming = Bank.BranchMessage.parseDelimitedFrom(in);
			if(incoming.hasInitBranch()) {
				bc.setBank(incoming.getInitBranch());
				//System.out.println(bank.getBalance());
				bc.setBalance(bc.bank.getBalance());
				bc.setConnections(bc.bank.getAllBranchesCount() - 1);
				
				for(int i=0; i< bc.bank.getAllBranchesCount(); i++) {
					if(!(bc.getName().equalsIgnoreCase(bc.bank.getAllBranches(i).getName()))){
						bc.branchesIp.put(bc.bank.getAllBranches(i).getName(), bc.bank.getAllBranches(i).getIp());
						bc.branchesPort.put(bc.bank.getAllBranches(i).getName(), bc.bank.getAllBranches(i).getPort());
						bc.branches.add(bc.bank.getAllBranches(i).getName());
					}
				}
			}
			Thread controlRequest = new Thread(new ControlHandler(request, brobj, "control"));
			controlRequest.start();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		Thread t[] = new Thread[bc.getConnections()];
		
		for(int i=0; i< bc.bank.getAllBranchesCount(); i++) {
			if((bc.bank.getAllBranches(i).getName().compareToIgnoreCase(bc.getName())) > 0) {
				try{
					Socket socket = new Socket(bc.bank.getAllBranches(i).getIp(), bc.bank.getAllBranches(i).getPort());
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					String text = bc.getName()+'\n';
					bc.socket.put(bc.bank.getAllBranches(i).getName(),socket);
				//	System.out.println("Connection request sent" + text);
					socket.setKeepAlive(true);
					bc.decrementConnections();
					out.writeBytes(text);
					out.flush();
					int val = bc.getExConn();
					t[val] = new Thread(new BranchHandler(socket, brobj, bc.bank.getAllBranches(i).getName()));
					t[val].start();
					bc.increExConn();
					
					
					}
					catch(IOException e){
						System.out.println(e);
					}	
			}
		}
		
	
		
		Thread receiver = new Thread(new Runnable() {
			
			Socket receive;
			public void run() {
				while(true) {
					try {
						System.out.flush();
						receive = brobj.server.accept();
						InputStream in = receive.getInputStream();	
						if(bc.getConnections() != 0) {
							BufferedReader br = new BufferedReader(new InputStreamReader(in));
							String line = br.readLine();
							String branch = line.toString();
							bc.socket.put(branch, receive);
							//System.out.println("Connection request received" + branch);
							bc.decrementConnections();
							int val = bc.getExConn();
							t[val] = new Thread(new BranchHandler(receive, brobj, branch));
							t[val].start();
							bc.increExConn();
							System.out.flush();
							
						}	
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
			}
			
		});
		receiver.start();	
	
		System.out.println("Initial Balance " + bc.getBalance());

	/*		
		for(String key: bc.branchesIp.keySet()) {
			System.out.println(key + " " + bc.branchesIp.get(key));
		}
		
		for(String key: bc.branchesPort.keySet()) {
			System.out.println(key + " " + bc.branchesPort.get(key));
		}
		
		if(bc.getConnections() == 0) {
			for(String key: bc.socket.keySet()) {
				System.out.println(key + " " + bc.socket.get(key));
			}
		}
	 */
		
		
	try {
		Thread.sleep(4000);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
		
	Thread sender = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while(true) {
				
				int value = bc.getRandomBalance();
				if(value != 0) {
					try {
						sendMoney(value);
						Random rand = new Random();
						int randomNum = rand.nextInt((5 - 1)) + 1;
						int waitTime = randomNum + 1000;
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}	
	});
	
	if(bc.getConnections() == 0) {
		System.out.println("Connections Established");
		System.out.flush();
	}
	
	sender.start();
	}

}
