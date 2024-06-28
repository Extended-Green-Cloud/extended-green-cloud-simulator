import pandas as pd

from typing import List, Tuple

class ARGO_STATUS:
    SUCCEEDED = 'Succeeded'
    ERROR = 'Error'
    FAILED = 'Failed'

class DETAILED_STATUS:
    UNDEFINED = 'undefined'
    STOP_STRATEGY = 'stopped with strategy "stop"'
    LEADER = 'leader changed'
    NO_RETRIES = 'no more retries left'
    TIME_OUT = 'request timed out'
    NOT_FOUND = 'resource not found'

class OUTPUT_STATUS:
    UNDEFINED = 'undefined error'
    SUCCEEDED = 'product processed successfully'

class ORDER_ITEM_STATUS:
    UNDEFINED = 'undefined'
    SUCCEEDED = 'done'
    CANCELLED = 'cancelled'
    FAILED = 'failed'
    DOWNLOAD = 'downloading'

class DB_FEATURES:
    WORKFLOW_UID = 'workflow_uuid'
    ORDER_NAME = 'order_name'
    ORDER_ID = 'order_id'
    ORDER_ITEM_ID = 'id'
    ORDER_ITEM_STATUS = 'status'
    ORDER_STATUS = 'status.1'
    EXTRA_INFO = 'extra_info'


class WORKFLOW_FEATURES:
    WORKFLOW_UID = 'uid'
    ORDER_NAME = 'order_name'
    ORDER_NAME_CODE = 'order_name_code'
    PROCESSOR_TYPE = 'processor_name'
    PROCESSOR_TYPE_GROUP = 'processor_name_group'
    PROCESSOR_TYPE_CODE = 'processor_name_code'
    PROCESSOR_TYPE_GROUP_CODE = 'processor_name_group_code'
    ORDER_ITEM_STATUS = 'status'
    ORDER_ITEM_STATUS_CODE = 'status_code'
    ORDER_STATUS = 'order_status'
    ORDER_STATUS_CODE = 'order_status_code'
    ORDER_ID = 'order_id'
    ARGO_STATUS = 'argo_status'
    ARGO_STATUS_CODE = 'argo_status_code'
    ARGO_OUTPUT_MSG = 'argo_output_message'
    ARGO_OUTPUT_MSG_CODE = 'argo_output_message_code'
    ARGO_STATUS_DETAILS = 'argo_detailed_status'
    ARGO_STATUS_DETAILS_CODE = 'argo_detailed_status_code'
    CPU = 'cpu'
    MEMORY = 'memory'
    EPHEMERAL_STORAGE = 'ephemeral_storage'
    STORAGE = 'storage'
    PROCESSED_SIZE = 'processed_size'
    DURATION = 'duration'
    DEADLINE = 'deadline'
    PRIORITY = 'priority'
    STEPS_NO = 'initial_steps_no'
    EXECUTED_STEPS_NO = 'executed_steps_no'
    WORKFLOW_STEPS = 'workflow_steps'
    WORKFLOW_STEPS_ENCODED = 'workflow_steps_encoded'
    WORKFLOW_STEPS_ENCODED_CODE = 'workflow_steps_encoded_code'
    NODES = 'nodes_per_step'
    NODES_ENCODED = 'nodes_per_step_encoded'
    NODES_ENCODED_CODE = 'nodes_per_step_encoded_code'
    STEPS_STATUSES = 'status_per_step'
    STEPS_STATUSES_ENCODED = 'status_per_step_encoded'
    STEPS_STATUSES_ENCODED_CODE = 'status_per_step_encoded_code'


class ORDER_FEATURES:
    ORDER_ID = 'order_id'
    ORDER_NAME = 'order_name'
    ORDER_NAME_CODE = 'order_name_code'
    ORDER_STATUS = 'order_status'
    ORDER_STATUS_CODE = 'order_status_code'
    CPU = 'cpu'
    MEMORY = 'memory'
    EPHEMERAL_STORAGE = 'ephemeral_storage'
    STORAGE = 'storage'
    PROCESSED_SIZE = 'processed_size'
    DURATION = 'duration'
    WORKFLOW_NO = 'workflow_no'


