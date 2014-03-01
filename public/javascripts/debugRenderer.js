var game;
var METER_TO_PIXEL = 10

var bodies = {}
var preload = function() {}
var create = function() {}
var serverTick = function(msg) {
  console.log(msg.data, bodies)
  var data = JSON.parse(msg.data)
  for(i in data) {
    var b = data[i]
    if(typeof bodies[i] == 'undefined') {
      bodies[i] = b
    }
    var body = bodies[i]
    body.debugShape = new Phaser.Rectangle(
        (b.position.x-b.size.x/2)*METER_TO_PIXEL
      , (-b.position.y-b.size.y/2)*METER_TO_PIXEL
      , b.size.x*METER_TO_PIXEL
      , b.size.y*METER_TO_PIXEL
    )
  }
}
var update = function() {}
var render = function() {
  for(i in bodies) {
    b = bodies[i]
    game.debug.renderRectangle(b.debugShape, b.static ? "#0F0" : "#F00")
  }
}

$(function () {
  game = new Phaser.Game(800, 600, Phaser.CANVAS, '', { preload: preload, create: create, update: update, render: render });
  setTimeout( function() {
    $("canvas").click(function(e) {
      createBox(e.offsetX/METER_TO_PIXEL, -e.offsetY/METER_TO_PIXEL)
    })

    createBody(
        {x: 200/METER_TO_PIXEL, y: -600/METER_TO_PIXEL }
      , {x: 100, y: 10 }
      , true
    )
  }, 100)
})
