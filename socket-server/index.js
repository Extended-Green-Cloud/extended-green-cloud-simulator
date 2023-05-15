var cors = require("cors");
var bodyParser = require('body-parser')
var express = require("express");
var app = express();
var expressWs = require("express-ws")(app);
require('dotenv').config();

const { WELCOMING_MESSAGE, ROUTE_TYPES } = require("./lib/constants/constants");
const { MESSAGE_HANDLERS } = require("./lib/constants/message-handlers");
const { handlePowerShortage, async } = require("./lib/module/agents/event-handler");
const { reportSimulationStatistics } = require("./lib/module/simulation/report-handler");
const { parseData } = require("./lib/utils/parse-utils")
const { logUserConnected, logNewMessage, logStateReset } = require("./lib/utils/logger-utils")
const { resetSystemState, getSystemState, getReportsState, getAgentsState, getClientsState, getManagingState, getNetworkState, getClient, getGraphState, getAgent } = require("./lib/utils/state-utils");
const { AGENTS_REPORTS_STATE } = require("./lib/module/agents/agents-state");
const { CLIENTS_REPORTS_STATE } = require("./lib/module/clients/clients-state");
const { NETWORK_REPORTS_STATE } = require("./lib/module/network/network-state");
const { MANAGING_SYSTEM_REPORTS } = require("./lib/module/managing-system/managing-system-state");

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

app.ws("/powerShortage", function (ws, _) {
  ws.route = '/powerShortage'
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
  res.send(JSON.stringify(AGENTS_REPORTS_STATE))
})

app.get(ROUTE_TYPES.FRONT + '/reports/client', async (req, res) => {
  res.send(JSON.stringify(CLIENTS_REPORTS_STATE))
})

app.get(ROUTE_TYPES.FRONT + '/reports/network', async (req, res) => {
  res.send(JSON.stringify(NETWORK_REPORTS_STATE))
})

app.get(ROUTE_TYPES.FRONT + '/reports/managing', async (req, res) => {
  res.send(JSON.stringify(MANAGING_SYSTEM_REPORTS))
})

app.post(ROUTE_TYPES.FRONT + '/powerShortage', (req, res) => {
  const msg = req.body
  const dataToPass = handlePowerShortage(msg)
  expressWs.getWss().clients.forEach(client => {
    if (client.route === '/powerShortage') {
      client.send(JSON.stringify(dataToPass))
    }
  })
})

reportSimulationStatistics
app.listen(process.env.PORT);  