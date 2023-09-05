import numpy as np

from sklearn.metrics import pairwise_distances
from scipy.stats import median_abs_deviation

from sklearn.mixture import GaussianMixture
from sklearn.cluster import KMeans, Birch
from skfuzzy.cluster import cmeans
from hdbscan import HDBSCAN
from typing import Tuple, List, Callable
from enum import Enum

from src.helpers.statistics_operations import calculate_jaccard_similarity, calculate_lof
from src.clustering.cluster_reader import get_cluster_with_appended_point, get_points_for_cluster, get_data_for_points_in_cluster


def use_k_means_clustering(data: np.ndarray,
                           cluster_no: int,
                           run_no: int = 30,
                           max_iter: int = 1000,
                           rand_state: int = 0,
                           only_labels: bool = False) -> Tuple[KMeans, KMeans, np.ndarray] or np.ndarray:
    '''
    Method performs k-mean clustering on the given sample of data and returns the obtained clusters.

    Parameters:
    data - data sample
    cluster_no - number of clusters
    run_no - number of k-means iterations for different seeds (default: 30)
    max_iter - the boundary of k-means iterations (default: 1000)
    rand_state - random number generation for centroid (default: 0)
    only_labels - flag indicating if only clustering labels should be returned (default: False)

    Returns: job clusters
    '''
    model = KMeans(n_clusters=cluster_no,
                   algorithm="lloyd",
                   tol=1e-8,
                   n_init=run_no,
                   max_iter=max_iter,
                   random_state=rand_state)
    estimator = model.fit(data)
    labels = model.predict(data)

    return model, estimator, labels if not only_labels else labels


def use_birch_clustering(data: np.ndarray,
                         cluster_no: int,
                         branching_factor: int = 50,
                         threshold: float = 0.3,
                         only_labels: bool = False) -> Tuple[Birch, Birch, np.ndarray] or np.ndarray:
    '''
    Method performs birch clustering on the given sample of data and returns the obtained clusters.

    Parameters:
    data - data sample
    cluster_no - number of clusters
    branching_factor - max number of subclusters (default: 50)
    threshold - radius of subcluster (default: 0.3)
    only_labels - flag indicating if only clustering labels should be returned (default: False)

    Returns: job clusters
    '''
    model = Birch(threshold=threshold,
                  branching_factor=branching_factor,
                  n_clusters=cluster_no)

    estimator = model.fit(data)
    labels = model.predict(data)

    return model, estimator, labels if not only_labels else labels


def use_GMM_clustering(data: np.ndarray,
                       cluster_no: int,
                       only_labels: bool = False) -> Tuple[GaussianMixture, np.ndarray] or np.ndarray:
    '''
    Method performs GMM clustering on the given sample of data and returns the obtained clusters.

    Parameters:
    cluster_no - number of desired clusters
    covariance_type - type of covariance parameters to use
    only_labels - flag indicating if only clustering labels should be returned (default: False)

    Returns: job clusters
    '''
    gmm = GaussianMixture(n_components=cluster_no,
                          covariance_type='full',
                          random_state=0).fit(data)
    labels = gmm.predict(data)

    return gmm, labels if not only_labels else labels


def use_fuzzy_clustering(data: np.ndarray,
                         cluster_no: int,
                         error: float = 0.00001,
                         max_iter: int = 2000,
                         dist_metric: str = 'euclidean',
                         only_labels: bool = False) -> Tuple[np.ndarray, np.ndarray, float, np.ndarray] or np.ndarray:
    '''
    Method performs FCM clustering on the given sample of data and returns the obtained clusters.

    Parameters:
    cluster_no - number of desired clusters
    error - stopping criteria error (default: 0.00001)
    max_iter - maximum number of iterations (default: 2000)
    dist_metric - method to select cluster (default: euclidean)
    only_labels - flag indicating if only clustering labels should be returned (default: False)

    Returns: job clusters
    '''
    gmm, labels = use_GMM_clustering(data, cluster_no)
    partition_matrix = gmm.predict_proba(data).T

    cntr, u, _, _, _, _, fpc = cmeans(data=data.T,
                                      c=cluster_no,
                                      m=2,
                                      error=error,
                                      maxiter=max_iter,
                                      init=partition_matrix,
                                      metric=dist_metric)
    labels = u.argmax(axis=0)
    return u, cntr, fpc, labels if not only_labels else labels


