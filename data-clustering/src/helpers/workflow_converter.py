import pandas as pd
import numpy as np

from typing import List, Tuple
from src.models.Workflow import Workflow
from src.helpers.feature_encoder import factorize_feature, encode_categorical_list, WORKFLOW_FEATURES
from src.helpers.workflow_filter import WORKFLOW_NUMERIC_FEATURES

WORKFLOWS_COLUMNS = [
    WORKFLOW_FEATURES.WORKFLOW_UID,
    WORKFLOW_FEATURES.ORDER_NAME,
    WORKFLOW_FEATURES.ORDER_ID,
    WORKFLOW_FEATURES.ORDER_ITEM_STATUS,
    WORKFLOW_FEATURES.ORDER_STATUS,
    WORKFLOW_FEATURES.ARGO_STATUS,
    WORKFLOW_FEATURES.ARGO_STATUS_DETAILS,
    WORKFLOW_FEATURES.ARGO_OUTPUT_MSG,
    WORKFLOW_FEATURES.PROCESSOR_TYPE,
    WORKFLOW_FEATURES.CPU,
    WORKFLOW_FEATURES.MEMORY,
    WORKFLOW_FEATURES.EPHEMERAL_STORAGE,
    WORKFLOW_FEATURES.STORAGE,
    WORKFLOW_FEATURES.PROCESSED_SIZE,
    WORKFLOW_FEATURES.DURATION,
    WORKFLOW_FEATURES.DEADLINE,
    WORKFLOW_FEATURES.PRIORITY,
    WORKFLOW_FEATURES.STEPS_NO,
    WORKFLOW_FEATURES.EXECUTED_STEPS_NO,
    WORKFLOW_FEATURES.WORKFLOW_STEPS,
    WORKFLOW_FEATURES.WORKFLOW_STEPS_ENCODED,
    WORKFLOW_FEATURES.NODES,
    WORKFLOW_FEATURES.NODES_ENCODED,
    WORKFLOW_FEATURES.STEPS_STATUSES,
    WORKFLOW_FEATURES.STEPS_STATUSES_ENCODED]


def get_unique_values_for_workflows_steps(workflows: List[Workflow], key: str) -> np.ndarray:
    '''
    Method returns list of unique string values of given key for all workflows steps.

    Parameters:
    workflows - list od workflows
    key - parameter for which unique values are to be retrieved

    Return: numpy array of unique names
    '''
    return np.unique([getattr(step, key) for workflow in workflows for step in workflow.steps])


def get_workflows_columns(unique_steps: List[str]) -> List[str]:
    '''
    Method returns list of columns describing workflow details.

    Parameters:
    unique_steps - list of unique steps names

    Returns: list of workflow columns
    '''
    columns = WORKFLOWS_COLUMNS.copy()

    columns.extend([f'{name}_cpu' for name in unique_steps])
    columns.extend([f'{name}_memory' for name in unique_steps])
    columns.extend([f'{name}_ephemeral_storage' for name in unique_steps])
    columns.extend([f'{name}_duration' for name in unique_steps])

    return columns


def get_resource_utilization_for_steps(workflow: Workflow,
                                       unique_steps: List[str]) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
    '''
    Method returns list representing resource utilization of workflow steps.

    Parameters:
    workflows - workflow from which values are retrieved
    unique_steps - list of unique steps names
    '''
    steps = [step.name for step in workflow.steps]

    cpu_per_step = np.zeros(len(unique_steps))
    memory_per_step = np.zeros(len(unique_steps))
    ephemeralStorage_per_step = np.zeros(len(unique_steps))
    duration_per_step = np.zeros(len(unique_steps))

    for idx, step in enumerate(unique_steps):
        if step in steps:
            step_data = [el for el in workflow.steps if el.name == step][0]

            cpu_per_step[idx] = step_data.cpu
            memory_per_step[idx] = step_data.memory
            ephemeralStorage_per_step[idx] = step_data.ephemeralStorage
            duration_per_step[idx] = step_data.duration

    return cpu_per_step, memory_per_step, ephemeralStorage_per_step, duration_per_step


def map_step_details(workflow: Workflow,
                     processing_details: List,
                     unique_steps: List[str],
                     unique_nodes: List[str],
                     unique_statuses: List[str]) -> np.ndarray:
    '''
    Method expands details of workflow processing with the information regarding results of individual workflow steps.

    Parameters:
    workflow - workflow which values are mapped
    processing_details - initialized processing details
    unique_steps - list of unique names of possible workflows steps
    unique_nodes - list of unique names of processing nodes
    unique_statuses - list of unique names of step statuses

    Returns: expanded processing details list
    '''
    steps, encoded_steps = encode_categorical_list(
        unique_steps, workflow.steps, 'name')
    nodes, encoded_nodes = encode_categorical_list(
        unique_nodes, workflow.steps, 'node')
    statuses, encoded_statuses = encode_categorical_list(
        unique_statuses, workflow.steps, WORKFLOW_FEATURES.ORDER_ITEM_STATUS)

    processing_details.append(steps)
    processing_details.append(encoded_steps)
    processing_details.append(nodes)
    processing_details.append(encoded_nodes)
    processing_details.append(statuses)
    processing_details.append(encoded_statuses)

    cpu_vals, memory_vals, ephemeral_vals, duration_vals = \
        get_resource_utilization_for_steps(workflow, unique_steps)

    processing_details.extend(cpu_vals)
    processing_details.extend(memory_vals)
    processing_details.extend(ephemeral_vals)
    processing_details.extend(duration_vals)

    return processing_details


