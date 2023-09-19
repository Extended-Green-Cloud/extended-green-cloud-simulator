export const EVENT_MAP = {
   POWER_SHORTAGE_EVENT: {
      labels: {
         ACTIVE: 'Start power shortage',
         INACTIVE: 'Finish power shortage'
      },
      title: 'Power shortage event',
      description: 'Event that decreases the maximum capacity of selected to 0'
   },
   WEATHER_DROP_EVENT: {
      label: 'Trigger drop in weather conditions',
      title: 'Weather drop event',
      description: 'Event that decreases available power of all Green Sources connected to Servers of given CNA'
   }
}
