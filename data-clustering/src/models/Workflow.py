import numpy as np
import pandas as pd

from typing import Dict, List
from src.helpers.value_reader import read_value_or_return_default, read_first_or_return_default, read_json_or_return_default
from src.models.Metadata import Metadata
from src.models.WorkflowStep import WorkflowStep
from src.models.ProcessingDetails import ProcessingDetails


class Workflow():
    '''
    Class representing client job workflow.
    '''
    SUCCEEDED_STATUSES = ['successfully processed',
                          'processing finished successfully']

    def FINAL_STATE_PREDICATE(
        self, param): return param['name'] == 'final_state'

    def __init__(self, database_data: Dict, metadata: Dict, spec: Dict, status: Dict) -> None:
        '''
        Method initialize job workflow.

        Parameters:
        database_data - information about workflow obtained from the database
        metadata - dictionary of metadata obtained from Argo workflow configuration json
        workflowSpec - dictionary of workflow specification obtained from Argo workflow configuration json
        workflowStatus - dictionary of final workflow status obtained from Argo workflow configuration json
        '''
        self.argo_workflow_status = status['phase']
        self.argo_detailed_status_message = \
            self.get_detailed_status_message(status)
        self.argo_output_message = self.get_output_message(status)
        self.order_item_status = read_value_or_return_default(
            'status', database_data)
        self.order_status = read_value_or_return_default(
            'status.1', database_data)
        self.priority = read_value_or_return_default(
            'priority', database_data, 0)

        self.get_task_completion_percentage(status['progress'])
        self.initialize_steps(spec, status)

        self.processingDetails = ProcessingDetails(
            metadata, spec, status, database_data)
        self.metadata = Metadata(metadata, database_data)

    def get_task_completion_percentage(self, progress: str) -> float:
        '''
        Method assigns task completion percentage.

        Parameters:
        progress - progress in executing given task

        Returns: numerical task completion percentage
        '''
        num, denom = progress.split('/')
        self.completion = float(num) / float(denom) if denom != '0' else 0

    def initialize_steps(self, workflowSpec: Dict, workflowStatus: Dict) -> None:
        '''
        Method initialize steps of the workflow.

        Parameters:
        workflowSpec - dictionary of workflow specification obtained from Argo workflow configuration json
        workflowStatus - dictionary of final workflow status obtained from Argo workflow configuration json
        '''
        workflow_steps: List[WorkflowStep] = []

        if not 'templates' in workflowSpec:
            self.steps = workflow_steps
            self.steps_no = len(workflow_steps)
            return

        for template in workflowSpec['templates']:
            if 'dag' not in template:
                if 'nodes' in workflowStatus:
                    nodes = workflowStatus['nodes'].values()
                    step_statuses = [status for status in nodes
                                     if status['templateName'] == template['name'] and status['type'] == 'Pod']

                    if len(step_statuses) > 0:
                        def filter_retry(param):
                            return param['templateName'] == template['name'] and param['type'] == 'Retry'
                        retry = next(filter(filter_retry, nodes), None)

                        step = WorkflowStep(template, step_statuses, retry)
                        workflow_steps.append(step)

                else:
                    workflow_steps.append(WorkflowStep(template, None, None))

        self.steps = workflow_steps
        self.steps_no = len(workflow_steps)

    def get_detailed_status_message(self,  status: Dict) -> str:
        '''
        Method returns detailed message providing more information regarding workflow final status.

        Parameters:
        status - parameters of final workflow status obtained from argo config

        Returns: string being detailed workflow status message
        '''
        if 'message' in status and status['message'] != '':
            lower_case_message = str(status['message']).lower()

            if lower_case_message.__contains__('no more retries left'):
                return 'no more retries left'
            elif lower_case_message.__contains__('request timed out'):
                return 'request timed out'
            elif lower_case_message.__contains__('leader changed'):
                return 'leader changed'
            elif lower_case_message.__contains__('stopped'):
                return 'stopped with strategy "stop"'
            elif lower_case_message.__contains__('cannot get resource'):
                return 'resource not found'

            return 'undefined'
        else:
            return 'undefined'

    def get_output_message(self,  status: Dict) -> str:
        '''
        Method returns message from output parameters.

        Parameters:
        status - parameters of final workflow status obtained from argo config

        Returns: string being final argo workflow status message
        '''
        is_output_available = 'outputs' in status and 'parameters' in status['outputs']

        if is_output_available:
            final_state = read_first_or_return_default(
                status['outputs']['parameters'], self.FINAL_STATE_PREDICATE)
            final_state_val = final_state['value'] if final_state else None
            final_state = read_json_or_return_default(final_state_val)

            if not final_state or 'message' not in final_state or str(final_state['message']) == '':
                return self.get_undefined_status()

            message = str(final_state['message']).lower()
            return 'product processed successfully' if message in self.SUCCEEDED_STATUSES else message
        else:
            return self.get_undefined_status()

    def get_undefined_status(self) -> str:
        '''
        Method that returns specified undefined status.

        Returns: workflow status
        '''
        return 'product processed successfully' if self.argo_workflow_status == 'Succeeded' else 'undefined error'
