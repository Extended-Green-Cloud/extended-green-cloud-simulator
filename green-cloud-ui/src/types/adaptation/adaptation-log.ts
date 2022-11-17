import { LogType } from 'types/enum'

export type AdaptationLog = {
   type: LogType
   description: string
   agentName?: string
   time: number
}
