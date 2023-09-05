
import numpy as np
import pandas as pd

from typing import List
from src.helpers.feature_encoder import ORDER_FEATURES, factorize_feature
from src.models.Order import Order

ORDER_COLUMNS = [
    ORDER_FEATURES.ORDER_ID,
    ORDER_FEATURES.ORDER_NAME,
    ORDER_FEATURES.ORDER_STATUS,
    ORDER_FEATURES.WORKFLOW_NO,
    ORDER_FEATURES.CPU,
    ORDER_FEATURES.MEMORY,
    ORDER_FEATURES.DURATION,
    ORDER_FEATURES.EPHEMERAL_STORAGE,
    ORDER_FEATURES.STORAGE,
    ORDER_FEATURES.PROCESSED_SIZE]


def map_order_processing_details(order: Order) -> np.ndarray:
    '''
    Method returns overall processing details of the given order.

    Parameters:
    order - order which values are mapped

    Returns: numpy array of order processing details
    '''
    processing_details = []

    processing_details.append(order.id)
    processing_details.append(order.name)
    processing_details.append(order.status)
    processing_details.append(order.workflows_no)
    processing_details.append(order.cpu)
    processing_details.append(order.memory)
    processing_details.append(order.duration)
    processing_details.append(order.ephemeral_storage)
    processing_details.append(order.storage)
    processing_details.append(order.processed_size)

    return np.array(processing_details)


def convert_orders_to_data_frame(orders: List[Order]) -> pd.DataFrame:
    '''
    Method returns data frame of processing details for given list of orders.

    Parameters:
    workflows - list of orders for which the processing details are to be retrieved

    Returns: data frame of orders processing details
    '''
    columns = ORDER_COLUMNS
    order_details = np.array(
        [map_order_processing_details(order) for order in orders])

    df = pd.DataFrame(order_details, columns=columns)
    df = factorize_feature(ORDER_FEATURES.ORDER_NAME, df)
    df = factorize_feature(ORDER_FEATURES.ORDER_STATUS, df)

    return df
