var connection = new RTCMultiConnection();

// provide socket url ( since we don't have hosted one we are using freely available one )
connection.socketURL = 'https://rtcmulticonnection.herokuapp.com:443/';

connection.socketMessageEvent = 'video-conference-demo';

connection.session = {
    audio: true,
    video: true
};

connection.sdpConstraints.mandatory = {
    OfferToReceiveAudio: true,
    OfferToReceiveVideo: true
};

connection.videosContainer = document.getElementById('videos-container');

// handle stream when received (add it to the ui )
connection.onstream = function(event) {
    console.log("stream appeared "+event.streamid);

    var existing = document.getElementById(event.streamid);
    if(existing && existing.parentNode) {
      existing.parentNode.removeChild(existing);
    }

    // style the video element and add to ui
    var mediaElement = event.mediaElement;
    mediaElement.id = event.streamid;
    mediaElement.controls = false;
    mediaElement.style.borderRadius = "15px";
    mediaElement.style.width = "40%";
    mediaElement.style.margin = "10px 10px";
    mediaElement.style.height = "200px";

    connection.videosContainer.appendChild(mediaElement);
};

// open room with ${room_id}
function openRoom(room_id) {
    connection.open(room_id, function(isRoomOpened, roomid, error) {
        if(isRoomOpened === true) {
            console.log("room opened")
            // notify android that call is joined
            Android.onRoomJoined();
        }
        else {
          if(error === 'Room not available') {
            // a room with given room_id is already exists try to join room or ask for different id

            // This will never happen as because we always send a random id for room
            // ('Oops error occurred. Please try again!');

            Android.onCallSetUpError(error);
            return;
          }
          Android.onCallSetUpError(error);
        }
    });
}

// join room with ${room_id}
function joinRoom(room_id) {
    connection.join(room_id, function(isJoinedRoom, roomid, error) {
            if(isRoomOpened === true) {
                console.log("room joined")
                // notify android that call is joined
                Android.onRoomJoined();
            } else if (error) {
                if(error === 'Room not available') {
                  Android.onCallSetUpError('This room is not active for now. Please wait for moderator to Enter in the room.');
                  return;
                }
                Android.onCallSetUpError(error);
            }
        });
}

// leave the room
function leaveRoom(){
    connection.leave();
}

// only for admin use ( Remove user from room )
var socket = connection.getSocket();
function removeFromRoom(){
var userToEject = connection.getAllParticipants()[0]; // or "prompt()"
    socket.emit('custom-message', {
        'remoteUserId': userToEject,
        'leaveMyRoom': true
    });
}

socket.on('custom-message', function(data) {
    if (data.remoteUserId !== connection.userid) return;

    // room owner asked to leave his room
    if (data.leaveMyRoom === true) {
        connection.leave();
    }
});

function toggleVideo(b) {
    var localStream = connection.attachStreams[0];
    console.log("toggle Video "+b);
    if (b == "true") {
        localStream.unmute('video');
    } else {
        localStream.mute('video');
    }
}

function toggleAudio(b) {
    var localStream = connection.attachStreams[0];
    console.log("toggle Audio "+b);
    if (b == "true") {
        localStream.unmute('audio');
    } else {
        localStream.mute('audio');
    }
}

// listen for stream ending
connection.onstreamended = function(event) {
    var mediaElement = document.getElementById(event.streamid);
    if (mediaElement) {
        mediaElement.parentNode.removeChild(mediaElement);
    }
};