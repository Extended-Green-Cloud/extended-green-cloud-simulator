from typing import Dict
from src.helpers.value_reader import read_value_or_return_default


class Metadata():
    '''
    Class representing metadata information of workflow
    '''

    def __init__(self, metadata: Dict, db_data: Dict) -> None:
        '''
        Method initialize metadata of the given workflow.

        Parameters:
        metadata - dictionary of metadata obtained from Argo workflow configuration json
        db_data - information about workflow obtained from the database
        '''
        self.uid = metadata['uid']
        self.orderId = metadata['labels']['order-id']
        self.orderItemId = metadata['labels']['order-item-id']
        self.orderName = read_value_or_return_default('order_name', db_data)
