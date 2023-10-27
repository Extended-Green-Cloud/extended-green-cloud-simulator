import pandas as pd
import numpy as np

from typing import List
from src.helpers.feature_encoder import WORKFLOW_FEATURES, DB_FEATURES


WORKFLOW_RESOURCE_FEATURES = [
    WORKFLOW_FEATURES.CPU,
    WORKFLOW_FEATURES.MEMORY,
    WORKFLOW_FEATURES.EPHEMERAL_STORAGE,
    WORKFLOW_FEATURES.STORAGE,
]

WORKFLOW_NON_RESOURCE_FEATURES = [
    WORKFLOW_FEATURES.DURATION,
    WORKFLOW_FEATURES.PROCESSED_SIZE,
    WORKFLOW_FEATURES.DEADLINE,
    WORKFLOW_FEATURES.PRIORITY
]


def filter_workflows_by_label(workflow_df: pd.DataFrame,
                              label: str or int) -> pd.DataFrame:
    '''
    Method returns data frame that contains only workflows for given label.

    Parameters:
    workflows_df - data frame with all workflows
    label - label of workflows of interest

    Return: data frame with filtered labels
    '''
    if workflow_df['label'].dtype == np.int64:
        return workflow_df[(workflow_df['label'] == int(label))]
    else:
        return workflow_df[(workflow_df['label'] == str(int(label)))]


def filter_workflows_steps_features(df: pd.DataFrame) -> pd.DataFrame:
    '''
    Method filters out from the data frame features related to workflow steps that are unnecessary with respect to given clustering.

    Parameters:
    df - workflows data frame

    Returns: data frame with filtered columns
    '''
    if WORKFLOW_FEATURES.WORKFLOW_STEPS not in list(df.columns):
        return df

    workflow_steps = np.unique(list(df[WORKFLOW_FEATURES.WORKFLOW_STEPS]))
    steps_names = [el for steps in workflow_steps for el in steps.split('>')]

    PREFIX = tuple(steps_names)
    SUFFIX = ('_cpu', '_memory', '_ephemeral_storage', '_duration')

    ALL_STEPS_COLUMNS = [col for col in df.columns if col.endswith(SUFFIX)]
    INCLUDED_STEPS_COLUMNS = \
        [col for col in df.columns if col in ALL_STEPS_COLUMNS and col.startswith(
            PREFIX)]
    EXCLUDED_COLS = \
        set(ALL_STEPS_COLUMNS).difference(set(INCLUDED_STEPS_COLUMNS))

    return df.loc[:, ~df.columns.isin(EXCLUDED_COLS)]


def filter_out_test_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method removes test records from the data frame.

    Parameters:
    data - workflows data frame

    Returns: modified data frame
    '''
    return data[(~data[WORKFLOW_FEATURES.ORDER_NAME].str.contains('test', case=False)) &
                (data[WORKFLOW_FEATURES.ORDER_NAME] != 'frsdf') &
                (data[WORKFLOW_FEATURES.ORDER_NAME] != 'resr')]


def filter_out_undefined_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method filters out workflows data that do not have database records.

    Parameters:
    data - database data frame
    '''
    return data[data[DB_FEATURES.ORDER_NAME] != 'undefined']


def filter_by_undefined_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method selects workflows data that do not have database records.

    Parameters:
    data - database data frame
    '''
    return data[data[DB_FEATURES.ORDER_NAME] == 'undefined']


def filter_out_download_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method removes from the data frame workflows of type download.

    Parameters:
    data - workflows data frame

    Returns: modified data frame
    '''
    return data[~data[WORKFLOW_FEATURES.PROCESSOR_TYPE].str.contains('download')]


def filter_by_download_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method selects from the data frame only workflows of type download.

    Parameters:
    data - workflows data frame

    Returns: modified data frame
    '''
    return data[data[WORKFLOW_FEATURES.PROCESSOR_TYPE].str.contains('download')]


def filter_by_numerical_features(data: pd.DataFrame, step_names: List[str]) -> pd.DataFrame:
    '''
    Method selects workflows columns that are numerical.

    Parameters:
    data - workflows data frame
    step_names - list of steps taken into account

    Returns: data frame with filtered columns
    '''
    SUFFIX = ('_cpu', '_memory', '_ephemeral_storage', '_duration')
    STEPS_COLUMNS = [col for col in data.columns 
                     if col.endswith(SUFFIX) and col.startswith(tuple(step_names))]

    NUMERIC_COLUMNS = STEPS_COLUMNS + WORKFLOW_RESOURCE_FEATURES + WORKFLOW_NON_RESOURCE_FEATURES
    return data[NUMERIC_COLUMNS]
