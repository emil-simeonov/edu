This a brief explanation how you could run the examples for using blocking and unblocking TCP sockets.

List of examples:

1. StandaloneTCPClient - a very simple TCP client using blocking sockets and a ready-made TCP server
2. TCPClient - a simple TCP client relying on custom TCP server implementations (see below)
3. BlockingTCPServer - a simple custom TCP server based on java.io and relying on blocking TCP sockets
4. NonBlockingTCPServer - a simple custom TCP server based on java.nio and relying on non-blocking TCP sockets

Additional dependencies (frameworks, libraries, etc.):

None

Setup of development environment (independent of the used operating system):

1. Install git (if not already installed)
2. Install JavaSE 1.8 Java Development Kit (JDK)
3. Choose a folder on your local machine where you will "clone" the Git repository containing the examples. From here on this folder is denoted as ${PRJ_HOME}. From the command line (Windows: Git Bash) navigate to ${PRJ_HOME} and execute the following command there:

git clone https://github.com/emil-simeonov/edu.git

From here on the "edu" repository will be synced into your ${PRJ_HOME} folder.

4. Install an Integrated Development Environment (IDE) - both IntelliJ IDEA Community edition and Eclipse will do the job for you. 
5. Import the existing project "sockets" (${PRJ_HOME}/edu/tu-sofia/modern-java-technologies/tcp/sockets) in your IDE (Initially developed with IntelliJ IDEA). You could find many examples how you could do this on the Internet.
6. You could run the examples by starting the corresponding Java classes (they all have main(...) methods).

	Notes:

	1. Do not forget to start the blocking/non-blocking servers prior to starting the TCPClient main method.
	2. The standalone TCP client does not rely on any other classes being started in advance.

I wish you luck,
-Emo
