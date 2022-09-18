import { EnergyType } from "types/enum";
import { CommonAgentInterface } from "./common/common-agent";

export interface MonitoringAgent extends CommonAgentInterface {
    greenEnergyAgent: string
}