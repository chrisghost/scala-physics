var createRandomBody = function(x, y) {
  var obj = { body:
    {
      position:{x:parseInt(x),y:parseInt(y)},
      velocity:{x:0.0,y:-0},
      acceleration:{x:0.0,y:0},
      density:1.0,
      restitution:0.5,
      static:false,
      id:""
    }
  }
  if(Math.random() > 0.5) {
    obj.body.shape = "box"
    obj.body.size = {
      x:Math.floor(Math.random()*10 +1),
      y:Math.floor(Math.random()*10 +1)
    }
  } else {
    obj.body.shape = "circle"
    obj.body.radius = Math.floor(Math.random()*10 + 1)
  }
  console.log(obj)
  socket.send(JSON.stringify(obj))
}
var createBody = function(pos, size, static, shape) {
    var obj = { body:
    {
      position: pos,
      velocity:{x:0.0,y:-0},
      acceleration:{x:0.0,y:0},
      shape: shape,
      density:1.0,
      restitution:0.5,
      static: static,
      id:""
    }
  }
  if(shape == "box") {
    obj.body.size = size
  } else {
    obj.body.radius = size
  }
  socket.send(JSON.stringify(obj))
}
