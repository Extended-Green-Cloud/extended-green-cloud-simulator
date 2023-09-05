import pandas as pd

from pandas.testing import assert_frame_equal
from unittest import TestCase, main
from src.helpers.feature_encoder import WORKFLOW_FEATURES
from src.helpers.workflow_filter import filter_workflows_by_label, filter_out_undefined_workflows, filter_workflows_steps_features


class TestFilterWorkflowsByLabel(TestCase):
    '''
    Class with tests of the method filter_workflows_by_label
    '''

    def test_filter_workflows_by_label(self):
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
        actual = filter_workflows_by_label(test_df, test_label)

        # then
        expected_size = 2

        self.assertEqual(expected_size, len(actual),
                         f'Method should return {expected_size} records but returned {len(actual)}')


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

class TestFilterWorkflowsStepsFeatures(TestCase):
    '''
    Class with tests of the method filter_workflows_steps_features
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
        actual = filter_workflows_steps_features(test_df)

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
