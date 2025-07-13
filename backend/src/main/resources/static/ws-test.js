const stompClient = new StompJs.Client({
  brokerURL: 'ws://localhost:8080/ws/game-room',
  // ✅ 하트비트 끄기
  heartbeatIncoming: 0,
  heartbeatOutgoing: 0,
});

stompClient.onConnect = (frame) => {
  setConnected(true);
  console.log('Connected: ' + frame);
  stompClient.subscribe('/sub/room/1', (response) => {
    console.log("hi");
    console.log(response);
    //showGreeting(JSON.parse(response.body).content);
    const parseBody = JSON.parse(response.body);
    // switch (body.type){
    //   case
    // }
  });

  //구독 후 입장 요청 보내기 (send)
  stompClient.publish({
    destination: "/pub/room/enter/1",
  });

};

stompClient.onWebSocketError = (error) => {
  console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
  console.error('Broker reported error: ' + frame.headers['message']);
  console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
  $("#connect").prop("disabled", connected);
  $("#disconnect").prop("disabled", !connected);
  if (connected) {
    $("#conversation").show();
  }
  else {
    $("#conversation").hide();
  }
  $("#greetings").html("");
}

function connect() {

  stompClient.activate();
}

function disconnect() {
  stompClient.deactivate();
  setConnected(false);
  console.log("Disconnected");
}

function sendName() {
  stompClient.publish({
    destination: "/app/hello",
    body: JSON.stringify({'name': $("#name").val()})
  });
}

function showGreeting(message) {
  $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
  $("form").on('submit', (e) => e.preventDefault());
  $( "#connect" ).click(() => connect());
  $( "#disconnect" ).click(() => disconnect());
  $( "#send" ).click(() => sendName());
});
