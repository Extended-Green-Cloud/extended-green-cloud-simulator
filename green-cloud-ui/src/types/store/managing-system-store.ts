import { AdaptationGoal, AdaptationGoalQuality, AdaptationLog } from 'types/adaptation'

export type ManagingSystemStore = {
   systemIndicator: number
   goalQualityIndicators: AdaptationGoalQuality[]
   performedAdaptations: number
   weakAdaptations: number
   strongAdaptations: number
   adaptationLogs: AdaptationLog[]
   adaptationGoals: AdaptationGoal[]
}
