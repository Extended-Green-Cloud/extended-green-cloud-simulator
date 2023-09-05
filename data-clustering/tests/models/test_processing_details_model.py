from datetime import datetime
from unittest import TestCase, main
from unittest.mock import patch
from src.models.ProcessingDetails import ProcessingDetails
from tests.mocks.mock_workflows import DatabaseMocks, WorkflowStatusMocks, WorkflowSpecMocks


class TestProcessingDetailsClass(TestCase):
    '''
    Class contains tests of ProcessingDetails class
    '''

    def test_get_deadline_for_dates_unspecified(self):
        ''' Test should assign 0 duration when one of the dates is not specified'''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_params = [
                {},
                {'add_date': "2020-12-1 12:00:00.00 Z"},
                {'referential_eta': "2020-12-1 12:00:00.00 Z"}
            ]

            for test_db in test_params:
                with self.subTest(test_database_data=test_db):
                    # when
                    MockProcessingDetails.get_deadline(test_db)
                    actual = MockProcessingDetails.deadline

                    # then
                    expected = 0

                    self.assertEqual(actual, expected,
                                     f'Method should return {expected} but returned {actual}')

    def test_get_deadline_for_dates_specified(self):
        ''' Test should assign nonzero duration when both dates are specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_database_data = DatabaseMocks.ONLY_DATES.value

            # when
            MockProcessingDetails.get_deadline(test_database_data)
            actual = MockProcessingDetails.deadline

            # then
            expected = 30

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_duration(self):
        ''' Test should assign nonzero duration when both dates are specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = WorkflowStatusMocks.ONLY_DATES.value

            # when
            MockProcessingDetails.get_duration(test_workflow_status)
            actual_duration = MockProcessingDetails.duration
            actual_start = MockProcessingDetails.startTime
            actual_end = MockProcessingDetails.endTime

            # then
            excepted_duration = 30
            excepted_start = datetime(2020, 12, 1, 12, 00, 00)
            excepted_end = datetime(2020, 12, 1, 12, 00, 30)

            self.assertEqual(actual_duration, excepted_duration,
                             f'Method should return {excepted_duration} but returned {actual_duration}')
            self.assertEqual(actual_start, excepted_start,
                             f'Method should return {excepted_start} but returned {actual_start}')
            self.assertEqual(actual_end, excepted_end,
                             f'Method should return {excepted_end} but returned {actual_end}')

    def test_get_storage_size_for_no_volume_claim(self):
        ''' Test should assign 0 storage size when volume claim was not specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_spec = {}

            # when
            MockProcessingDetails.get_storage_size(test_workflow_spec)
            actual = MockProcessingDetails.storage

            # then
            expected = 0

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_storage_size_for_different_volume_claims(self):
        ''' Test should assign nonzero storage size when volume claim is specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_params = [
                ('1Gi', 1),
                ('1Ki', 0.000001),
                ('1Mi', 0.001),
                ('1Ti', 1000),
                ('1Pi', 1000000),
                ('1Ei', 1152921504.61)
            ]

            for test_storage_size, expected in test_params:
                with self.subTest(test_storage_size=test_storage_size):
                    # given
                    test_workflow_spec = {
                        'volumeClaimTemplates': [
                            {'spec': {
                                'resources': {
                                    'requests': {
                                        'storage': test_storage_size
                                    }
                                }
                            }
                            }
                        ]
                    }
                    # when
                    MockProcessingDetails.get_storage_size(test_workflow_spec)
                    actual = MockProcessingDetails.storage

                    self.assertEqual(actual, expected,
                                     f'Method should return {expected} but returned {actual}')

    def test_get_final_processed_size_for_no_output(self):
        ''' Test should assign 0 processed size when output is not specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = {}

            # when
            MockProcessingDetails.get_final_processed_size(
                test_workflow_status)
            actual = MockProcessingDetails.processedSize

            # then
            expected = 0

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_final_processed_size_for_no_output(self):
        ''' Test should assign 0 processed size when output parameters list is empty '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = {'outputs': {'parameters': []}}

            # when
            MockProcessingDetails.get_final_processed_size(
                test_workflow_status)
            actual = MockProcessingDetails.processedSize

            # then
            expected = 0

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_final_processed_size_for_empty_params(self):
        ''' Test should assign 0 processed size when output parameters list is empty '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = {'outputs': {'parameters': []}}

            # when
            MockProcessingDetails.get_final_processed_size(
                test_workflow_status)
            actual = MockProcessingDetails.processedSize

            # then
            expected = 0

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_final_processed_size_for_specified_new_size(self):
        ''' Test should assign nonzero processed size when output parameters list contains final state size '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = WorkflowStatusMocks.ONLY_OUTPUTS.value

            # when
            MockProcessingDetails.get_final_processed_size(
                test_workflow_status)
            actual = MockProcessingDetails.processedSize

            # then
            expected = 782191340

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_workflow_steps(self):
        ''' Test should assign nonzero number of workflow steps corresponding to the number of defined dag templates'''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_templates = WorkflowSpecMocks.STORAGE_AND_DAG_TEMPLATE.value['templates']

            # when
            MockProcessingDetails.get_workflow_steps(test_templates)
            actual = MockProcessingDetails.workflowSteps

            # then
            expected = 2

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_executed_workflow_steps_for_no_nodes(self):
        ''' Test should assign 0 number of executed steps when nodes are not specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = {}

            # when
            MockProcessingDetails.get_executed_workflow_steps(
                test_workflow_status)
            actual = MockProcessingDetails.executedSteps

            # then
            expected = 0

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_get_executed_workflow_steps_for_nodes_specified(self):
        ''' Test should assign 0 number of executed steps when nodes are not specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(ProcessingDetails, '__init__', return_value=None):
            MockProcessingDetails = ProcessingDetails(None, None, None, None)

            # given
            test_workflow_status = WorkflowStatusMocks.ONLY_NODES.value

            # when
            MockProcessingDetails.get_executed_workflow_steps(
                test_workflow_status)
            actual = MockProcessingDetails.executedSteps

            # then
            expected = 3

            self.assertEqual(actual, expected,
                             f'Method should return {expected} but returned {actual}')

    def test_initialize_when_resource_duration_not_specified(self):
        ''' Test should initialize resource utilization with 0s when resource duration is not specified in argo file '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_metadata = {"labels": {}}
        test_workflow_spec = WorkflowSpecMocks.STORAGE_AND_DAG_TEMPLATE.value
        test_workflow_status = WorkflowStatusMocks.NODES_OUTPUTS_DATES.value
        test_database_data = DatabaseMocks.ONLY_DATES.value

        # when
        MockProcessingDetails = ProcessingDetails(
            test_metadata,
            test_workflow_spec,
            test_workflow_status,
            test_database_data
        )
        actual_processor_name = MockProcessingDetails.processor
        actual_cpu = MockProcessingDetails.cpu
        actual_memory = MockProcessingDetails.memory
        actual_ephemeral = MockProcessingDetails.ephemeralStorage

        # then
        expected_processor_name = 'undefined'
        expected = 0

        self.assertEqual(actual_processor_name, expected_processor_name,
                         f'Method should return {expected_processor_name} but returned {actual_processor_name}')
        self.assertEqual(actual_cpu, expected,
                         f'Method should return {expected} but returned {actual_cpu}')
        self.assertEqual(actual_memory, expected,
                         f'Method should return {expected} but returned {actual_memory}')
        self.assertEqual(actual_ephemeral, expected,
                         f'Method should return {expected} but returned {actual_ephemeral}')

    def test_initialize_with_resource_duration(self):
        ''' Test should initialize nonzero resource utilization when resource duration is specified in argo file '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_metadata = {"labels": {
            "processor-name": "test_name"
        }}
        test_workflow_spec = WorkflowSpecMocks.STORAGE_AND_DAG_TEMPLATE.value
        test_workflow_status = WorkflowStatusMocks.NODES_OUTPUTS_DATES_RESOURCES.value
        test_database_data = DatabaseMocks.ONLY_DATES.value

        # when
        MockProcessingDetails = ProcessingDetails(
            test_metadata,
            test_workflow_spec,
            test_workflow_status,
            test_database_data
        )
        actual_processor_name = MockProcessingDetails.processor
        actual_cpu = MockProcessingDetails.cpu
        actual_memory = MockProcessingDetails.memory
        actual_ephemeral = MockProcessingDetails.ephemeralStorage

        # then
        expected_processor_name = 'test_name'

        self.assertEqual(actual_processor_name, expected_processor_name,
                         f'Method should return {expected_processor_name} but returned {actual_processor_name}')
        self.assertEqual(actual_cpu, 991,
                         f'Method should return {991} but returned {actual_cpu}')
        self.assertEqual(actual_memory, 31251,
                         f'Method should return {31251} but returned {actual_memory}')
        self.assertEqual(actual_ephemeral, 42,
                         f'Method should return {42} but returned {actual_ephemeral}')


if __name__ == '__main__':
    main()
