import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

from typing import List, Any

from src.helpers.dimensionality_reducer import DimensionalityReducer
from src.helpers.scaler import Scaler

from src.clustering.clustering_evaluation import ClusteringMetrics, print_clustering_metrics
from src.clustering.clustering_methods import ClusteringMethod
from src.helpers.parameter_concatenator import concatenate_parameters


def plot_metrics_per_cluster_no(evaluation_data: List, evaluation_metric_name: str, cluster_range: List[int]) -> None:
    '''
    Method displays a plot of given clustering evaluation metric against different clusters number.

    Parameters:
    evaluation_data - clustering evaluation metrics data
    evaluation_metric_name - name of computed evaluation metric
    cluster_range - range of clusters number for which clustering was performed
    '''
    plt.figure(figsize=(5, 5))
    plt.plot(cluster_range, evaluation_data, marker="s", linestyle="dotted")
    plt.title(f'{evaluation_metric_name} per clusters no.')
    plt.xlabel('Clusters no.')
    plt.ylabel(f'{evaluation_metric_name}')
    plt.show()


def testing_k_means_clustering(data: np.ndarray, cluster_range: List[int], plot: bool = True) -> None:
    '''
    Method tests results of K-means clustering for different numbers of clusters.

    Parameters:
    data - data used in clustering
    cluster_range - range od clusters of which K-means is to be tested
    '''
    inertia = []

    for cluster_no in cluster_range:
        _, estimator, labels = ClusteringMethod.K_MEANS(data, cluster_no)

        inertia.append(estimator.inertia_)
        print_clustering_metrics([ClusteringMetrics.SILHOUETTE], data, labels)

    if plot:
        plot_metrics_per_cluster_no(inertia, 'Inertia', cluster_range)


def testing_fuzzy_c_means_clusters_no(data: np.ndarray, cluster_range: List[int], plot: bool = True) -> None:
    '''
    Method tests results of Fuzzy C-Means clustering for different numbers of clusters.

    Parameters:
    data - data used in clustering
    cluster_range - range od clusters of which K-means is to be tested
    '''
    fpcs = []

    for cluster_no in cluster_range:
        u, cntr, fpc, labels = ClusteringMethod.FUZZY_C_MEANS(data, cluster_no)
        _, xie_beni = ClusteringMetrics.XIE_BENI(data, 2, u, cntr, cluster_no)

        fpcs.append(xie_beni)

        print_clustering_metrics([ClusteringMetrics.SILHOUETTE], data, labels)
        print(f'Partition Coefficient: {fpc}')
        print(f'Xie-Beni Index: {xie_beni}')

    if plot:
        plot_metrics_per_cluster_no(
            fpcs, 'Partition Coefficient', cluster_range)


def testing_ICCM_clustering(data: pd.DataFrame,
                            algorithms: List[ClusteringMethod],
                            algorithms_params: List,
                            max_clusters: int,
                            test_params: np.ndarray) -> None:
    '''
    Method test results of ICCM clustering with PCA on different number of clusters.

    Parameters:
    data - data used in clustering
    algorithms - list of clustering algorithms used in ICCM
    algorithms_params - list of clustering params
    max_clusters - maximal number of clusters among parallel clustering algorithms
    test_params - parameters on which clustering algorithm is to be tested
    '''
    sub_clusters, sub_clusters_centers = \
        ClusteringMethod.ICCM_DATA(
            data, algorithms, algorithms_params, max_clusters)

    for param_set in test_params:
        labels = ClusteringMethod.ICCM_CLUSTERS(
            data, sub_clusters, sub_clusters_centers, *param_set)

        print(f'\nParameters: {param_set}')
        print_clustering_metrics([ClusteringMetrics.SILHOUETTE,
                                  ClusteringMetrics.CALINSKI,
                                  ClusteringMetrics.DAVIES],
                                 data, labels)


def testing_HDBSCAN_clustering(data: pd.DataFrame, params: np.ndarray) -> None:
    '''
    Method test results of HDBSCAN clustering with PCA on different number of clusters.

    Parameters:
    data - data used in clustering
    params - parameters on which clustering algorithm is to be tested
    '''
    for param_set in params:
        labels = ClusteringMethod.HDBSCAN(data, *param_set, only_labels=True)

        print(f'\nParameters: {param_set}')
        print_clustering_metrics([ClusteringMetrics.SILHOUETTE,
                                  ClusteringMetrics.CALINSKI,
                                  ClusteringMetrics.DAVIES],
                                 data, labels)


def testing_IClust_clustering(data: pd.DataFrame, multipliers_params: np.ndarray) -> None:
    '''
    Method test results of IClust clustering with PCA for different parameters.

    Parameters:
    data - data used in clustering
    multipliers_params - list of multipliers used in IClust
    '''
    for multiplier in multipliers_params:
        labels = ClusteringMethod.ICLUST(data, multiplier)

        print(f'\nMultiplier: {multiplier}')
        print_clustering_metrics([ClusteringMetrics.SILHOUETTE,
                                  ClusteringMetrics.CALINSKI,
                                  ClusteringMetrics.DAVIES],
                                 data, labels)


