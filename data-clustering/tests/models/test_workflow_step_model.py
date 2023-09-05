from datetime import datetime
from unittest import TestCase, main
from unittest.mock import patch
from src.models.WorkflowStep import WorkflowStep
from tests.mocks.mock_workflows import WorkflowStepMocks


class TestWorkflowStepClass(TestCase):
    '''
    Class contains tests of WorkflowStep class
    '''

    def test_initialize_default_step_execution_details(self):
        ''' Test should initialize default values of step execution. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(WorkflowStep, '__init__', return_value=None):
            MockWorkflowStep = WorkflowStep(None, None, None)

        # when
        MockWorkflowStep.initialize_default_step_execution_details()

        # then
        self.assertEqual(
            MockWorkflowStep.status, 'Unknown', f'Method should return Unknown but returned {MockWorkflowStep.status}')
        self.assertEqual(
            MockWorkflowStep.node, 'Unspecified', f'Method should return Unspecified but returned {MockWorkflowStep.node}')
        self.assertEqual(
            MockWorkflowStep.cpu, 0, f'Method should return 0 but returned {MockWorkflowStep.cpu}')
        self.assertEqual(
            MockWorkflowStep.memory, 0, f'Method should return 0 but returned {MockWorkflowStep.memory}')
        self.assertEqual(
            MockWorkflowStep.ephemeralStorage, 0, f'Method should return 0 but returned {MockWorkflowStep.ephemeralStorage}')
        self.assertEqual(
            MockWorkflowStep.duration, 0, f'Method should return 0 but returned {MockWorkflowStep.duration}')

    def test_initialize_step_resources(self):
        ''' Test should increment resource utilization parameters by values specified in step execution details. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(WorkflowStep, '__init__', return_value=None):
            MockWorkflowStep = WorkflowStep(None, None, None)

        # given
        test_step_details = [
            {},
            {"resourcesDuration": {
                "cpu": 50,
                "memory": 100,
                "ephemeral-storage": 10
            }},
            {"resourcesDuration": {
                "cpu": 20,
                "ephemeral-storage": 30
            }}
        ]

        # when
        MockWorkflowStep.initialize_default_step_execution_details()
        MockWorkflowStep.initialize_step_resources(test_step_details)

        # then
        expected_cpu = 70
        expected_memory = 100
        expected_ephemeral = 40

        self.assertEqual(
            MockWorkflowStep.cpu, expected_cpu, f'Method should return {expected_cpu} but returned {MockWorkflowStep.cpu}')
        self.assertEqual(
            MockWorkflowStep.memory, expected_memory, f'Method should return {expected_memory} but returned {MockWorkflowStep.memory}')
        self.assertEqual(
            MockWorkflowStep.ephemeralStorage, expected_ephemeral, f'Method should return {expected_ephemeral} but returned {MockWorkflowStep.ephemeralStorage}')

    def test_initialize_workflow_step_with_default_values(self):
        ''' Test should initialize workflow step with only default values when not all fields of step specification and details are specified. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_step_spec = {'name': 'test_name'}
        test_step_details = None
        test_retry_details = None

        # when
        MockWorkflowStep = WorkflowStep(
            test_step_spec, test_step_details, test_retry_details)

        # then
        self.assertEqual(
            MockWorkflowStep.name, 'test_name', f'Method should return test_name but returned {MockWorkflowStep.name}')
        self.assertEqual(
            MockWorkflowStep.status, 'Unknown', f'Method should return Unknown but returned {MockWorkflowStep.status}')
        self.assertEqual(
            MockWorkflowStep.node, 'Unspecified', f'Method should return Unspecified but returned {MockWorkflowStep.node}')
        self.assertEqual(
            MockWorkflowStep.retryLimit, 0, f'Method should return 0 but returned {MockWorkflowStep.retryLimit}')
        self.assertEqual(
            MockWorkflowStep.cpu, 0, f'Method should return 0 but returned {MockWorkflowStep.cpu}')
        self.assertEqual(
            MockWorkflowStep.memory, 0, f'Method should return 0 but returned {MockWorkflowStep.memory}')
        self.assertEqual(
            MockWorkflowStep.ephemeralStorage, 0, f'Method should return 0 but returned {MockWorkflowStep.ephemeralStorage}')
        self.assertEqual(
            MockWorkflowStep.duration, 0, f'Method should return 0 but returned {MockWorkflowStep.duration}')

    def test_initialize_workflow_step_no_retry_strategy(self):
        ''' Test should initialize workflow step with step specification and details values but no retry strategy when retry strategy is unspecified. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_step_spec = WorkflowStepMocks.ONLY_STEP_SPEC.value
        test_step_details = WorkflowStepMocks.ONLY_STEP_DETAILS.value
        test_retry_details = None

        # when
        MockWorkflowStep = WorkflowStep(
            test_step_spec, test_step_details, test_retry_details)

        # then
        expected_start = datetime(2020, 12, 1, 12, 00, 00)
        expected_end = datetime(2020, 12, 1, 12, 00, 30)

        self.assertEqual(
            MockWorkflowStep.name, 'test_name', f'Method should return test_name but returned {MockWorkflowStep.name}')
        self.assertEqual(
            MockWorkflowStep.status, 'Succeeded', f'Method should return Succeeded but returned {MockWorkflowStep.status}')
        self.assertEqual(
            MockWorkflowStep.node, 'test_host', f'Method should return test_host but returned {MockWorkflowStep.node}')
        self.assertEqual(
            MockWorkflowStep.startTime, expected_start, f'Method should return {expected_start} but returned {MockWorkflowStep.startTime}')
        self.assertEqual(
            MockWorkflowStep.endTime, expected_end, f'Method should return {expected_end} but returned {MockWorkflowStep.endTime}')
        self.assertEqual(
            MockWorkflowStep.retryLimit, 10, f'Method should return 10 but returned {MockWorkflowStep.retryLimit}')
        self.assertEqual(
            MockWorkflowStep.cpu, 70, f'Method should return 70 but returned {MockWorkflowStep.cpu}')
        self.assertEqual(
            MockWorkflowStep.memory, 100, f'Method should return 100 but returned {MockWorkflowStep.memory}')
        self.assertEqual(
            MockWorkflowStep.ephemeralStorage, 40, f'Method should return 40 but returned {MockWorkflowStep.ephemeralStorage}')
        self.assertEqual(
            MockWorkflowStep.duration, 30, f'Method should return 30 but returned {MockWorkflowStep.duration}')

    def test_initialize_workflow_step_with_retry_strategy(self):
        ''' Test should initialize workflow step with step specification and retry strategy  when retry strategy is specified. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_step_spec = WorkflowStepMocks.ONLY_STEP_SPEC.value
        test_step_details = WorkflowStepMocks.ONLY_STEP_DETAILS.value
        test_retry_details = WorkflowStepMocks.ONLY_STEP_RETRY.value

        # when
        MockWorkflowStep = WorkflowStep(
            test_step_spec, test_step_details, test_retry_details)

        # then
        expected_start = datetime(2020, 12, 1, 13, 00, 00)
        expected_end = datetime(2020, 12, 1, 14, 00, 00)

        self.assertEqual(
            MockWorkflowStep.name, 'test_name', f'Method should return test_name but returned {MockWorkflowStep.name}')
        self.assertEqual(
            MockWorkflowStep.status, 'Failed', f'Method should return Failed but returned {MockWorkflowStep.status}')
        self.assertEqual(
            MockWorkflowStep.node, 'test_host', f'Method should return test_host but returned {MockWorkflowStep.node}')
        self.assertEqual(
            MockWorkflowStep.startTime, expected_start, f'Method should return {expected_start} but returned {MockWorkflowStep.startTime}')
        self.assertEqual(
            MockWorkflowStep.endTime, expected_end, f'Method should return {expected_end} but returned {MockWorkflowStep.endTime}')
        self.assertEqual(
            MockWorkflowStep.retryLimit, 10, f'Method should return 10 but returned {MockWorkflowStep.retryLimit}')
        self.assertEqual(
            MockWorkflowStep.cpu, 70, f'Method should return 70 but returned {MockWorkflowStep.cpu}')
        self.assertEqual(
            MockWorkflowStep.memory, 100, f'Method should return 100 but returned {MockWorkflowStep.memory}')
        self.assertEqual(
            MockWorkflowStep.ephemeralStorage, 40, f'Method should return 40 but returned {MockWorkflowStep.ephemeralStorage}')
        self.assertEqual(
            MockWorkflowStep.duration, 3600, f'Method should return 3600 but returned {MockWorkflowStep.duration}')


if __name__ == '__main__':
    main()
