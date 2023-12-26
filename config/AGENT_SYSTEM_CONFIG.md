## Configuration instruction of agent-system module

Module _agent-system_ accepts single configuration file: _config.properties_ (**it is important to not change the file
name**).

It contains the following properties:

- `weather.api.key` - key used to access the default _OpenWeather API_ account via which _Monitoring Agents_ retrieve
  weather conditions. The user should keep in mind, that the account provided by default has **limited number** of API
  request.
- `offline.mode` - boolean flag indicating, whether _Monitoring Agents_ should use external weather API service, or
  should operate in "offline" manner (meaning, using mock data in place of the real weather conditions).
