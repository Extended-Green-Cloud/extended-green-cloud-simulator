import pandas as pd
import numpy as np

from typing import List
from src.helpers.feature_encoder import DB_FEATURES, FEATURES_DISPLAY_NAMES, WORKFLOW_FEATURES
from sklearn.neighbors import LocalOutlierFactor


def calculate_cov_for_column(column) -> float:
    '''
    Method calculates coefficient of variance of given data frame column.

    Parameters:
    column - column for which coefficient of variance is calculated

    Returns: coefficient of variance
    '''
    return float(column['std']) / float(column['mean']) if float(column['mean']) != 0 else 0


def calculate_jaccard_similarity(set_1: List[int], set_2: List[int]) -> float:
    '''
    Method calculates Jaccard similarity measure.

    Parameters:
    set_1 - first set used for similarity comparison
    set_2 - first set used for similarity comparison

    Returns: Jaccard similarity
    '''
    intersection = set(set_1).intersection(set_2)
    union = set(set_1).union(set_2)

    return float(len(intersection) / len(union))


def calculate_lof(data: np.ndarray, q: int) -> float:
    '''
    Method computes local outlier factor for given data points.

    Parameters:
    data - points belonging to the given cluster
    q - size of the neighborhood point

    Returns: local outlier factor
    '''
    lof = LocalOutlierFactor(q)
    lof.fit_predict(data)

    return lof.negative_outlier_factor_ * (-1)


def append_coefficient_of_variance(df: pd.DataFrame) -> pd.DataFrame:
    '''
    Method computes coefficient of variance of given data frame statistics and appends it to the data frame.

    Parameters:
    df - data frame

    Returns: data frame with coefficient of variance column
    '''
    cov_column = [calculate_cov_for_column(df[column]) for column in df]
    return pd.concat([df, pd.Series(cov_column, index=df.columns, name='cov').to_frame().T])


def filter_out_outliers(df: pd.DataFrame) -> pd.DataFrame:
    '''
    Method returns data frame without outliers.

    Parameters:
    df - data frame
    '''
    q25 = df.quantile(0.25)
    q75 = df.quantile(0.75)
    IQR = q75 - q25

    return df[~((df < (q25 - 1.5 * IQR)) | (df > (q75 + 1.5 * IQR)))]


def find_outliers_no(df: pd.DataFrame) -> int:
    '''
    Method returns number of outliers in the data.

    Parameters:
    df - data frame
    '''
    q25 = df.quantile(0.25)
    q75 = df.quantile(0.75)
    IQR = q75 - q25

    return len(df) - len(df[~((df < (q25 - 1.5 * IQR)) | (df > (q75 + 1.5 * IQR)))])


def filter_out_unused_step_features(df: pd.DataFrame) -> pd.DataFrame:
    '''
    Method filters out from the data frame features related to workflow steps that are unnecessary with respect to given clustering.

    Parameters:
    df - data frame
    '''
    if WORKFLOW_FEATURES.WORKFLOW_STEPS not in list(df.columns):
        return df

    unique_steps_combinations = np.unique(
        list(df[WORKFLOW_FEATURES.WORKFLOW_STEPS]))
    unique_steps_names = [
        el for step_combination in unique_steps_combinations for el in step_combination.split('>')]

    PREFIX = tuple(unique_steps_names)
    SUFFIX = ('_cpu', '_memory', '_ephemeral_storage', '_duration')

    ALL_STEPS_COLUMNS = [col for col in df.columns if col.endswith(SUFFIX)]
    INCLUDED_STEPS_COLUMNS = [col for col in df.columns if col.endswith(
        SUFFIX) and col.startswith(PREFIX)]
    EXCLUDED_COLS = \
        set(ALL_STEPS_COLUMNS).difference(set(INCLUDED_STEPS_COLUMNS))

    return df.loc[:, ~df.columns.isin(EXCLUDED_COLS)]


def convert_fields_to_numeric(df: pd.DataFrame, fields: List[str]) -> pd.DataFrame:
    '''
    Method converts given fields to numeric type

    Parameters:
    df - data frame
    fields - list of fields that are to be converted to numeric 
    '''
    df_copy = df.copy()

    for field in fields:
        df_copy[field] = df_copy[field].astype(float)

    return df_copy


def get_column_names_with_count(columns: List[str]) -> List[str]:
    '''
    Method returns names associated with given columns.

    Parameters:
    columns - list of columns

    Returns: list of displayable column names
    '''
    return [FEATURES_DISPLAY_NAMES[name] if name in FEATURES_DISPLAY_NAMES else name for name in columns] + ['Count']


def get_column_count(data: pd.DataFrame,
                     columns: List[str],
                     sort: bool = True,
                     dropna: bool = True) -> pd.DataFrame:
    '''
    Method returns data frame with count for columns.

    Parameters:
    data - data frame
    columns - list of columns
    sort - optional sorting attribute
    dropna - optional dropna attribute

    Returns: data frame columns count
    '''
    data_frame_count = data[columns]\
        .value_counts(sort=sort, dropna=dropna).reset_index()
    data_frame_count.columns = get_column_names_with_count(columns)

    return data_frame_count


def filter_out_undefined_workflows(data: pd.DataFrame) -> pd.DataFrame:
    '''
    Method filters out workflows data that do not have database records.

    Parameters:
    data - data frame
    '''
    return data[data[DB_FEATURES.ORDER_NAME] != 'undefined']

def compute_range(statistics: pd.DataFrame) -> float:
    '''
    Method computes range from data frame statistics.

    Parameters:
    statistics - data frame statistics

    Returns: range of values
    '''
    return statistics['max'] - statistics['min']

def compute_percentage_error(statistics: pd.DataFrame, real_statistics, property_name: str) -> float:
    '''
    Method computes percentage error from data frame statistics.

    Parameters:
    statistics - data frame statistics
    real_statistics - true data statistics
    property_name - name of the property for which percentage error is to be computed

    Returns: range of values
    '''
    return 0 if statistics[property_name][0] == 0.0 else \
        abs(100 * (real_statistics[property_name] - statistics[property_name]) / real_statistics[property_name])