def testing_clustering_with_silhouette(data: np.ndarray,
                                       cluster_range: List[int],
                                       clustering_method: ClusteringMethod,
                                       plot: bool = True) -> None:
    '''
    Method tests results of clustering for different numbers of clusters.

    Parameters:
    data - data used in clustering
    cluster_range - range od clusters number
    clustering_methods - method used for clustering
    '''
    silhouettes = []

    for cluster_no in cluster_range:
        labels = clustering_method(data, cluster_no, only_labels=True)
        silhouette = ClusteringMetrics.SILHOUETTE(data, labels)

        silhouettes.append(silhouette)
        print_clustering_metrics([ClusteringMetrics.SILHOUETTE], data, labels)

    if plot:
        plot_metrics_per_cluster_no(silhouettes, 'Silhouette', cluster_range)


class TestingClusteringParameters():
    '''
    Class used for evaluating clustering results for different parameters. 
    '''

    def __init__(self,
                 clustering_method: ClusteringMethod,
                 dim_reduction_type: DimensionalityReducer) -> None:
        '''
        Method initialize the class by selecting testing methods according to passed clustering types.

        Parameters:
        clustering_method - type of clustering that is to be tested
        dim_reduction_type - type of dimensionality reduction that is used
        '''
        self.name = clustering_method.__name__
        self.cluster_range_PCA = range(2, 15)
        self.cluster_range_UMAP = range(2, 15, 2)
        self.dimensionality_reduction = dim_reduction_type
        self.method = clustering_method

    def test(self, data: np.ndarray, *args: Any) -> None:
        '''
        Method tests clustering against different parameters

        Parameters:
        data - data used in clustering
        args - optional parameters passed to test method
        '''
        if self.dimensionality_reduction == DimensionalityReducer.PCA:
            self.test_PCA(data, args)
        else:
            self.test_UMAP(data, args)

    def test_PCA(self, data: np.ndarray, *args: Any) -> None:
        '''
        Method evaluates clustering results for different parameters with PCA reduction. 

        Parameters:
        data - data used in clustering
        args - optional parameters passed to test method
        '''
        reduced_workflows = DimensionalityReducer.PCA(Scaler.STANDARD, data)
        self.call_clustering_test(reduced_workflows, *args,
                                  cluster_range=self.cluster_range_PCA)

    def test_UMAP(self, data: np.ndarray, *args: Any) -> None:
        '''
        Method evaluates clustering results for different parameters with UMAP reduction. 

        Parameters:
        data - data used in clustering
        args - optional parameters passed to test method
        '''
        NEIGHBORS_RANGE = [500, 700, 1000]
        MIN_DIST_RANGE = [0.1, 0.2, 0.5]
        params = concatenate_parameters(NEIGHBORS_RANGE, MIN_DIST_RANGE)

        if self.method in [ClusteringMethod.ICCM, ClusteringMethod.ICLUST, ClusteringMethod.HDBSCAN]:
            print(
                f'Algorithm {self.name} does not support clustering with UMAP.')
            return

        for param_set in params:
            reduced_workflows = \
                DimensionalityReducer.UMAP(Scaler.STANDARD, data, *param_set)

            print(f'{self.name} results for UMAP params: {param_set}')
            self.call_clustering_test(reduced_workflows, *args,
                                      cluster_range=self.cluster_range_UMAP)

    def call_clustering_test(self, data: np.ndarray, *args, cluster_range: List[int]) -> None:
        '''
        Method invokes clustering results testing. 

        Parameters:
        data - data used in clustering
        args - optional parameters passed to test method
        cluster_range - range od clusters of which K-means is to be tested
        '''
        if self.method in [ClusteringMethod.BIRCH, ClusteringMethod.GMM]:
            testing_clustering_with_silhouette(
                data, cluster_range, self.method)

        elif self.method == ClusteringMethod.K_MEANS:
            testing_k_means_clustering(data, cluster_range)

        elif self.method == ClusteringMethod.FUZZY_C_MEANS:
            testing_fuzzy_c_means_clusters_no(data, cluster_range)

        elif self.method == ClusteringMethod.HDBSCAN:
            MIN_CLUSTER_SIZE_RANGE = [5, 100, 500, 1000]
            MIN_SAMPLE_RANGE = [300, 500, 1000, 2000]
            DISTANCE_RANGE = [0.3, 0.5, 0.7]
            DISTANCE_METRIC = ['manhattan', 'euclidean']

            params = concatenate_parameters(MIN_CLUSTER_SIZE_RANGE,
                                            MIN_SAMPLE_RANGE,
                                            DISTANCE_RANGE,
                                            DISTANCE_METRIC)
            testing_HDBSCAN_clustering(data, params)

        elif self.method == ClusteringMethod.ICCM:
            CENTER_CLUSTERING_METHODS = [
                ClusteringMethod.K_MEANS,
                ClusteringMethod.FUZZY_C_MEANS
            ]
            params = concatenate_parameters(cluster_range,
                                            CENTER_CLUSTERING_METHODS)

            testing_ICCM_clustering(data, *args, test_params=params)

        elif self.method == ClusteringMethod.ICLUST:
            multiplier_range = [1.7, 2, 3, 5]
            testing_IClust_clustering(data, multiplier_range)
