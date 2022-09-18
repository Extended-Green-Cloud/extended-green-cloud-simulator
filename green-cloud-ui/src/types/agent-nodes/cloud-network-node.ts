import { CloudNetworkTraffic } from "../enum";
import { CommonAgentNodeInterface } from "./common";

export interface CloudNetworkNode extends CommonAgentNodeInterface {
    traffic: CloudNetworkTraffic
}