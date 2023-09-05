import { ENERGY_TYPE } from "../../../constants";
import { CommonAgent } from "./common-agent";

export interface GreenEnergyAgentStatic {
	monitoringAgent: string;
	serverAgent: string;
	connectedServers: string[];
	agentLocation: Location;
	energyType: ENERGY_TYPE;
	pricePerPower: number;
	maximumCapacity: number;
}

export interface GreenEnergyAgentDynamic {
	weatherPredictionError: number;
	availableGreenEnergy: number;
	traffic: number;
	energyInUse: number;
	numberOfExecutedJobs: number;
	numberOfJobsOnHold: number;
	successRatio: number;
}

export type GreenEnergyAgent = CommonAgent & GreenEnergyAgentStatic & GreenEnergyAgentDynamic;
