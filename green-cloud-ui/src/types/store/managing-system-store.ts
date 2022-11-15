import { AdaptationGoal, AdaptationLog } from 'types/adaptation'

export type ManagingSystemStore = {
   systemIndicator: number
   jobSuccessRatio: number
   performedAdaptations: number
   weakAdaptations: number
   strongAdaptations: number
   adaptationLogs: AdaptationLog[]
   adaptationGoals: AdaptationGoal[]
}
