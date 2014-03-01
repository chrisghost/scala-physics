$(function() {
    var username = "a"
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var chatSocket = new WS(wsUrl)

    chatSocket.onmessage = serverTick

})
