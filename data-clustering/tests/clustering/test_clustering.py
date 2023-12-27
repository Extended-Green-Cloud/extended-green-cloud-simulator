import pandas as pd
import numpy as np

from unittest import TestCase, main
from unittest.mock import patch

from pandas.testing import assert_frame_equal
from src.clustering.clustering import Clustering, ClusteringObjective
from src.clustering.clustering_methods import ClusteringMethod
from src.clustering.clustering_evaluation import ClusteringMetrics
from src.clustering.clustering_pre_processing import ClusteringPreProcessing
from src.helpers.dimensionality_reducer import DimensionalityReducer
from src.helpers.scaler import Scaler
from src.helpers.feature_encoder import WORKFLOW_FEATURES
from src.clustering.clustering_parameter_selection import TestingClusteringParameters

MOCK_VALIDATOR_RESULTS = pd.DataFrame({
    'Cluster no.': 2,
    'Calinski-Harabasz Score': 2,
    'Silhouette Score': 10
}, index=[0])

MOCK_DATA = pd.DataFrame({
    WORKFLOW_FEATURES.CPU: ['10', '20', '5', '15'],
    WORKFLOW_FEATURES.MEMORY: ['2', '10', '25', '60'],
    WORKFLOW_FEATURES.ARGO_STATUS_DETAILS: ['stopped with strategy "stop"', 'leader changed', 'undefined', 'undefined'],
    'argo_detailed_status_code_stopped with strategy "stop"': [1.0, 0.0, 0.0, 0.0],
    'argo_detailed_status_code_leader changed': [0.0, 1.0, 0.0, 0.0],
    'argo_detailed_status_code_undefined': [0.0, 0.0, 1.0, 1.0],
    'argo_detailed_status_code_no more retries left': [0.0, 0.0, 0.0, 0.0],
    'argo_detailed_status_code_resource not found': [0.0, 0.0, 0.0, 0.0],
    'argo_detailed_status_code_request timed out': [0.0, 0.0, 0.0, 0.0],
    WORKFLOW_FEATURES.ARGO_OUTPUT_MSG: ['test_output', 'test_output_2', 'test_output_3', 'test_output_4'],
    'argo_output_message_code_undefined error': [1.0, 1.0, 1.0, 1.0],
    WORKFLOW_FEATURES.ORDER_NAME: ['name1', 'name2', 'test_order', 'frsdf']
})

MOCK_NUMERIC_DATA = pd.DataFrame({
    WORKFLOW_FEATURES.CPU: [10.0, 20.0, 5.0, 15.0],
    WORKFLOW_FEATURES.MEMORY: [2.0, 10.0, 25.0, 60.0],
    WORKFLOW_FEATURES.ARGO_STATUS_DETAILS: ['stopped with strategy "stop"', 'leader changed', 'undefined', 'undefined'],
    'argo_detailed_status_code_stopped with strategy "stop"': [1.0, 0.0, 0.0, 0.0],
    'argo_detailed_status_code_leader changed': [0.0, 1.0, 0.0, 0.0],
    'argo_detailed_status_code_undefined': [0.0, 0.0, 1.0, 1.0],
    'argo_detailed_status_code_no more retries left': [0.0, 0.0, 0.0, 0.0],
    'argo_detailed_status_code_resource not found': [0.0, 0.0, 0.0, 0.0],
    'argo_detailed_status_code_request timed out': [0.0, 0.0, 0.0, 0.0],
    WORKFLOW_FEATURES.ARGO_OUTPUT_MSG: ['test_output', 'test_output_2', 'test_output_3', 'test_output_4'],
    'argo_output_message_code_undefined error': [1.0, 1.0, 1.0, 1.0],
    WORKFLOW_FEATURES.ORDER_NAME: ['name1', 'name2', 'test_order', 'frsdf']
})