def map_workflow_processing_details(workflow: Workflow, unique_steps: List[str], unique_nodes: List[str], unique_steps_statuses: List[str]) -> np.ndarray:
    '''
    Method returns overall processing details of the given workflow.

    Parameters:
    workflow - workflow which values are mapped
    unique_steps - list of unique names of possible workflows steps
    unique_nodes - list of unique names of processing nodes
    unique_steps_statuses - list of unique names of step statuses

    Returns: numpy array of workflow processing details
    '''
    processing_details = []

    processing_details.append(workflow.metadata.uid)
    processing_details.append(workflow.metadata.orderName)
    processing_details.append(workflow.metadata.orderId)
    processing_details.append(workflow.order_item_status)
    processing_details.append(workflow.order_status)
    processing_details.append(workflow.argo_workflow_status)
    processing_details.append(workflow.argo_detailed_status_message)
    processing_details.append(workflow.argo_output_message)
    processing_details.append(workflow.processingDetails.processor)
    processing_details.append(workflow.processingDetails.cpu)
    processing_details.append(workflow.processingDetails.memory)
    processing_details.append(workflow.processingDetails.ephemeralStorage)
    processing_details.append(workflow.processingDetails.storage)
    processing_details.append(workflow.processingDetails.processedSize)
    processing_details.append(workflow.processingDetails.duration)
    processing_details.append(workflow.processingDetails.deadline)
    processing_details.append(workflow.priority)
    processing_details.append(workflow.steps_no)
    processing_details.append(workflow.processingDetails.executedSteps)

    processing_details = map_step_details(workflow,
                                          processing_details,
                                          unique_steps,
                                          unique_nodes,
                                          unique_steps_statuses)

    return np.array(processing_details)


def convert_workflows_to_data_frame(workflows: List[Workflow]) -> pd.DataFrame:
    '''
    Method returns data frame of processing details for given list of workflows.

    Parameters:
    workflows - list of workflows for which the processing details are to be retrieved

    Returns: data frame of workflows processing details
    '''
    workflows_unique_steps = get_unique_values_for_workflows_steps(
        workflows, 'name')
    workflows_unique_nodes = get_unique_values_for_workflows_steps(
        workflows, 'node')
    workflows_unique_steps_statuses = get_unique_values_for_workflows_steps(
        workflows, 'status')

    columns = get_workflows_columns(workflows_unique_steps)

    workflow_details = np.array(
        [map_workflow_processing_details(workflow, workflows_unique_steps, workflows_unique_nodes, workflows_unique_steps_statuses) for workflow in workflows])

    df = pd.DataFrame(workflow_details, columns=columns)
    df = factorize_feature(WORKFLOW_FEATURES.ORDER_NAME, df)
    df = factorize_feature(WORKFLOW_FEATURES.ORDER_ITEM_STATUS, df)
    df = factorize_feature(WORKFLOW_FEATURES.ORDER_STATUS, df)
    df = factorize_feature(WORKFLOW_FEATURES.ARGO_STATUS, df)
    df = factorize_feature(WORKFLOW_FEATURES.ARGO_STATUS_DETAILS, df)
    df = factorize_feature(WORKFLOW_FEATURES.ARGO_OUTPUT_MSG, df)
    df = factorize_feature(WORKFLOW_FEATURES.PROCESSOR_TYPE, df)
    df = factorize_feature(WORKFLOW_FEATURES.WORKFLOW_STEPS_ENCODED, df)
    df = factorize_feature(WORKFLOW_FEATURES.NODES_ENCODED, df)
    df = factorize_feature(WORKFLOW_FEATURES.STEPS_STATUSES_ENCODED, df)

    return df

def convert_synthetic_workflows_to_dict(synthetic_workflows: pd.DataFrame, workflow_steps: List[str]) -> List[dict]:
    '''
    Method converts synthetically generated data frame of workflows to list of dictionaries.

    Parameters:
    synthetic_workflows - data frame of synthetically generated workflows
    workflow_steps - list of workflow steps taken into account in conversion
    '''
    step_features = ['cpu', 'memory', 'ephemeral_storage', 'duration']
    workflows_dicts = []

    for workflow in synthetic_workflows.to_dict('records'):
        workflow_dict = {key: round(workflow[key]) for key in WORKFLOW_NUMERIC_FEATURES}
        workflow_dict[WORKFLOW_FEATURES.PROCESSOR_TYPE] = workflow[WORKFLOW_FEATURES.PROCESSOR_TYPE]
        workflow_dict['deadline'] = workflow_dict['deadline'] + workflow_dict['duration'] if workflow_dict['deadline'] > 0 else 0
        workflow_dict['steps'] = []
        workflow_end_step = None

        for step in workflow_steps:
            features = [f'{step}_{feature}' for feature in step_features]
            step_dict = {key.split('_')[-1]: round(workflow[key]) if workflow[key] >= 0 else 0 for key in features}
            step_dict['name'] = step

            if step == 'on-exit':
                workflow_end_step = step_dict
            else:
                workflow_dict['steps'].append(step_dict)

        if workflow_end_step:
           workflow_dict['steps'].append(workflow_end_step)

        if(any(workflow_dict[prop] < 0 for prop in WORKFLOW_NUMERIC_FEATURES)):
            continue
        else:
            workflows_dicts.append(workflow_dict)
    
    return workflows_dicts

    
