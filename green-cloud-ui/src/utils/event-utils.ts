import { Agent, EventType } from '@types'

/**
 * Method returns event of given type
 *
 * @param type type of event
 * @param agent agent name
 *
 * @returns event
 */
export const getEventByType = (type: EventType, agent: Agent) => agent.events.find((event) => event.type === type)
