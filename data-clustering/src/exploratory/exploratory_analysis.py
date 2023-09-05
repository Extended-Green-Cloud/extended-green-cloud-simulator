import pandas as pd
import numpy as np
import plotly.express as ptx
import plotly.io as pio

from IPython.display import display
from typing import List

from src.helpers.value_reader import FORMATTER, FORMATTER_CORR
from src.helpers.path_reader import PathReader
from src.helpers.feature_encoder import WORKFLOW_FEATURES, DB_FEATURES
from src.helpers.statistics_operations import get_column_count, filter_out_undefined_workflows, convert_fields_to_numeric, append_coefficient_of_variance
from src.helpers.subplot_maker import initialize_subplot, add_feature_trace, display_and_save_multiplot

from src.exploratory.exploratory_constants import STATUS_FIELDS, STATS_FOR_WORKFLOWS_WITHOUT_ARGO_FILES, ARGO_ORDER_AGGREGATION, CATEGORICAL_FIELDS, DB_ORDER_AGGREGATION


class ExploratoryAnalysis():
    '''
    Class represents exploratory analysis segment of the module.
    '''

    def __init__(self,
                 workflows: pd.DataFrame,
                 workflows_db: pd.DataFrame,
                 features: List[str],
                 store_result: bool = False,
                 is_test: bool = False) -> None:
        '''
        Method initialize exploratory analysis module.

        Parameters:
        workflows - set of workflows based on argo files
        workflows_db - set of workflows based on database
        features - list of features taken into account in analysis
        store_result - flag indicating if the result should be saved to .csv
        is_test - flag indicating if the method should use test path
        '''
        workflows = convert_fields_to_numeric(workflows, features)

        self.data_argo = workflows.fillna('undefined')
        self.data_db = workflows_db.fillna('undefined')
        self.features = features
        self.store_result = store_result
        self.is_test = is_test

    def run_all(self) -> None:
        '''
        Method runs exploratory analysis.
        '''
        self.analyze_categorical_features()
        self.analyze_workflow_steps()
        self.analyze_workflows_without_db_records()
        self.analyze_workflows_without_argo_files()
        self.analyze_workflows_statuses()
        self.display_statistics_summary()
        self.univariate_analysis('box')
        self.univariate_analysis('hist')
        self.multivariate_analysis_scatter()
        self.multivariate_analysis_correlation_matrix()

    def analyze_categorical_features(self) -> None:
        '''
        Method runs segment responsible for categorical features analysis.
        '''
        data = filter_out_undefined_workflows(self.data_argo)

        for field in CATEGORICAL_FIELDS:
            self.display_codes_for_column(data, field)

        unique_orders = get_column_count(data, DB_ORDER_AGGREGATION, False)
        self.display_codes_for_column(
            unique_orders, WORKFLOW_FEATURES.ORDER_STATUS)
        
    def analyze_workflow_steps(self) -> None:
        '''
        Method runs segment responsible for displaying unique workflows steps.
        '''
        data = filter_out_undefined_workflows(self.data_argo)
        workflow_steps = np.unique(list(data[WORKFLOW_FEATURES.WORKFLOW_STEPS]))
        steps_names = np.unique([el for steps in workflow_steps for el in steps.split('>')])
        display(pd.DataFrame({'Unique steps': steps_names.T}))

    def analyze_workflows_without_db_records(self) -> None:
        '''
        Method uns segment responsible for analyzing statistics of workflows which have argo files but do not have corresponding database records.
        '''
        undefined_workflows = self.data_argo[self.data_argo[WORKFLOW_FEATURES.ORDER_NAME] == 'undefined']
        undefined_workflows = get_column_count(
            undefined_workflows, ARGO_ORDER_AGGREGATION, False, False)

        if self.store_result:
            file_name = f'workflows-without-db-records-count.csv'
            self.save_analysis_result(undefined_workflows, file_name)

        display(undefined_workflows)

    def analyze_workflows_without_argo_files(self) -> None:
        '''
        Method uns segment responsible for analyzing statistics of workflows which have database records but do not have corresponding argo files
        '''
        data_db = self.data_db
        data_argo = self.data_argo

        workflows_without_argo = data_db[
            (~data_db[DB_FEATURES.WORKFLOW_UID].isin(
                list(data_argo[WORKFLOW_FEATURES.WORKFLOW_UID])))
            | (data_db[DB_FEATURES.WORKFLOW_UID].isna())
        ]

        unique_orders = np.unique(
            workflows_without_argo[WORKFLOW_FEATURES.ORDER_ID])
        data_no_argo = [order for order in unique_orders if
                        len(self.get_workflows_for_order(order)) == 0]
        data_no_argo = data_db[data_db[WORKFLOW_FEATURES.ORDER_ID].isin(
            data_no_argo)]

        for params in STATS_FOR_WORKFLOWS_WITHOUT_ARGO_FILES:
            data = params['filter'](data_no_argo)
            columns_count = get_column_count(data, params['columns'])
            display(columns_count)

            if self.store_result:
                self.save_analysis_result(columns_count, params['name'])

    def analyze_workflows_statuses(self) -> None:
        '''
        Method runs segment responsible for printing statistics of workflows statuses.
        '''
        data = filter_out_undefined_workflows(self.data_argo)
        sorted_workflows = get_column_count(data, STATUS_FIELDS, False)

        if self.store_result:
            file_name = f'workflow-status-analysis.csv'
            self.save_analysis_result(sorted_workflows, file_name)

        display(sorted_workflows)

    def display_statistics_summary(self) -> None:
        '''
        Method runs segment displaying summary of workflows statistics for:
        1. all workflows 
        2. workflows which have both database records and argo files.
        '''
        stats = self.data_argo[self.features].describe().apply(FORMATTER)
        stats = append_coefficient_of_variance(stats)

        data = filter_out_undefined_workflows(self.data_argo)
        stats_filtered = data[self.features].describe().apply(FORMATTER)
        stats_filtered = append_coefficient_of_variance(stats_filtered)

        print(
            f"\nStatistics for all statuses (workflows no. {stats[WORKFLOW_FEATURES.CPU]['count']}):")
        display(stats.drop('count'))

        print(
            f"\nStatistics for workflows with database records (workflows no. {stats_filtered[WORKFLOW_FEATURES.CPU]['count']}):")
        display(stats_filtered.drop('count'))

        if self.store_result:
            self.save_analysis_result(
                stats, f'workflow-all-statistics-summary.csv')
            self.save_analysis_result(
                stats_filtered, f'workflow-with-db-records-statistics-summary.csv')

    def univariate_analysis(self, type: str) -> None:
        '''
        Method runs segment responsible for univariate analysis.

        Parameters:
        type - type of subplot
        '''
        fig = initialize_subplot(len(self.features))

        for idx, column in enumerate(self.data_argo[self.features].describe().columns):
            add_feature_trace(column, fig, self.data_argo,
                              column, idx, filter_outliers=False, type=type)

        fig_title = f'{"Boxplot" if type == "box" else "Histogram"} of workflow fields'
        file_name = f'univariate-{type}-analysis.png'
        file_path = PathReader.EXPLORATORY_PATH(file_name, self.is_test)

        display_and_save_multiplot(fig, fig_title, file_path)

    def multivariate_analysis_scatter(self) -> None:
        '''
        Method runs segment responsible for displaying scatter matrix of features.
        '''
        fig = ptx.scatter_matrix(self.data_argo,
                                 dimensions=self.features,
                                 color=WORKFLOW_FEATURES.ORDER_ITEM_STATUS,
                                 width=1600,
                                 height=1600)

        file_name = 'multivariate-scatter-matrix.png'
        file_path = PathReader.EXPLORATORY_PATH(file_name, self.is_test)

        pio.write_image(fig, file_path, format='png')
        fig.show()

    def multivariate_analysis_correlation_matrix(self) -> None:
        '''
        Method runs segment responsible for displaying correlation matrix of features.
        '''
        correlation_metrics = self.data_argo[self.features].corr().apply(
            FORMATTER_CORR)

        fig = ptx.imshow(correlation_metrics.to_numpy(),
                         x=self.features,
                         y=self.features,
                         text_auto=True,
                         width=1400,
                         height=1000)

        file_name = 'multivariate-correlation-matrix.png'
        file_path = PathReader.EXPLORATORY_PATH(file_name, self.is_test)

        pio.write_image(fig, file_path, format='png')
        fig.show()

    def save_analysis_result(self, data: pd.DataFrame, name: str,) -> None:
        '''
        Method saves the results of analysis in .csv file

        Parameters:
        data - data frame to be saved
        name - name of the file
        '''
        file_path = PathReader.EXPLORATORY_PATH(name, self.is_test)
        data.to_csv(file_path)

    def display_codes_for_column(self, data: pd.DataFrame,  column_name: str) -> None:
        '''
        Method displays the codes assigned to given column values.

        Parameters:
        data - data frame
        column_name - name of the column taken into account
        '''
        grouped_values = data.groupby(
            [column_name, f'{column_name}_code']).size().to_frame().reset_index()

        name = column_name.replace('_', ' ').capitalize()
        grouped_values.columns = [name, f'{name} code', 'Count']

        sorted_result = grouped_values.sort_values(
            by=['Count'], ascending=False)

        if self.store_result:
            file_name = f'{column_name.replace("_", "-")}-count.csv'
            self.save_analysis_result(sorted_result, file_name)

        display(sorted_result)
        return sorted_result

    def get_workflows_for_order(self, order: str) -> pd.DataFrame:
        '''
        Method returns database workflows of given order which have corresponding argo files.

        Parameters:
        order - order id for which the workflows are to be retrieved

        Returns: database workflows for given order which have argo files
        '''
        return self.data_db[(self.data_db[WORKFLOW_FEATURES.ORDER_ID] == order) & (
            self.data_db[DB_FEATURES.WORKFLOW_UID].isin(list(self.data_argo[WORKFLOW_FEATURES.WORKFLOW_UID])))]
