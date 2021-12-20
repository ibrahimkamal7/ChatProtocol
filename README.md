# ChatProtocol

How To Run: The programs are tested on the Windows OS with multiple
clients. JAR executable files are included with the source code of the
Client and Server program. These JAR files are the portal to run the
client and the server. Here are the commands to run the JAR files:

1.  Navigate to the Server.jar file and run it using the following
    command: java -jar Server.jar
2.  Navigate to the CLient.jar file and run it using the following
    command java -jar Client.jar

Here are the credentials and channel rights used for logging in and
sending messages:

User Database:

[Username Password Rights] =>
[Admin 123456 admin]
[ik363 123456 Not admin]
[hg387 123456 Not admin]
[mtk24 123456 Not admin]

Sample CONNECT command: \CONNECT hg387@192.168.1.169:8081 V1.0 -u

[Channels Database Name Users] =>
[Channel1 "hg387", "mtk24", "Admin"]
[Channel2 "hg387", "ik363", "Admin"]
[Channel3 "hg387", "Admin"]

Analysis

Mocking command line inputs was not possible so, all the tests are done
manually. The discussed fixes made in DFA states and transitions
terminated the chance of ending up in a wrong state. With keeping track
of connected clients, if anyone tries to connect as an already connected
user, the server would respond as not authorized and closed that
connection attempt. Likewise, if the user re-tries to re-join the
channel, the server responds back with the Already joined. Through the
use of Admin Mode, whenever the user without admin rights tries to
change the properties of the channel/user, then the server would throw a
not authorized response to these tries.

The \CONNECT command is parsed on the client-side and until we get a
valid command from the user, no further requests are sent to the
protocol. The remaining commands are handled by the server. The command
parser on the server-side is also pretty robust and performs very well
in parsing the commands and separating out the invalid requests from the
valid ones. If an invalid command is entered, the response would be:

Please enter a command.

\LISP -u 511 Invalid Command

The application is pretty robust in its current state based on the
results of manual user testing. All the exceptions and unexpected input
from the client are handled as gracefully as possible and the Clients
receive a response for every command they pass to the protocol. The
response depends on the type of command entered, whether the command was
valid or not and whether the protocol was able to process the request
successfully. As a result of user testing, most of the edge cases were
discovered and fixed and now this implementation can be accepted as
protocol.

Moreover, the application utilizes timeout sockets, also these timeouts
vary according to the state for example 3 mins for entering the
password, 4 mins when connected. These timeouts ensure that protocol
never halts even when there are unprecedented network issues.

Also, the protocol consists of two-way closing where the server first
acks client exit request, once the client receives server acks, then
client closes its socket and streams. Simultaneously, the server closes
the socket and streams. This ensures that the connection closes
properly.
