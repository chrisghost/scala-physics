var game;
var METER_TO_PIXEL = 10

var bodies = {}
var preload = function() {}
var create = function() {}
var serverTick = function(msg) {
  //console.log(msg.data, bodies)
  var data = JSON.parse(msg.data)
  for(i in data) {
    var b = data[i]
    if(typeof bodies[i] == 'undefined') {
      bodies[i] = b
    }
    var body = bodies[i]
    //console.log(body.shape)

    if(body.shape == "box") {
      body.debugShape = new Phaser.Rectangle(
          (b.position.x-b.size.x/2)*METER_TO_PIXEL+400
        , (-b.position.y-b.size.y/2)*METER_TO_PIXEL+300
        , b.size.x*METER_TO_PIXEL
        , b.size.y*METER_TO_PIXEL
      )
    } else if(body.shape = "circle") {
      body.debugShape = new Phaser.Circle(
          (b.position.x)*METER_TO_PIXEL+400
        , (-b.position.y)*METER_TO_PIXEL+300
        , 2*b.radius*METER_TO_PIXEL
      )
    }
  }
}
var update = function() {}
var render = function() {
  for(i in bodies) {
    b = bodies[i]
    if (b.shape == "box")
      game.debug.renderRectangle(b.debugShape, b.static ? "#0F0" : "#F00")
    else if (b.shape == "circle")
      game.debug.renderCircle(b.debugShape, b.static ? "#0F0" : "#F00")
  }
}

$(function () {
  game = new Phaser.Game(800, 600, Phaser.CANVAS, '', { preload: preload, create: create, update: update, render: render });
  setTimeout( function() {
    $("canvas").click(function(e) {
      createRandomBody((e.offsetX-400)/METER_TO_PIXEL, (300-e.offsetY)/METER_TO_PIXEL)
    })

    createBody(
        {x: 100/METER_TO_PIXEL, y: -100/METER_TO_PIXEL }
      , 5
      , true
      , "circle"
    )
    createBody(
        {x: 100/METER_TO_PIXEL, y: -300/METER_TO_PIXEL }
      , {x: 100, y: 10 }
      , true
      , "box"
    )
    createBody(
        {x: -350/METER_TO_PIXEL, y: -100/METER_TO_PIXEL }
      , {x: 10, y: 30 }
      , true
      , "box"
    )
    createBody(
        {x: +350/METER_TO_PIXEL, y: -100/METER_TO_PIXEL }
      , {x: 10, y: 30 }
      , true
      , "box"
    )
  }, 100)
})
