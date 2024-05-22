import numpy as np
import pandas as pd

from enum import Enum
from typing import List

from scipy.spatial.distance import euclidean
from sklearn.metrics import silhouette_score, davies_bouldin_score, calinski_harabasz_score
from src.helpers.path_reader import PathReader, parse_file_name


def calculate_xie_beni_index(data: np.ndarray,
                             fuzzier: int,
                             membership_matrix: np.ndarray,
                             center: np.ndarray,
                             cluster_no: int) -> float:
    '''
    Method computes Xie Beni index for given clustering.

    Parameters:
    data - data set containing clustered points
    fuzzier - fuzzier exponential
    membership_matrix - membership matrix of clustered points
    center - cluster centers
    cluster_no - number of clusters

    Returns: xie-beni index
    '''
    weighted_distances = []
    distances_to_minimize = []

    for cluster in range(cluster_no):
        distances = np.array(
            [euclidean(x, center[cluster]) ** 2 for x in data])

        weighted_distances.append(
            (distances * (membership_matrix[cluster] ** fuzzier)))

        distances_to_minimize.append(np.array(
            [euclidean(x, center[cluster]) ** 2 for idx, x in enumerate(center) if idx != cluster]))

    distances_sum = np.sum(weighted_distances)
    min_dist = np.min(distances_to_minimize)

    return distances_sum / (data.shape[0] * min_dist)


class ClusteringMetrics(Enum):
    def SILHOUETTE(data, *args): \
        return 'Silhouette Score', silhouette_score(data, *args)

    def CALINSKI(data, *args): \
        return 'Calinski-Harabasz Score', calinski_harabasz_score(data, *args)

    def DAVIES(data, *args): \
        return 'Davies-Bouldin Score', davies_bouldin_score(data, *args)

    def XIE_BENI(data, *args): \
        return 'Xie-Beni Index', calculate_xie_beni_index(data, *args)
    
    @classmethod
    def get_metric_by_name(cls, name: str):
        if name == 'SILHOUETTE': return cls.SILHOUETTE
        if name == 'CALINSKI': return cls.CALINSKI
        if name == 'DAVIES': return cls.DAVIES
        if name == 'XIE_BENI': return cls.XIE_BENI

        raise AttributeError(f'Metric type {name} is undefined!')


def print_clustering_metrics(metrics: List[ClusteringMetrics],
                             data: np.ndarray,
                             labels: np.ndarray,
                             save_data: bool = False,
                             name: str = None) -> pd.DataFrame:
    '''
    Method computes and prints the clustering metrics indicated in the parameters.

    Parameters:
    metrics - metrics that are to be computed
    data - data based on which metrics are computed
    labels - resulting clustering labels
    save_data - flag indicating if the metrics are to be saved in form of csv
    name - name of the clustering algorithm
    '''
    results = dict()
    cluster_no = len(np.unique(labels))

    print(f'\nCluster no. {cluster_no}')
    results['Cluster no.'] = cluster_no

    for metric in metrics:
        label, result = metric(data, labels)
        print(f'{label}: {result}')

        results[label] = result

    results_df = pd.DataFrame(results, index=[0])

    if save_data:
        save_validation_metrics(results_df, name)

    return results_df


def save_validation_metrics(metrics: pd.DataFrame,
                            name: str,
                            is_test: bool = False) -> None:
    '''
    Method saves the clustering validation metrics in .csv file.

    Parameters:
    metrics - metrics data frame
    name - name of the output file
    is_test - flag indicating if the method should use test path
    '''
    dir_name = parse_file_name(name)
    file_name = f'{dir_name}-validation-metrics.csv'
    metrics.to_csv(PathReader.CLUSTERING_PATH(dir_name, file_name, is_test))
