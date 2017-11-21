import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
* 
*
* @author  Surendrakumar Koneti
* @since   2017-11-16
*/

public class Snapshot {
	
	HashMap<String,MarkerState> channels = new HashMap<String,MarkerState>();
	Map<String, MarkerState> treeMap = null;
	
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
		treeMap = new TreeMap<String, MarkerState>(channels);
		
		for (String br : channels.keySet()) {
				if(channels.get(br).getChannelState().compareToIgnoreCase("final") != 0) {
					return 0;
				}
		}
		return 1;
	}

}
