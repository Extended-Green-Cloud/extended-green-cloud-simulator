import numpy as np

from typing import List


def concatenate_parameters(*lists: List) -> np.ndarray:
    '''
    Method concatenates lists of different parameters into a single grid.

    Parameters:
    lists - lists that are to be concatenated

    Returns: grid of parameters
    '''
    parameters_no = len(lists)

    return np.array(np.meshgrid(*lists)).T.reshape(-1, parameters_no)
