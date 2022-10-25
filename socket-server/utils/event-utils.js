const { getAgentByName } = require("./agent-utils")
const { EVENT_TYPE, EVENT_STATE } = require('../constants/constants')
const { logPowerShortageEvent } = require("./logger-utils")
const sleep = require('util').promisify(setTimeout)

const getEventByType = (events, type) => {
    return events.find(event => event.type === type)
}

const getEventOccurenceTime = () => {
    const occurenceTime = new Date()
    occurenceTime.setSeconds(occurenceTime.getSeconds() + 2)
    return occurenceTime
}

async function unlockEvent(event) {
    await sleep(3000)
    event.disabled = false
}

module.exports = {
    handlePowerShortage: function (state, data) {
        const agent = getAgentByName(state.agents.agents, data.agentName)

        if (agent) {
            const event = getEventByType(agent.events, EVENT_TYPE.POWER_SHORTAGE_EVENT)

            if (event) {
                const eventState = event.state === EVENT_STATE.ACTIVE ?
                    'triggered' :
                    'finished'
                logPowerShortageEvent(agent.name, eventState)
                const maxCapacity = data.data.newMaximumCapacity

                agent.currentMaximumCapacity = EVENT_STATE.ACTIVE === event.state ?
                    maxCapacity :
                    agent.initialMaximumCapacity
                event.disabled = true
                const dataToReturn = {
                    agentName: agent.name,
                    type: EVENT_TYPE.POWER_SHORTAGE_EVENT,
                    data: {
                        newMaximumCapacity: maxCapacity,
                        occurrenceTime: getEventOccurenceTime(),
                        isFinished: event.state !== EVENT_STATE.ACTIVE
                    }
                }
                event.state = event.state === EVENT_STATE.ACTIVE ?
                    EVENT_STATE.INACTIVE :
                    EVENT_STATE.ACTIVE;

                unlockEvent(event)
                return dataToReturn
            }
        }
    }
}