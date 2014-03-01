var createBox = function(x, y) {
  socket.send(JSON.stringify({ body:
    {
      position:{x:parseInt(x),y:parseInt(y)},
      velocity:{x:0.0,y:-0},
      acceleration:{x:0.0,y:0},
      size:{x:1.0,y:1.0},
      density:1.0,
      static:false,
      id:""
    }
  }))
}
var createBody = function(pos, size, static) {
  socket.send(JSON.stringify({ body:
    {
      position: pos,
      velocity:{x:0.0,y:-0},
      acceleration:{x:0.0,y:0},
      size: size,
      density:1.0,
      static: static,
      id:""
    }
  }))
}
