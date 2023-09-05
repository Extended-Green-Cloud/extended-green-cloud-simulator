from unittest import TestCase, main
from unittest.mock import patch, Mock
from src.clustering.clustering_methods import ClusteringMethod
from src.clustering.clustering_parameter_selection import testing_k_means_clustering, testing_fuzzy_c_means_clusters_no, testing_ICCM_clustering, testing_HDBSCAN_clustering, testing_IClust_clustering, testing_clustering_with_silhouette, TestingClusteringParameters
from src.clustering.clustering_evaluation import ClusteringMetrics
from src.helpers.dimensionality_reducer import DimensionalityReducer
from src.helpers.scaler import Scaler


class TestTestingKMeansClustering(TestCase):
    '''
    Class with tests of the method testing_k_means_clustering
    '''

    @patch('builtins.print')
    def test_testing_k_means_clustering(self, mock_print):
        """ Test should compute k-means clustering for each cluster number in specified cluster range with default parameters. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        mock_estimator = Mock()
        mock_estimator.inertia_ = 10

        with patch.object(ClusteringMethod, 'K_MEANS',
                          return_value=(None, mock_estimator, None)) as k_means:
            with patch.object(ClusteringMetrics,
                              'SILHOUETTE',
                              return_value=('Silhouette Score', 10)) as silhouette:

                # given
                test_cluster_range = range(2, 10)

                # when
                testing_k_means_clustering(None, test_cluster_range, False)

                # then
                self.assertEqual(k_means.call_count, 8,
                                 f'K-means should have been called 8 times but instead was called {k_means.call_count} times')
                self.assertEqual(silhouette.call_count, 8,
                                 f'Silhouette should have been computed 8 times but instead was computed {silhouette.call_count} times')
                mock_print.assert_any_call('Silhouette Score: 10')

                for cluster_no in test_cluster_range:
                    k_means.assert_any_call(None, cluster_no)


class TestTestingFuzzyCMeansClustering(TestCase):
    '''
    Class with tests of the method testing_fuzzy_c_means_clusters_no
    '''

    @patch('builtins.print')
    def test_testing_fuzzy_c_means_clusters_no(self, mock_print):
        """ Test should compute fuzzy c-means clustering for each cluster number in specified cluster range with default parameters. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ClusteringMethod, 'FUZZY_C_MEANS',
                          return_value=(None, None, 2, None)) as fuzzy_c_means:
            with patch.object(ClusteringMetrics, 'XIE_BENI',
                              return_value=('Xie-Beni Index', 5)) as xie_beni:
                with patch.object(ClusteringMetrics,
                                  'SILHOUETTE',
                                  return_value=('Silhouette Score', 10)) as silhouette:

                    # given
                    test_cluster_range = range(2, 10)

                    # when
                    testing_fuzzy_c_means_clusters_no(
                        None, test_cluster_range, False)

                    # then
                    self.assertEqual(fuzzy_c_means.call_count, 8,
                                     f'Fuzzy C-Means should have been called 8 times but instead was called {fuzzy_c_means.call_count} times')
                    self.assertEqual(xie_beni.call_count, 8,
                                     f'Xie-beni should have been computed 8 times but instead was computed {xie_beni.call_count} times')
                    self.assertEqual(silhouette.call_count, 8,
                                     f'Silhouette should have been computed 8 times but instead was computed {silhouette.call_count} times')
                    mock_print.assert_any_call('Silhouette Score: 10')
                    mock_print.assert_any_call('Xie-Beni Index: 5')
                    mock_print.assert_any_call('Partition Coefficient: 2')

                    for cluster_no in test_cluster_range:
                        fuzzy_c_means.assert_any_call(None, cluster_no)
                        xie_beni.assert_any_call(
                            None, 2, None, None, cluster_no)


