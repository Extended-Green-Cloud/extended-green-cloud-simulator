import numpy as np
import pandas as pd

from typing import List, Dict
from src.helpers.value_reader import FORMATTER
from src.helpers.scaler import Scaler

def get_cluster_statistics(data: pd.DataFrame,
                           statistic_fields: List[str],
                           features: List[str],
                           labels: List[int]) -> Dict[int, pd.DataFrame]:
    '''
    Method returns dictionary with aggregated statistics of each cluster.

    Parameters:
    data - clustered data
    statistic_fields - statistics that are to be computed
    features - list of features names for which statistics are to be computed
    labels - list of clustering labels

    Returns: dictionary of statistics per each label
    '''
    data_copy = data.copy()
    data_copy['label'] = data_copy['label'].astype(str) 
    resulting_statistics = dict()

    for label in np.unique(labels):
        workflows_df_label = get_workflows_for_label(data_copy, label)

        resulting_statistics[label] = workflows_df_label[features]\
            .describe(include="all").apply(FORMATTER).loc[statistic_fields]

    return resulting_statistics

def get_cluster_resource_encoding(stats_per_label: pd.DataFrame, features: List[str]) -> str:
    '''
    Method encodes given label based on the average cluster values.

    Parameters:
    stats_per_label - statistics of the cluster corresponding to the given label
    features - list of features used in clustering

    Returns: encoded point
    '''
    return ''.join(['1' if float(stats_per_label[feature]['mean']) > 0.6 else '0' for feature in features])


def get_encoded_cluster_resources(data: pd.DataFrame, labels: List[str], features: List[str]) -> dict:
    '''
    Method returns data that was clustered with an additional column representing the cluster encoding.
    In particular, it uses the approach from: https://www.sciencedirect.com/science/article/pii/S0020025520304588.
    For each feature used in clustering, it takes its normalized value and if it is > 0.6 then it encodes it as 1 otherwise as 0.
    Encoded values form a single vector.

    Parameters:
    data - clustered data
    labels - list of final labels
    features - list of features used in clustering

    Returns: encoded clusters
    '''
    data_copy = data.copy()
    data_copy[features] = Scaler.MIN_MAX(data_copy[features])

    statistics = ['mean']
    stats_per_label = get_cluster_statistics(data_copy, statistics, features, data['label'])

    cluster_encoding = {str(label): get_cluster_resource_encoding(stats_per_label[label], features) for label in labels}
    
    return cluster_encoding


def get_points_for_cluster(data_labels: List[int], cluster_label: int) -> List[int]:
    '''
    Method returns indexes of data points from a given cluster.

    Parameters:
    data_labels - set of cluster labels assigned for each data point
    cluster_label - cluster label for which data points are to be retrieved

    Returns: list of indexes of data points assigned with a given label
    '''
    return [idx for idx in range(len(data_labels)) if data_labels[idx] == cluster_label]


def get_data_for_points_in_cluster(data: np.ndarray, data_labels: List[int], cluster_label: int) -> List[int]:
    '''
    Method returns data for points assigned to the given cluster.

    Parameters:
    data - data points that were clustered
    data_labels - set of cluster labels assigned for each data point
    cluster_label - cluster label for which data points are to be retrieved

    Returns: list of data for points assigned to the given cluster
    '''
    return [data[idx] for idx in range(len(data_labels)) if data_labels[idx] == cluster_label]


def create_cluster_with_point_or_append_point(point: int, cluster: int, existing_clusters: dict) -> List:
    '''
    Method adds new point to the cluster or creates a cluster if it has not previously existed.

    Parameters:
    point - new point that is to be added to the cluster
    cluster - index of the cluster to which the point is to be added
    existing_clusters - list of currently existing clusters

    Returns: cluster with appended point
    '''
    return [point] if cluster not in existing_clusters else existing_clusters[cluster] + [point]


def get_df_with_cluster_labels(data_frame: pd.DataFrame, labels: List[int]) -> pd.DataFrame:
    '''
    Method assigns labels to a given data frame.

    Parameters:
    data_frame - data frame to which the labels are to be assigned
    labels - list of labels 

    Returns: data frame with assigned clustering labels
    '''
    data_frame['label'] = 0

    for i in np.unique(labels):
        indexes = [idx for idx in range(len(labels)) if labels[idx] == i]
        data_frame.iloc[indexes, -1] = i

    return data_frame

def get_data_for_all_clusters(data: pd.DataFrame,
                                 labels: List[str]) -> dict:
    '''
    Method returns dictionary with clusters and assigned data points.

    Parameters:
    data - clustered data
    labels - list of unique labels

    Return: dictionary of labels and assigned data points
    '''
    data_copy = data.copy()
    data_copy['label'] = data_copy['label'].astype(str)

    return {str(label): get_workflows_for_label(data_copy, label).to_dict('records') for label in labels} 


def get_workflows_for_label(workflow_df: pd.DataFrame,
                            label: str | int) -> pd.DataFrame:
    '''
    Method returns data frame that contains only workflows for given label.

    Parameters:
    workflows_df - data frame with all workflows
    label - label of workflows of interest

    Return: data frame with filtered labels
    '''
    return workflow_df[(workflow_df['label'] == str(int(label)))]
