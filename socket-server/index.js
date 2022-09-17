const { WebSocket } = require("ws");

const wss = new WebSocket.Server({
  port: 8080,
});

const WELCOMING_MESSAGE = {
  type: 'SOCKET_CONNECTED',
  data: 'Connection to the socket established successfully'
}

wss.on('connection', (ws) => {
  ws.send(JSON.stringify(WELCOMING_MESSAGE));

  ws.on('message', (data) => {
    wss.clients.forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        try {
          client.send(JSON.stringify(JSON.parse(data)))
        } catch (e) {
          client.send(JSON.stringify(data))
        }
      }
    });
  });
});