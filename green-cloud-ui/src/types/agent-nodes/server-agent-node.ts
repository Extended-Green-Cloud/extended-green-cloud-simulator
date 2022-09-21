import { ServerState } from "../enum";
import { CommonAgentNodeInterface } from "./common";

export interface ServerNode extends CommonAgentNodeInterface {
    state: ServerState
}