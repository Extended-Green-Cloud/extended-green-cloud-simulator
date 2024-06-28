from unittest import TestCase, main

from shutil import copyfile
from os import path, remove, mkdir
from src.helpers.path_reader import get_data_file_path, INPUT_DIR, WORKFLOWS_INPUT_FILE, ORDERS_INPUT_FILE
from src.data.data_parser import import_workflow_data_database, import_workflow_data, save_parsed_workflow_data, import_workflows_from_csv, import_workflows_from_json, import_orders_from_workflows, save_parsed_order_data, import_orders_from_csv, import_workflows_from_clustering_file
from src.helpers.feature_encoder import WORKFLOW_FEATURES


class TestImportWorkflowDataDatabase(TestCase):
    '''
    Class with tests of the method import_workflow_data_database
    '''

    def test_import_workflow_data_database(self):
        ''' Test should return data frame test workflow database records. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        test_workflow_db = import_workflow_data_database(is_test=True)

        # then
        self.assertEqual(3, len(
            test_workflow_db), f'Method should return 2 database records but returned {len(test_workflow_db)}')

        self.assertListEqual(
            ['test_name_1', 'test_name_2', 'test_name_3'],
            list(test_workflow_db['name']),
            f'Method should database records with names [test_name_1, test_name_2, test_name_3] but returned {list(test_workflow_db["name"])}')


class TestImportWorkflowData(TestCase):
    '''
    Class with tests of the method import_workflow_data
    '''

    def test_import_workflow_data(self):
        ''' Test should return list of test workflows with data corresponding to test argo files. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        test_workflows = import_workflow_data(is_test=True)
        workflows_statuses = [
            workflow.argo_workflow_status for workflow in test_workflows]

        # then
        self.assertEqual(2, len(
            test_workflows), f'Method should return 2 workflows but returned {len(test_workflows)}')
        self.assertEqual(['Succeeded', 'Succeeded'], workflows_statuses,
                         f'Method should return workflows with statuses Succeeded, Succeeded but returned {workflows_statuses}')


class TestImportOrdersFromWorkflows(TestCase):
    '''
    Class with tests of the method import_orders_from_workflows
    '''

    def test_import_orders_from_workflows(self):
        ''' Test should return list of orders aggregating given workflows. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows = import_workflow_data(is_test=True)

        # when
        actual = import_orders_from_workflows(test_workflows)
        parsed_order = actual[0]

        # then
        self.assertEqual(len(actual), 1)
        self.assertEqual(parsed_order.cpu, 300)
        self.assertEqual(parsed_order.memory, 130)
        self.assertEqual(parsed_order.ephemeral_storage, 50)
        self.assertEqual(parsed_order.storage, 21)
        self.assertEqual(parsed_order.status, "done")
        self.assertEqual(parsed_order.id, "test_order_id")
        self.assertEqual(parsed_order.name, "test_order_name")
        self.assertEqual(parsed_order.processed_size, 0)
        self.assertEqual(len(parsed_order.workflows), 2)


class TestSaveParsedWorkflowData(TestCase):
    '''
    Class with tests of the method save_parsed_workflow_data
    '''

    def test_save_parsed_workflow_data(self):
        ''' Test should create file with saved workflow data. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        output_file = path.join(INPUT_DIR, WORKFLOWS_INPUT_FILE)
        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), saved workflow data file should not exist')

        # when
        test_workflows = import_workflow_data(is_test=True)
        save_parsed_workflow_data(test_workflows, is_test=True)

        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_file, True)), f'Saved workflow data file should exist')


