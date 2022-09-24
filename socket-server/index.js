var express = require("express");
const { WELCOMING_MESSAGE, MESSAGE_ROUTES } = require("./constants/constants");
const { parseData } = require("./utils/parse-utils");
var app = express();
var expressWs = require("express-ws")(app);

let currentPlanned = 0
let currentActive = 0
let currentClients = 0

app.ws("/", function (ws, req) {
  ws.route = '/'
  ws.on("open", () => ws.send(JSON.stringify(WELCOMING_MESSAGE)))

  ws.on("message", function (msg) {
    const message = parseData(msg)
    const type = message.type
    const dataToPass = handleMessage(message, type)
    console.log(dataToPass)
    expressWs.broadcast(JSON.stringify(dataToPass), MESSAGE_ROUTES[type])
  });
});

app.ws("/network", function (ws, req) {
  ws.route = '/network'
  ws.on("open", () => ws.send(JSON.stringify(WELCOMING_MESSAGE)))
});

app.ws("/agent", function (ws, req) {
  ws.route = '/agent'
  ws.on("open", () => ws.send(JSON.stringify(WELCOMING_MESSAGE)))
});


const handleMessage = (message, type) => {
  if (type === 'UPDATE_CURRENT_PLANNED_JOBS') {
    currentPlanned += message.data
    return { ...message, data: currentPlanned }
  } else if (type === 'UPDATE_CURRENT_ACTIVE_JOBS') {
    currentActive += message.data
    return { ...message, data: currentActive }
  } else if (type === 'UPDATE_CURRENT_CLIENTS') {
    currentClients += message.data
    return { ...message, data: currentClients }
  }
  return message
}


expressWs.broadcast = function (data, route) {
  expressWs.getWss().clients.forEach(client => {
    if (client.route == route) {
      client.send(data)
    }
  })
}

app.listen(8080);