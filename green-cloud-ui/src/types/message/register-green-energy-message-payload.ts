import { EnergyType } from "types/enum"
import { Location } from "../location"

export type RegisterGreenEnergyMessage = {
    name: string,
    maximumCapacity: number,
    monitoringAgent: string,
    serverAgent: string,
    agentLocation: Location,
    energyType: EnergyType
}