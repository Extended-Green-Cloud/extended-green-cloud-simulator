import pandas as pd

from datetime import datetime, timezone
from unittest import TestCase, main
from src.helpers.value_reader import read_value_or_return_default, read_first_or_return_default, read_date_value_or_return_default, read_json_or_return_default, DateParser


class TestReadValueOrReturnDefault(TestCase):
    '''
    Class with tests of the method read_value_or_return_default
    '''

    def test_read_value_or_return_default_for_unspecified_object(self):
        """ Test should return "undefined" when object and default value were not specified. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_key = 'key'
        test_object = None

        # when
        actual = read_value_or_return_default(test_key, test_object)

        # then
        excepted = 'undefined'

        self.assertEqual(actual, excepted,
                         'Method should return undefined')

    def test_read_value_or_return_default_for_key_not_present(self):
        """ Test should return "undefined" when key is not present in a given object. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_key = 'key'
        test_object = {'different_key': 0}

        # when
        actual = read_value_or_return_default(test_key, test_object)

        # then
        excepted = 'undefined'

        self.assertEqual(actual, excepted,
                         'Method should return undefined')

    def test_read_value_or_return_default_for_na_value(self):
        """ Test should return "undefined" when value for a given key is NA. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_key = 'key'
        test_object = {'key': pd.NA}

        # when
        actual = read_value_or_return_default(test_key, test_object)

        # then
        excepted = 'undefined'

        self.assertEqual(actual, excepted,
                         'Method should return undefined')

    def test_read_value_or_return_default_for_default_val(self):
        """ Test should return value corresponding to specified default. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_key = 'key'
        test_object = {}
        test_default_values = [None, 'default_string', 0]

        for default_val in test_default_values:
            with self.subTest(default=default_val, expected=default_val):
                # when
                actual = read_value_or_return_default(
                    test_key, test_object, default_val)

                # then
                excepted = default_val

                self.assertEqual(actual, excepted,
                                 f'Method should return {default_val}')

    def test_read_value_or_return_default_for_present_key_value(self):
        """ Test should return correct value of given key. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_object = {'key_1': 0, 'key_2': ['10Gi', '20Kb'], 'key_3': 'test'}
        test_params = [
            ('key_1', 0), ('key_2', ['10Gi', '20Kb']), ('key_3', 'test')]

        for key, value in test_params:
            with self.subTest(key=key, value=value):
                # when
                actual = read_value_or_return_default(key, test_object)

                # then
                excepted = value

                if key != 'key_2':
                    self.assertEqual(actual, excepted,
                                     f'Method should return {value}')
                else:
                    self.assertCountEqual(actual, excepted,
                                          f'Method should return {value}')


class TestReadDateValueOrReturnDefault(TestCase):
    '''
    Class with tests of the method read_date_value_or_return_default
    '''

    def test_read_date_value_or_return_default_for_no_date_string(self):
        """ Test should return None for the date string not present in the object. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_key = 'key'
        test_object = {}

        # when
        actual = read_date_value_or_return_default(test_key, test_object)

        # then
        excepted = None

        self.assertEqual(actual, excepted,
                         'Method should return None')

    def test_read_date_value_or_return_default_for_different_parsers(self):
        """ Test should return correctly parsed date for different parsers. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_key = 'date'
        test_params = [
            (DateParser.PARSER_ARGO, "2020-12-1T12:00:00Z",
             datetime(2020, 12, 1, 12, 00, 00)),
            (DateParser.PARSER_DATABASE, "2020-12-1 12:00:00.00 Z",
             datetime(2020, 12, 1, 12, 00, 00, 00, timezone.utc))
        ]

        for parser, date_string, date in test_params:
            with self.subTest(date_string=date_string, date=date):
                test_object = {test_key: date_string}
                # when
                actual = read_date_value_or_return_default(
                    test_key, test_object, parser)

                # then
                excepted = date

                self.assertEqual(actual, excepted,
                                 f'Method should return {date} but returned {actual}')


class TestReadFirstOrReturnDefault(TestCase):
    '''
    Class with tests of the method read_first_or_return_default
    '''

    def test_read_first_or_return_default_for_empty_list(self):
        """ Test should return None for empty list """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_list = []
        def predicate(x): return x == 'element'

        # when
        actual = read_first_or_return_default(test_list, predicate)

        # then
        expected = None

        self.assertEqual(actual, expected)

    def test_read_first_or_return_default_for_specified_default(self):
        """ Test should return empty directory if it is passed as default """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_list = []
        test_default = dict()
        def predicate(x): return x == 'element'

        # when
        actual = read_first_or_return_default(
            test_list, predicate, test_default)

        # then
        expected = dict()

        self.assertEqual(actual, expected)

    def test_read_first_or_return_default_for_no_value_matching_predicate(self):
        """ Test should return None no value matching predicate """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_list = ['other element']
        def predicate(x): return x == 'element'

        # when
        actual = read_first_or_return_default(test_list, predicate)

        # then
        expected = None

        self.assertEqual(actual, expected)

    def test_read_first_or_return_default_for_value_matching_predicate(self):
        """ Test should return first list value matching predicate for value present in the list """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_params = [
            [
                {'name': 'element', 'val': 10},
                {'name': 'element', 'val': 12}
            ],
            [{'name': 'element', 'val': 10}]
        ]
        def predicate(x): return x['name'] == 'element'

        for test_list in test_params:
            with self.subTest(test_list=test_list):
                # when
                actual = read_first_or_return_default(test_list, predicate)

                # then
                excepted = {'name': 'element', 'val': 10}

                self.assertDictEqual(actual, excepted,
                                     f'Method should return {excepted} but returned {actual}')


class TestReadJsonOrReturnDefault(TestCase):
    '''
    Class with tests of the method read_json_or_return_default
    '''

    def test_read_json_or_return_default_for_none(self):
        """ Test should return None when the json to be parsed is None. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_string_json = None

        # when
        actual = read_json_or_return_default(test_string_json)

        # then
        excepted = None

        self.assertEqual(actual, excepted,
                         'Method should return None')

    def test_read_json_or_return_default_for_correct_object(self):
        """ Test should return correctly parsed date for different parsers. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_string_json = '{"status": "Succeeded"}'

        # when
        actual = read_json_or_return_default(test_string_json)

        # then
        excepted = {'status': 'Succeeded'}

        self.assertEqual(actual, excepted,
                         f'Method should return {excepted} but returned {actual}')


if __name__ == '__main__':
    main()
