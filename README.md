# Distributed Banking Application - Snapshot Algorithm

Programming language - Java

#To compile: -same directory as makefile

	make

#To clean:

	make clean

#To Run:

	chmod +x controller.sh
	./controller 4000 {branch.txt}

	chmod +x branch.sh
	./branch.sh branch1 9090

#Note:
Bank.Java should be placed inside the /src folder
makefile does not give execute permissions to the controller and branch script.
branch.txt file containing should be in the same directory as makefile.

##Implementation Details

	The controller class reads the file and sends out the init branch message. 
	When a branch receives the init branch message, it starts a thread controlhandle() that handles the messages. 
	The branch sents out connection request with its name as string message to all the other branches.
	When a branch receives a connection request, it starts a thread with the socket and branch name it received from. 
	The branchContext class records all the information - balance Snapshot details and incoming channel states. 
	The Branch class maintains a hashmap and records the snapshot object with snapshot id as key. 
	The snapshot object has n-1 channelState objects that handles the incoming channel states for each snapshotId. 
	Since, Each incoming channel runs in a thread which has shared access to the branch class(main), it can record the messages if it follows the marker receiver rule.  
