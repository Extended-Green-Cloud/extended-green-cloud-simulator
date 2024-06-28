import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

from src.helpers.scaler import Scaler
from enum import Enum
from sklearn.decomposition import PCA
from umap import UMAP


def apply_PCA(scaler: Scaler, data: pd.DataFrame, plot: bool = False) -> np.ndarray:
    '''
    Method performs PCA reducing the dimensionality of the data.

    Parameters:
    scaler - type of the scaler used along the PCA
    data - data which dimensionality is to be reduced

    Returns: data with reduced dimensionality
    '''
    scaled_data = scaler(data)

    pca = PCA()
    pca.fit(scaled_data)

    if plot:
        plt.figure(figsize=(10, 10))
        plt.plot(range(1, len(data.columns) + 1),
                 pca.explained_variance_ratio_.cumsum(), marker="s", linestyle="dotted")
        plt.title('Explained Variance')
        plt.xlabel('Component no.')
        plt.ylabel('Cumulative Explained Variance')

    pca = PCA(n_components=0.95)
    return pca.fit_transform(scaled_data)


def apply_UMAP(scaler: Scaler, data: pd.DataFrame, n_neighbors: int, min_dist: float, n_components: int = 2, plot: bool = False) -> np.ndarray:
    '''
    Method performs UMAP reducing the dimensionality of the data.

    Parameters:
    scaler - type of the scaler used along the UMAP
    data - data which dimensionality is to be reduced

    Returns: data with reduced dimensionality
    '''
    scaled_data = scaler(data)

    reducer = UMAP(n_neighbors=n_neighbors,
                   min_dist=min_dist, 
                   n_components=n_components)
    reduced_data = reducer.fit_transform(scaled_data)

    if plot:
        plt.figure(figsize=(10, 10))
        plt.scatter(reduced_data[:, 0], reduced_data[:, 1], linewidth=0)

    return reduced_data


class DimensionalityReducer(Enum):
    def NONE(scaler: Scaler, data: pd.DataFrame, plot=False): \
        return data.to_numpy()
    def PCA(scaler: Scaler, data: pd.DataFrame, plot=False): \
        return apply_PCA(scaler, data, plot)

    def UMAP(scaler, data: pd.DataFrame, n_neighbors, min_dist, n_components, plot=False): \
        return apply_UMAP(scaler, data, n_neighbors, min_dist, n_components, plot)

    @classmethod
    def get_reducer_from_name(cls, name: str):
        if name == 'PCA': return cls.PCA
        if name == 'UMAP': return cls.UMAP
        if name == 'NONE': return cls.NONE

        raise AttributeError(f'Dimensionality reduction of type {name} is undefined!')
