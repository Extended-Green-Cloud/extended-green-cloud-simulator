from src.helpers.feature_encoder import WORKFLOW_FEATURES, DB_FEATURES

CATEGORICAL_FIELDS = [WORKFLOW_FEATURES.PROCESSOR_TYPE,
                      WORKFLOW_FEATURES.ORDER_NAME,
                      WORKFLOW_FEATURES.ORDER_ITEM_STATUS,
                      WORKFLOW_FEATURES.ARGO_STATUS,
                      WORKFLOW_FEATURES.ARGO_STATUS_DETAILS,
                      WORKFLOW_FEATURES.ARGO_OUTPUT_MSG,
                      WORKFLOW_FEATURES.WORKFLOW_STEPS_ENCODED]

STATUS_FIELDS = [WORKFLOW_FEATURES.ORDER_ITEM_STATUS,
                 WORKFLOW_FEATURES.ORDER_STATUS,
                 WORKFLOW_FEATURES.ARGO_STATUS,
                 WORKFLOW_FEATURES.ARGO_STATUS_DETAILS,
                 WORKFLOW_FEATURES.ARGO_OUTPUT_MSG]


INDIVIDUAL_DB_ORDER = [DB_FEATURES.ORDER_ITEM_ID,
                       DB_FEATURES.ORDER_ITEM_STATUS,
                       DB_FEATURES.ORDER_STATUS,
                       DB_FEATURES.EXTRA_INFO,
                       DB_FEATURES.ORDER_ID,
                       DB_FEATURES.ORDER_NAME]

DB_ORDER_AGGREGATION = [WORKFLOW_FEATURES.ORDER_ID,
                        WORKFLOW_FEATURES.ORDER_STATUS]

DB_ORDER_NAME_AGGREGATION = [WORKFLOW_FEATURES.ORDER_ID,
                            WORKFLOW_FEATURES.ORDER_NAME]

ARGO_ORDER_AGGREGATION = [WORKFLOW_FEATURES.ARGO_STATUS,
                          WORKFLOW_FEATURES.ARGO_STATUS_DETAILS,
                          WORKFLOW_FEATURES.PROCESSOR_TYPE]


STATS_FOR_WORKFLOWS_WITHOUT_ARGO_FILES = [
    {
        'columns': [DB_FEATURES.ORDER_ITEM_STATUS,
                    DB_FEATURES.ORDER_STATUS,
                    DB_FEATURES.ORDER_ID,
                    DB_FEATURES.ORDER_NAME],
        'name': f'workflows-without-argo-files-general-count.csv',
        'filter': lambda data: data
    },
    {
        'columns': [DB_FEATURES.ORDER_STATUS],
        'name': f'workflows-without-argo-files-status-count.csv',
        'filter': lambda data: data
    },
    {
        'columns': INDIVIDUAL_DB_ORDER,
        'name': f'failed-workflows-without-argo-files-count.csv',
        'filter': lambda data: data[data[DB_FEATURES.ORDER_STATUS] == 'failed']
    },
    {
        'columns': INDIVIDUAL_DB_ORDER,
        'name': f'done-workflows-without-argo-files-count.csv',
        'filter': lambda data: data[data[DB_FEATURES.ORDER_STATUS] == 'done']
    },
    {
        'columns': INDIVIDUAL_DB_ORDER,
        'name': f'downloading-workflows-without-argo-files-count.csv',
        'filter': lambda data: data[data[DB_FEATURES.ORDER_STATUS] == 'downloading']
    }
]
