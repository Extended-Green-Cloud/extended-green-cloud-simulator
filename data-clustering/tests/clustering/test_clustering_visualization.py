import numpy as np

from os import path, remove
from unittest import TestCase, main
from unittest.mock import patch

from src.data.data_parser import import_workflows_from_json
from src.helpers.path_reader import get_data_file_path, CLUSTERING_RESULTS_DIR, parse_file_name
from src.helpers.statistics_operations import convert_fields_to_numeric
from src.clustering.clustering import ClusteringObjective
from src.clustering.clustering_visualization import display_clustering_scatter_plot, display_data_frame_with_labels, display_cluster_statistics
from src.helpers.feature_encoder import WORKFLOW_FEATURES


class TestDisplayClusteringScatterPlot(TestCase):
    '''
    Class with tests of the method display_clustering_scatter_plot
    '''

    @patch('matplotlib.pyplot.show')
    def test_display_clustering_scatter_plot(self, mock_show):
        """ Test should save the scatter plot of clustering results in the indicated place """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_data = np.array([
            [10, 5],
            [11, 5],
            [12, 5],
            [13, 5],
            [15, 5]
        ])
        test_labels = [1, 1, 2, 2, 2]
        test_name = 'scatter_plot_visualization_test'

        file_name = f'{parse_file_name(test_name)}-result-features.png'
        output_file = path.join(CLUSTERING_RESULTS_DIR, test_name, file_name)

        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), scatter plot file should not exist')

        # when
        display_clustering_scatter_plot(
            test_data, test_labels, test_name, True)

        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_file, True)), f'Scatter plot file should exist')
        mock_show.assert_called_once()


class TestDisplayDataFrameWithLabels(TestCase):
    '''
    Class with tests of the method display_data_frame_with_labels
    '''

    def test_display_clustering_scatter_plot(self):
        """ Test should save the data frame with assigned labels in corresponding place. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows, _ = import_workflows_from_json(True)
        test_workflows['label'] = 0
        test_name = 'data_frame_with_labels_test'

        file_name = f'{parse_file_name(test_name)}-output-labels.csv'
        output_file = path.join(CLUSTERING_RESULTS_DIR, test_name, file_name)

        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), data frame with labels file should not exist')

        # when
        display_data_frame_with_labels(test_workflows, test_name, True)

        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_file, True)), f'Data frame with labels file should exist')


class TestDisplayClusterStatistics(TestCase):
    '''
    Class with tests of the method display_cluster_statistics
    '''

    def test_display_cluster_statistics(self):
        """ Test should save statistics of each cluster given labeled data. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows, _ = import_workflows_from_json(True)
        test_features = [WORKFLOW_FEATURES.CPU,
                         WORKFLOW_FEATURES.MEMORY,
                         WORKFLOW_FEATURES.DURATION]
        test_workflows = convert_fields_to_numeric(
            test_workflows, test_features)
        test_name = 'data_statistics_test'
        test_workflows['label'] = 0
        test_labels = [0]
        test_workflows['label'] = test_workflows['label'].astype(str)

        output_cluster_1 = path.join(CLUSTERING_RESULTS_DIR, test_name,
                                     f'{parse_file_name(test_name)}-cluster-1-data.csv')
        output_cluster_1_labeled = path.join(CLUSTERING_RESULTS_DIR, test_name,
                                             f'{parse_file_name(test_name)}-cluster-1-overall-statistics.csv')
        output_cluster_1_status = path.join(CLUSTERING_RESULTS_DIR, test_name,
                                            f'{parse_file_name(test_name)}-cluster-1-status-statistics.csv')
        output_cluster_1_processor = path.join(CLUSTERING_RESULTS_DIR, test_name,
                                               f'{parse_file_name(test_name)}-cluster-1-processor_name-statistics.csv')
        output_cluster_1_status_processor = path.join(CLUSTERING_RESULTS_DIR, test_name,
                                                      f'{parse_file_name(test_name)}-cluster-1-processor_name-status-statistics.csv')

        if path.exists(get_data_file_path(output_cluster_1, True)):
            remove(get_data_file_path(output_cluster_1, True))
        if path.exists(get_data_file_path(output_cluster_1_labeled, True)):
            remove(get_data_file_path(output_cluster_1_labeled, True))
        if path.exists(get_data_file_path(output_cluster_1_status, True)):
            remove(get_data_file_path(output_cluster_1_status, True))
        if path.exists(get_data_file_path(output_cluster_1_processor, True)):
            remove(get_data_file_path(output_cluster_1_processor, True))
        if path.exists(get_data_file_path(output_cluster_1_status_processor, True)):
            remove(get_data_file_path(output_cluster_1_status_processor, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_cluster_1, True)), f'Initially (for test), workflow statistic file for cluster 1 should not exist')
        self.assertFalse(path.exists(get_data_file_path(
            output_cluster_1_labeled, True)), f'Initially (for test), labeled workflow statistic file for cluster 1 should not exist')
        self.assertFalse(path.exists(get_data_file_path(
            output_cluster_1_status, True)), f'Initially (for test), status statistic file for cluster 1 should not exist')
        self.assertFalse(path.exists(get_data_file_path(
            output_cluster_1_processor, True)), f'Initially (for test), processor statistic file for cluster 1 should not exist')
        self.assertFalse(path.exists(get_data_file_path(
            output_cluster_1_status_processor, True)), f'Initially (for test), processor and status statistic file for cluster 1 should not exist')

        # when
        display_cluster_statistics(test_workflows,
                                   test_labels,
                                   test_features,
                                   test_name,
                                   ClusteringObjective.WORKFLOWS.value,
                                   True)
        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_cluster_1, True)), f'Data frame file for cluster 1 should exist')
        self.assertTrue(path.exists(get_data_file_path(
            output_cluster_1_labeled, True)), f'Labeled data frame file for cluster 1 should exist')
        self.assertTrue(path.exists(get_data_file_path(
            output_cluster_1_status, True)), f'Status statistic file for cluster 1 should exist')
        self.assertTrue(path.exists(get_data_file_path(
            output_cluster_1_processor, True)), f'Processor statistic file for cluster 1 should exist')
        self.assertTrue(path.exists(get_data_file_path(
            output_cluster_1_status_processor, True)), f'Processor and status statistic file for cluster 1 should exist')


if __name__ == '__main__':
    main()
