import pandas as pd

from unittest import TestCase, main
from src.helpers.parameter_concatenator import concatenate_parameters


class TestConcatenateParameters(TestCase):
    '''
    Class with tests of the method concatenate_parameters
    '''

    def test_concatenate_parameters(self):
        """ Test should return grid of all possible parameters combinations when lists of parameters are passed. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_params_1 = [10, 20, 30]
        test_params_2 = [12, 15]

        # when
        actual = concatenate_parameters(test_params_1, test_params_2)
        expected_col_1 = [10, 10, 20, 20, 30, 30]
        expected_col_2 = [12, 15, 12, 15, 12, 15]

        # then
        self.assertEqual(len(actual), 6,
                         f'Method should return 6 parameters combinations, but returned {len(actual)}')
        self.assertCountEqual(actual[:, 0], expected_col_1,
                              f'Method should return {expected_col_1}, but returned {actual[:, 0]}')
        self.assertCountEqual(actual[:, 1], expected_col_2,
                              f'Method should return {expected_col_2}, but returned {actual[:, 1]}')


if __name__ == '__main__':
    main()
