from os import path, mkdir, getenv
from enum import Enum
from dotenv import load_dotenv

WORKFLOWS_INPUT_FILE = 'input_workflows.csv'
ORDERS_INPUT_FILE = 'input_orders.csv'
WORKFLOWS_DATABASE_FILE = 'database.csv'

SYNTHETIC_SAMPLE_FILE = 'synthetic_workflows.json'

RELATIVE_DATA_PATH = 'data'
RELATIVE_TEST_DATA_PATH = path.join('tests', 'data')

RESULTS_DIR = 'results'
INPUT_DIR = 'input'
SYNTHETIC_DIR = 'synthetic'

EXPLORATORY_RESULTS_DIR = path.join(RESULTS_DIR, 'exploratory-results')
CLUSTERING_RESULTS_DIR = path.join(RESULTS_DIR, 'clustering-results')


def parse_file_name(file_name: str) -> str:
    return file_name.lower().replace(" ", "-")


def get_data_file_path(file_name: str, is_test: bool = False) -> str:
    '''
    Method returns the full path to the file with data (relative to file directory).

    Parameters:
    file_name - name of the file for which the path is to be returned
    is_test - flag indicating if the method should use test path

    Returns: path to stored data files
    '''
    if is_test:
        abs_path = path.abspath(RELATIVE_TEST_DATA_PATH)
        return path.join(abs_path, file_name)

    current_path = path.dirname(__file__)
    file_absolute_path = current_path.replace('helpers', RELATIVE_DATA_PATH)
    return path.join(file_absolute_path, file_name)


def get_file_path_to_argo_data(is_test: bool = False) -> str:
    '''
    Method returns the full path to the directory storing argo files data.

    Parameters:
    is_test - flag indicating if the method should use test path

    Returns: path to the argo workflows directory
    '''

    load_dotenv()
    return getenv('DATA_DIRECTORY') if not is_test else path.abspath(getenv('TEST_DATA_DIRECTORY'))


def get_file_path_to_database_data(is_test: bool = False) -> str:
    '''
    Method returns the full path to the file with workflows database data.

    Parameters:
    is_test - flag indicating if the method should use test path

    Returns: path to the workflows database file
    '''
    workflows_data_dir = get_file_path_to_argo_data(is_test)
    return path.join(workflows_data_dir, WORKFLOWS_DATABASE_FILE)


def get_file_path_to_synthetic_data(is_test: bool = False) -> str:
    '''
    Method returns the full path to the file with synthetically generates workflow data.

    Parameters:
    is_test - flag indicating if the method should use test path

    Returns: path to directory with synthetically generates workflows
    '''

    if not path.exists(get_data_file_path(SYNTHETIC_DIR, is_test)):
        mkdir(get_data_file_path(SYNTHETIC_DIR, is_test))

    relative_file_path = path.join(SYNTHETIC_DIR, SYNTHETIC_SAMPLE_FILE)
    return get_data_file_path(relative_file_path, is_test)


def get_file_path_to_input_data(is_test: bool = False, file_name: str = WORKFLOWS_INPUT_FILE) -> str:
    '''
    Method returns the full path to the file with parsed input workflow data.

    Parameters:
    is_test - flag indicating if the method should use test path
    file_name - name of the file that is to be saved

    Returns: path to directory with parsed workflows input
    '''

    if not path.exists(get_data_file_path(INPUT_DIR, is_test)):
        mkdir(get_data_file_path(INPUT_DIR, is_test))

    relative_file_path = path.join(INPUT_DIR, file_name)
    return get_data_file_path(relative_file_path, is_test)


def get_file_path_to_exploratory_results(file_name: str, is_test: bool = False) -> str:
    '''
    Method returns the full path to the file with exploratory analysis results.

    Parameters:
    file_name - name of the file for which the path is to be returned
    is_test - flag indicating if the method should use test path

    Returns: path to directory with exploratory results
    '''

    if not path.exists(get_data_file_path(RESULTS_DIR, is_test)):
        mkdir(get_data_file_path(RESULTS_DIR, is_test))

    if not path.exists(get_data_file_path(EXPLORATORY_RESULTS_DIR, is_test)):
        mkdir(get_data_file_path(EXPLORATORY_RESULTS_DIR, is_test))

    relative_file_path = path.join(EXPLORATORY_RESULTS_DIR, file_name)
    return get_data_file_path(relative_file_path, is_test)


def get_file_path_to_clustering_results(dir_name: str, file_name: str, is_test: bool = False) -> str:
    '''
    Method returns the full path to the file with clustering results.

    Parameters:
    dir_name - name of results for given clustering
    file_name - name of the file for which the path is to be returned
    is_test - flag indicating if the method should use test path

    Returns: path to directory storing clustering results
    '''

    if not path.exists(get_data_file_path(RESULTS_DIR, is_test)):
        mkdir(get_data_file_path(RESULTS_DIR, is_test))

    if not path.exists(get_data_file_path(CLUSTERING_RESULTS_DIR, is_test)):
        mkdir(get_data_file_path(CLUSTERING_RESULTS_DIR, is_test))

    path_to_results = path.join(CLUSTERING_RESULTS_DIR, dir_name)
    if not path.exists(get_data_file_path(path_to_results, is_test)):
        mkdir(get_data_file_path(path_to_results, is_test))

    relative_file_path = path.join(path_to_results, file_name)
    return get_data_file_path(relative_file_path, is_test)


class PathReader(Enum):
    def INPUT_PATH(is_test, file_name=WORKFLOWS_INPUT_FILE): return get_file_path_to_input_data(
        is_test, file_name)

    def ARGO_PATH(is_test): return get_file_path_to_argo_data(is_test)
    def DATABASE_PATH(is_test): return get_file_path_to_database_data(is_test)

    def CLUSTERING_PATH(dir_name, file_name, is_test=False): return get_file_path_to_clustering_results(
        dir_name, file_name, is_test)

    def EXPLORATORY_PATH(
        file_name, is_test=False): return get_file_path_to_exploratory_results(file_name, is_test)

    def SYNTHETIC_PATH(
        is_test=False): return get_file_path_to_synthetic_data(is_test)
