import numpy as np
import pandas as pd

from unittest import TestCase, main
from unittest.mock import patch
import plotly.graph_objects as go

from src.helpers.subplot_maker import initialize_subplot, add_feature_trace
from src.helpers.feature_encoder import WORKFLOW_FEATURES


class TestInitializeSubPlot(TestCase):
    '''
    Class with tests of the method initialize_subplot
    '''

    def test_initialize_subplot(self):
        """ Test should call make_subplot with correct number of rows and columns """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        test_cols = 5
        subplots_no = 10

        # when
        actual = initialize_subplot(subplots_no, test_cols)
        row_range, cols_range = actual._get_subplot_rows_columns()

        # then
        self.assertEqual(range(1, 3), row_range,
                         f'Row range should be {range(1, 3)}')
        self.assertEqual(range(1, 6), cols_range,
                         f'Column range should be {range(1, 6)}')


class TestAddFeatureTrace(TestCase):
    '''
    Class with tests of the method add_feature_trace
    '''

    @patch('plotly.graph_objects.Figure')
    def test_add_feature_trace(self, mock_fig):
        """ Test should create correct plot trace """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        name = 'test name'
        data = pd.DataFrame({
            WORKFLOW_FEATURES.CPU: [10, 20, 400, 600]
        })
        feature = WORKFLOW_FEATURES.CPU
        idx = 1

        # when
        add_feature_trace(name, mock_fig, data, feature, idx)

        # then
        mock_fig.add_trace.assert_called_with(go.Histogram(
            x=np.array([10, 20, 400, 600]),
            name='test name'),
            row=1,
            col=2
        )


if __name__ == '__main__':
    main()
