import pandas as pd

from unittest import TestCase, main
from src.helpers.feature_encoder import factorize_feature, encode_categorical_list


class TestFactorizeFeature(TestCase):
    '''
    Class with tests of the method factorize_feature
    '''

    def test_factorize_feature(self):
        ''' Test should return data frame with additional column that contains numerical representation of categorical feature. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        categorical_values = ["CPU", "MEMORY", "STORAGE"]
        test_df = pd.DataFrame(categorical_values, columns=["resource"])

        # when
        new_df = factorize_feature("resource", test_df)
        new_df_columns = list(new_df.columns)

        # then
        expected_columns = ['resource', 'resource_code']
        expected_codes = [0, 1, 2]

        self.assertListEqual(new_df_columns, expected_columns)
        self.assertListEqual(list(new_df['resource_code']), expected_codes)


class TestEncodeCategoricalList(TestCase):
    '''
    Class with tests of the method encode_categorical_list
    '''

    def test_encode_categorical_list_for_empty_list(self):
        ''' Test should return empty string if list is empty. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        unique_categorical_values = ["GPU", "CPU", "MEMORY", "STORAGE"]
        test_list = []

        # when
        actual_value, actual_encoded = encode_categorical_list(
            unique_categorical_values, test_list, "resource")

        # then
        expected_value = ""
        expected_encoded = ""

        self.assertEqual(actual_value, expected_value)
        self.assertEqual(actual_encoded, expected_encoded)

    def test_encode_categorical_list(self):
        ''' Test should string of values and encoded values for given list. '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        unique_categorical_values = ["GPU", "CPU", "MEMORY", "STORAGE"]
        test_list = [
            {"resource": "CPU"},
            {"resource": "MEMORY"},
            {"resource": "STORAGE"}
        ]

        # when
        actual_value, actual_encoded = encode_categorical_list(
            unique_categorical_values, test_list, "resource")

        # then
        expected_value = "CPU>MEMORY>STORAGE"
        expected_encoded = "1>2>3"

        self.assertEqual(actual_value, expected_value)
        self.assertEqual(actual_encoded, expected_encoded)


if __name__ == '__main__':
    main()
