import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Snapshot {
	
	HashMap<String,ChannelState> channels = new HashMap<String,ChannelState>();
	Map<String, ChannelState> treeMap = null;
	
	public int initialBalance;
	public int id;
	public String SnapState;
	
	public Snapshot(int idIn) {
		this.id = idIn;
	}
	
	public void setInitialBalance(int value) {
		this.initialBalance = value;
	}
	
	public int getBalance() {
		return this.initialBalance;
	}
	
	public void setSnapshotId(int value) {
		this.id = value;
	}
	
	public int getSnapshotId(){
		return this.id;
	}
	
	public synchronized void InitSnapState() {
		this.SnapState = "initial";
	}
	
	public synchronized void endSnapState() {
		this.SnapState = "final";
	}
	
	public String getSnapState() {
		return this.SnapState;
	}
	
	public synchronized int checkStatus(){
		treeMap = new TreeMap<String, ChannelState>(channels);
		
		for (String br : channels.keySet()) {
				if(channels.get(br).getChannelState().compareToIgnoreCase("final") != 0) {
					return 0;
				}
		}
		return 1;
	}

}
