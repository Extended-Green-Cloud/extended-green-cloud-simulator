import { SIMULATION_STATE } from "../module/simulation/simulation-state"

const getCurrentTime = () => {
    if (SIMULATION_STATE.systemStartTime !== null) {
        const timeDiff = new Date().getTime() - SIMULATION_STATE.timeDifference - SIMULATION_STATE.systemStartTime
        const timeMultiplier = 3600 / SIMULATION_STATE.secondsPerHour;
        const realTimeDifference = timeDiff * timeMultiplier;

        return SIMULATION_STATE.systemStartTime + Math.round(realTimeDifference)
    }
}

const isWithinMonth = (time: number) => {
    return time >= new Date(getCurrentTime() - 60 * 24 * 7 * 30 * 60 * 1000).getTime()
}

export {
    getCurrentTime,
    isWithinMonth
}