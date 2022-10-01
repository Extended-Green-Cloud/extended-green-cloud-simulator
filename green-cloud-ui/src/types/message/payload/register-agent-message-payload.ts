import { RegisterClientMessage } from "./register-client-message-payload";
import { RegisterCloudNetworkMessage } from "./register-cloud-network-message-payload";
import { RegisterGreenEnergyMessage } from "./register-green-energy-message-payload";
import { RegisterMonitoringMessage } from "./register-monitoring-message-payload";
import { RegisterServerMessage } from "./register-server-message-payload";

export type RegisterAgent = RegisterClientMessage | RegisterCloudNetworkMessage | RegisterGreenEnergyMessage | RegisterServerMessage | RegisterMonitoringMessage