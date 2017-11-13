
public class ChannelState{

	public String markerState = "Negative"; 
	public int incomingBalance;
	public String name;

	public ChannelState(String nameIn) {
		this.name = nameIn;
	}
	
	public synchronized void setStartState() {
	//	System.out.println("Start recording " + name);
		this.markerState = "initial";
	}
	
	public synchronized void setFinalState() {
	//	System.out.println("Stop recording " + name);
		this.markerState = "final";
	}
	
	public void recordChannel(int val) {
	//	System.out.println("Recording " + val + " " + name);
		this.incomingBalance += val;
	}
	
	public synchronized String getChannelState() {
		return this.markerState;
	}
	
	public void clearChannel() {
	//	System.out.println("clear recording" + name);
		this.incomingBalance = 0;
		this.setFinalState();
	}
	
	public int getMessages() {
		return this.incomingBalance;
	}

}