def use_HDBSCAN_clustering(data: np.ndarray,
                           min_cluster_size: int or str,
                           min_samples: int or str,
                           distance_threshold: float or str,
                           metric: dict,
                           cluster_selection_method: str = 'leaf',
                           only_labels: bool = False) -> Tuple[HDBSCAN, HDBSCAN, np.ndarray] or np.ndarray:
    '''
    Method performs HDBSCAN clustering on the given sample of data and returns the obtained clusters.

    Parameters:
    min_cluster_size - min number of elements in a cluster
    min_samples - min number of samples in the neighborhood
    distance_threshold - clusters below the threshold are merged
    metric - distance metric
    cluster_selection_method - method to select clusters (default: leaf)
    only_labels - flag indicating if only clustering labels should be returned (default: False)

    Returns: job clusters
    '''
    model = HDBSCAN(min_cluster_size=int(min_cluster_size),
                    min_samples=int(min_samples),
                    cluster_selection_epsilon=float(distance_threshold),
                    cluster_selection_method=cluster_selection_method,
                    metric=metric)

    estimator = model.fit(data)
    labels = model.labels_

    return model, estimator, labels if not only_labels else labels


def cluster_data_points_ICCM(data: np.ndarray,
                             algorithms: List[Callable],
                             params_per_algorithm: np.ndarray,
                             max_clusters: int) -> Tuple[List, np.ndarray]:
    '''
    Method performs ICCM clustering on the given sample of data and returns the obtained sub-clusters.

    Parameters:
    data - data that is to be clustered
    algorithms - algorithms used for parallel clustering
    params_per_algorithm - params used in each clustering algorithm
    max_clusters - maximal number of clusters defined in the clustering algorithms

    Returns: sub-clusters and sub-clusters centers for each algorithm iteration
    '''
    OBJECTIVE_ALGORITHM_IDX = 0
    data_to_cluster = dict((idx, point) for idx, point in enumerate(data))
    sub_clusters = []

    # iterate until there is still data to cluster
    while len(data_to_cluster) > 0:
        data_to_cluster_values = np.ndarray(list(data_to_cluster.values()))

        clusters_per_iteration = dict()
        labels_per_algorithm = []
        cluster_assignment_per_algorithm = []

        # 1st step: clustering unclustered data points with different algorithms
        for i, algorithm in enumerate(algorithms):
            labels = algorithm(data_to_cluster_values,
                               *params_per_algorithm[i],
                               only_labels=True)
            points_per_label = [get_points_for_cluster(labels, label)
                                for label in np.unique(labels)]

            labels_per_algorithm.append(points_per_label)
            cluster_assignment_per_algorithm.append(labels)

        labels_for_objective_algorithm = labels_per_algorithm[OBJECTIVE_ALGORITHM_IDX]

        # 2nd step: voting for final partial cluster assignment
        for algorithm_idx, algorithm_labels in enumerate(labels_per_algorithm):

            # skip algorithm which is used as reference point
            # (i.e. clustering results with which other results are compared)
            if algorithm_idx == OBJECTIVE_ALGORITHM_IDX:
                continue

            new_cluster_assignment = np.zeros(len(algorithm_labels))

            for cluster_idx, cluster in enumerate(algorithm_labels):
                clusters_confusion_matrix = [calculate_jaccard_similarity(cluster, objective_cluster)
                                             for objective_cluster in labels_for_objective_algorithm]

                best_cluster = np.argmax(clusters_confusion_matrix)
                new_cluster_assignment[cluster_idx] = best_cluster

            prev_cluster_assignment = cluster_assignment_per_algorithm[algorithm_idx]
            cluster_assignment_per_algorithm[algorithm_idx] = np.array([int(new_cluster_assignment[prev_cluster])
                                                                        for prev_cluster in prev_cluster_assignment])

        cluster_assignment_per_algorithm = np.array(
            cluster_assignment_per_algorithm).T

        data_for_next_iteration = dict()

        for point_idx, clusters_for_point in enumerate(cluster_assignment_per_algorithm):
            clusters_count = np.bincount(clusters_for_point)
            maxima_no = len(
                [val for val in clusters_count if val == clusters_count.max()])

            point = list(data_to_cluster.keys())[point_idx]

            # if there is single maximum value in clusters count
            if(maxima_no == 1):
                final_cluster = np.argmax(clusters_count)
                clusters_per_iteration[final_cluster] = get_cluster_with_appended_point(
                    point, final_cluster, clusters_per_iteration)
            else:
                data_for_next_iteration[point_idx] = point

        # if number of data points is smaller than desired number of clusters
        if len(data_for_next_iteration) < max_clusters:
            for point_idx, point in data_for_next_iteration.items():
                clusters_for_point = cluster_assignment_per_algorithm[point_idx]
                labels_count = np.bincount(clusters_for_point)
                final_cluster = np.argmax(labels_count)
                clusters_per_iteration[final_cluster] = get_cluster_with_appended_point(
                    point, final_cluster, clusters_per_iteration)
            data_to_cluster = []
        else:
            data_to_cluster = data_for_next_iteration
            print(
                f"\nData no. in the next iteration: {len(data_for_next_iteration)}")

        sub_clusters.append(clusters_per_iteration)

    # return list of sub-clusters and their centers for all iterations
    sub_clusters = [cluster for cluster_per_iteration in sub_clusters
                    for _, cluster in cluster_per_iteration.items()]
    sub_clusters_points = [list(map(lambda val: data[val], cluster))
                           for cluster in sub_clusters]
    sub_clusters_centers = [np.mean(cluster, axis=0)
                            for cluster in sub_clusters_points]

    return sub_clusters, np.array(sub_clusters_centers)


