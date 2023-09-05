from datetime import datetime
from unittest import TestCase, main
from unittest.mock import patch
from src.models.Workflow import Workflow
from tests.mocks.mock_workflows import WorkflowSpecMocks, WorkflowStatusMocks, MetadataMocks, DatabaseMocks


class TestWorkflowClass(TestCase):
    '''
    Class contains tests of Workflow class
    '''

    def test_get_task_completion_percentage_for_zeros(self):
        '''Test should return 0 when progress values are zero. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        test_progress = "0/0"

        # when
        MockWorkflow.get_task_completion_percentage(test_progress)
        actual = MockWorkflow.completion

        # then
        expected = 0

        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')

    def test_get_task_completion_percentage_nonzero(self):
        '''Test should return nonzero value when progress is greater than zero. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        test_params = [("3/3", 1), ("2/4", 0.5)]

        for test_progress, expected in test_params:
            with self.subTest(test_progress=test_progress):
                # when
                MockWorkflow.get_task_completion_percentage(test_progress)
                actual = MockWorkflow.completion

                self.assertEqual(actual, expected,
                                 f'Method should return {expected} but returned {actual}')

    def test_get_detailed_status_message_undefined(self):
        '''Test should return undefined message if there is no message key or message is empty. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        test_params = [
            {},
            {'message': ''}
        ]

        for test_message in test_params:
            with self.subTest(test_message=test_message):
                # when
                actual = MockWorkflow.get_detailed_status_message(test_message)

                self.assertEqual(actual, 'undefined',
                                 f'Method should return undefined but returned {actual}')

    def test_get_detailed_status_message(self):
        '''Test should return correct detailed message for different message types. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        test_params = [
            ({'message': 'Msg: No more retries left'}, 'no more retries left'),
            ({'message': 'request Timed out'}, 'request timed out'),
            ({'message': 'Error: LEADER CHANGED'}, 'leader changed'),
            ({'message': 'Workflow execution stopped'},
             'stopped with strategy "stop"'),
            ({'message': 'cannot get resource error'}, 'resource not found'),
            ({'message': 'different message'}, 'undefined')
        ]

        for test_message, expected in test_params:
            with self.subTest(test_message=test_message):
                # when
                actual = MockWorkflow.get_detailed_status_message(test_message)

                self.assertEqual(actual, expected,
                                 f'Method should return {expected} but returned {actual}')

    def test_get_undefined_status_succeeded(self):
        '''Test should return message that product was processed successfully when the argo status is succeeded. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        MockWorkflow.argo_workflow_status = 'Succeeded'

        # when
        actual = MockWorkflow.get_undefined_status()

        # then
        expected = 'product processed successfully'

        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')

    def test_get_undefined_status_error(self):
        '''Test should return message about undefined error when the argo status is different than succeeded. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        MockWorkflow.argo_workflow_status = 'Failed'

        # when
        actual = MockWorkflow.get_undefined_status()

        # then
        expected = 'undefined error'

        self.assertEqual(actual, expected,
                         f'Method should return {expected} but returned {actual}')

    def test_get_output_message_output_not_available(self):
        '''Test should return message about undefined error when output parameters are not available. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        MockWorkflow.argo_workflow_status = 'Failed'
        test_params = [
            {},
            {'outputs': {}}
        ]

        for test_output in test_params:
            with self.subTest(test_output=test_output):
                # when
                actual = MockWorkflow.get_output_message(test_output)

                # then
                expected = 'undefined error'

                self.assertEqual(actual, expected,
                                 f'Method should return {expected} but returned {actual}')

    def test_get_output_message_output_no_matching_msg(self):
        '''Test should return message about undefined error when there is no matching message in output parameters. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        MockWorkflow.argo_workflow_status = 'Failed'
        test_params = [
            {'outputs': {
                'parameters': [{
                    'name': 'different name'
                }]
            }},
            {'outputs': {
                'parameters': [{
                    'name': 'final_state',
                    'value': "{\"status\": 0}"
                }]
            }},
            {'outputs': {
                'parameters': [{
                    'name': 'final_state',
                    'value': "{\"status\": 0, \"message\": \"\"}"
                }]
            }}
        ]

        for test_output in test_params:
            with self.subTest(test_output=test_output):
                # when
                actual = MockWorkflow.get_output_message(test_output)

                # then
                expected = 'undefined error'

                self.assertEqual(actual, expected,
                                 f'Method should return {expected} but returned {actual}')

    def test_get_output_message_output(self):
        '''Test should return message in lower case corresponding to output parameters. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        MockWorkflow.argo_workflow_status = 'Failed'
        test_params = [
            ({'outputs': {
                'parameters': [{
                    'name': 'final_state',
                    'value': "{\"status\": 0, \"message\": \"Message1\"}"
                }]
            }}, 'message1'),
            ({'outputs': {
                'parameters': [{
                    'name': 'final_state',
                    'value': "{\"status\": 0, \"message\": \"successfully processed\"}"
                }]
            }}, 'product processed successfully')
        ]

        for test_output, expected in test_params:
            with self.subTest(test_output=test_output):
                # when
                actual = MockWorkflow.get_output_message(test_output)

                # then
                self.assertEqual(actual, expected,
                                 f'Method should return {expected} but returned {actual}')

    def test_initialize_steps_for_no_steps(self):
        '''Test should assign empty list of steps when no steps are specified in specification. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        test_params = [{}, {'templates': []}]

        for test_spec in test_params:
            with self.subTest(test_spec=test_spec):
                # when
                MockWorkflow.initialize_steps(test_spec, None)
                actual_steps = MockWorkflow.steps
                actual_steps_no = MockWorkflow.steps_no

                # then
                self.assertEqual(actual_steps_no, 0,
                                 f'Method should return 0 but returned {actual_steps_no}')
                self.assertListEqual(actual_steps, [],
                                     f'Method should return [] but returned {actual_steps}')

    def test_initialize_steps(self):
        '''Test should assign list of steps and no steps for given nodes specification. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Workflow, '__init__', return_value=None):
            MockWorkflow = Workflow(None, None, None, None)

        # given
        test_spec = WorkflowSpecMocks.ONLY_TEMPLATE_NAMES.value
        test_status = WorkflowStatusMocks.ONLY_NODES_FULL.value

        # when
        MockWorkflow.initialize_steps(test_spec, test_status)
        actual_steps = MockWorkflow.steps
        actual_septs_names = [step.name for step in actual_steps]
        actual_steps_no = MockWorkflow.steps_no

        expected_steps_names = ['exit', 'test_name_2']

        # then
        self.assertEqual(actual_steps_no, 2,
                         f'Method should return 2 but returned {actual_steps_no}')
        self.assertListEqual(actual_septs_names, expected_steps_names,
                             f'Method should return {expected_steps_names} but returned {actual_septs_names}')

    def test_initialize_workflows_incomplete_data(self):
        '''Test should initialize workflow with default values for missing data fields. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_database_data = {}
        test_metadata = MetadataMocks.FULL_METADATA.value
        test_spec = WorkflowSpecMocks.ONLY_TEMPLATES.value
        test_status = WorkflowStatusMocks.PHASE_AND_RESOURCES.value

        # when
        MockWorkflow = Workflow(
            test_database_data, test_metadata, test_spec, test_status)

        # then
        self.assertEqual('Succeeded', MockWorkflow.argo_workflow_status,
                         f'Method should return Succeeded but returned { MockWorkflow.argo_workflow_status}')
        self.assertEqual('undefined', MockWorkflow.order_item_status,
                         f'Method should return undefined but returned { MockWorkflow.order_item_status}')
        self.assertEqual('undefined', MockWorkflow.order_status,
                         f'Method should return undefined but returned { MockWorkflow.order_status}')
        self.assertEqual(0, MockWorkflow.priority,
                         f'Method should return 0 but returned { MockWorkflow.priority}')

    def test_initialize_workflows_full_data(self):
        '''Test should initialize workflow with all data fields. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_database_data = DatabaseMocks.FULL.value
        test_metadata = MetadataMocks.FULL_METADATA.value
        test_spec = WorkflowSpecMocks.ONLY_TEMPLATES.value
        test_status = WorkflowStatusMocks.PHASE_AND_RESOURCES.value

        # when
        MockWorkflow = Workflow(
            test_database_data, test_metadata, test_spec, test_status)

        # then
        self.assertEqual('Succeeded', MockWorkflow.argo_workflow_status,
                         f'Method should return Succeeded but returned { MockWorkflow.argo_workflow_status}')
        self.assertEqual('done', MockWorkflow.order_item_status,
                         f'Method should return done but returned { MockWorkflow.order_item_status}')
        self.assertEqual('done_with_errors', MockWorkflow.order_status,
                         f'Method should return done_with_errors but returned { MockWorkflow.order_status}')
        self.assertEqual(10, MockWorkflow.priority,
                         f'Method should return 10 but returned { MockWorkflow.priority}')


if __name__ == '__main__':
    main()