def initialize_mock_clustering() -> Clustering:
    test_name = "Test Name"
    test_data = MOCK_DATA
    test_features = [
        WORKFLOW_FEATURES.CPU,
        WORKFLOW_FEATURES.MEMORY,
        WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE,
        WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE
    ]
    test_features_analyzed = [WORKFLOW_FEATURES.CPU]
    test_clustering = ClusteringMethod.K_MEANS
    test_validation_metrics = [
        ClusteringMetrics.CALINSKI, ClusteringMetrics.SILHOUETTE]
    test_dim_reduction = DimensionalityReducer.PCA
    test_scaler = Scaler.MIN_MAX
    test_objective = ClusteringObjective.WORKFLOWS
    test_pre_processing = [ClusteringPreProcessing.FILTER_TEST_WORKFLOWS]

    return Clustering(test_name, test_data, test_features,
                      test_features_analyzed,
                      test_clustering,
                      test_validation_metrics,
                      test_dim_reduction,
                      test_objective,
                      test_pre_processing,
                      test_scaler)


class TestClusteringModel(TestCase):
    '''
    Class with tests of the Clustering class
    '''

    def test_clustering_init(self):
        """ Test should correctly assign clustering fields """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_name = "Test Name"
        test_data = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: ['10', '20'],
            WORKFLOW_FEATURES.MEMORY: ['2', '10']
        })
        test_features = [WORKFLOW_FEATURES.CPU, WORKFLOW_FEATURES.MEMORY]
        test_features_analyzed = [WORKFLOW_FEATURES.CPU]
        test_clustering = ClusteringMethod.K_MEANS
        test_validation_metrics = [
            ClusteringMetrics.CALINSKI, ClusteringMetrics.SILHOUETTE]
        test_dim_reduction = DimensionalityReducer.PCA
        test_pre_processing = [ClusteringPreProcessing.FILTER_TEST_WORKFLOWS]
        test_objective = ClusteringObjective.WORKFLOWS
        test_scaler = Scaler.MIN_MAX

        # when
        result = Clustering(test_name, test_data, test_features,
                            test_features_analyzed,
                            test_clustering,
                            test_validation_metrics,
                            test_dim_reduction,
                            test_objective,
                            test_pre_processing,
                            test_scaler)

        # then
        expected_df = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: [10.0, 20.0],
            WORKFLOW_FEATURES.MEMORY: [2.0, 10.0]
        })

        try:
            assert_frame_equal(result.data, expected_df)
        except AssertionError as e:
            raise self.failureException(
                f'Method should return {expected_df} but returned {result.data}') from e

        self.assertEqual(test_name, result.name)
        self.assertListEqual(test_features, result.features)
        self.assertListEqual(test_features_analyzed, result.analyzed_features)
        self.assertEqual(test_clustering, result.cluster)
        self.assertListEqual(test_validation_metrics, result.validators)
        self.assertEqual(test_dim_reduction, result.reducer)
        self.assertEqual(test_scaler, result.scaler)
        self.assertEqual(test_pre_processing, result.preprocessing_operations)

    def test_run_data_preprocessing_no_status_preprocessing(self):
        """ Test should run reduction preprocessing but no status preprocessing """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(DimensionalityReducer, 'PCA', return_value=[1, 2, 3]):

            # given
            clustering = initialize_mock_clustering()
            clustering.preprocessing_operations = []

            # when
            reduced_data = clustering.run_data_preprocessing()

            # then
            expected_df = MOCK_NUMERIC_DATA

            self.assertListEqual(reduced_data, [1, 2, 3])

            try:
                assert_frame_equal(clustering.data, expected_df)
            except AssertionError as e:
                raise self.failureException(
                    f'Method should return {expected_df} but returned {clustering.data}') from e

    def test_run_data_preprocessing_with_status_preprocessing(self):
        """ Test should run reduction preprocessing and status preprocessing """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(DimensionalityReducer, 'PCA', return_value=[1, 2, 3]):
            # given
            clustering = initialize_mock_clustering()
            clustering.preprocessing_operations = [ClusteringPreProcessing.FILTER_TEST_WORKFLOWS,
                                                   ClusteringPreProcessing.MERGE_STATUSES]

            # when
            reduced_data = clustering.run_data_preprocessing()

            # then
            expected_df = pd.DataFrame({
                WORKFLOW_FEATURES.CPU: [10.0, 20.0],
                WORKFLOW_FEATURES.MEMORY: [2.0, 10.0],
                WORKFLOW_FEATURES.ARGO_STATUS_DETAILS: ['stopped with strategy "stop"', 'leader changed'],
                'argo_detailed_status_code_stopped with strategy "stop"': [1.0, 0.0],
                'argo_detailed_status_code_leader changed': [0.0, 1.0],
                'argo_detailed_status_code_undefined': [0.0, 0.0],
                'argo_detailed_status_code_no more retries left': [0.0, 0.0],
                'argo_detailed_status_code_resource not found': [0.0, 0.0],
                'argo_detailed_status_code_request timed out': [0.0, 0.0],
                WORKFLOW_FEATURES.ARGO_OUTPUT_MSG: ['stopped with strategy "stop"', 'leader changed'],
                'argo_output_message_code_undefined error': [0.0, 0.0],
                WORKFLOW_FEATURES.ORDER_NAME: ['name1', 'name2'],
                'argo_output_message_code_stopped with strategy "stop"': [1.0, 0.0],
                'argo_output_message_code_leader changed': [0.0, 1.0]
            })

            self.assertListEqual(reduced_data, [1, 2, 3])

            try:
                pd.set_option('display.max_rows', 1000)
                print(clustering.data)
                assert_frame_equal(clustering.data, expected_df)
            except AssertionError as e:
                raise self.failureException(
                    f'Method should return {expected_df} but returned {clustering.data}') from e

    @patch("src.clustering.clustering.print_clustering_metrics", return_value=MOCK_VALIDATOR_RESULTS)
    @patch("src.clustering.clustering.save_validation_metrics")
    def test_print_validators_not_fuzzy(self, mock_saver, mock_printer):
        """ Test should call print clustering metrics method for correct validators"""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        clustering = initialize_mock_clustering()

        # when
        clustering.print_validators(None,
                                    ['test_labels_array'],
                                    ['test_data_array'],
                                    True)

        mock_printer.assert_called_once_with(clustering.validators,
                                             ['test_data_array'], [
                                                 'test_labels_array'],
                                             True, clustering.name)
        mock_saver.assert_called_once_with(
            MOCK_VALIDATOR_RESULTS, clustering.name)

    @patch('builtins.print')
    @patch("src.clustering.clustering.print_clustering_metrics", return_value=MOCK_VALIDATOR_RESULTS)
    @patch("src.clustering.clustering.save_validation_metrics")
    def test_print_validators_fuzzy(self, mock_saver, mock_printer, mock_builtin_print):
        """ Test should call print clustering metrics method for correct validators"""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        clustering = initialize_mock_clustering()
        clustering.cluster = ClusteringMethod.FUZZY_C_MEANS

        with patch.object(ClusteringMetrics, 'XIE_BENI', return_value=('Xie-Beni Index', 10)) as mock_xie:
            # when
            test_labels = [0, 1, 1, 0]
            test_data = np.array(['test_data_array']).reshape(1, -1)
            test_results = [test_labels, 5, 10]
            clustering.print_validators(test_results,
                                        test_labels,
                                        test_data,
                                        True)

            mock_printer.assert_called_once_with(clustering.validators,
                                                 test_data,
                                                 test_labels,
                                                 False,
                                                 clustering.name)
            mock_xie.assert_called_once_with(test_data, 2, test_labels, 5, 2)
            mock_builtin_print.assert_any_call('\nXie-Beni Index: 10')
            mock_builtin_print.assert_any_call('\nPartition coefficient: 10')
            mock_saver.assert_called_once_with(
                MOCK_VALIDATOR_RESULTS, clustering.name)

    @patch("src.clustering.clustering.Clustering.run_data_preprocessing", return_value=['test_reduced_data'])
    @patch("src.clustering.cluster_reader.get_df_with_cluster_labels", return_value=['test_data_with_labels'])
    @patch("src.clustering.clustering_visualization.display_clustering_scatter_plot")
    @patch("src.clustering.clustering_visualization.display_data_frame_with_labels")
    @patch("src.clustering.clustering_visualization.display_cluster_statistics")
    @patch("src.clustering.clustering_visualization.display_histograms_per_feature_for_clusters")
    @patch("src.clustering.clustering_visualization.display_histograms_per_clusters")
    def test_run_with_test(self,
                           mock_cluster_display,
                           mock_feature_display,
                           mock_statistic_display,
                           mock_df_labels_display,
                           mock_scatter_plot_display,
                           mock_get_df_with_clusters,
                           mock_run_data_preprocessing):
        """ Test should call only param testing method"""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(TestingClusteringParameters, 'test') as mock_test:

            # given
            clustering = initialize_mock_clustering()

            # when
            clustering.run((), (), test_params=True, save_data=False)
            clustering.preprocessing_operations = [
                ClusteringPreProcessing.FILTER_TEST_WORKFLOWS,
                ClusteringPreProcessing.MERGE_STATUSES]

            # then
            mock_test.assert_called_once()
            mock_cluster_display.assert_not_called()
            mock_feature_display.assert_not_called()
            mock_statistic_display.assert_not_called()
            mock_df_labels_display.assert_not_called()
            mock_scatter_plot_display.assert_not_called()
            mock_get_df_with_clusters.assert_not_called()
            mock_run_data_preprocessing.assert_not_called()

    @patch("src.clustering.clustering.Clustering.run_data_preprocessing", return_value=['test_reduced_data'])
    @patch("src.clustering.clustering.get_df_with_cluster_labels", return_value=['test_data_with_labels'])
    @patch("src.clustering.clustering.display_clustering_scatter_plot")
    @patch("src.clustering.clustering.display_data_frame_with_labels")
    @patch("src.clustering.clustering.Clustering.print_validators")
    @patch("src.clustering.clustering.display_cluster_statistics")
    @patch("src.clustering.clustering.display_histograms_per_feature_for_clusters")
    @patch("src.clustering.clustering.display_histograms_per_clusters")
    def test_run_not_only_db_no_test(self,
                                     mock_cluster_display,
                                     mock_feature_display,
                                     mock_statistic_display,
                                     mock_validators_print,
                                     mock_df_labels_display,
                                     mock_scatter_plot_display,
                                     mock_get_df_with_clusters,
                                     mock_run_data_preprocessing):
        """ Test should call run clustering methods without filtering workflows with regard to db"""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(TestingClusteringParameters, '__init__', return_value=None):

            with patch.object(ClusteringMethod, 'K_MEANS', return_value=['test_result']) as mock_k_means:

                # given
                clustering = initialize_mock_clustering()

                # when
                clustering.run((), (), test_params=False, save_data=True)
                clustering.preprocessing_operations = [
                    ClusteringPreProcessing.FILTER_TEST_WORKFLOWS,
                    ClusteringPreProcessing.MERGE_STATUSES]

                # then
                mock_run_data_preprocessing.assert_called_once()
                mock_k_means.assert_called_once_with(['test_reduced_data'])
                mock_get_df_with_clusters.assert_called_once_with(
                    clustering.data, 'test_result')
                mock_scatter_plot_display.assert_called_once_with(
                    ['test_reduced_data'], 'test_result', clustering.name)
                mock_df_labels_display.assert_called_once_with(
                    ['test_data_with_labels'], clustering.name)
                mock_validators_print.assert_called_once_with(
                    ['test_result'], 'test_result', ['test_reduced_data'], True)
                mock_statistic_display.assert_called_once_with(
                    ['test_data_with_labels'], 'test_result', clustering.analyzed_features,
                    clustering.name, statistics_to_display=ClusteringObjective.WORKFLOWS.value)
                mock_feature_display.assert_called_once_with(
                    ['test_data_with_labels'], clustering.analyzed_features, 'test_result', clustering.name)
                mock_cluster_display.assert_called_once_with(
                    ['test_data_with_labels'], clustering.analyzed_features, 'test_result', clustering.name)


if __name__ == '__main__':
    main()
