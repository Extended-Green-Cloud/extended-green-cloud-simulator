import { SIMULATION_STATE } from "./simulation-state";

const handleSystemTimeMessage = (msg) => {
	if (SIMULATION_STATE.systemStartTime === null) {
		SIMULATION_STATE.systemStartTime = msg.time;
		SIMULATION_STATE.secondsPerHour = msg.secondsPerHour;
	}
};

export { handleSystemTimeMessage };
