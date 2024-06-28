import pandas as pd
import numpy as np

from typing import List
from src.clustering.clustering_evaluation import ClusteringMetrics
from src.clustering.clustering_methods import ClusteringMethod
from src.helpers.dimensionality_reducer import DimensionalityReducer
from src.helpers.feature_encoder import one_hot_encode_feature, get_all_encoded_column_names
from src.helpers.value_reader import read_value_or_return_default

class REST_INPUT:
    CONFIGURATION = 'configuration'
    NAME = 'name'
    FEATURES = 'features'
    ALL_FEATURES = 'all_features'
    CATEGORICAL_FEATURES = 'categorical_features'
    METHOD = 'method'
    CLUSTERING_METHOD = 'clustering'
    VALIDATION_METHOD = 'validation'
    DIM_REDUCTION_METHOD = 'dimensionality_reduction'
    PARAMETERS = 'parameters'
    CLUSTERING_PARAMETERS = 'clustering'
    DIM_REDUCTION_PARAMETERS = 'dimensionality_reduction'
    DATA = 'data'

MIN_CLUSTERING_SIZE = 2

DEFAULT_CLUSTERING_NAME = 'K-Means clustering'
DEFAULT_CLUSTERING_METHOD = 'K_MEANS'
DEFAULT_DIMENSIONALITY_REDUCTION = 'PCA'
DEFAULT_VALIDATION_METRICS = ['SILHOUETTE', 'CALINSKI', 'DAVIES']
DEFAULT_VISUAL_FEATURES = []
DEFAULT_PRE_PROCESSING = []

def parse_clustering_method(rest_method: dict) -> ClusteringMethod:
    '''
    Method converts string obtained in REST body into ClusteringMethod.

    Parameters:
    rest_method - dictionary of predefined methods

    Returns: ClusteringMethod or K_MEANS as default
    '''
    method_name = read_value_or_return_default(REST_INPUT.CLUSTERING_METHOD, rest_method, DEFAULT_CLUSTERING_METHOD)
    return ClusteringMethod.get_clustering_by_name(method_name)

def parse_validation_metrics(rest_method: str) -> List[ClusteringMetrics]:
    '''
    Method converts string of validation metrics obtained in REST body into a list of ClusteringMetrics.

    Parameters:
    rest_method - dictionary of predefined methods

    Returns: list of ClusteringMetrics or default metrics
    '''
    metrics_names = read_value_or_return_default(REST_INPUT.VALIDATION_METHOD, rest_method, DEFAULT_VALIDATION_METRICS) 
    return [ClusteringMetrics.get_metric_by_name(metric) for metric in metrics_names]

def parse_reduction_method(rest_method: str) -> List[ClusteringMetrics]:
    '''
    Method converts string of dimensionality reduction method obtained in REST body into a DimensionalityReducer.

    Parameters:
    rest_method - dictionary of predefined methods

    Returns: DimensionalityReducer or default method PCA
    '''
    reducer_name = read_value_or_return_default(REST_INPUT.DIM_REDUCTION_METHOD, rest_method, DEFAULT_DIMENSIONALITY_REDUCTION) 
    return DimensionalityReducer.get_reducer_from_name(reducer_name)

def parse_clustering_features(parsed_data: pd.DataFrame, features: List[str], categorical_features: List[str]) -> List[str]:
    '''
    Method returns complete list of feature names that are to be used in clustering.

    Parameters:
    parsed_data - input data used in clustering
    features - user-given features
    categorical_features - user-given categorical features

    Returns: List of features names
    '''
    features_list = list(set(features) - set(categorical_features))

    for feature in categorical_features:
        features_list.extend(get_all_encoded_column_names(f'{feature}_', parsed_data))

    return features_list

def parse_rest_workflow(rest_workflow: dict) -> np.ndarray:
    '''
     Method converts JSON input workflow REST data into a DataFrame.

    Parameters:
    rest_workflow - workflow data

    Returns numeric representation of workflow
    '''
    return np.array(tuple(rest_workflow.values()))
    

def parse_rest_workflows_to_data_frame(rest_workflows: List[dict], literal_features: List[str]) -> pd.DataFrame:
    '''
    Method converts JSON input workflow REST data into a DataFrame. It uses a user-given features list to recognize dictionary keys.

    Parameters:
    rest_workflows - workflows list
    literal_features - features that are to be one-hot encoded

    Returns: DataFrame with REST input data
    '''
    workflows_data = np.array([parse_rest_workflow(workflow) for workflow in rest_workflows])
    
    df = pd.DataFrame(workflows_data, columns=rest_workflows[0].keys())

    for feature in literal_features:
         df = one_hot_encode_feature(feature, df, f'{feature}_code')

    return df




