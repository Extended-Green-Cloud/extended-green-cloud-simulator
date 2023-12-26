## Configuration instruction of green-cloud-ui module

Module _green-cloud-ui_ contains a single configuration file that specifies the addresses of corresponding WebSockets
that are used by the GUI to communicate with _socket-server_:

- REACT_APP_WEB_SOCKET_AGENTS_FRONTEND_URL=http://`socket address`/frontend - IP address of the host that runs WebSocket
  responsible for passing agent-related data
- REACT_APP_WEB_SOCKET_CLIENTS_FRONTEND_URL=http://`socket address`/frontend - IP address of the host that runs
  WebSocket responsible for passing client-related data
- REACT_APP_WEB_SOCKET_MANAGING_FRONTEND_URL=http://`socket address`/frontend - IP address of the host that runs
  WebSocket responsible for passing adaptation-related
- REACT_APP_WEB_SOCKET_NETWORK_FRONTEND_URL=http://`socket address`/frontend - IP address of the host that runs
  WebSocket responsible for passing general network statistics data
- REACT_APP_WEB_SOCKET_EVENT_FRONTEND_URL=http://`socket address`/frontend - IP address of the host that runs WebSocket
  responsible for passing external events to agents

They should correspond to the addresses specified in the _engine_ configuration.