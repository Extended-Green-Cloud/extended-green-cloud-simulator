import numpy as np
import pandas as pd

from math import ceil
from plotly.graph_objects import Figure
from plotly.subplots import make_subplots
import plotly.graph_objects as go
import plotly.io as pio

from src.helpers.feature_encoder import WORKFLOW_FEATURES
from src.helpers.statistics_operations import filter_out_outliers

INCLUDE_OUTLIERS = [WORKFLOW_FEATURES.ARGO_STATUS_CODE,
                    WORKFLOW_FEATURES.ARGO_STATUS_DETAILS_CODE,
                    WORKFLOW_FEATURES.ARGO_OUTPUT_MSG_CODE,
                    WORKFLOW_FEATURES.STORAGE,
                    WORKFLOW_FEATURES.ORDER_ITEM_STATUS_CODE,
                    WORKFLOW_FEATURES.PROCESSOR_TYPE_CODE,
                    WORKFLOW_FEATURES.ORDER_STATUS_CODE]


def initialize_subplot(subplots_no: int,
                       col_no: int = 3) -> Figure:
    '''
    Method initialize a figure with subplots.

    Parameters:
    subplots_no - number of unique subplots
    col_no - desired number of columns (default: 3)

    Returns: plotly subplot figure
    '''
    rows_no = ceil(subplots_no / col_no)
    fig = make_subplots(rows=rows_no, cols=col_no)

    return fig


def add_feature_trace(name: str,
                      fig: Figure,
                      data: pd.DataFrame,
                      feature: str,
                      idx: int,
                      col_no: int = 3,
                      filter_outliers: bool = True,
                      type: str = 'hist') -> None:
    '''
    Method adds to the given figure subplot of given feature.

    Parameters:
    name - name of the subplot
    fig - figure to which the subplot is to be added
    data - data frame based on which trace is to be plot
    feature - name of the feature for which the subplot is to be made
    idx - index of subplot
    col_no - desired number of columns (default: 3)
    filter_outliers - flag indicating if outlier filtering should be applied (default: True)
    type - type of the subplot - either hist (histogram) or box (boxplot) (default: hist)
    '''
    data_no_outliers = filter_out_outliers(data) if filter_outliers else data
    x = data if feature in INCLUDE_OUTLIERS else data_no_outliers

    if type == 'hist':
        fig.add_trace(go.Histogram(
            x=x[feature],
            name=name),
            row=int((idx / col_no) + 1),
            col=(idx % col_no) + 1
        )
    elif type == 'box':
        fig.add_trace(go.Box(
            x=x[feature],
            name=name),
            row=int((idx / col_no) + 1),
            col=(idx % col_no) + 1
        )


def display_and_save_multiplot(fig: Figure,
                               fig_name: str,
                               fig_path: str,
                               height: int = 800,
                               width: int = 1000) -> None:
    '''
    Method displays multi plot and saves it in .png file.

    Parameters:
    fig - figure that is to be saved
    fig_name - title of the figure
    fig_path - path to save the figure
    height - optional figure height (default: 800)
    width - optional figure width (default: 1000)
    '''
    fig.update_layout(height=height,
                      width=width,
                      title_text=fig_name)
    pio.write_image(fig, fig_path, format='png')
    fig.show()
