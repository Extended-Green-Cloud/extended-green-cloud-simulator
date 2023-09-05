import numpy as np
import pandas as pd

from os import path, remove
from pandas.testing import assert_frame_equal
from unittest import TestCase, main
from unittest.mock import patch
from src.helpers.path_reader import PathReader, get_data_file_path
from src.clustering.clustering_evaluation import calculate_xie_beni_index, ClusteringMetrics, print_clustering_metrics, save_validation_metrics


class TestCalculateXieBeniIndex(TestCase):
    '''
    Class with tests of the method calculate_xie_beni_index
    '''

    def test_calculate_xie_beni_index(self):
        """ Test should return correct value of Xie-Beni coefficient. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_data = np.array([
            [1, 2, 3],
            [0.2, 0.3, 0.4],
            [0.5, 0.6, 0.7],
            [5, 6, 7],
        ])
        test_membership_matrix = np.array([
            [0.8, 0.2, 0.3, 0.9],
            [0.2, 0.8, 0.7, 0.1]
        ])
        test_centers = np.array([
            [4.2, 5.4, 6.3],
            [0.3, 0.45, 0.6]
        ])
        test_cluster_no = 2
        test_params = [
            (2, 0.11320),
            (3, 0.07098)
        ]

        for test_fuzzier, expected in test_params:
            with self.subTest(test_fuzzier=test_fuzzier):
                # when
                actual = calculate_xie_beni_index(test_data,
                                                  test_fuzzier, test_membership_matrix, test_centers, test_cluster_no)

                # then
                self.assertAlmostEqual(actual, expected, 4,
                                       f'Method should return {expected} but returned {actual}')


class TestPrintClusteringMetrics(TestCase):
    '''
    Class with tests of the method print_clustering_metrics
    '''

    def test_print_clustering_metrics(self):
        """ Test should return data frame with correct values of indicated clustering metrics. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ClusteringMetrics,
                          'SILHOUETTE',
                          return_value=('Silhouette Score', 10)) as silhouette:
            with patch.object(ClusteringMetrics,
                              'CALINSKI',
                              return_value=('Calinski-Harabasz Score', 2)) as calinski:

                # given
                test_params = [
                    (
                        [ClusteringMetrics.SILHOUETTE],
                        pd.DataFrame({
                            'Cluster no.': 2,
                            'Silhouette Score': 10
                        }, index=[0])
                    ),
                    (
                        [ClusteringMetrics.CALINSKI, ClusteringMetrics.SILHOUETTE],
                        pd.DataFrame({
                            'Cluster no.': 2,
                            'Calinski-Harabasz Score': 2,
                            'Silhouette Score': 10
                        }, index=[0])
                    )
                ]
                test_labels = [1, 2, 2]

                for test_scores, expected in test_params:
                    with self.subTest(test_scores=test_scores):
                        # when
                        actual = print_clustering_metrics(
                            test_scores, None, test_labels)

                        # then
                        try:
                            assert_frame_equal(actual, expected)
                        except AssertionError as e:
                            raise self.failureException(
                                f'Method should return {expected} but returned {actual}') from e

                silhouette.assert_called_with(None, test_labels)
                calinski.assert_called_once_with(None, test_labels)


class TestSaveValidationMetrics(TestCase):
    '''
    Class with tests of the method save_validation_metrics
    '''

    def test_save_validation_metrics(self):
        """ Test should save validation results in .csv file. """

        # given
        test_df = pd.DataFrame({
            'silhouette': [10],
            'davies-bouldin': [5],
            'calinski': [2]
        })
        test_name = 'test'

        # given
        file_name = 'test-validation-metrics.csv'
        output_file = PathReader.CLUSTERING_PATH(test_name, file_name, True)

        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), validation metrics file should not exist')

        # when
        save_validation_metrics(test_df, test_name, True)

        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_file, True)), f'Saved metrics file should exist')


if __name__ == '__main__':
    main()
