import pandas as pd

from enum import Enum
from src.helpers.feature_encoder import WORKFLOW_FEATURES
from src.helpers.statistics_operations import filter_out_undefined_workflows


def merge_argo_statuses(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method change selected argo output statuses to argo detail statuses.

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()

    new_data.loc[new_data[WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE]
                 == 1, WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE] = 11
    new_data.loc[new_data[WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE] == 1,
                 WORKFLOW_FEATURES.ARGO_OUTPUT_MSG] = 'stopped with strategy "stop"'

    new_data.loc[(new_data[WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE] != 0) & (
        new_data[WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE] == 4),  WORKFLOW_FEATURES.ARGO_OUTPUT_MSG] = new_data[WORKFLOW_FEATURES.ARGO_STATUS_DETAILS]
    new_data.loc[(new_data[WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE] != 0) & (
        new_data[WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE] == 4), WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE] = 10 + new_data[WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE]

    return new_data


def filter_out_test_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method removes test records from the data frame.

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()
    new_data = new_data[(~new_data[WORKFLOW_FEATURES.ORDER_NAME].str.contains('test', case=False)) &
                        (new_data[WORKFLOW_FEATURES.ORDER_NAME] != 'frsdf') &
                        (new_data[WORKFLOW_FEATURES.ORDER_NAME] != 'resr')]

    return new_data

def filter_out_download_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method filters out workflows of type 'download'

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()
    new_data = new_data[(new_data[WORKFLOW_FEATURES.PROCESSOR_TYPE] != 'download')]

    return new_data

def take_only_download_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method takes only workflows of type 'download'

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()
    new_data = new_data[(new_data[WORKFLOW_FEATURES.PROCESSOR_TYPE] == 'download')]

    return new_data

class ClusteringPreProcessing(Enum):
    def ONLY_DB_RECORDS(data): return filter_out_undefined_workflows(data)
    def MERGE_STATUSES(data): return merge_argo_statuses(data)
    def FILTER_TEST_WORKFLOWS(data): return filter_out_test_workflows(data)
    def FILTER_OUT_DOWNLOAD_WORKFLOWS(data): return filter_out_download_workflows(data)
    def TAKE_ONLY_DOWNLOAD_WORKFLOWS(data): return take_only_download_workflows(data)
