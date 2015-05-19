# MCTF.local

The framework is for synchronizing multiple media communication channels. 
Synchronization is the information coordination between two connected entities. 
Different applications can employ their own media channels. 
And the entities involved in the application scenario are equality, not one acts as a server and another is a client. 
Although default media types supported by the framework are video, audio and touch information, it can be further extended for supporting more interesting channels. 
In addition, the framework is lightweight, without dependence of other third-part middleware, and is totally constructed with Java.

The framework consists of two layers, “RUDP Communication Fundamental” and “Channel Controller”. A concrete aplication is built above the framework.
In detail, “RUDP Communication Fundamental” is a realization of RUDP (Reliable UDP), with routines of sending reliable and unreliable messages. 
“Channel Controller” is responsible for synchronizing multiple media channels. 
“Central Controller” and “Relay Server” help to set network communication connection between two sides, whether they are in P2P or C/S communication model. 
“Video Controller”, “Audio Controller” and “Touch Controller” manage local or remote media information. 
The whole framework works in Java runtime environment.
