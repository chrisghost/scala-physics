$(function() {
    var username = "a"
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var chatSocket = new WS(wsUrl)

    var receiveEvent = function(event) {
        var data = JSON.parse(event.data)
        console.log(data)
    }

    chatSocket.onmessage = receiveEvent

})