def get_ICCM_labels(data: np.ndarray,
                    sub_clusters: np.ndarray,
                    sub_clusters_centers: np.ndarray,
                    cluster_no: int,
                    algorithm: Callable,
                    *params) -> List[int]:
    '''
    Method computes labels for ICCM sub-clusters.

    Parameters:
    data - data used in clustering
    sub_clusters - sub-clusters obtained after ICCM clustering
    sub_clusters_centers - centers of sub-clusters obtained after ICCM clustering
    algorithm - algorithm used to cluster the centers
    params - parameters used by clustering algorithm

    Returns: list with assigned labels
    '''
    cluster_no = len(sub_clusters) \
        if cluster_no is not None and len(sub_clusters) < cluster_no else cluster_no

    params = (cluster_no,) + params if cluster_no is not None else params

    labels_sub_clusters = algorithm(sub_clusters_centers, params)
    labels = np.zeros(len(data))

    for cluster_label in np.unique(labels_sub_clusters):
        indexes = get_points_for_cluster(labels_sub_clusters, cluster_label)
        cluster_elements = [el for idx, cluster in enumerate(
            sub_clusters) if idx in indexes for el in cluster]

        for el in cluster_elements:
            labels[el] = cluster_label

    return labels


def compute_initial_iclust_clusters(data: np.ndarray,
                                    initial_multiplier: float) -> np.ndarray:
    '''
    Using K-means clustering, method computes initial clusters for IClust.

    Parameters:
    data - data that is to be clustered
    initial_multiplier - multiplier used to obtain optimal number of clusters
    '''
    cluster_no = np.log(len(data)) * initial_multiplier
    return ClusteringMethod.K_MEANS(data, int(cluster_no), only_labels=True)


def compute_cv_for_iclust_cluster(lofs: np.ndarray) -> float:
    '''
    Method computes critical value (CV) used as a threshold for merging two clusters.

    Parameters:
    lofs - local outlier factors for cluster

    Returns: critical value
    '''
    return np.median(lofs, axis=None) + 2 * median_abs_deviation(lofs, axis=None)


def compute_cv_and_lof_for_iclust_cluster(points_in_cluster: np.ndarray,
                                          point: np.ndarray,
                                          q_max: int) -> Tuple[float, float]:
    '''
    Method computes critical value and final lof for cluster.

    Parameters:
    points_in_cluster - points belonging to the given cluster
    point - point from the second considered cluster
    q_max - maximal q

    Returns: critical value and local outlier factor
    '''
    points_for_loaf = [point] + points_in_cluster
    q = np.min([len(points_for_loaf) - 1, q_max])
    lofs_cluster = \
        np.array([calculate_lof(points_for_loaf, i) for i in range(1, q + 1)])

    cv_cluster = compute_cv_for_iclust_cluster(lofs_cluster)
    lof = np.sum(lofs_cluster[:, 0]) / q

    return cv_cluster, lof


