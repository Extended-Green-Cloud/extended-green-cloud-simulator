import pandas as pd

from unittest import TestCase, main
from src.clustering.cluster_reader import get_points_for_cluster, create_cluster_with_point_or_append_point, get_data_for_points_in_cluster, get_df_with_cluster_labels, get_workflows_for_label
from src.helpers.feature_encoder import WORKFLOW_FEATURES


class TestGetPointsForCluster(TestCase):
    '''
    Class with tests of the method get_points_for_cluster
    '''

    def test_get_points_for_cluster_no_points_for_cluster(self):
        ''' Test should return empty list when there are not points in a given cluster. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_params = [
            [],
            [1, 2, 3, 1, 2, 3, 1, 1]
        ]
        test_cluster_label = 4

        for labels_assignment in test_params:
            with self.subTest(labels_assignment=labels_assignment):
                # when
                actual = get_points_for_cluster(
                    labels_assignment, test_cluster_label)

                # then
                self.assertTrue(len(actual) == 0,
                                f'Method should return empty list')

    def test_get_points_for_cluster(self):
        ''' Test should return nonempty list of points assigned to a given cluster. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_labels = [1, 2, 3, 1, 2, 3, 1, 1]
        test_cluster_label = 2

        # when
        actual = get_points_for_cluster(test_labels, test_cluster_label)

        # then
        expected = [1, 4]

        self.assertListEqual(expected, actual,
                             f'Method should return {expected} but returned {actual}')


class TestGetDataForPointsInCluster(TestCase):
    '''
    Class with tests of the method get_data_for_points_in_cluster
    '''

    def test_get_data_for_points_in_cluster_no_points_in_cluster(self):
        ''' Test should return empty list when there are not points in a given cluster. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_data = [10, 12, 13, 17, 20, 32, 11, 18]
        test_params = [
            [],
            [1, 2, 3, 1, 2, 3, 1, 1]
        ]
        test_cluster_label = 4

        for labels_assignment in test_params:
            with self.subTest(labels_assignment=labels_assignment):
                # when
                actual = get_data_for_points_in_cluster(
                    test_data, labels_assignment, test_cluster_label)

                # then
                self.assertTrue(len(actual) == 0,
                                f'Method should return empty list')

    def test_get_data_for_points_in_cluster(self):
        ''' Test should return nonempty list of points assigned to a given cluster. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_data = [10, 12, 13, 17, 20, 32, 11, 18]
        test_labels = [1, 2, 3, 1, 2, 3, 1, 1]
        test_cluster_label = 2

        # when
        actual = get_data_for_points_in_cluster(
            test_data, test_labels, test_cluster_label)

        # then
        expected = [12, 20]

        self.assertListEqual(expected, actual,
                             f'Method should return {expected} but returned {actual}')


class TestGetClusterWithAppendedPoint(TestCase):
    '''
    Class with tests of the method get_cluster_with_appended_point
    '''

    def test_get_cluster_with_appended_point_nonexisting_cluster(self):
        ''' Test should return new cluster with a single data point when the cluster was not existing previously. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_clusters = {
            1: [10, 20, 30]
        }
        test_point = 15
        test_cluster = 2

        # when
        actual = create_cluster_with_point_or_append_point(
            test_point, test_cluster, test_clusters)

        # then
        expected_new_cluster = [15]

        self.assertListEqual(expected_new_cluster, actual,
                             f'Method should return {expected_new_cluster} but returned {actual}')

    def test_get_cluster_with_point_existing_cluster(self):
        ''' Test should return data already assigned to a given cluster with new data point appended when the cluster has already exist. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_clusters = {
            1: [10, 20, 30]
        }
        test_point = 15
        test_cluster = 1

        # when
        actual = create_cluster_with_point_or_append_point(
            test_point, test_cluster, test_clusters)

        # then
        expected_new_cluster = [10, 20, 30, 15]

        self.assertListEqual(expected_new_cluster, actual,
                             f'Method should return {expected_new_cluster} but returned {actual}')


class TestDFWithClusterLabels(TestCase):
    '''
    Class with tests of the method get_df_with_cluster_labels
    '''

    def test_assign_clustering_labels_for_df(self):
        ''' Test should return data frame with appended column containing clustering labels '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: [10, 20, 30],
            WORKFLOW_FEATURES.MEMORY: [20, 100, 120]
        })
        test_labels = [1, 2, 2]

        # when
        actual = get_df_with_cluster_labels(test_df, test_labels)

        # then
        self.assertTrue('label' in list(actual.columns),
                        f'Created data frame should contain column label')
        self.assertListEqual(test_labels, list(actual['label']),
                             f'Column with labels should contain values {test_labels} but instead contains {list(actual["label"])}')


class TestGetWorkflowsForLabel(TestCase):
    '''
    Class with tests of the method get_workflows_for_label
    '''

    def test_get_workflows_for_label(self):
        ''' Test should return data frame containing only workflows with selected label '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: [10, 20, 30],
            WORKFLOW_FEATURES.MEMORY: [20, 100, 120],
            'label': ['1', '2', '2']
        })
        test_label = 2.0

        # when
        actual = get_workflows_for_label(test_df, test_label)

        # then
        expected_size = 2

        self.assertEqual(expected_size, len(actual),
                         f'Method should return {expected_size} records but returned {len(actual)}')


if __name__ == '__main__':
    main()
