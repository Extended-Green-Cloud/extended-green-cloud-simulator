import pandas as pd
import numpy as np
import json

from os import path, listdir
from src.models.Workflow import Workflow
from src.models.Order import Order
from typing import List, Tuple
from enum import Enum
from src.helpers.workflow_converter import convert_workflows_to_data_frame
from src.helpers.orders_converter import convert_orders_to_data_frame
from src.helpers.path_reader import PathReader, ORDERS_INPUT_FILE, WORKFLOWS_INPUT_FILE


def import_workflow_data_database(sub_directory = None, is_test: bool = False) -> pd.DataFrame:
    '''
    Method returns database data of client workflows.

    Parameters:
    sub_directory - sub directory in which the data is to be looked for
    is_test - flag indicating if the method should use test path

    Returns: data frame with database workflow records
    '''
    workflows_database_path = PathReader.DATABASE_PATH(sub_directory, is_test)
    database_workflows = pd.read_csv(workflows_database_path, low_memory=False, on_bad_lines='warn')

    return database_workflows


def import_workflow_data(sub_directory = None, is_test: bool = False) -> List[Workflow]:
    '''
    Method imports data from argo json files and .csv database and parse it to predefined workflow model.

    Parameters:
    sub_directory - sub directory in which the data is to be looked for
    is_test - flag indicating if the method should use test path

    Returns: list of Workflow objects
    '''
    workflows_data = PathReader.ARGO_PATH(sub_directory, is_test)
    database = import_workflow_data_database(sub_directory, is_test)
    workflows = []

    for idx, file_name in enumerate(listdir(workflows_data)):

        if file_name.endswith('.json'):
            argo_file = path.join(workflows_data, file_name)

            with open(argo_file) as file:
                argo_data = json.load(file)
                uid = argo_data['metadata']['uid']

                database_data = database[database['workflow_uuid'] == uid].to_dict(
                    'records')
                database_data = database_data[0] \
                    if len(database_data) > 0 else dict()

                workflows.append(Workflow(database_data, **argo_data))
                print(f"Number of processed workflows: {idx}")

    return workflows


def import_orders_from_workflows(workflows: List[Workflow]) -> List[Order]:
    '''
    Method imports data of client orders based on a given set of workflows.

    Parameters:
    workflows - list of workflows that are to be converted into client orders

    Returns: orders data frame
    '''
    unique_orders = np.unique(
        [workflow.metadata.orderId for workflow in workflows])
    workflows_per_order = [
        [workflow for workflow in workflows if workflow.metadata.orderId == order] for order in unique_orders]

    return [Order(workflow_group) for workflow_group in workflows_per_order]


def save_parsed_workflow_data(workflows: List[Workflow], sub_directory = None, is_test: bool = False) -> pd.DataFrame:
    '''
    Method parse workflow data to data frame and saves it into csv.

    Parameters:
    sub_directory - sub directory in which the data is to be looked for
    is_test - flag indicating if the method should use test path

    Returns: workflows data frame
    '''
    file_name = WORKFLOWS_INPUT_FILE if sub_directory == None else '_'.join([sub_directory, WORKFLOWS_INPUT_FILE])

    workflow_df = convert_workflows_to_data_frame(workflows)
    workflow_df.to_csv(PathReader.INPUT_PATH(is_test, file_name))
    return workflow_df


def save_parsed_order_data(orders: List[Order], sub_directory = None, is_test: bool = False) -> pd.DataFrame:
    '''
    Method parse orders data to data frame and saves it into csv.

    Parameters:
    sub_directory - sub directory in which the data is to be looked for
    is_test - flag indicating if the method should use test path

    Returns: orders data frame
    '''
    file_name = ORDERS_INPUT_FILE if sub_directory == None else '_'.join([sub_directory, ORDERS_INPUT_FILE])

    orders_df = convert_orders_to_data_frame(orders)
    orders_df.to_csv(PathReader.INPUT_PATH(is_test, file_name))
    return orders_df


def import_workflows_from_csv(prefix = None, is_test: bool = False) -> pd.DataFrame:
    '''
    Method returns parsed data of client workflows in form of the data frame.

    Parameters:
    prefix - optional prefix attached to the file name
    is_test - flag indicating if the method should use test path

    Returns: workflows data frame
    '''
    file_name = WORKFLOWS_INPUT_FILE if prefix == None else '_'.join([prefix, WORKFLOWS_INPUT_FILE])

    return pd.read_csv(PathReader.INPUT_PATH(is_test, file_name))


def import_orders_from_csv(prefix = None, is_test: bool = False) -> pd.DataFrame:
    '''
    Method returns parsed data of client orders in form of the data frame.

    Parameters:
    prefix - optional prefix attached to the file name
    is_test - flag indicating if the method should use test path

    Returns: orders data frame
    '''
    file_name = ORDERS_INPUT_FILE if prefix == None else '_'.join([prefix, ORDERS_INPUT_FILE])

    return pd.read_csv(PathReader.INPUT_PATH(is_test, file_name), index_col=0)


def import_workflows_from_clustering_file(dir_name: str,
                                          file_name: str,
                                          is_test: bool = False) -> pd.DataFrame:
    '''
    Method returns data of client workflows from indicated file.

    Parameters:
    dir_name - name of directory
    file_name - name of the file
    is_test - flag indicating if the method should use test path

    Returns: workflows data frame
    '''
    return pd.read_csv(PathReader.CLUSTERING_PATH(dir_name, file_name, is_test), index_col=0)


def import_workflows_from_json(sub_directory = None, is_test: bool = False) -> Tuple[pd.DataFrame, pd.DataFrame]:
    '''
    Method imports data of client workflows from json files, converts it into data frame,
    saves the result in .csv and returns workflows data frame.

    Parameters:
    sub_directory - sub directory in which the data is to be looked for
    is_test - flag indicating if the method should use test path

    Returns: workflows data frame
    '''
    workflows = import_workflow_data(sub_directory, is_test)
    orders = import_orders_from_workflows(workflows)
    workflows_df = save_parsed_workflow_data(workflows, sub_directory, is_test)
    orders_df = save_parsed_order_data(orders, sub_directory, is_test)

    return workflows_df, orders_df


class WorkflowsImport(Enum):
    def IMPORT_FROM_JSON(sub_directory: str = None): return import_workflows_from_json(sub_directory)[0]
    def IMPORT_FROM_CSV(prefix: str = None): return import_workflows_from_csv(prefix)
    def IMPORT_FROM_FILE(dir_name, file_name): return import_workflows_from_clustering_file(dir_name, file_name)

    def IMPORT_FROM_DB(): return import_workflow_data_database()


class OrdersImport(Enum):
    def IMPORT_FROM_JSON(sub_directory: str = None): return import_workflows_from_json(sub_directory)[1]
    def IMPORT_FROM_CSV(prefix: str = None): return import_orders_from_csv(prefix)