class TestTestingICCMClustering(TestCase):
    '''
    Class with tests of the method testing_ICCM_clustering
    '''

    @patch('builtins.print')
    def test_testing_ICCM_clustering(self, mock_print):
        """ Test should compute ICCM clustering for each set of parameters. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ClusteringMethod, 'ICCM_DATA',
                          return_value=(None, None)) as iccm_data:
            with patch.object(ClusteringMethod, 'ICCM_CLUSTERS', return_value=None) \
                    as iccm_clusters:
                with patch.object(ClusteringMetrics,
                                  'SILHOUETTE',
                                  return_value=('Silhouette Score', 10)) as silhouette:
                    with patch.object(ClusteringMetrics,
                                      'CALINSKI',
                                      return_value=('Calinski-Harabasz Score', 5)) as calinski:
                        with patch.object(ClusteringMetrics,
                                          'DAVIES',
                                          return_value=('Davies-Bouldin Score', 2)) as davies:

                            # given
                            test_algorithms = [
                                ClusteringMethod.BIRCH, ClusteringMethod.GMM]
                            test_algorithms_params = [(10), (5)]
                            test_max_clusters = 10
                            test_param_list = [
                                [10, ClusteringMethod.K_MEANS],
                                [8, ClusteringMethod.FUZZY_C_MEANS]
                            ]

                            # when
                            testing_ICCM_clustering(None,
                                                    test_algorithms, test_algorithms_params,
                                                    test_max_clusters,
                                                    test_param_list)

                            # then
                            iccm_data.assert_called_once_with(
                                None, test_algorithms, test_algorithms_params, test_max_clusters)
                            self.assertEqual(iccm_clusters.call_count, 2,
                                             f'ICCM clustering should have been called 2 times but instead was called {iccm_clusters.call_count} times')
                            self.assertEqual(silhouette.call_count, 2,
                                             f'Silhouette should have been computed 2 times but instead was computed {silhouette.call_count} times')
                            self.assertEqual(davies.call_count, 2,
                                             f'Silhouette should have been computed 2 times but instead was computed {davies.call_count} times')
                            self.assertEqual(calinski.call_count, 2,
                                             f'Silhouette should have been computed 2 times but instead was computed {calinski.call_count} times')

                            mock_print.assert_any_call('Silhouette Score: 10')
                            mock_print.assert_any_call(
                                'Calinski-Harabasz Score: 5')
                            mock_print.assert_any_call(
                                'Davies-Bouldin Score: 2')

                            iccm_clusters.assert_any_call(
                                None, None, None, 10, ClusteringMethod.K_MEANS)
                            iccm_clusters.assert_any_call(
                                None, None, None, 8, ClusteringMethod.FUZZY_C_MEANS)
                            mock_print.assert_any_call(
                                f'\nParameters: {[10, ClusteringMethod.K_MEANS]}')
                            mock_print.assert_any_call(
                                f'\nParameters: {[8, ClusteringMethod.FUZZY_C_MEANS]}')


class TestTestingHDBSCANClustering(TestCase):
    '''
    Class with tests of the method testing_HDBSCAN_clustering
    '''

    @patch('builtins.print')
    def test_testing_HDBSCAN_clustering(self, mock_print):
        """ Test should compute HDBSCAN clustering for each set of parameters. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ClusteringMethod, 'HDBSCAN', return_value=None) as hdbscan:
            with patch.object(ClusteringMetrics,
                              'SILHOUETTE',
                              return_value=('Silhouette Score', 10)) as silhouette:
                with patch.object(ClusteringMetrics,
                                  'CALINSKI',
                                  return_value=('Calinski-Harabasz Score', 5)) as calinski:
                    with patch.object(ClusteringMetrics,
                                      'DAVIES',
                                      return_value=('Davies-Bouldin Score', 2)) as davies:

                        # given
                        test_param_list = [
                            ['5', '300', '0.4', 'manhattan'],
                            ['100', '500', '0.7', 'euclidean']
                        ]

                        # when
                        testing_HDBSCAN_clustering(None, test_param_list)

                        # then
                        self.assertEqual(hdbscan.call_count, 2,
                                         f'HDBSCAN clustering should have been called 2 times but instead was called {hdbscan.call_count} times')
                        self.assertEqual(silhouette.call_count, 2,
                                         f'Silhouette should have been computed 2 times but instead was computed {silhouette.call_count} times')
                        self.assertEqual(davies.call_count, 2,
                                         f'Silhouette should have been computed 2 times but instead was computed {davies.call_count} times')
                        self.assertEqual(calinski.call_count, 2,
                                         f'Silhouette should have been computed 2 times but instead was computed {calinski.call_count} times')

                        mock_print.assert_any_call('Silhouette Score: 10')
                        mock_print.assert_any_call(
                            'Calinski-Harabasz Score: 5')
                        mock_print.assert_any_call(
                            'Davies-Bouldin Score: 2')

                        hdbscan.assert_any_call(
                            None, '5', '300', '0.4', 'manhattan', only_labels=True)
                        hdbscan.assert_any_call(
                            None, '100', '500', '0.7', 'euclidean', only_labels=True)
                        mock_print.assert_any_call(
                            f'\nParameters: {["5", "300", "0.4", "manhattan"]}')
                        mock_print.assert_any_call(
                            f'\nParameters: {["100", "500", "0.7", "euclidean"]}')


