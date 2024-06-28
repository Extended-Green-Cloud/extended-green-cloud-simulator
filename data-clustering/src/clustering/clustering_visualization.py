import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from typing import List
from IPython.display import display

from src.helpers.subplot_maker import initialize_subplot, add_feature_trace, display_and_save_multiplot
from src.helpers.value_reader import FORMATTER
from src.helpers.path_reader import PathReader, parse_file_name
from src.helpers.statistics_operations import filter_out_unused_step_features, append_coefficient_of_variance
from src.clustering.cluster_reader import get_workflows_for_label

STATISTIC_FIELDS = ['count', 'mean', 'std', 'min', 'max']

def display_clustering_scatter_plot(data: np.ndarray,
                                    labels: List[int],
                                    name: str,
                                    is_test: bool = False) -> None:
    '''
    Method visualize clustering results on the scatter plot.

    Parameters:
    data - data that has been clustered
    labels - list of assigned labels
    name - name of the algorithm used for clustering
    is_test - flag indicating if the method should use test path
    '''
    dir_name = parse_file_name(name)
    file_name = f'{dir_name}-result-features.png'
    file_path = PathReader.CLUSTERING_PATH(dir_name, file_name, is_test)

    cluster_no = len(np.unique(labels))

    plt.figure(figsize=(10, 10))

    for i in np.unique(labels):
        plt.scatter(data[labels == i, 0], data[labels == i, 1], label=i + 1)

    plt.title(f'{name} with {cluster_no} clusters')
    plt.legend()
    plt.savefig(file_path, bbox_inches='tight')
    plt.show()


def display_data_frame_with_labels(data: pd.DataFrame,
                                   name: str,
                                   is_test: bool = False) -> None:
    '''
    Method displays data frame with added labels.

    Parameters:
    data - data that has been clustered
    name - name of the algorithm used for clustering
    is_test - flag indicating if the method should use test path
    '''
    dir_name = parse_file_name(name)
    file_name = f'{dir_name}-output-labels.csv'
    file_path = PathReader.CLUSTERING_PATH(dir_name, file_name, is_test)

    data['label'] = data['label'].astype(str)
    data.to_csv(file_path)
    display(data)


def display_and_save_aggregation_for_feature(df: pd.DataFrame,
                                             features: List[str],
                                             dir_name: str,
                                             label: str,
                                             is_test: bool = False) -> None:
    '''
    Method prints the count values for given cluster and feature column and stores the results in the .csv.

    Parameters:
    df - data frame of interest
    features - names of features columns used in aggregation
    dir_name - name of the directory in which the result is to be stored
    label - label of the cluster
    is_test - flag indicating if the method should use test path
    '''
    feature_name = ' and '.join([feature.replace("_", " ") for feature in features])
    output_file = f'{dir_name}-cluster-{label}-{"-".join(features)}-statistics.csv'
    aggregated_df = df[features].value_counts().to_frame()

    print(f'\nCount per {feature_name}:')
    display(aggregated_df)
    aggregated_df.to_csv(
        PathReader.CLUSTERING_PATH(dir_name, output_file, is_test))

def display_cluster_statistics(data: pd.DataFrame,
                               labels: List[int],
                               features: List[str],
                               name: str,
                               statistics_to_display: List,
                               is_test: bool = False) -> None:
    '''
    Method displays statistics of individual clusters.

    Parameters:
    data - data that has been clustered
    labels - list of assigned labels
    features - features taken into account
    name - clustering name used to store files
    statistic_to_display - list of feature statistics which should be printed during the clustering analysis
    is_test - flag indicating if the method should use test path
    '''
    for label in np.unique(labels):
        workflows_df_label = get_workflows_for_label(data, label)
        cluster_result_to_store = filter_out_unused_step_features(
            workflows_df_label)

        dir_name = parse_file_name(name)
        label_no = int(label + 1)
        cluster_result_to_store.to_csv(
            PathReader.CLUSTERING_PATH(dir_name, f'{dir_name}-cluster-{label_no}-data.csv', is_test))

        stats = workflows_df_label[features]\
            .describe(include="all").apply(FORMATTER).loc[STATISTIC_FIELDS]
        stats = append_coefficient_of_variance(stats)

        # displaying results
        print(f"\n\nCluster {label + 1} (size: {len(workflows_df_label)}):")
        display(stats.drop('count'))
        stats.drop('count').to_csv(
            PathReader.CLUSTERING_PATH(dir_name, f'{dir_name}-cluster-{label_no}-overall-statistics.csv', is_test))

        for statistic_set in statistics_to_display:
            display_and_save_aggregation_for_feature(
                workflows_df_label, statistic_set, dir_name, label_no, is_test)


def display_histograms_per_feature_for_clusters(data_frame: pd.DataFrame,
                                                features: List[str],
                                                labels: List[int],
                                                name: str,
                                                is_test: bool = False) -> None:
    '''
    Method displays histograms for each feature of each cluster (all clusters are aggregated on one multi-plot).

    Parameters:
    data_frame - clustered data
    features - list of features
    labels - list of clustering labels
    name - name of the algorithm used for clustering
    is_test - flag indicating if the method should use test path
    '''
    unique_labels = np.unique(labels)

    for feature in data_frame[features].describe().columns:
        fig = initialize_subplot(len(unique_labels))

        for idx, label in enumerate(unique_labels):
            data_per_label = get_workflows_for_label(data_frame, label)[features]
            subplot_name = f'{feature} for cluster {label + 1}'
            add_feature_trace(subplot_name, fig, data_per_label, feature, idx)

        dir_name = parse_file_name(name)
        file_name = f'{dir_name}-{feature}_per_cluster.png'
        file_path = PathReader.CLUSTERING_PATH(dir_name, file_name, is_test)

        display_and_save_multiplot(
            fig, f"{feature} per label (histograms)", file_path)


def display_histograms_per_clusters(data_frame: pd.DataFrame,
                                    features: List[str],
                                    labels: List[int],
                                    name: str,
                                    is_test: bool = False) -> None:
    '''
    Method displays histograms for each feature of each cluster (one multi-plot per cluster.

    Parameters:
    data_frame - clustered data
    features - list of features
    labels - list of clustering labels
    name - name of the algorithm used for clustering
    is_test - flag indicating if the method should use test path
    '''
    unique_labels = np.unique(labels)

    for label in unique_labels:
        data_per_label = get_workflows_for_label(data_frame, label)[features]
        fig = initialize_subplot(len(features))

        for idx, feature in enumerate(data_frame[features].describe().columns):
            subplot_name = f'{feature}'
            add_feature_trace(subplot_name, fig, data_per_label, feature, idx)

        dir_name = parse_file_name(name)
        file_name = f'{dir_name}-cluster_{label + 1}_histograms.png'
        file_path = PathReader.CLUSTERING_PATH(dir_name, file_name, is_test)

        display_and_save_multiplot(fig, f"Cluster {label + 1}", file_path)
