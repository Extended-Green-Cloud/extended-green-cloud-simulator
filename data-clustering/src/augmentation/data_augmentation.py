import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import json

from typing import List
from sklearn.decomposition import PCA
from sklearn.mixture import GaussianMixture
from src.helpers.path_reader import PathReader
from src.helpers.workflow_filter import filter_by_numerical_features, filter_workflows_by_label
from src.helpers.feature_encoder import get_codes_of_feature, WORKFLOW_FEATURES
from src.helpers.workflow_converter import convert_synthetic_workflows_to_dict


def generate_synthetic_sample(data: np.ndarray,
                              sample_size: int,
                              n_component_range: List[int]) -> np.ndarray:
    '''
    Method uses Gaussian Mixture Models to generate synthetic data sample.

    Parameters:
    data - data from which the sample is to be drawn
    sample_size - size of the sample that is to be drawn
    n_components_range - range of GMM components that is to be tested in order to find the optimal probabilistic model

    Returns:
    synthetically generated sample
    '''
    pca = PCA(n_components=0.95, whiten=True)
    reduced_data = pca.fit_transform(data)

    gmm_models = [GaussianMixture(component_no if component_no < len(data) else len(data), 
                                  covariance_type='full', 
                                  random_state=0)
                  for component_no in n_component_range]
    aics = [model.fit(reduced_data).aic(reduced_data) for model in gmm_models]

    min_aic = np.argmin(aics)
    components_no = n_component_range[min_aic]
    components_no = components_no if components_no < len(data) else len(data)

    # create probabilistic model for selected no. of components
    gmm = GaussianMixture(
        components_no, covariance_type='full', random_state=0)
    gmm.fit(reduced_data)

    # draw final sample
    sample, _ = gmm.sample(sample_size)
    return pca.inverse_transform(sample)

class AugmentWorkflows():
    '''
    Class used to augment workflow data
    '''

    def __init__(self,
                 workflows: List[pd.DataFrame],
                 labels: List[List[int]],
                 sample_size: List[int]) -> None:
        '''
        Method initialize augmentation parameters.

        Parameters:
        workflows - workflows based on which synthetic data is to be generated
        sample_size - size of the sample that is to be generated
        '''
        self.workflows_set = workflows
        self.sample_size_set = sample_size
        self.labels_set = labels
        self.gmm_range = list(range(100, 150, 10))

    def run_and_save(self) -> None:
        '''
        Method generates synthetic workflow sample and stores it in .json file.
        '''
        final_synthetic_data = []

        for idx_workflows, workflows_in_clustering in enumerate(self.workflows_set):
            for idx_label, label in enumerate(self.labels_set[idx_workflows]):
                workflows = filter_workflows_by_label(workflows_in_clustering, str(label))
                processor_name = workflows[WORKFLOW_FEATURES.PROCESSOR_TYPE].value_counts().index[0]
                workflow_steps = workflows[WORKFLOW_FEATURES.WORKFLOW_STEPS].value_counts().index[0].split('>')

                workflows_numeric = filter_by_numerical_features(workflows, workflow_steps)

                columns = list(workflows_numeric.columns)
                workflows = workflows_numeric.to_numpy()

                synthetic_workflows = generate_synthetic_sample(
                    workflows, self.sample_size_set[idx_workflows][idx_label], self.gmm_range)
                synthetic_workflows_df = \
                    pd.DataFrame(synthetic_workflows, columns=columns)
                synthetic_workflows_df[WORKFLOW_FEATURES.PROCESSOR_TYPE] = processor_name
                
                final_synthetic_data += convert_synthetic_workflows_to_dict(synthetic_workflows_df, workflow_steps)

        with open(PathReader.SYNTHETIC_PATH(), "w") as file:
            json.dump(final_synthetic_data, file)