class TestTestingIClustClustering(TestCase):
    '''
    Class with tests of the method testing_IClust_clustering
    '''

    @patch('builtins.print')
    def test_testing_IClust_clustering(self, mock_print):
        """ Test should compute IClust clustering for different multipliers passed as parameters. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ClusteringMethod, 'ICLUST', return_value=None) as iclust:
            with patch.object(ClusteringMetrics,
                              'SILHOUETTE',
                              return_value=('Silhouette Score', 10)) as silhouette:
                with patch.object(ClusteringMetrics,
                                  'CALINSKI',
                                  return_value=('Calinski-Harabasz Score', 5)) as calinski:
                    with patch.object(ClusteringMetrics,
                                      'DAVIES',
                                      return_value=('Davies-Bouldin Score', 2)) as davies:

                        # given
                        test_param_list = [10, 15, 20]

                        # when
                        testing_IClust_clustering(None, test_param_list)

                        # then
                        self.assertEqual(iclust.call_count, 3,
                                         f'IClust clustering should have been called 3 times but instead was called {iclust.call_count} times')
                        self.assertEqual(silhouette.call_count, 3,
                                         f'Silhouette should have been computed 3 times but instead was computed {silhouette.call_count} times')
                        self.assertEqual(davies.call_count, 3,
                                         f'Silhouette should have been computed 3 times but instead was computed {davies.call_count} times')
                        self.assertEqual(calinski.call_count, 3,
                                         f'Silhouette should have been computed 3 times but instead was computed {calinski.call_count} times')

                        mock_print.assert_any_call('Silhouette Score: 10')
                        mock_print.assert_any_call(
                            'Calinski-Harabasz Score: 5')
                        mock_print.assert_any_call(
                            'Davies-Bouldin Score: 2')

                        iclust.assert_any_call(None, 10)
                        iclust.assert_any_call(None, 15)
                        iclust.assert_any_call(None, 20)
                        mock_print.assert_any_call('\nMultiplier: 10')
                        mock_print.assert_any_call('\nMultiplier: 15')
                        mock_print.assert_any_call('\nMultiplier: 20')


class TestTestingClusteringWithSilhouette(TestCase):
    '''
    Class with tests of the method testing_clustering_with_silhouette
    '''

    @patch('builtins.print')
    def test_testing_clustering_with_silhouette(self, mock_print):
        """ Test should compute clustering for different cluster numbers and clustering methods passed as parameters. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ClusteringMethod, 'BIRCH', return_value=None) as birch:
            with patch.object(ClusteringMethod, 'GMM', return_value=None) as gmm:
                with patch.object(ClusteringMetrics,
                                  'SILHOUETTE',
                                  return_value=('Silhouette Score', 10)) as silhouette:

                    # given
                    test_clustering_methods = [
                        ClusteringMethod.BIRCH, ClusteringMethod.GMM]
                    test_cluster_range = range(2, 10)

                    for test_method in test_clustering_methods:
                        with self.subTest(test_method=test_method):
                            # when
                            testing_clustering_with_silhouette(
                                None, test_cluster_range, test_method, False)

                    # then
                    self.assertEqual(birch.call_count, 8,
                                     f'BIRCH clustering should have been called 8 times but instead was called {birch.call_count} times')
                    self.assertEqual(gmm.call_count, 8,
                                     f'GMM clustering should have been called 8 times but instead was called {gmm.call_count} times')
                    self.assertEqual(silhouette.call_count, 32,
                                     f'Silhouette should have been computed 32 times but instead was computed {silhouette.call_count} times')
                    mock_print.assert_any_call('Silhouette Score: 10')

                    for cluster_no in test_cluster_range:
                        birch.assert_any_call(
                            None, cluster_no, only_labels=True)
                        gmm.assert_any_call(None, cluster_no, only_labels=True)


