from unittest import TestCase, main
from src.helpers.path_reader import get_data_file_path, get_file_path_to_exploratory_results, get_file_path_to_clustering_results, get_file_path_to_input_data, parse_file_name, get_file_path_to_argo_data, get_file_path_to_database_data, ORDERS_INPUT_FILE


class TestGetDataFilePath(TestCase):
    '''
    Class with tests of the method get_data_file_path
    '''

    def test_get_data_file_path(self):
        """ Test should return file path for src data directory. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # give
        test_file_name = 'test_file.txt'

        # when
        actual = get_data_file_path(test_file_name)

        # then
        expected_end = 'data-clustering\\src\\data\\test_file.txt'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')

    def test_get_data_file_path_test_dir(self):
        """ Test should return file path for test data directory. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # give
        test_file_name = 'test_file.txt'

        # when
        actual = get_data_file_path(test_file_name, True)

        # then
        expected_end = 'data-clustering\\tests\\data\\test_file.txt'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')


class TestParseFileName(TestCase):
    '''
    Class with tests of the method parse_file_name
    '''

    def test_parse_file_name(self):
        """ Test should return file name with blank spaces replaced with hyphens. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # give
        test_params = [
            ('nonmodifiablename', 'nonmodifiablename'),
            ('nameWithUpper', 'namewithupper'),
            ('name with dash', 'name-with-dash')
        ]

        for test_file_name, expected in test_params:
            with self.subTest(test_file_name=test_file_name):
                # when
                actual = parse_file_name(test_file_name)

                # then
                self.assertEqual(actual, expected,
                                 f'Method should return {expected} but returned {actual}')


class TestGetFilePathToExploratoryResults(TestCase):
    '''
    Class with tests of the method get_file_path_to_exploratory_results
    '''

    def test_get_file_path_to_exploratory_results(self):
        """ Test should return correct path to file in exploratory results directory. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # give
        test_file_name = 'test_file.txt'

        # when
        actual = get_file_path_to_exploratory_results(test_file_name)

        # then
        expected_end = 'data-clustering\\src\\data\\results\\exploratory-results\\test_file.txt'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')


class TestGetFilePathToInputData(TestCase):
    '''
    Class with tests of the method get_file_path_to_input_data
    '''

    def test_get_file_path_to_input_data(self):
        """ Test should return correct path to file in input directory. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        actual = get_file_path_to_input_data()

        # then
        expected_end = 'data-clustering\\src\\data\\input\\input_workflows.csv'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')

    def test_get_file_path_to_input_data_for_orders(self):
        """ Test should return correct path to orders file in input directory. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        actual = get_file_path_to_input_data(
            is_test=True, file_name=ORDERS_INPUT_FILE)

        # then
        expected_end = 'data-clustering\\tests\\data\\input\\input_orders.csv'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')


class TestGetFilePathToArgoData(TestCase):
    '''
    Class with tests of the method get_file_path_to_argo_data
    '''

    def test_get_file_path_to_argo_data(self):
        """ Test should return correct path to test argo data. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        actual = get_file_path_to_argo_data(True)

        # then
        expected_end = 'data-clustering\\tests\\mocks\\mock-input'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')


class TestGetFilePathToDatabaseData(TestCase):
    '''
    Class with tests of the method get_file_path_to_database_data
    '''

    def test_get_file_path_to_database_data(self):
        """ Test should return correct path to test workflow database data. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        actual = get_file_path_to_database_data(True)

        # then
        expected_end = 'data-clustering\\tests\\mocks\\mock-input\\database.csv'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')


class TestGetFilePathToClusteringResults(TestCase):
    '''
    Class with tests of the method get_file_path_to_exploratory_results
    '''

    def test_get_file_path_to_clustering_results(self):
        """ Test should return correct path to file in clustering results directory. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # give
        test_file_name = 'test_file.txt'
        test_dir = 'test-dir'

        # when
        actual = get_file_path_to_clustering_results(test_dir, test_file_name)

        # then
        expected_end = 'data-clustering\\src\\data\\results\\clustering-results\\test-dir\\test_file.txt'

        self.assertTrue(actual.endswith(
            expected_end), f'Method should return path that ends with {expected_end} but instead it is {actual}')


if __name__ == '__main__':
    main()
