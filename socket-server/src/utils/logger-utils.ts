const logNewMessage = (msg) =>
	console.log(`[${new Date().toLocaleTimeString()}] Retrieved message: ${JSON.stringify(msg.type)}`);
const logUserConnected = () => console.log(`[${new Date().toLocaleTimeString()}] User connected to WebSocket`);
const logStateReset = () => console.log(`[${new Date().toLocaleTimeString()}] State has been reset`);
const logPowerShortageEvent = (agentName, actionState) =>
	console.log(`[${new Date().toLocaleTimeString()}] Power shortage ${actionState} for ${agentName}`);
const logWeatherDropEvent = (agentName) =>
	console.log(`[${new Date().toLocaleTimeString()}] Weather drop for ${agentName}`);
const logSwitchOnOffEvent = (agentName, eventType) =>
	console.log(`[${new Date().toLocaleTimeString()}] Switching ${eventType} server ${agentName}`);

export {
	logNewMessage,
	logUserConnected,
	logStateReset,
	logWeatherDropEvent,
	logPowerShortageEvent,
	logSwitchOnOffEvent,
};
