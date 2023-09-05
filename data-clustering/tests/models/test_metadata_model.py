import pandas as pd

from unittest import TestCase, main
from src.models.Metadata import Metadata
from tests.mocks.mock_workflows import MetadataMocks


class TestMetadataClass(TestCase):
    '''
    Class contains tests of Metadata class
    '''

    def test_initialize_metadata_for_all_fields_specified(self):
        ''' Test should initialize Metadata class with all fields specified when initialization parameters contain values for all keys '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_argo_metadata = MetadataMocks.FULL_METADATA.value
        test_db_data = {'order_name': 'test_order_name'}

        # when
        actual = Metadata(test_argo_metadata, test_db_data)

        self.assertEqual(actual.uid, 'test_uid')
        self.assertEqual(actual.orderId, 'test_order_id')
        self.assertEqual(actual.orderItemId, 'test_order_item_id')
        self.assertEqual(actual.orderName, 'test_order_name')

    def test_initialize_metadata_for_order_name_not_specified(self):
        ''' Test should initialize Metadata class with order name being "undefined" when order name is not specified '''
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_argo_metadata = MetadataMocks.FULL_METADATA.value
        test_params = [{'order_name': pd.NA}, {}]

        for db_data in test_params:
            with self.subTest(database_data=db_data):
                # when
                actual = Metadata(test_argo_metadata, db_data)

                self.assertEqual(actual.uid, 'test_uid')
                self.assertEqual(actual.orderId, 'test_order_id')
                self.assertEqual(actual.orderItemId, 'test_order_item_id')
                self.assertEqual(actual.orderName, 'undefined')


if __name__ == '__main__':
    main()
