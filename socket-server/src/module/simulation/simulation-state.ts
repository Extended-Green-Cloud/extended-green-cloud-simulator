
interface SimulationState {
    systemStartTime: number | null
    timeDifference: number
    secondsPerHour: number
}

let SIMULATION_STATE: SimulationState = {
    systemStartTime: null,
    timeDifference: 0,
    secondsPerHour: 0,
}


const resetSimulationState = () =>
    Object.assign(SIMULATION_STATE,
        ({
            systemStartTime: null,
            timeDifference: 0,
            secondsPerHour: 0,
        }))

export {
    SIMULATION_STATE,
    resetSimulationState
}