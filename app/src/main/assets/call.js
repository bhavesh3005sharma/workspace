let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

localVideo.onplaying = () => { localVideo.style.opacity = 1 }
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 }

let peer
function init(userId) {
    console.log(userId);
    peer = new Peer(userId, {
        host: '18319108ecec.ngrok.io',
        port: 443,
        secure:true,
        path: '/'
    });

    // peer = new Peer(userId,{
    //     config: {'iceServers': [
    //       { url: 'https://169a8dec470b.ngrok.io/videocallapp' }
    //       ]} /* Sample servers, please use appropriate ones */
    //   });

    peer.on('open',() => {
        Android.onPeerConnected()
    })

    listen()
}
var maincall;
let localStream
function listen() {
    peer.on('call', (call) => {
        console.log('call arrived');
        if(maincall){
            console.log('already on call');
            call.close();
            return;
        }else maincall = call;

        navigator.mediaDevices.getUserMedia({
            audio: true, 
            video: true
        })
        .then(function(stream) {
            localVideo.srcObject = stream
            localStream = stream
            maincall = call;
            maincall.answer(stream)
            maincall.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream

                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"

            });
           
        })
        .catch(function(err) {
            /* handle the error */
            console.log(err);
        });

        maincall.on('close',function(){
            console.log("listen -> listen for call close");
            maincall = null;
            localStream = null;
            localVideo.srcObject = localStream;
            remoteVideo.srcObject = null;
        });
        
    })
}

function cancelCall(){
    console.log("cancelCall called");
    if(maincall)
        maincall.close(),maincall=null;
}

function disconnectPeer(){
    console.log("disconnectPeer called");
    if(peer)
        peer.disconnect();
}

function startCall(otherUserId) {
    if(maincall){
        console.log("already on a call");
        return;
    }

    navigator.mediaDevices.getUserMedia({
        audio: true, 
        video: true
    })
    .then(function(stream) {
        localVideo.srcObject = stream
        localStream = stream

        const call = peer.call(otherUserId, stream)
        maincall = call;
        maincall.on('stream', (remoteStream) => {
            remoteVideo.srcObject = remoteStream

            remoteVideo.className = "primary-video"
            localVideo.className = "secondary-video"
        });
        maincall.on('close',function(){
            maincall = null;
            localStream = null;
            localVideo.srcObject = localStream;
            remoteVideo.srcObject = null;

            console.log("startcall -> listen for call close");
        });
    })
    .catch(function(err) {
        /* handle the error */
        console.log(err);
    });
}

function toggleVideo(b) {
    console.log("toggle Video "+b);
    if (b == "true") {
        localStream.getVideoTracks()[0].enabled = true
    } else {
        localStream.getVideoTracks()[0].enabled = false
    }
} 

function toggleAudio(b) {
console.log("toggle Audio "+b);
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
} 