def use_iclust_clustering(data: np.ndarray,
                          initial_multiplier: float,
                          q_max: int = 10) -> np.ndarray:
    '''
    Method computes IClust clustering.

    Parameters:
    data - data that is to be clustered
    initial_multiplier - multiplier used when computing number of clusters
    q_max - max number of nearest neighbors (default: 10)

    Returns: list with assigned labels
    '''
    labels = compute_initial_iclust_clusters(data, initial_multiplier)
    points_per_cluster = \
        [get_data_for_points_in_cluster(data, labels, label)
         for label in np.unique(labels)]
    stop_merging = False

    # iterate until there are still clusters that can be merged
    while not stop_merging:
        stop_merging = True
        cluster_no = len(points_per_cluster)
        print(f'Cluster no: {cluster_no}')

        min_distances = np.full((cluster_no, cluster_no), np.inf, dtype=float)
        min_distances_points = np.zeros(
            (cluster_no, cluster_no), dtype=(float, 2))

        # finding two closest clusters
        for cluster_1_idx, points_1 in enumerate(points_per_cluster):
            points_per_second_cluster = dict(
                (idx, points) for idx, points in enumerate(points_per_cluster) if idx != cluster_1_idx)

            for cluster_2_idx, points_2 in points_per_second_cluster.items():
                distances = \
                    pairwise_distances(np.array(points_1), np.array(points_2))
                min_distance_arg = np.unravel_index(
                    np.argmin(distances, axis=None), distances.shape)

                min_distances_points[cluster_1_idx][cluster_2_idx] = \
                    list(min_distance_arg)
                min_distances[cluster_1_idx][cluster_2_idx] = np.min(distances)

        # iterate until no clusters have been merged and there are still two clusters which distances can be compared
        while stop_merging and not np.all(np.isinf(min_distances)):
            min_dist_clusters = np.unravel_index(
                np.argmin(min_distances, axis=None), min_distances.shape)
            min_dist_clusters = (
                int(min_dist_clusters[0]), int(min_dist_clusters[1]))

            min_points_cluster = min_distances_points[min_dist_clusters[0]
                                                      ][min_dist_clusters[1]]

            point1 = points_per_cluster[min_dist_clusters[0]][int(
                min_points_cluster[0])]
            point2 = points_per_cluster[min_dist_clusters[1]][int(
                min_points_cluster[1])]

            cv1, lof1 = compute_cv_and_lof_for_iclust_cluster(
                points_per_cluster[min_dist_clusters[1]], point1, q_max)
            cv2, lof2 = compute_cv_and_lof_for_iclust_cluster(
                points_per_cluster[min_dist_clusters[0]], point2, q_max)

            # merge similar clusters if conditions are satisfied
            if lof1 < cv1 and lof2 < cv2:
                merged_cluster = points_per_cluster[min_dist_clusters[0]] \
                    + points_per_cluster[min_dist_clusters[1]]
                points_per_cluster[min_dist_clusters[0]] = merged_cluster

                print(
                    f'Merged {min_dist_clusters[0]} with {min_dist_clusters[1]}')

                stop_merging = False
                points_per_cluster = [points for idx, points in enumerate(
                    points_per_cluster) if idx != min_dist_clusters[1]]
                labels = [label if label < min_dist_clusters[1]
                          else (min_dist_clusters[0] if label == min_dist_clusters[1] else label - 1) for label in labels]
            else:
                min_distances[min_dist_clusters[0]
                              ][min_dist_clusters[1]] = np.inf
                min_distances_points[min_dist_clusters[0]
                                     ][min_dist_clusters[1]] = np.inf
                min_distances[min_dist_clusters[1]
                              ][min_dist_clusters[0]] = np.inf
                min_distances_points[min_dist_clusters[1]
                                     ][min_dist_clusters[0]] = np.inf

    return labels


def use_ICCM_clustering(data: np.ndarray,
                        algorithms: List[Callable],
                        params_per_algorithm: np.ndarray,
                        max_clusters: int,
                        final_cluster_no: int,
                        centers_algorithm: Callable,
                        *centers_algorithm_params) -> np.ndarray:
    '''
    Method performs full ICCM clustering and returns final labels.

    Parameters:
    data - data that is to be clustered
    algorithms - algorithms used for parallel clustering
    params_per_algorithm - params used in each clustering algorithm
    max_clusters - maximal number of clusters defined in the clustering algorithms
    final_cluster_no - final number of desired clusters
    centers_algorithm - algorithm used to cluster the centers
    centers_algorithm_params - parameters used by clustering algorithm
    '''
    sub_clusters, sub_clusters_centers = use_ICCM_clustering(
        data, algorithms, params_per_algorithm, max_clusters)

    return get_ICCM_labels(data, sub_clusters, sub_clusters_centers,
                           final_cluster_no,                         centers_algorithm,
                           centers_algorithm_params)


class ClusteringMethod(Enum):
    def K_MEANS(data, *args, only_labels=False): \
        return use_k_means_clustering(data, *args, only_labels=only_labels)

    def BIRCH(data, *args, only_labels=False): \
        return use_birch_clustering(data, *args, only_labels=only_labels)

    def GMM(data, *args, only_labels=False): \
        return use_GMM_clustering(data, *args, only_labels=only_labels)

    def FUZZY_C_MEANS(data, *args, only_labels=False): \
        return use_fuzzy_clustering(data, *args, only_labels=only_labels)

    def HDBSCAN(data, *args, only_labels=False): \
        return use_HDBSCAN_clustering(data, *args, only_labels=only_labels)

    def ICCM(data, *args): return use_ICCM_clustering(data, *args)
    def ICCM_DATA(data, *args): return cluster_data_points_ICCM(data, *args)
    def ICCM_CLUSTERS(data, *args): return get_ICCM_labels(data, *args)
    def ICLUST(data, *args): return use_iclust_clustering(data, *args)
