import pandas as pd

from enum import Enum
from typing import Any
from src.helpers.feature_encoder import WORKFLOW_FEATURES, DETAILED_STATUS, OUTPUT_STATUS, ARGO_STATUS, ORDER_ITEM_STATUS, get_encoded_column_name
from src.helpers.statistics_operations import filter_out_undefined_workflows

def add_new_error_output_column(df: pd.DataFrame, detailed_name: str, criteria: Any) -> pd.DataFrame:
    '''
    Helper method used to create new encoded ARGO_OUTPUT_MSG column.
    Initially, values for the column are set to 0.0 unless given criteria are met.

    Parameters:
    df - data frame to which column is to be appended
    detailed_name - name appended to ARGO_OUTPUT_MSG_CODE prefix of newly created column
    criteria - criteria applied to set values of newly created

    Returns: modified data frame
    '''
    new_column_name = get_encoded_column_name(WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE, detailed_name)

    df[new_column_name] = 0.0
    df.loc[criteria, [new_column_name, WORKFLOW_FEATURES.ARGO_OUTPUT_MSG]] = [1.0, detailed_name]
    
    return df


def merge_argo_statuses(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method change selected argo output statuses to argo detail statuses.

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()

    stopped_by_k8_column = get_encoded_column_name(WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE, DETAILED_STATUS.STOP_STRATEGY)
    stopped_by_k8_criteria = (new_data[stopped_by_k8_column] == 1.0)

    new_data = add_new_error_output_column(new_data, DETAILED_STATUS.STOP_STRATEGY, stopped_by_k8_criteria)
    
    OTHER_ERROR_COLUMNS = [DETAILED_STATUS.LEADER, DETAILED_STATUS.NO_RETRIES, DETAILED_STATUS.NOT_FOUND, DETAILED_STATUS.TIME_OUT]
    undefined_error_column = get_encoded_column_name(WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE, OUTPUT_STATUS.UNDEFINED)

    for column_name in OTHER_ERROR_COLUMNS:
        error_column_name = get_encoded_column_name(WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE, column_name)
        error_criteria = (new_data[error_column_name] == 1.0) & (new_data[undefined_error_column] == 1.0)

        if len(new_data[error_criteria]) > 0:
            new_data = add_new_error_output_column(new_data, column_name, error_criteria)

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

def merge_all_card_coh_into_one_type(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method substitutes processor types of all workflows belonging to 'card coh' cluster to 'card_coh'

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()
    
    new_data[WORKFLOW_FEATURES.PROCESSOR_TYPE] = new_data[WORKFLOW_FEATURES.PROCESSOR_TYPE].replace('card_coh12_public', 'card_coh')
    new_data[WORKFLOW_FEATURES.PROCESSOR_TYPE] = new_data[WORKFLOW_FEATURES.PROCESSOR_TYPE].replace('coh', 'card_coh')

    return new_data

def take_only_succeeded(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method extracts from the data frame, only the workflows which execution fully succeeded

    Parameters:
    data - data frame

    Returns: modified data frame
    '''
    new_data = data.copy()
    
    argo_succeeded_column = get_encoded_column_name(WORKFLOW_FEATURES.ARGO_STATUS_CODE, ARGO_STATUS.SUCCEEDED)
    succeeded_output_message_column = get_encoded_column_name(WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE, OUTPUT_STATUS.SUCCEEDED)
    succeeded_item_status_column = get_encoded_column_name(WORKFLOW_FEATURES.ORDER_ITEM_STATUS_CODE, ORDER_ITEM_STATUS.SUCCEEDED)

    return new_data[(new_data[argo_succeeded_column] == 1.0) 
                    & (new_data[succeeded_output_message_column] == 1.0) 
                    & (new_data[succeeded_item_status_column] == 1.0)]

class ClusteringPreProcessing(Enum):
    def ONLY_DB_RECORDS(data): return filter_out_undefined_workflows(data)
    def MERGE_STATUSES(data): return merge_argo_statuses(data)
    def FILTER_TEST_WORKFLOWS(data): return filter_out_test_workflows(data)
    def FILTER_OUT_DOWNLOAD_WORKFLOWS(data): return filter_out_download_workflows(data)
    def TAKE_ONLY_DOWNLOAD_WORKFLOWS(data): return take_only_download_workflows(data)
    def MERGE_CARD_COH(data): return merge_all_card_coh_into_one_type(data)
    def ONLY_SUCCEEDED(data): return take_only_succeeded(data)
