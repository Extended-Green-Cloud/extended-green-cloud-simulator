import pandas as pd


from os import path, remove

from plotly.graph_objects import Figure
from pandas.testing import assert_frame_equal
from unittest.mock import patch
from unittest import TestCase, main
from src.data.data_parser import import_workflow_data, import_workflow_data_database, save_parsed_workflow_data, import_workflows_from_csv
from src.exploratory.exploratory_analysis import ExploratoryAnalysis
from src.helpers.path_reader import get_data_file_path, EXPLORATORY_RESULTS_DIR
from src.helpers.feature_encoder import WORKFLOW_FEATURES


class TestExploratoryAnalysisClass(TestCase):
    '''
    Class with tests of the ExploratoryAnalysis class
    '''

    def initialize_test_data(self) -> ExploratoryAnalysis:
        save_parsed_workflow_data(import_workflow_data(True), True)
        test_workflows = import_workflows_from_csv(True)
        test_workflow_db = import_workflow_data_database(True)
        test_features = [WORKFLOW_FEATURES.CPU, WORKFLOW_FEATURES.MEMORY]

        return ExploratoryAnalysis(test_workflows, test_workflow_db, test_features, is_test=True)

    def test_initialize_exploratory_analysis(self):
        """ Test should correctly initialize values of ExploratoryAnalysis class. """
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # when
        actual = self.initialize_test_data()

        # then
        self.assertEqual(True, actual.is_test)
        self.assertEqual(False, actual.store_result)

    def test_get_workflows_for_order(self):
        """ Test should return workflows corresponding to given order id."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()

        # when
        actual = exploratory.get_workflows_for_order("test_order_id")
        actual_order_names = list(actual[WORKFLOW_FEATURES.ORDER_NAME])

        # then
        self.assertEqual(2, len(actual))
        self.assertTrue('test_name_3' not in actual_order_names)

    def test_save_analysis_result(self):
        """ Test should save analysis results in indicated directory."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        actual = self.initialize_test_data()
        test_file_name = 'test_results.csv'

        output_file = path.join(EXPLORATORY_RESULTS_DIR, test_file_name)
        if path.exists(get_data_file_path(output_file, True)):
            remove(get_data_file_path(output_file, True))

        self.assertFalse(path.exists(get_data_file_path(
            output_file, True)), f'Initially (for test), results file should not exist')

        # when
        actual.save_analysis_result(actual.data_db, test_file_name)

        # then
        self.assertTrue(path.exists(get_data_file_path(output_file, True)),
                        f'Analysis results file used in test should exist')

    @patch("src.exploratory.exploratory_analysis.display")
    def test_display_codes_for_columns_without_save(self, mock_display):
        """ Test should return workflows corresponding to given order id."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()
        test_col_name = WORKFLOW_FEATURES.PROCESSOR_TYPE

        # when
        actual = exploratory.display_codes_for_column(
            exploratory.data_argo, test_col_name)

        # then
        expected = pd.DataFrame({
            "Processor name": ["test_processor_name_1", "test_processor_name_2"],
            "Processor name code": [0, 1],
            "Count": [1, 1]
        })

        mock_display.assert_called_once()

        try:
            assert_frame_equal(actual, expected)
        except AssertionError as e:
            raise self.failureException(
                f'Method should return {expected} but returned {actual}') from e

    @patch("src.exploratory.exploratory_analysis.display")
    @patch("src.exploratory.exploratory_analysis.ExploratoryAnalysis.save_analysis_result")
    def test_display_codes_for_columns_with_save(self, mock_save, mock_display):
        """ Test should return workflows corresponding to given order id and save result in .csv."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()
        exploratory.store_result = True
        test_col_name = WORKFLOW_FEATURES.PROCESSOR_TYPE

        # when
        exploratory.display_codes_for_column(
            exploratory.data_argo, test_col_name)

        # then
        mock_display.assert_called_once()
        mock_save.assert_called_once()

    def test_multivariate_analysis_correlation_matrix(self):
        """ Test should compute correlation matrix and save it as .png file."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Figure, 'show') as mock_show:
            # given
            exploratory = self.initialize_test_data()

            output_file = path.join(EXPLORATORY_RESULTS_DIR,
                                    'multivariate-correlation-matrix.png')
            if path.exists(get_data_file_path(output_file, True)):
                remove(get_data_file_path(output_file, True))

            self.assertFalse(path.exists(get_data_file_path(
                output_file, True)), f'Initially (for test), correlation matrix file should not exist')

            # when
            exploratory.multivariate_analysis_correlation_matrix()

            # then
            mock_show.assert_called_once()
            self.assertTrue(path.exists(get_data_file_path(output_file, True)),
                            f'Correlation matrix file used in test should exist')

    def test_multivariate_analysis_scatter(self):
        """ Test should compute scatter matrix and save it as .png file."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        with patch.object(Figure, 'show') as mock_show:
            # given
            exploratory = self.initialize_test_data()

            output_file = path.join(EXPLORATORY_RESULTS_DIR,
                                    'multivariate-scatter-matrix.png')
            if path.exists(get_data_file_path(output_file, True)):
                remove(get_data_file_path(output_file, True))

            self.assertFalse(path.exists(get_data_file_path(
                output_file, True)), f'Initially (for test), scatter matrix file should not exist')

            # when
            exploratory.multivariate_analysis_scatter()

            # then
            mock_show.assert_called_once()
            self.assertTrue(path.exists(get_data_file_path(output_file, True)),
                            f'Scatter matrix file used in test should exist')

    @patch("src.exploratory.exploratory_analysis.initialize_subplot")
    @patch("src.exploratory.exploratory_analysis.add_feature_trace")
    @patch("src.exploratory.exploratory_analysis.display_and_save_multiplot")
    def test_univariate_analysis(self,
                                 mock_display_save,
                                 mock_add_features,
                                 mock_initialize):
        """ Test should prepare ans save subplot of given type with summary of initialized features."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()
        test_type = 'box'

        # when
        exploratory.univariate_analysis(test_type)

        # then
        mock_initialize.assert_called_once_with(2)
        mock_display_save.assert_called_once()
        self.assertEqual(2, mock_add_features.call_count)

    @patch("src.exploratory.exploratory_analysis.append_coefficient_of_variance")
    @patch("src.exploratory.exploratory_analysis.filter_out_undefined_workflows")
    def test_display_statistics_summary_without_save(self,
                                                     mock_filter,
                                                     mock_append):
        """ Test should print summary of features statistics for both: 1) all workflows and 2) workflows only having db records."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()

        # when
        exploratory.display_statistics_summary()

        # then
        mock_filter.assert_called_once_with(exploratory.data_argo)
        self.assertEqual(2, mock_append.call_count)

    @patch("src.exploratory.exploratory_analysis.append_coefficient_of_variance")
    @patch("src.exploratory.exploratory_analysis.filter_out_undefined_workflows")
    @patch("src.exploratory.exploratory_analysis.ExploratoryAnalysis.save_analysis_result")
    def test_display_statistics_summary_with_save(self,
                                                  mock_save_result,
                                                  mock_filter,
                                                  mock_append):
        """ Test should print and save summary of features statistics for both: 1) all workflows and 2) workflows only having db records."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()
        exploratory.store_result = True

        # when
        exploratory.display_statistics_summary()

        # then
        self.assertEqual(2, mock_save_result.call_count)

    @patch("src.exploratory.exploratory_analysis.get_column_count")
    @patch("src.exploratory.exploratory_analysis.filter_out_undefined_workflows")
    def test_analyze_workflows_statuses_without_save(self,
                                                     mock_column_count,
                                                     mock_filter):
        """ Test should display summary of workflow statuses."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()

        # when
        exploratory.analyze_workflows_statuses()

        # then
        mock_filter.assert_called_once()
        mock_column_count.assert_called_once()

    @patch("src.exploratory.exploratory_analysis.get_column_count")
    @patch("src.exploratory.exploratory_analysis.filter_out_undefined_workflows")
    @patch("src.exploratory.exploratory_analysis.ExploratoryAnalysis.save_analysis_result")
    def test_analyze_workflows_statuses_with_save(self,
                                                  mock_save_result,
                                                  mock_column_count,
                                                  mock_filter):
        """ Test should display and save summary of workflow statuses."""
        print(f'\nTEST ({self._testMethodName}): {self.shortDescription()}')

        # given
        exploratory = self.initialize_test_data()
        exploratory.store_result = True

        # when
        exploratory.analyze_workflows_statuses()

        # then
        mock_save_result.assert_called_once()


if __name__ == '__main__':
    main()
