$(function() {
    var username = "a"
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    window.socket = new WS(wsUrl)

    window.socket.onmessage = serverTick

})
