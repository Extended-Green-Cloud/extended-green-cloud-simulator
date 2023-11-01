var cors = require("cors");
var bodyParser = require('body-parser')
var express = require("express");
var app = express();
var expressWs = require("express-ws")(app);
require('dotenv').config();

const { WELCOMING_MESSAGE, ROUTE_TYPES } = require("./lib/constants/constants");
const { MESSAGE_HANDLERS } = require("./lib/constants/message-handlers");
const { handlePowerShortage, async, handleWeatherDrop, handleServerSwitchOnOff, handleServerMaintenanceSend, handleServerMaintenanceReset } = require("./lib/module/agents/event-handler");
const { reportSimulationStatistics } = require("./lib/module/simulation/report-handler");
const { parseData } = require("./lib/utils/parse-utils")
const { logUserConnected, logNewMessage, logStateReset } = require("./lib/utils/logger-utils")
const { resetSystemState, getSystemState, getAgentsState, getClientsState, getManagingState, getNetworkState, getClient, getGraphState, getAgent, getNetworkReportsState, getClientsReportsState, getAgentsReportsState, getManaginReportsState } = require("./lib/utils/state-utils");
const { handleCreateClientEvent } = require("./lib/module/network/event-handlers");

app.use(cors())
app.use(bodyParser.urlencoded({ extended: false }))
app.use(bodyParser.json())

app.ws("/", function (ws, _) {
  ws.route = '/'
  logUserConnected()
  ws.send(JSON.stringify(WELCOMING_MESSAGE))

  ws.on("message", function (msg) {
    const message = parseData(msg)
    const type = message.type
    const messageHandler = MESSAGE_HANDLERS[type]

    if (messageHandler) {
      logNewMessage(message)
      messageHandler(message)
    }
  });
});

app.ws("/event", function (ws, _) {
  ws.route = '/event'
  logUserConnected()
  ws.send(JSON.stringify(WELCOMING_MESSAGE))
});


app.get(ROUTE_TYPES.FRONT, (_, res) => {
  res.send(JSON.stringify(getSystemState()))
})

app.get(ROUTE_TYPES.FRONT + '/agents', async (req, res) => {
  res.send(JSON.stringify(getAgentsState()))
})

app.get(ROUTE_TYPES.FRONT + '/agent', async (req, res) => {
  res.send(JSON.stringify(getAgent(req.query.name)))
})

app.get(ROUTE_TYPES.FRONT + '/graph', async (req, res) => {
  res.send(JSON.stringify(getGraphState()))
})

app.get(ROUTE_TYPES.FRONT + '/clients', async (req, res) => {
  res.send(JSON.stringify(getClientsState()))
})

app.get(ROUTE_TYPES.FRONT + '/client', async (req, res) => {
  res.send(JSON.stringify(getClient(req.query.name)))
})

app.get(ROUTE_TYPES.FRONT + '/managing', async (req, res) => {
  res.send(JSON.stringify(getManagingState()))
})

app.get(ROUTE_TYPES.FRONT + '/network', async (req, res) => {
  res.send(JSON.stringify(getNetworkState()))
})

app.get(ROUTE_TYPES.FRONT + '/reset', async (req, res) => {
  resetSystemState()
  logStateReset()
})

app.get(ROUTE_TYPES.FRONT + '/reports/agent', async (req, res) => {
  res.send(JSON.stringify(getAgentsReportsState()))
})

app.get(ROUTE_TYPES.FRONT + '/reports/client', async (req, res) => {
  res.send(JSON.stringify(getClientsReportsState()))
})

app.get(ROUTE_TYPES.FRONT + '/reports/network', async (req, res) => {
  res.send(JSON.stringify(getNetworkReportsState()))
})

app.get(ROUTE_TYPES.FRONT + '/reports/managing', async (req, res) => {
  res.send(JSON.stringify(getManaginReportsState()))
})

app.post(ROUTE_TYPES.FRONT + '/powerShortage', (req, res) => {
  const msg = req.body
  const dataToPass = handlePowerShortage(msg)
  expressWs.getWss().clients.forEach(client => {
    if (client.route === '/event') {
      client.send(JSON.stringify(dataToPass))
    }
  })
})

app.post(ROUTE_TYPES.FRONT + '/weatherDrop', (req, res) => {
  const msg = req.body
  const dataToPass = handleWeatherDrop(msg)
  expressWs.getWss().clients.forEach(client => {
    if (client.route === '/event') {
      client.send(JSON.stringify(dataToPass))
    }
  })
})

app.post(ROUTE_TYPES.FRONT + '/switchOnOffServer', (req, res) => {
  const msg = req.body
  const dataToPass = handleServerSwitchOnOff(msg)
  expressWs.getWss().clients.forEach(client => {
    if (client.route === '/event') {
      client.send(JSON.stringify(dataToPass))
    }
  })
})

app.post(ROUTE_TYPES.FRONT + '/serverMaintenance', (req, res) => {
  const msg = req.body
  const dataToPass = handleServerMaintenanceSend(msg)
  expressWs.getWss().clients.forEach(client => {
    if (client.route === '/event') {
      client.send(JSON.stringify(dataToPass))
    }
  })
})

app.post(ROUTE_TYPES.FRONT + '/createClient', (req, res) => {
  const msg = req.body
  const dataToPass = handleCreateClientEvent(msg)
  expressWs.getWss().clients.forEach(client => {
    if (client.route === '/event') {
      client.send(JSON.stringify(dataToPass))
    }
  })
})

app.post(ROUTE_TYPES.FRONT + '/resetServerMaintenance', (req, res) => {
  const msg = req.body
  handleServerMaintenanceReset(msg)
})

reportSimulationStatistics
app.listen(process.env.PORT);  