class TestSaveParsedOrderData(TestCase):
    '''
    Class with tests of the method save_parsed_order_data
    '''

    def test_save_parsed_order_data(self):
        ''' Test should create file with saved order data. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows = import_workflow_data(is_test=True)
        test_orders = import_orders_from_workflows(test_workflows)

        output_file = path.join(INPUT_DIR, ORDERS_INPUT_FILE)
        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), saved order data file should not exist')

        # when
        save_parsed_order_data(test_orders, is_test=True)

        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_file, True)), f'Saved order data file should exist')


class TestImportWorkflowsFromCsv(TestCase):
    '''
    Class with tests of the method import_workflows_from_csv
    '''

    def test_import_workflows_from_csv(self):
        ''' Test should correctly import saved workflow data. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows = import_workflow_data(is_test=True)
        save_parsed_workflow_data(test_workflows, is_test=True)

        # when
        test_workflows = import_workflows_from_csv(is_test=True)
        workflows_statuses = list(
            test_workflows[WORKFLOW_FEATURES.ARGO_STATUS])

        # then
        self.assertEqual(2, len(
            test_workflows), f'Method should return 2 workflows but returned {len(test_workflows)}')
        self.assertEqual(['Succeeded', 'Succeeded'], workflows_statuses,
                         f'Method should return workflows with statuses [Succeeded, Succeeded] but returned {workflows_statuses}')


class TestImportOrdersFromCsv(TestCase):
    '''
    Class with tests of the method import_orders_from_csv
    '''

    def test_import_orders_from_csv(self):
        ''' Test should correctly import saved orders data. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_workflows = import_workflow_data(is_test=True)
        test_orders = import_orders_from_workflows(test_workflows)
        save_parsed_order_data(test_orders, is_test=True)

        # when
        actual = import_orders_from_csv(is_test=True)

        # then
        self.assertEqual(1, len(actual))


class TestImportWorkflowsFromClusteringFile(TestCase):
    '''
    Class with tests of the method import_workflows_from_clustering_file
    '''

    def test_import_workflows_from_clustering_file(self):
        ''' Test should correctly import saved and clustered workflow data. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_dir = 'test-clustering-results'
        test_file_name = 'test-workflow-clustered.csv'
        self.prepare_file_for_test(test_dir, test_file_name)

        # when
        actual = import_workflows_from_clustering_file(
            test_dir, test_file_name, True)
        workflows_statuses = list(actual[WORKFLOW_FEATURES.ARGO_STATUS])

        # then
        self.assertEqual(2, len(
            actual), f'Method should return 2 workflows but returned {len(actual)}')
        self.assertEqual(['Succeeded', 'Succeeded'], workflows_statuses,
                         f'Method should return workflows with statuses Succeeded, Succeeded but returned {workflows_statuses}')

    def prepare_file_for_test(self, test_dir, test_file_name):
        mock_workflows_path = path.abspath(path.join(
            '.', 'tests', 'mocks', 'mock-input', 'test-workflow-clustered.csv'))
        print(mock_workflows_path)

        destination_dir_path = path.abspath(path.join(
            '.', 'tests', 'data', 'results', 'clustering-results', test_dir))
        destination_path = path.join(destination_dir_path, test_file_name)

        if not path.exists(destination_dir_path):
            mkdir(destination_dir_path)

        self.assertTrue(path.exists(mock_workflows_path),
                        f'Workflows file used in test should exist')

        copyfile(mock_workflows_path, destination_path)


class TestImportWorkflowsFromJson(TestCase):
    '''
    Class with tests of the method import_workflows_from_json
    '''

    def test_import_workflows_from_json(self):
        ''' Test should correctly import workflow data from argo json files. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        output_file = path.join(INPUT_DIR, WORKFLOWS_INPUT_FILE)
        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), saved workflow data file should not exist')

        # when
        test_workflows, _ = import_workflows_from_json(is_test=True)
        workflows_statuses = list(
            test_workflows[WORKFLOW_FEATURES.ARGO_STATUS])

        # then
        self.assertTrue(path.exists(get_data_file_path(
            output_file, True)), f'Saved workflow data file should exist')
        self.assertEqual(2, len(
            test_workflows), f'Method should return 2 workflows but returned {len(test_workflows)}')
        self.assertEqual(['Succeeded', 'Succeeded'], workflows_statuses,
                         f'Method should return workflows with statuses Succeeded, Succeeded but returned {workflows_statuses}')


if __name__ == '__main__':
    main()
