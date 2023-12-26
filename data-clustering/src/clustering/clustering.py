import pandas as pd
import numpy as np

from typing import List, Tuple
from src.helpers.scaler import Scaler
from src.helpers.statistics_operations import convert_fields_to_numeric

from src.clustering.cluster_reader import get_df_with_cluster_labels
from src.clustering.clustering_methods import ClusteringMethod
from src.clustering.clustering_evaluation import ClusteringMetrics, print_clustering_metrics, save_validation_metrics
from src.clustering.clustering_visualization import display_data_frame_with_labels, display_clustering_scatter_plot, display_cluster_statistics, display_histograms_per_clusters, display_histograms_per_feature_for_clusters
from src.clustering.clustering_pre_processing import ClusteringPreProcessing
from src.helpers.dimensionality_reducer import DimensionalityReducer
from src.helpers.feature_encoder import WORKFLOW_FEATURES, ORDER_FEATURES, get_all_column_names_for_features
from src.clustering.clustering_parameter_selection import TestingClusteringParameters
from enum import Enum

CLUSTERING_WORKFLOW_STATISTICS = [
    [WORKFLOW_FEATURES.ORDER_ITEM_STATUS],
    [WORKFLOW_FEATURES.ORDER_STATUS],
    [WORKFLOW_FEATURES.ARGO_STATUS],
    [WORKFLOW_FEATURES.ARGO_OUTPUT_MSG],
    [WORKFLOW_FEATURES.PROCESSOR_TYPE],
    [
        WORKFLOW_FEATURES.PROCESSOR_TYPE,
        WORKFLOW_FEATURES.ORDER_ITEM_STATUS,
        WORKFLOW_FEATURES.ARGO_OUTPUT_MSG,
        WORKFLOW_FEATURES.ARGO_STATUS_DETAILS
    ],
    [
        WORKFLOW_FEATURES.PROCESSOR_TYPE,
        WORKFLOW_FEATURES.ORDER_ITEM_STATUS
    ]
]

CLUSTERING_ORDERS_STATISTICS = [
    [ORDER_FEATURES.ORDER_NAME],
    [ORDER_FEATURES.ORDER_STATUS],
    [
        ORDER_FEATURES.ORDER_STATUS,
        ORDER_FEATURES.ORDER_NAME
    ]
]


class ClusteringObjective(Enum):
    ORDERS = CLUSTERING_ORDERS_STATISTICS
    WORKFLOWS = CLUSTERING_WORKFLOW_STATISTICS


class Clustering():
    '''
    Class responsible for full clustering execution.
    '''

    def __init__(self,
                 name: str,
                 data: pd.DataFrame,
                 features: List[str],
                 analyzed_features: List[str],
                 clustering_method: ClusteringMethod,
                 validation_metrics: List[ClusteringMetrics],
                 dim_reduction: DimensionalityReducer,
                 clustering_objective: ClusteringObjective,
                 preprocessing_operations: List[ClusteringPreProcessing],
                 scaler: Scaler = Scaler.STANDARD) -> None:
        '''
        Method initialize clustering execution.

        Parameters:
        name - name of the clustering
        data - data that is to be clustered
        features - list of features taken into account while clustering
        analyzed_features - list of features displayed during clustering result analysis
        clustering_method - method used for clustering
        validation_metrics - list of metrics for final clustering evaluation
        dim_reduction - method used to reduce data dimensionality
        clustering_objective - types of elements that are to be clustered
        preprocessing_operations - list of preprocessing methods applied on the data before clustering
        scaler - scaler used in dimensionality reduction (default: STANDARD)
        '''
        features = get_all_column_names_for_features(features, data)
        self.data = convert_fields_to_numeric(data, features)
        self.name = name
        self.features = features
        self.analyzed_features = analyzed_features
        self.validators = validation_metrics
        self.reducer = dim_reduction
        self.cluster = clustering_method
        self.scaler = scaler
        self.clustering_objective = clustering_objective
        self.preprocessing_operations = preprocessing_operations

        self.tester = TestingClusteringParameters(
            clustering_method, dim_reduction)

    def run(self,
            clustering_params: Tuple,
            reduction_params: Tuple,
            test_params: bool = False,
            save_data: bool = True) -> None:
        '''
        Method runs clustering on the data.

        Parameters:
        clustering_params - parameters used in clustering (either testing or final clustering)
        reduction_params - parameters used in data reduction
        test_params - flag indicating if run should call clustering parameters testing (default: False)
        save_data - flag indicating if the metrics are to be saved in form of csv
        '''

        if test_params:
            self.tester.test(self.data[self.features], *clustering_params)
            return

        reduced_data = self.run_data_preprocessing(*reduction_params)
        result = self.cluster(reduced_data, *clustering_params)
        labels = result[-1]

        data_with_labels = get_df_with_cluster_labels(self.data, labels)
        display_clustering_scatter_plot(reduced_data, labels, self.name)
        display_data_frame_with_labels(data_with_labels, self.name)
        self.print_validators(result, labels, reduced_data, save_data)
        display_cluster_statistics(
            data_with_labels, labels, self.analyzed_features, self.name,
            statistics_to_display=self.clustering_objective.value)
        display_histograms_per_feature_for_clusters(
            data_with_labels, self.analyzed_features, labels, self.name)
        display_histograms_per_clusters(
            data_with_labels, self.analyzed_features, labels, self.name)

    def print_validators(self,
                         result: Tuple,
                         labels: np.ndarray,
                         reduced_data: np.ndarray,
                         save_data: bool) -> None:
        '''
        Method prints results of clustering by using corresponding validation metrics.

        Parameters:
        result - tuple of clustering results
        labels - labels assigned during clustering
        reduced_data - data array used in clustering
        '''
        is_fuzzy = self.cluster == ClusteringMethod.FUZZY_C_MEANS

        save_initial_data = False if is_fuzzy else save_data
        metrics = print_clustering_metrics(
            self.validators, reduced_data, labels, save_initial_data, self.name)

        if self.cluster == ClusteringMethod.FUZZY_C_MEANS:
            xie_params = (2, result[0], result[1], len(np.unique(labels)))
            label, xie = ClusteringMetrics.XIE_BENI(
                reduced_data, *xie_params)

            print(f'\n{label}: {xie}')
            metrics[label] = xie

            print(f'\nPartition coefficient: {result[2]}')
            metrics[label] = result[2]

        if save_data:
            save_validation_metrics(metrics, self.name)

    def run_data_preprocessing(self, *args) -> np.ndarray:
        '''
        Method prepares data for clustering.

        Parameters:
        data - data used in clustering
        args - arguments of dimensionality reduction
        '''
        plot_dim_reduction = self.reducer == DimensionalityReducer.PCA

        for process_data in self.preprocessing_operations:
            self.data = process_data(self.data)

        return self.reducer(self.scaler, self.data[self.features], *args, plot=plot_dim_reduction)
