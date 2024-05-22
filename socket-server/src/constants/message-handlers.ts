import {
	handleIncrementFailedJobs,
	handleIncrementFinishJobs,
	handleIncrementStrongAdaptations,
	handleIncrementWeakAdaptations,
	handleRegisterGoals,
	handleAddAdaptationLog,
	handleSetClientJobDurationMap,
	handleSetClientJobStatus,
	handleSystemTimeMessage,
	handleUpdateIndicators,
	handleCurrentClientsNumber,
	handleExecutedJobs,
	handlePlannedJobs,
	handleUpdateAdaptationAction,
	handleUpdateJobExecutionProportion,
	handleExecutedInCloudJobs,
	handleIncrementFinishInCloudJobs,
	handleUpdateJobExecutor,
	handleUpdateJobFinalPrice,
	handleUpdateJobEstimatedPrice,
	handleUpdateJobEstimatedTime,
	handleUpdateJobFinishDate,
} from "../module";
import {
	handleUpdateJobQueue,
	handleSetTraffic,
	handleSetBackUpTraffic,
	handleUpdateResources,
	handleSetJobsCount,
	handleSetClientNumber,
	handleServerDisabling,
	handleServerEnabling,
	handleSetActive,
	handleSetJobsOnHold,
	handleSetSuccessRatio,
	handleUpdateServerConnection,
	handleWeatherPredictionError,
	handleUpdateGreenEnergy,
	handleUpdateEnergyInUse,
	handleRegisterAgent,
	handleRemoveAgent,
	handleUpdateDefaultResources,
	handleUpdateServerMaintenanceState,
} from "../module/agents/message-handlers";

export const MESSAGE_HANDLERS = {
	INCREMENT_FINISHED_JOBS: handleIncrementFinishJobs,
	INCREMENT_FINISHED_IN_CLOUD_JOBS: handleIncrementFinishInCloudJobs,
	INCREMENT_FAILED_JOBS: handleIncrementFailedJobs,
	INCREMENT_WEAK_ADAPTATIONS: handleIncrementWeakAdaptations,
	INCREMENT_STRONG_ADAPTATIONS: handleIncrementStrongAdaptations,
	UPDATE_JOB_QUEUE: handleUpdateJobQueue,
	UPDATE_SERVER_CONNECTION: handleUpdateServerConnection,
	UPDATE_INDICATORS: handleUpdateIndicators,
	UPDATE_CURRENT_CLIENTS: handleCurrentClientsNumber,
	UPDATE_CURRENT_PLANNED_JOBS: handlePlannedJobs,
	UPDATE_CURRENT_ACTIVE_JOBS: handleExecutedJobs,
	UPDATE_CURRENT_IN_CLOUD_ACTIVE_JOBS: handleExecutedInCloudJobs,
	SET_TRAFFIC: handleSetTraffic,
	SET_IS_ACTIVE: handleSetActive,
	SET_JOBS_COUNT: handleSetJobsCount,
	SET_ON_HOLD_JOBS_COUNT: handleSetJobsOnHold,
	SET_CLIENT_NUMBER: handleSetClientNumber,
	SET_CLIENT_JOB_STATUS: handleSetClientJobStatus,
	SET_CLIENT_JOB_DURATION_MAP: handleSetClientJobDurationMap,
	SET_SERVER_BACK_UP_TRAFFIC: handleSetBackUpTraffic,
	SET_JOB_SUCCESS_RATIO: handleSetSuccessRatio,
	SET_WEATHER_PREDICTION_ERROR: handleWeatherPredictionError,
	SET_AVAILABLE_GREEN_ENERGY: handleUpdateGreenEnergy,
	REGISTER_AGENT: handleRegisterAgent,
	REMOVE_AGENT: handleRemoveAgent,
	REGISTER_MANAGING: handleRegisterGoals,
	ADD_ADAPTATION_LOG: handleAddAdaptationLog,
	REPORT_SYSTEM_START_TIME: handleSystemTimeMessage,
	DISABLE_SERVER: handleServerDisabling,
	ENABLE_SERVER: handleServerEnabling,
	UPDATE_ADAPTATION_ACTION: handleUpdateAdaptationAction,
	UPDATE_SERVER_RESOURCES: handleUpdateResources,
	UPDATE_DEFAULT_RESOURCES: handleUpdateDefaultResources,
	UPDATE_ENERGY_IN_USE: handleUpdateEnergyInUse,
	UPDATE_JOB_EXECUTION_PROPORTION: handleUpdateJobExecutionProportion,
	UPDATE_SERVER_MAINTENANCE_STATE: handleUpdateServerMaintenanceState,
	UPDATE_SERVER_FOR_CLIENT: handleUpdateJobExecutor,
	UPDATE_FINAL_COST_FOR_CLIENT: handleUpdateJobFinalPrice,
	UPDATE_ESTIMATED_COST_FOR_CLIENT: handleUpdateJobEstimatedPrice,
	UPDATE_ESTIMATED_TIME_FOR_CLIENT: handleUpdateJobEstimatedTime,
	UPDATE_FINAL_EXECUTION_DATE_FOR_CLIENT: handleUpdateJobFinishDate,
};
