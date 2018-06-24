# Distributed Banking Application - Global Snapshot Algorithm

An Distributed Banking Application that transfers money to a randomly selected branch at unpredictable times.

## Prerequisites
1. Google Protobuf
2. Java Development kit

 __Note:__ Protobuf.jar file is in the lib folder, after downloading, update the path to the protobuf in the following files.  
 - client.sh
 - server.sh
 - Makefile

## Instructions

**To compile:** - same directory as makefile
```
make
```

**To clean:**  
```
make clean
```

**To run:**  
Controller executable takes two inputs - Total amount in the distributed bank and a local file that stores the names, IP address and port numbers of all branches.  
```
chmod +x controller.sh
./controller 4000 {branch.txt}
```

Branch executable takes two inputs - name of the branch as in the text file and port number the branch runs on.  
```
chmod +x branch.sh
./branch.sh branch1 9090
```

__Note__: Bank.Java should be placed inside the /src folder. branch.txt file should be in the same directory as makefile.  
  
## Project Description
A Distributed Banking Application with a single TCP connection set up between every pair of branches, ensuring **FIFO** message delivery. Each branch starts with an initial balance. The branch then randomly selects another destination branch and sends a random amount of money to the destination at unpredictable times. Every branch is both a sender and a receiver. A branch's balance will not become negative, until then it keeps sending money randomly between 1% and 5% of the branch's initial balance. Intervals between consecutive sending operations is drawn uniformly at random between 0 and 5 seconds.  

In a distributed bank, a global snapshot contains both the local state of each branch and the amount of money in transit on all communication channels. Each branch will be responsible for recording and reporting its own local state as well as the total money in transit on each of its incoming channels. The controller will contact one of the branches to initiate the globa snapshot by sending a message to the selected branch. the selected branch will then initiate the snapshot and sends out ''Marker'' messsages to all other branches. If the snapshot is correct, the total amount in all branches and in tansit should be equal to the command line argumant given to the controller.  

## Chandy-Lamport Algorithm
**Initiate the Snapshot**  

- **P<sub>i** initiates the snapshot by first recording its own local state  
- **for** j=1 to N except i  
	- **P<sub>i** sends out a _Marker_ Message on outgoing channel **C<sub>ij**  
- Starts recording the incoming messages on each of the incoming channels at **P<sub>i</sub>: C<sub>ji** (for j=1 to N except i)  

**Marker Receiving Rule**  
Whenever a process **P<sub>i** receives a _Marker_ message on an incoming channel **C<sub>ji</sub> (P<sub>j</sub>->P<sub>i</sub>)**  

- **If** (this is the first _Marker_ **P<sub>i** is seeing)
	- **P<sub>i** recorde its own state first  
	- Marks the state of the channel **C<sub>ji** as "empty"
	- **for** j=1 to N except i
		- **P<sub>i** sends out Marker Message on outgoing channel **C<sub>ij**
	- Starts recoding the incoming messages on each of the incoming channels at **P<sub>i</sub>: C<sub>ji** (for j=1 to n except i)
- **Else** (already seen a _Marker_ message)  
	- Mark the state of channel **C<sub>ji** as all the messages that have arrived on it since recording was turned on for **C<sub>ji**
	- Stop recoding on channel **C<sub>ji**  

**Assumptions**  
1. Neither channels nor processes fail; communication is reliable.
2. Channels are unidirectional and provide FIFO message deliver.
3. The graph of processes and channels is strongly connected (there is a path between any two processes).
4. Any process may initiate a global snapshot at any time.
5. Processes may continue with their execution and send and receive normal messages while the snapshot takes place.

**Correctness**  
- Due to FIFO property of channels, it follows that no message sent after the marker on that channel is recorded in the channel state.
- When a process **P<sub>j** receives message **C<sub>ij** that precedes the marker on channel **C<sub>ij** , it acts as follows: If process **P<sub>j** has not taken its snapshot yet, then it includes **C<sub>ij** in its recorded snapshot. Otherwise, it records **C<sub>ij** in the state of the channel **C<sub>ij**.

**Complexity**  
- The recording part of a single instance of the algorithm requires O(e) messages and O(d) time, where e is the number of edges in the network(i.e. outgoinh or incoming communication channels) and d is the diameter of the network(total number of branches in the distributed bank).


## Implementation Details

**Distributed Banking Application**  

**Branch and Controller**   
1. ``InitBranch`` this messages contains two pieces of information: initial balance and a list of all branches(including itself) in the distributed bank.

2. ``Transfer`` this message indicates that a remote, source branch is transferring money to the current, target branch. The message contains an integer representing the amount of money transferred.  

**Taking Global Snapshot of the Bank**  
1. ``InitSnapshot`` upon receiving this message, a branch records its own local state (balance) and sends out _Marker_ messages to all other branches. To identify multiple snapshots, the controller includes a _snapshot_id_ to this initSnapshot message, and all the Marker messages should include this _snapshot_id_ as well.  

2. ``Marker`` every _Marker_ message includes a _snapshot_id_. Upon receiving this message, the receiving branch does the following:

	- If this is the ﬁrst Marker message with the snapshot_id the receiving branch has seen, the receiving branch records its own local state (balance), records the state of the incoming channel from the sender to itself as empty, immediately starts recording on other incoming channels, and sends out _Marker_ messages to all of its outgoing channels (i.e., all branches except itself). 
	- otherwise, the receiving branch records the state of the incoming channel as the sequence of money transfers that arrived between when it recorded its local state and when it received the _Marker_.  

3. ``RetieveSnapshot``  the controller sends _retrieveSnapshot_ messages to all branches to collect snapshots. This message will contain the _snapshot_id_ that uniquely identiﬁes a snapshot. A receiving branch should its recorded local and channel states and return them to the caller (i.e., the controller) by sending a _returnSnapshot_ message.  

4. ``ReturnSnapshot``  a branch returns the controller its captured local snapshot in this message. This message should include the _snapshot_id_, captured local state, as well as all incoming channel states.  

The Controller is fully automated. It periodically sends the _InitSnapshot_ message with monotonically increasing _snapshot_id_ on a randomly selected branch and outputs to the console the aggregated global snapshot retrieved from all branches in the correct format. In addition, the snapshot taken by branches needs to be identiﬁed by their names: e.g., “branch1” to represent branch1’s local state, and “branch2->branch1” to represent the channel state. Here is an example controller output:  

_snapshot_id: 10_  
_branch1: 1000, branch2->branch1: 10, branch3->branch1: 0_  
_branch2: 1000, branch1->branch2: 0, branch3->branch2: 15_   
_branch3: 960, branch->branch3: 15, branch2->branch3: 0_  

**Protocol Buffer**  
_bank.proto_ file defines the messages to be transmitted among processes in the protocol buffer. I have included the proto generated file _bank.java_ in the \src folder. If you want to manually compile the proto file, you can use the protocol compiler, __PROTOC__, to compile and use the auto-generated code for marshalling and unmarshalling messages.  
To use _protoc_, set the environment variable **PATH** to the \bin folder in the protocol buffer library and then use the following command,  
```
protoc --java_out=./ bank.proto
```





