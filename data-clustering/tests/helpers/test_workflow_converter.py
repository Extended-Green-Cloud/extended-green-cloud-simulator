from unittest import TestCase, main
from src.helpers.workflow_converter import convert_workflows_to_data_frame
from src.models.Workflow import Workflow
from tests.mocks.mock_workflows import MOCK_WORKFLOWS
from src.helpers.feature_encoder import WORKFLOW_FEATURES


class TestConvertWorkflowToDataFrame(TestCase):
    '''
    Class with tests of the method convert_workflows_to_data_frame
    '''

    def test_convert_workflows_to_data_frame(self):
        ''' Test should correctly convert list of workflows to data frame '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows = [Workflow(data['database_data'],
                                   data['metadata'],
                                   data['spec'],
                                   data['status'])
                          for data in MOCK_WORKFLOWS]

        # when
        actual = convert_workflows_to_data_frame(test_workflows)
        actual_columns = actual.columns
        actual_column_order_name = list(actual[WORKFLOW_FEATURES.ORDER_NAME])
        actual_column_order_status = list(
            actual[WORKFLOW_FEATURES.ORDER_STATUS])
        actual_column_step_2_memory = list(actual['step_2_memory'])
        actual_workflow_steps_encoded = list(
            actual[WORKFLOW_FEATURES.WORKFLOW_STEPS_ENCODED])

        # then
        expected_columns = ['uid', 'order_name', 'order_id', 'status', 'order_status',
       'argo_status', 'argo_detailed_status', 'argo_output_message',
       'processor_name', 'cpu', 'memory', 'ephemeral_storage', 'storage',
       'processed_size', 'duration', 'deadline', 'priority',
       'initial_steps_no', 'executed_steps_no', 'workflow_steps',
       'workflow_steps_encoded', 'nodes_per_step', 'nodes_per_step_encoded',
       'status_per_step', 'status_per_step_encoded', 'step_1_cpu',
       'step_2_cpu', 'step_3_cpu', 'step_1_memory', 'step_2_memory',
       'step_3_memory', 'step_1_ephemeral_storage', 'step_2_ephemeral_storage',
       'step_3_ephemeral_storage', 'step_1_duration', 'step_2_duration',
       'step_3_duration', 'status_code_cancelled', 'status_code_done',
       'order_status_code_done', 'order_status_code_processing',
       'argo_status_code_Succeeded', 'argo_detailed_status_code_undefined',
       'argo_output_message_code_critical error',
       'argo_output_message_code_product processed successfully',
       'processor_name_code_test_processor_name_1',
       'processor_name_code_test_processor_name_2']
        expected_column_order_name = ['test_order_1', 'undefined']
        expected_column_order_status = ['done', 'processing']
        expected_column_step_2_memory = ['50.0', '60.0']
        expected_workflow_steps_encoded = ['0>1', '1>2']

        self.assertCountEqual(actual_columns, expected_columns,
                              f'Method should return {expected_columns} but returned {actual_columns}')
        self.assertCountEqual(actual_column_order_name,
                              expected_column_order_name,
                              f'Method should return {expected_columns} but returned {actual_columns}')
        self.assertCountEqual(actual_column_order_status,
                              expected_column_order_status,
                              f'Method should return {expected_column_order_status} but returned {actual_column_order_status}')
        self.assertCountEqual(actual_column_step_2_memory,
                              expected_column_step_2_memory,
                              f'Method should return {expected_column_step_2_memory} but returned {actual_column_step_2_memory}')
        self.assertCountEqual(actual_workflow_steps_encoded,
                              expected_workflow_steps_encoded,
                              f'Method should return {expected_workflow_steps_encoded} but returned {actual_workflow_steps_encoded}')


if __name__ == '__main__':
    main()
