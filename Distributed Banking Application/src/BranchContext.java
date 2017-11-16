import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
* 
*
* @author  Surendrakumar Koneti
* @since   2017-11-16
*/

public class BranchContext {
	
	private String name = null;
	private int port;
	private int balance = 0;
	public Bank.InitBranch bank = null;
	public int connections = 0;
	public List<String> branches = new ArrayList<String>();
	public HashMap<String,String> branchesIp = new HashMap<String,String>();
	public HashMap<String,Integer> branchesPort = new HashMap<String,Integer>();
	public HashMap<String,Socket> socket = new HashMap<String,Socket>();
	public int Exconn = 0;
	
	public BranchContext(String ipIN,int portIN){
		this.name = ipIN;
		this.port = portIN;
	}
	
	public void setName(String nameIN) {
		this.name = nameIN;
	}
	
	public void setBank(Bank.InitBranch bankIN) {
		this.bank = bankIN;
	}
	
	public synchronized int getExConn() {
		return this.Exconn;
	}
	
	public synchronized void increExConn() {
		this.Exconn++;
	}
	
	public String getName() {
		return name;
	}
	
	public Bank.InitBranch getBank(){
		return bank;
	}
	
	public void setConnections(int connIN) {
		this.connections = connIN;
	}
	
	public int getConnections() {
		return this.connections;
	}
	
	public int getPort() {
		return port;
	}
	
	public synchronized void decrementConnections() {
		this.connections--;
	}
	
	public synchronized void setBalance(int value) {
		this.balance = value;
	}
	
	public synchronized int getBalance() {
		return balance;
	}
	
	public synchronized void addBalance(int value) {
		this.balance+=value;
	}
	
	public synchronized void deductBalance(int value) {
		this.balance-=value;
	}
	
	public synchronized int getRandomBalance() {
		
		int bal = this.getBalance();
		if(bal > 0) {
			int range1 = (int)(bal * 1.0/100.0);
			int range2 = (int)(bal * 5.0 /100.0);
			Random random = new Random();
			int randomNumber = random.nextInt(range2 - range1) + range1;
			return randomNumber;
		}
		else {
			return 0;
		}
	}
		
	public synchronized void updateMoney(int money) {
		this.addBalance(money);
	}

}
