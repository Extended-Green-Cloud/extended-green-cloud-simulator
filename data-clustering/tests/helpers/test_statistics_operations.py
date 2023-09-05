import pandas as pd

from pandas.testing import assert_frame_equal
from unittest import TestCase, main
from src.helpers.statistics_operations import calculate_cov_for_column, append_coefficient_of_variance, filter_out_outliers, convert_fields_to_numeric, calculate_jaccard_similarity, get_column_count, get_column_names_with_count, filter_out_undefined_workflows, filter_out_unused_step_features
from src.helpers.feature_encoder import WORKFLOW_FEATURES, DB_FEATURES


class TestCalculateCovForColumn(TestCase):
    '''
    Class with tests of the method calculate_cov_for_column
    '''

    def test_calculate_cov_for_column_zero_mean(self):
        """ Test should return 0 when mean is 0. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_column = {'mean': '0', 'std': '10'}

        # when
        actual = calculate_cov_for_column(test_column)

        # then
        expected = 0
        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')

    def test_calculate_cov_for_column_nonzero_mean(self):
        """ Test should return correct COV when mean is nonzero. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_column = {'mean': '2', 'std': '10'}

        # when
        actual = calculate_cov_for_column(test_column)

        # then
        expected = 5
        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')


class TestAppendCoefficientOfVariance(TestCase):
    '''
    Class with tests of the method append_coefficient_of_variance
    '''

    def test_append_coefficient_of_variance(self):
        """ Test should return append new row with coefficient of variance values. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: [10, 20],
            WORKFLOW_FEATURES.MEMORY: [2, 10]
        }, index=['std', 'mean'])

        # when
        actual = append_coefficient_of_variance(test_df)
        cov_row = actual.loc[['cov']].to_numpy()[0]

        # then
        self.assertEqual(len(cov_row), 2,
                         f'Coefficient of variance column should be appended to data frame')
        self.assertListEqual([0.5, 0.2], list(cov_row),
                             f'Method should append cov column with values 5, 2 but instead appended {cov_row}')


class TestFilterOutOutliers(TestCase):
    '''
    Class with tests of the method filter_out_outliers
    '''

    def test_filter_out_outliers(self):
        """ Test should return data frame without outliers. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: [2, 3, 2.5, 3.5, 10, 20, 400, 600]
        })

        # when
        actual = filter_out_outliers(test_df)
        actual = actual.dropna()

        # then
        expected = [2, 3, 2.5, 3.5, 10, 20]

        self.assertListEqual(expected, list(actual[WORKFLOW_FEATURES.CPU]),
                             f'Method should return {expected} but instead returned {list(actual["cpu"])}')


