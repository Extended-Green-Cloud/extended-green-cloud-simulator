import pandas as pd

from typing import List, Tuple


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

def get_codes_of_feature(data: pd.DataFrame,  feature: str) -> pd.DataFrame:
    '''
    Method returns data frame containing feature of given type,
    its corresponding codes and count.

    Parameters:
    data - data frame
    feature - name of the column taken into account

    Returns: data frame with codes
    '''
    feature_name = WORKFLOW_FEATURES.WORKFLOW_STEPS if feature == WORKFLOW_FEATURES.WORKFLOW_STEPS_ENCODED else feature
    grouped_values = data.groupby(
        [feature_name, f'{feature}_code']).size().to_frame().reset_index()

    name = feature_name.replace('_', ' ').capitalize()
    grouped_values.columns = [name, f'{name} code', 'Count']

    return grouped_values

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
    PROCESSOR_TYPE_CODE = 'processor_name_code'
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
    NODES = 'nodes_per_step'
    NODES_ENCODED = 'nodes_per_step_encoded'
    STEPS_STATUSES = 'status_per_step'
    STEPS_STATUSES_ENCODED = 'status_per_step_encoded'


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