class TestTestingClusteringParametersClass(TestCase):
    '''
    Class with tests of the method class TestingClusteringParameters
    '''

    def test__init__(self):
        """ Test should initialize class TestingClusteringParameters with passed parameters. """
        print(
            f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        clustering_method = ClusteringMethod.BIRCH
        dim_reduction = DimensionalityReducer.PCA

        # when
        actual = TestingClusteringParameters(clustering_method, dim_reduction)

        # then
        expected_cluster_range = range(2, 15)
        expected_cluster_range_UMAP = range(2, 15, 2)

        self.assertEqual(actual.name, 'BIRCH',
                         f'ClusteringMethod method name should be BIRCH but it was {actual.name}')
        self.assertEqual(actual.dimensionality_reduction, DimensionalityReducer.PCA,
                         f'Dimensionality reduction method should be {DimensionalityReducer.PCA} but was {actual.dimensionality_reduction}')
        self.assertEqual(actual.method, ClusteringMethod.BIRCH,
                         f'ClusteringMethod method should be {ClusteringMethod.BIRCH} but was {actual.method}')
        self.assertEqual(actual.cluster_range_PCA, expected_cluster_range,
                         f'Expected cluster range for PCA should be {expected_cluster_range} but was {actual.cluster_range_PCA}')
        self.assertEqual(actual.cluster_range_UMAP, expected_cluster_range_UMAP,
                         f'Expected cluster range for UMAP should be {expected_cluster_range_UMAP} but was {actual.cluster_range_UMAP}')

    @patch('src.clustering.clustering_parameter_selection.testing_clustering_with_silhouette')
    @patch('src.clustering.clustering_parameter_selection.testing_k_means_clustering')
    @patch('src.clustering.clustering_parameter_selection.testing_fuzzy_c_means_clusters_no')
    @patch('src.clustering.clustering_parameter_selection.testing_HDBSCAN_clustering')
    @patch('src.clustering.clustering_parameter_selection.testing_ICCM_clustering')
    @patch('src.clustering.clustering_parameter_selection.testing_IClust_clustering')
    def test_call_clustering_test(self,
                                  mock_IClust_clustering,
                                  mock_ICCM_clustering,
                                  mock_HDBSCAN_clustering,
                                  mock_fuzzy_c_means_clustering,
                                  mock_k_means_clustering,
                                  mock_clustering_silhouette):
        """ Test should call correct testing method when given clustering type is passed. """
        print(
            f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_clustering_methods = [
            (ClusteringMethod.BIRCH, mock_clustering_silhouette),
            (ClusteringMethod.GMM, mock_clustering_silhouette),
            (ClusteringMethod.K_MEANS, mock_k_means_clustering),
            (ClusteringMethod.FUZZY_C_MEANS, mock_fuzzy_c_means_clustering),
            (ClusteringMethod.HDBSCAN, mock_HDBSCAN_clustering),
            (ClusteringMethod.ICCM, mock_ICCM_clustering),
            (ClusteringMethod.ICLUST, mock_IClust_clustering)
        ]

        for test_method, test_mock in test_clustering_methods:
            with self.subTest(test_method=test_method):
                # when
                actual = TestingClusteringParameters(
                    test_method, DimensionalityReducer.PCA)
                actual.call_clustering_test(
                    None, None, cluster_range=None)

                # then
                self.assertEqual(1, test_mock.call_count,
                                 f'Method should be called exactly once but was {test_mock.call_count}')
                test_mock.reset_mock()

    def test_method_test_PCA(self):
        """ Test should call clustering testing method with PCA dimensionality reduced data. """
        print(
            f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(DimensionalityReducer, 'PCA', return_value=None) as pca_reduction:
            with patch.object(TestingClusteringParameters, 'call_clustering_test') as call_clustering_test:
                # when
                actual = TestingClusteringParameters(
                    ClusteringMethod.BIRCH, DimensionalityReducer.PCA)
                actual.test_PCA(None, None)

                # then
                pca_reduction.assert_called_once_with(Scaler.STANDARD, None)
                call_clustering_test.assert_called_once_with(
                    None, None, cluster_range=range(2, 15))

    def test_method_test_UMAP(self):
        """ Test should call clustering testing method with UMAP dimensionality reduced data for method which supports UMAP. """
        print(
            f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(DimensionalityReducer, 'UMAP', return_value=None) as umap_reduction:
            with patch.object(TestingClusteringParameters, 'call_clustering_test') as call_clustering_test:
                # when
                actual = TestingClusteringParameters(
                    ClusteringMethod.BIRCH, DimensionalityReducer.UMAP)
                actual.test_UMAP(None, None)

                # then
                self.assertEqual(9, umap_reduction.call_count,
                                 f'Reduction method should be called 6 times but was called {umap_reduction.call_count} times')
                self.assertEqual(9, call_clustering_test.call_count,
                                 f'ClusteringMethod testing should be called 6 times but was called {call_clustering_test.call_count} times')

                call_clustering_test.assert_any_call(
                    None, None, cluster_range=range(2, 15, 2))

    @patch('builtins.print')
    def test_method_test_UMAP_no_UMAP_supported(self, mock_print):
        """ Test should display the information that UMAP dimensionality reduction is not supported for clustering method does not have UMAP testing implemented. """
        print(
            f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        mock_print.reset_mock()
        actual = TestingClusteringParameters(
            ClusteringMethod.ICCM, DimensionalityReducer.UMAP)
        actual.test_UMAP(None, None)

        # then
        mock_print.assert_called_once_with(
            'Algorithm ICCM does not support clustering with UMAP.')

    def test_method_test(self):
        """ Test should call correct testing method depending on selected dimensionality reduction method. """
        print(
            f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(TestingClusteringParameters, 'test_PCA', return_value=None) as test_PCA:
            with patch.object(TestingClusteringParameters, 'test_UMAP', return_value=None) as test_UMAP:

                # given
                test_dimensionality = [
                    (DimensionalityReducer.PCA, test_PCA),
                    (DimensionalityReducer.UMAP, test_UMAP)
                ]

                for test_reduction_method, mock in test_dimensionality:
                    with self.subTest(test_reduction_method=test_reduction_method):

                        # when
                        actual = TestingClusteringParameters(
                            ClusteringMethod.ICCM, test_reduction_method)
                        actual.test(None, None)

                        # then
                        self.assertEqual(1, mock.call_count,
                                         f'Method should be called exactly once but was {mock.call_count}')
                        mock.reset_mock()


if __name__ == '__main__':
    main()
