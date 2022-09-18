import { GreenEnergyState } from "../enum";
import { CommonAgentNodeInterface } from "./common";

export interface GreenEnergyNode extends CommonAgentNodeInterface {
    state: GreenEnergyState
}