from typing import List, Dict
from src.helpers.value_reader import read_value_or_return_default, read_date_value_or_return_default, DateParser


class WorkflowStep():
    '''
    Class representing single step of the workflow.
    '''

    def __init__(self, stepSpec: Dict, stepDetails: List[Dict], stepRetryDetails: Dict) -> None:
        '''
        Method initialize single workflow step.

        Parameters:
        stepSpec - specification of the workflow step
        stepDetails - processing details of the workflow steps
        stepRetryDetails - processing details of the retry workflow step
        '''
        self.name = stepSpec['name']

        retry_strategy = read_value_or_return_default('retryStrategy', stepSpec, None)
        self.retryLimit = int(retry_strategy['limit']) if retry_strategy else 0

        self.initialize_default_step_execution_details()

        if stepDetails != None:
            step_details_resources = stepRetryDetails or stepDetails[0]

            self.status = step_details_resources['phase']
            self.node = read_value_or_return_default('hostNodeName', stepDetails[0], 'Unspecified')
            self.startTime = read_date_value_or_return_default('startedAt', step_details_resources, DateParser.PARSER_ARGO)
            self.endTime = read_date_value_or_return_default('finishedAt', step_details_resources, DateParser.PARSER_ARGO)
            self.duration = (self.endTime - self.startTime).total_seconds()

            self.initialize_step_resources(stepDetails)

    def initialize_default_step_execution_details(self) -> None:
        '''
        Method initialize default values of workflow step.
        '''
        self.status = 'Unknown'
        self.node = 'Unspecified'
        self.cpu = 0
        self.memory = 0
        self.ephemeralStorage = 0
        self.duration = 0

    def initialize_step_resources(self, stepDetails: List[Dict]) -> None:
        '''
        Method initialize resources of single workflow step.

        Parameters:
        stepDetails - processing details of the workflow steps
        '''
        for stepDetail in stepDetails:
            resource_duration = read_value_or_return_default('resourcesDuration', stepDetail, None)

            if not resource_duration:
                continue

            self.cpu += read_value_or_return_default('cpu', resource_duration, 0)
            self.memory += read_value_or_return_default('memory', resource_duration, 0)
            self.ephemeralStorage += read_value_or_return_default('ephemeral-storage', resource_duration, 0)