class TestConvertFieldsToNumeric(TestCase):
    '''
    Class with tests of the method convert_fields_to_numeric
    '''

    def test_convert_fields_to_numeric(self):
        """ Test should return data frame with numerical values instead of strings for indicated columns. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_columns = [WORKFLOW_FEATURES.CPU]
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: ['2', '10', '400', '600'],
            WORKFLOW_FEATURES.MEMORY: ['2', '10', '15', '10']
        })

        # when
        actual = convert_fields_to_numeric(test_df, test_columns)
        actual_cpu = list(actual[WORKFLOW_FEATURES.CPU])
        actual_memory = list(actual[WORKFLOW_FEATURES.MEMORY])

        # then
        expected_cpu = [2, 10, 400, 600]
        expected_memory = ['2', '10', '15', '10']

        self.assertListEqual(actual_cpu, expected_cpu,
                             f'For cpu method should return {expected_cpu} but returned {actual_cpu}')
        self.assertListEqual(actual_memory, expected_memory,
                             f'For memory method should return {expected_memory} but returned {actual_memory}')


class TestCalculateJaccardSimilarity(TestCase):
    '''
    Class with tests of the method calculate_jaccard_similarity
    '''

    def test_calculate_jaccard_similarity_non_overlapping_sets(self):
        """ Test should return 0 when two non-overlapping sets are passed """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_set_1 = [2, 3, 5]
        test_set_2 = [4, 6, 7]

        # when
        actual = calculate_jaccard_similarity(test_set_1, test_set_2)

        # then
        expected = 0
        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')

    def test_calculate_jaccard_similarity_equivalent_sets(self):
        """ Test should return 1 when two equivalent sets are passed """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_set_1 = [2, 3, 5]
        test_set_2 = [2, 3, 5]

        # when
        actual = calculate_jaccard_similarity(test_set_1, test_set_2)

        # then
        expected = 1
        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')

    def test_calculate_jaccard_similarity_partially_overlapping_sets(self):
        """ Test should return value between 0 and 1 when two partially overlapping sets are passed """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_set_1 = [2, 3, 5]
        test_set_2 = [2, 4, 5]

        # when
        actual = calculate_jaccard_similarity(test_set_1, test_set_2)

        # then
        expected = 0.5
        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')


class TestGetColumnNamesWithCount(TestCase):
    '''
    Class with tests of the method get_column_names_with_count
    '''

    def test_get_column_names_with_count_empty_list(self):
        """ Test should return only count column when no columns are passed """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_columns = []

        # when
        actual = get_column_names_with_count(test_columns)

        # then
        expected = ['Count']

        self.assertListEqual(expected, actual,
                             f'Method should return {expected} but returned {actual}')

    def test_get_column_names_with_count(self):
        """ Test should return names corresponding to given columns """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_columns = [DB_FEATURES.ORDER_ID, 'unknown']

        # when
        actual = get_column_names_with_count(test_columns)

        # then
        expected = ['Order ID', 'unknown', 'Count']

        self.assertListEqual(expected, actual,
                             f'Method should return {expected} but returned {actual}')


class TestGetColumnCount(TestCase):
    '''
    Class with tests of the method get_column_count
    '''

    def test_get_column_count(self):
        """ Test should return correct values count for columns """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.ORDER_ITEM_STATUS: ['Succeeded', 'Failed', 'Failed'],
            WORKFLOW_FEATURES.ORDER_ID: ['123', '123', '123']
        })
        test_columns = [WORKFLOW_FEATURES.ORDER_ITEM_STATUS,
                        WORKFLOW_FEATURES.ORDER_ID]

        # when
        actual = get_column_count(test_df, test_columns)

        # then
        self.assertEqual(2, len(actual),
                         f'Method should return 2 records but returned {len(actual)}')


class TestFilterOutUndefinedWorkflows(TestCase):
    '''
    Class with tests of the method filter_out_undefined_workflows
    '''

    def test_filter_out_undefined_workflows(self):
        """ Test should return workflows only with specified order names """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.ORDER_NAME: ['Test', 'undefined', 'Test'],
        })

        # when
        actual = filter_out_undefined_workflows(test_df)

        # then
        self.assertEqual(2, len(actual),
                         f'Method should return data frame of length 2.')


class TestFilterOutUnusedStepsFeatures(TestCase):
    '''
    Class with tests of the method filter_out_unused_step_features
    '''

    def test_filter_out_unused_step_features(self):
        """ Test should return data frame containing only step columns for which workflow steps are defined. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_df = pd.DataFrame({
            WORKFLOW_FEATURES.WORKFLOW_STEPS: [
                'step_1>step_2',
                'step_1>step_3'
            ],
            'step_1_cpu': [10, 5],
            'step_2_cpu': [15, 0],
            'step_3_cpu': [0, 20],
            'step_4_cpu': [0, 0],
            'step_5_cpu': [0, 0]
        })

        # when
        actual = filter_out_unused_step_features(test_df)

        # then
        expected = pd.DataFrame({
            WORKFLOW_FEATURES.WORKFLOW_STEPS: [
                'step_1>step_2',
                'step_1>step_3'
            ],
            'step_1_cpu': [10, 5],
            'step_2_cpu': [15, 0],
            'step_3_cpu': [0, 20]
        })

        try:
            assert_frame_equal(actual, expected)
        except AssertionError as e:
            raise self.failureException(
                f'Method should return {expected} but returned {actual}') from e


if __name__ == '__main__':
    main()
