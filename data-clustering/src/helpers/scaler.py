import numpy as np
import pandas as pd

from sklearn.preprocessing import StandardScaler, MinMaxScaler
from enum import Enum


def apply_min_max_scaler(data: pd.DataFrame) -> np.ndarray:
    '''
    Method applies min/max feature scaler.

    Parameters:
    data - data that is to be scaled
    '''
    scaler = MinMaxScaler()
    return scaler.fit_transform(data)


def apply_standard_scaler(data: pd.DataFrame) -> np.ndarray:
    '''
    Method initialize standardization feature scaler.

    Parameters:
    data - data that is to be scaled
    '''
    scaler = StandardScaler()
    return scaler.fit_transform(data)


class Scaler(Enum):
    def MIN_MAX(data): return apply_min_max_scaler(data)
    def STANDARD(data): return apply_standard_scaler(data)
