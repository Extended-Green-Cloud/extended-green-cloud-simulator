from typing import Dict, Any, List
from datetime import datetime

from src.helpers.value_reader import read_value_or_return_default, read_date_value_or_return_default, read_first_or_return_default, read_json_or_return_default, DateParser


def compute_duration_for_dates(start_time: datetime, end_time: datetime) -> Any:
    '''
    Method computes duration between a given start and end time

    Parameters:
    start_time - time from which the duration is to be computed
    end_time - time to which the duration is to be computed

    Returns: duration in seconds
    '''
    return (end_time - start_time).total_seconds() if end_time and start_time else 0


class ProcessingDetails():
    '''
    Class representing details regarding workflow processing
    '''

    def FINAL_STATE_PREDICATE(
        self, param): return param['name'] == 'final_state'

    def DAG_PREDICATE(self, template): return "dag" in template

    def __init__(self, metadata: Dict, workflowSpec: Dict, workflowStatus: Dict, database_data: Dict) -> None:
        '''
        Method initialize metadata of the given workflow.

        Parameters:
        metadata - dictionary of metadata obtained from Argo workflow configuration json
        workflowSpec - dictionary of workflow specification obtained from Argo workflow configuration json
        workflowStatus - dictionary of final workflow status obtained from Argo workflow configuration json
        database_data - information about workflow obtained from the database
        '''
        resource_duration = read_value_or_return_default(
            'resourcesDuration', workflowStatus, None)

        self.processor = read_value_or_return_default(
            'processor-name', metadata['labels'])
        self.ephemeralStorage = read_value_or_return_default(
            'ephemeral-storage', resource_duration, 0)
        self.memory = read_value_or_return_default(
            'memory', resource_duration, 0)
        self.cpu = read_value_or_return_default('cpu', resource_duration, 0)

        self.get_final_processed_size(workflowStatus)
        self.get_executed_workflow_steps(workflowStatus)
        self.get_workflow_steps(workflowSpec['templates'])
        self.get_storage_size(workflowSpec)
        self.get_duration(workflowStatus)
        self.get_deadline(database_data)

    def get_deadline(self, database_data: Dict) -> None:
        '''
        Method calculates workflow deadline and stores the corresponding information in the workflow data.

        Parameters:
        database_data - information about workflow obtained from the database
        '''
        add_date = read_date_value_or_return_default('add_date', database_data)
        deadline_date = read_date_value_or_return_default(
            'referential_eta', database_data)

        self.deadline = compute_duration_for_dates(add_date, deadline_date)

    def get_duration(self, workflowStatus: Dict) -> None:
        '''
        Method calculates execution duration and stores the corresponding information in the workflow data.

        Parameters:
        workflowStatus - dictionary of final workflow status obtained from Argo workflow configuration json
        '''
        self.startTime = read_date_value_or_return_default(
            'startedAt', workflowStatus, DateParser.PARSER_ARGO)
        self.endTime = read_date_value_or_return_default(
            'finishedAt', workflowStatus, DateParser.PARSER_ARGO)
        self.duration = compute_duration_for_dates(
            self.startTime, self.endTime)

    def get_storage_size(self, workflowSpec: Dict) -> None:
        '''
        Method computes and assigns storage size requested for given workflow.

        Parameters:
        workflowSpec - dictionary of workflow specification obtained from Argo workflow configuration json
        '''
        storage = workflowSpec['volumeClaimTemplates'][0]['spec']['resources']['requests']['storage'] \
            if 'volumeClaimTemplates' in workflowSpec else '0Gi'

        val = int(storage[:-2])
        unit = storage[-2:]

        if unit == 'Ki':
            val = val * 0.000001
        if unit == 'Mi':
            val = val * 0.001
        if unit == 'Ti':
            val = val * 1000
        if unit == 'Pi':
            val = val * 1000000
        if unit == 'Ei':
            val = val * 1152921504.61

        self.storage = val

    def get_final_processed_size(self, workflowStatus: Dict) -> None:
        '''
        Method stores the final size of processed data.

        Parameters:
        workflowStatus - dictionary of final workflow status obtained from Argo workflow configuration json
        '''
        output = read_value_or_return_default('outputs', workflowStatus, None)

        if not output:
            self.processedSize = 0
            return

        output_params = output['parameters']

        final_state = read_first_or_return_default(
            output_params, self.FINAL_STATE_PREDICATE)
        state_val = read_value_or_return_default('value', final_state, None)
        final_state_val = read_json_or_return_default(state_val)

        self.processedSize = int(read_value_or_return_default(
            'new_size', final_state_val, '0'))

    def get_workflow_steps(self, templates: List) -> None:
        '''
        Method stores the number of steps defined in the workflow.

        Parameters:
        templates - workflow templates
        '''
        dagTemplate = read_first_or_return_default(
            templates, self.DAG_PREDICATE)['dag']
        self.workflowSteps = len(dagTemplate['tasks'])

    def get_executed_workflow_steps(self, workflowStatus: Dict) -> None:
        '''
        Method stores the number of steps executed for the workflow.

        Parameters:
        workflowStatus - dictionary of final workflow status obtained from Argo workflow configuration json
        '''
        nodes = read_value_or_return_default('nodes', workflowStatus, None)

        if not nodes:
            self.executedSteps = 0
            return

        self.executedSteps = len(
            [node for node in nodes.values() if node['type'] == 'Pod'])
