# Workspace
Complete presentation is available here. [Workspace Slides](https://docs.google.com/presentation/d/1oipXLraF6mERSBWs-hJG3VaXgaCCc5NGi-eMWkm4WxU/edit?usp=sharing).
Demonstration Video of Application is here. [Workspace](https://www.youtube.com/watch?v=BgMDSSefJqg&t=42s).

‘Workspace’  lets the users to connect with each other, work collaboratively and effectively, to have video meetings or sessions, etc.


# Sections:
1) Authentication System
2) User's Connection System
3) Chats Sections
4) Workspace Management
5) User Account Section 
6) Duo Calls System
6) Meetings System


# Technolgies Used:
**IDE:** Android Studio(JAVA & XML), Visual Studio Code (HTML, CSS & JS)

**Version Control:** Github 

**Google Firebase:**
- Authentication   
- Realtime Database
- Firebase Cloud Messaging

**Libraries Used:**
- rtcmulticonnection			      
- peer js

# How to test the functionalities
- For duo participants video call you need to run the peer js locally or globally and provide its host, path, port, etc info to the [call.js](https://github.com/bhavesh3005sharma/workspace/blob/master/app/src/main/assets/call.js)
- Afetr installing peerjs run the following command to run peer js locally
  "peerjs --port 9000 --key peerjs --path /" -> it will run the peer js locally on 9000 port at path "/"
- Yeah thats it after that you find you can make the duo video calls.
- For multiparticipnts video call it already hosted so yu can use it directly.
