export const EVENT_MAP = {
   POWER_SHORTAGE_EVENT: {
      labels: {
         ACTIVE: 'Start power shortage',
         INACTIVE: 'Finish power shortage'
      },
      title: 'Power shortage event',
      description: 'Event that decreases the maximum capacity of selected agent to 0'
   },
   WEATHER_DROP_EVENT: {
      label: 'Trigger drop in weather conditions',
      title: 'Weather drop event',
      description: 'Event that decreases available power of all Green Sources connected to Servers of given RMA'
   },
   SWITCH_ON_OFF_EVENT: {
      labels: {
         OFF: 'Switch server off',
         ON: 'Switch server on'
      },
      title: 'Switch server on/off event',
      description: 'Event gracefully disables/enables given server making it inactive in the system'
   },
   SERVER_MAINTENANCE_EVENT: {
      label: 'Start configuring server resources',
      title: 'Server maintenance event',
      description:
         'Event initiates server maintenance, giving user the ability to change its resources. Important! Event can be executed only on servers that have been previously switched off.'
   },
   REMOVE_AGENT_EVENT: {
      label: 'Remove server and green energy sources',
      title: 'Remove server event',
      description: 'Event permanently removes the server and its green energy sources from the infrastructure.'
   }
}