FEATURES_DISPLAY_NAMES = {
    DB_FEATURES.ORDER_ITEM_STATUS: 'Status',
    DB_FEATURES.ORDER_STATUS: 'Order status',
    DB_FEATURES.ORDER_ID: 'Order ID',
    DB_FEATURES.ORDER_NAME: 'Order name',
    DB_FEATURES.ORDER_ITEM_ID: 'ID',
    DB_FEATURES.EXTRA_INFO: 'Detailed database information',
    WORKFLOW_FEATURES.ARGO_STATUS: 'Final status argo',
    WORKFLOW_FEATURES.ARGO_STATUS_DETAILS: 'Detailed argo message',
    WORKFLOW_FEATURES.PROCESSOR_TYPE: 'Processor name',
}

CATEGORICAL_FEATURES_PREFIXES = [
    WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE,
    WORKFLOW_FEATURES.ORDER_NAME_CODE,
    WORKFLOW_FEATURES.ARGO_STATUS_CODE,
    WORKFLOW_FEATURES.ORDER_STATUS_CODE,
    WORKFLOW_FEATURES.NODES_ENCODED_CODE,
    WORKFLOW_FEATURES.PROCESSOR_TYPE_CODE,
    WORKFLOW_FEATURES.ORDER_ITEM_STATUS_CODE,
    WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE,
    WORKFLOW_FEATURES.STEPS_STATUSES_ENCODED_CODE,
    WORKFLOW_FEATURES.WORKFLOW_STEPS_ENCODED_CODE
]

def one_hot_encode_feature(feature_name: str, df: pd.DataFrame, prefix: str = None) -> pd.DataFrame:
    '''
    Method adds to the data frame the one-hot-encoded values of given feature.

    Parameters:
    feature_name - name of the feature that is to be encoded
    df - data frame that is to be extended
    prefix - prefix added to the column name

    Returns: updated data frame
    '''
    encoded_columns = pd.get_dummies(df[feature_name], prefix=prefix, dtype=float)
    new_df = pd.concat([df, encoded_columns], axis=1)
    return new_df


def factorize_feature(feature_name: str, df: pd.DataFrame) -> pd.DataFrame:
    '''
    Method adds to the data frame the factorized value of given feature.

    Parameters:
    feature_name - name of the feature that is to be factorized
    df - data frame that is to be extended

    Returns: updated data frame
    '''
    df[f'{feature_name}_code'] = pd.factorize(df[feature_name])[0]
    return df


def encode_categorical_list(unique_values: List[str], categorical_list: List[object], category_name: str) -> Tuple[str, str]:
    '''
    Method encodes list of categorical values to a single string.

    Parameters:
    unique_values - list of unique values
    categorical_list - list of objects which are to be encoded
    category_name - key of the category for which values are to be retrieved

    Returns: values and encoded values for given combination
    '''
    indexes = dict((name, idx) for idx, name in enumerate(unique_values))
    category_values = [element[category_name] if type(element) == dict
                       else element.__dict__[category_name] for element in categorical_list]
    encoded_values = [str(indexes[value]) for value in category_values]

    return '>'.join(category_values), '>'.join(encoded_values)

def get_encoded_column_name(prefix: str,  detailed_name: str) -> str:
    '''
    Method returns full encoded column name.

    Parameters:
    prefix - prefix of the column name
    detailed_name - detailed encoded feature name

    Returns: full column name
    '''
    return '_'.join([prefix, detailed_name])

def get_all_encoded_column_names(prefix: str, df: pd.DataFrame) -> List[str]:
    '''
    Method returns names of all columns with one-hot encoded values that match given prefix.

    Parameters:
    prefix - prefix of the column name
    df - data frame for which columns are to be selected

    Returns: list of column names
    '''
    return [name for name in df.columns if name.startswith(prefix)]

def get_all_column_names_for_features(features: List[str], df: pd.DataFrame) -> List[str]:
    '''
    Method returns names of all columns (including with one-hot encoded values columns)
    for given list of features.

    Parameters:
    features - list of features
    df - data frame for which columns are to be selected

    Returns: list of column names
    '''
    column_names = []

    for feature in features:
        if feature not in CATEGORICAL_FEATURES_PREFIXES:
            column_names.append(feature)
        else:
            column_names.extend(get_all_encoded_column_names(feature, df))

    return column_names