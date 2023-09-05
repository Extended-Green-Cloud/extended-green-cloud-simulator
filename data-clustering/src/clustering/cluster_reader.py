import numpy as np
import pandas as pd

from typing import List


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


def get_cluster_with_appended_point(point: int, cluster: int, existing_clusters: dict) -> List:
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


def get_workflows_for_label(workflow_df: pd.DataFrame,
                            label: str or int) -> pd.DataFrame:
    '''
    Method returns data frame that contains only workflows for given label.

    Parameters:
    workflows_df - data frame with all workflows
    label - label of workflows of interest

    Return: data frame with filtered labels
    '''
    return workflow_df[(workflow_df['label'] == str(int(label)))]
