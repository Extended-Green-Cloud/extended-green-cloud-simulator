from enum import Enum

# Metadata mocks

MOCK_METADATA = {
    'uid': 'test_uid',
    'labels': {
        'order-id': 'test_order_id',
        'order-item-id': 'test_order_item_id'
    }
}


class MetadataMocks(Enum):
    FULL_METADATA = MOCK_METADATA


# Database mocks

MOCK_DATABASE_DATES = {
    'add_date': "2020-12-1 12:00:00.00 Z",
    'referential_eta': "2020-12-1 12:00:30.00 Z"
}

MOCK_DATABASE_FULL = {
    'status': 'done',
    'status.1': 'done_with_errors',
    'priority': 10,
    'add_date': "2020-12-1 12:00:00.00 Z",
    'referential_eta': "2020-12-1 12:00:30.00 Z"
}


class DatabaseMocks(Enum):
    ONLY_DATES = MOCK_DATABASE_DATES
    FULL = MOCK_DATABASE_FULL


# Status mocks

MOCK_STATUS_DATES = {
    'startedAt': "2020-12-1T12:00:00Z",
    'finishedAt': "2020-12-1T12:00:30Z"
}

MOCK_STATUS_PHASE = {
    'phase': 'Succeeded',
    'progress': '2/2',
    'message': '',
}

MOCK_STATUS_OUTPUTS = {
    'outputs': {
        'parameters': [
            {'name': 'final_state',
             'value': "{\"status\": 0, \"new_size\": 782191340, \"message\": \"Message1\"}"}
        ]
    }
}

MOCK_STATUS_NODES_SIMPLE = {
    "nodes": {
        "step_1": {
            "type": "Pod",
        },
        "step_2": {
            "type": "Pod",
        },
        "step_3": {
            "type": "Pod",
        }
    }
}

MOCK_STATUS_NODES_FULL = {
    "nodes": {
        "node_1": {
            "templateName": "exit",
            "type": "Pod",
            "hostNodeName": "test_host_1",
            "startedAt": "2020-12-1T12:00:00Z",
            "finishedAt": "2020-12-1T12:00:30Z",
            "phase": "Succeeded",
            "resourcesDuration": {
                "cpu": 100,
                "memory": 100,
                "ephemeral-storage": 100
            }
        },
        "node_1_retry": {
            "templateName": "exit",
            "type": "Retry",
            "startedAt": "2020-12-1T13:00:00Z",
            "finishedAt": "2020-12-1T14:00:00Z",
            "phase": "Succeeded"
        },
        "node_2": {
            "templateName": "test_name_1"
        },
        "node_3": {
            "templateName": "test_name_2",
            "type": "Pod",
            "hostNodeName": "test_host_2",
            "startedAt": "2020-12-1T12:00:00Z",
            "finishedAt": "2020-12-1T12:00:30Z",
            "phase": "Succeeded",
            "resourcesDuration": {
                "cpu": 50,
                "memory": 50
            }
        },
        "node_3_retry": {
            "templateName": "test_name_2",
            "type": "Retry",
            "startedAt": "2020-12-1T14:00:00Z",
            "finishedAt": "2020-12-1T14:00:30Z",
            "phase": "Failed"
        }
    }
}

MOCK_STATUS_RESOURCES = {
    "resourcesDuration": {
        "cpu": 991,
        "ephemeral-storage": 42,
        "memory": 31251
    },
}


class WorkflowStatusMocks(Enum):
    ONLY_DATES = MOCK_STATUS_DATES
    ONLY_OUTPUTS = MOCK_STATUS_OUTPUTS
    ONLY_NODES = MOCK_STATUS_NODES_SIMPLE
    ONLY_NODES_FULL = MOCK_STATUS_NODES_FULL
    PHASE_AND_RESOURCES = {**MOCK_STATUS_PHASE,
                           **MOCK_STATUS_RESOURCES}
    NODES_OUTPUTS_DATES = {**MOCK_DATABASE_DATES,
                           **MOCK_STATUS_NODES_SIMPLE,
                           **MOCK_STATUS_OUTPUTS}
    NODES_OUTPUTS_DATES_RESOURCES = {**MOCK_DATABASE_DATES,
                                     **MOCK_STATUS_NODES_SIMPLE,
                                     **MOCK_STATUS_OUTPUTS,
                                     **MOCK_STATUS_RESOURCES}

# Specification mocks


MOCK_SPEC_TEMPLATES_NAMES = {
    'templates': [
        {"name": "exit"},
        {"name": "test_name_1", "dag": {}},
        {"name": "test_name_2"},
    ]
}

MOCK_SPEC_TEMPLATES_DAG = {
    'templates': [
        {"name": "exit"},
        {"name": "dag", "dag": {
            "tasks": [
                {"name": "exit"},
                {"name": "test_name_2"}
            ]
        }},
        {"name": "test_name_2"},
    ]
}

MOCK_SPEC_VOLUME = {
    'volumeClaimTemplates': [
        {'spec': {
            'resources': {
                'requests': {
                    'storage': '1Gi'
                }
            }
        }
        }
    ]
}


class WorkflowSpecMocks(Enum):
    ONLY_TEMPLATE_NAMES = MOCK_SPEC_TEMPLATES_NAMES
    ONLY_TEMPLATES = MOCK_SPEC_TEMPLATES_DAG
    STORAGE_AND_DAG_TEMPLATE = {**MOCK_SPEC_TEMPLATES_DAG,
                                **MOCK_SPEC_VOLUME}

# Steps mocks


MOCK_STEP_SPEC = {
    'name': 'test_name',
    'retryStrategy': {
        'limit': 10
    }
}

MOCK_STEP_DETAILS = [
    {
        "hostNodeName": "test_host",
        "startedAt": "2020-12-1T12:00:00Z",
        "finishedAt": "2020-12-1T12:00:30Z",
        "phase": "Succeeded",
        "resourcesDuration": {
            "cpu": 50,
            "memory": 100,
            "ephemeral-storage": 10
        }
    },
    {"resourcesDuration": {
        "cpu": 20,
        "ephemeral-storage": 30
    }}

]

MOCK_STEP_RETRY = {
    "startedAt": "2020-12-1T13:00:00Z",
    "finishedAt": "2020-12-1T14:00:00Z",
    "phase": "Failed"
}


class WorkflowStepMocks(Enum):
    ONLY_STEP_SPEC = MOCK_STEP_SPEC
    ONLY_STEP_DETAILS = MOCK_STEP_DETAILS
    ONLY_STEP_RETRY = MOCK_STEP_RETRY


# Full workflows

MOCK_WORKFLOW_1 = {
    "database_data": {
        'status': 'done',
        'status.1': 'done',
        'priority': 10,
        "order_name": "test_order_1",
        'add_date': "2020-12-1 12:00:00.00 Z",
        'referential_eta': "2020-12-1 12:00:30.00 Z"
    },
    "metadata": {
        'uid': 'test_uid',
        'labels': {
            'order-id': 'test_order_id',
            'order-item-id': 'test_order_item_id',
            'processor-name': 'test_processor_name_1'
        }
    },
    "spec": {
        'templates': [
            {"name": "step_1"},
            {"name": "dag",
             "dag": {
                 "tasks": [
                    {"name": "step_1",
                     'retryStrategy': {
                         'limit': 10
                     }},
                     {"name": "step_2",
                      'retryStrategy': {
                          'limit': 5
                      }}
                 ]
             }},
            {"name": "step_2"},
        ],
        'volumeClaimTemplates': [
            {'spec': {
                'resources': {
                    'requests': {
                        'storage': '1Gi'
                    }
                }
            }
            }
        ]
    },
    "status": {
        'phase': 'Succeeded',
        'progress': '2/2',
        'message': '',
        'nodes': {
            "step_1": {
                "templateName": "step_1",
                "type": "Pod",
                "hostNodeName": "test_host_1",
                "startedAt": "2020-12-1T12:00:00Z",
                "finishedAt": "2020-12-1T12:00:30Z",
                "phase": "Succeeded",
                "resourcesDuration": {
                    "cpu": 100,
                    "memory": 100,
                    "ephemeral-storage": 100
                }
            },
            "step_1_retry": {
                "templateName": "step_1",
                "type": "Retry",
                "startedAt": "2020-12-1T13:00:00Z",
                "finishedAt": "2020-12-1T14:00:00Z",
                "phase": "Succeeded"
            },
            "step_2": {
                "templateName": "step_2",
                "type": "Pod",
                "hostNodeName": "test_host_2",
                "startedAt": "2020-12-1T12:00:00Z",
                "finishedAt": "2020-12-1T12:00:30Z",
                "phase": "Succeeded",
                "resourcesDuration": {
                    "cpu": 50,
                    "memory": 50
                }
            },
            "step_2_retry": {
                "templateName": "step_2",
                "type": "Retry",
                "startedAt": "2020-12-1T14:00:00Z",
                "finishedAt": "2020-12-1T14:00:30Z",
                "phase": "Failed"
            }
        },
        'outputs': {
            'parameters': [{
                'name': 'final_state',
                'value': "{\"status\": 0, \"message\": \"Product processed successfully\"}"
            }]
        }
    }
}

MOCK_WORKFLOW_2 = {
    "database_data": {
        'status': 'cancelled',
        'status.1': 'processing',
    },
    "metadata": {
        'uid': 'test_uid',
        'labels': {
            'order-id': 'test_order_id',
            'order-item-id': 'test_order_item_id',
            'processor-name': 'test_processor_name_2'
        }
    },
    "spec": {
        'templates': [
            {"name": "step_2"},
            {"name": "dag",
             "dag": {
                 "tasks": [
                    {"name": "step_2",
                     'retryStrategy': {
                         'limit': 5
                     }},
                     {"name": "step_3",
                      'retryStrategy': {
                          'limit': 2
                      }}
                 ]
             }},
            {"name": "step_3"},
        ],
        'volumeClaimTemplates': [
            {'spec': {
                'resources': {
                    'requests': {
                        'storage': '20Gi'
                    }
                }
            }
            }
        ]
    },
    "status": {
        'phase': 'Succeeded',
        'progress': '3/3',
        'message': '',
        'nodes': {
            "step_2": {
                "templateName": "step_2",
                "type": "Pod",
                "startedAt": "2020-12-1T13:00:00Z",
                "finishedAt": "2020-12-1T13:00:40Z",
                "phase": "Failed",
                "resourcesDuration": {
                    "cpu": 50,
                    "memory": 60,
                    "ephemeral-storage": 70
                }
            },
            "step_2_retry": {
                "templateName": "step_2",
                "type": "Retry",
                "startedAt": "2020-12-1T13:00:00Z",
                "finishedAt": "2020-12-1T13:00:40Z",
                "phase": "Failed"
            },
            "step_3": {
                "templateName": "step_3",
                "type": "Pod",
                "hostNodeName": "test_host_2",
                "startedAt": "2020-12-1T12:00:00Z",
                "finishedAt": "2020-12-1T12:00:50Z",
                "phase": "Error",
                "resourcesDuration": {
                    "cpu": 20,
                    "memory": 20,
                    "ephemeral-storage": 20
                }
            },
            "step_3_retry": {
                "templateName": "step_3",
                "type": "Retry",
                "startedAt": "2020-12-1T14:00:00Z",
                "finishedAt": "2020-12-1T14:00:50Z",
                "phase": "Error"
            }
        },
        'outputs': {
            'parameters': [{
                'name': 'final_state',
                'value': "{\"status\": 0, \"message\": \"Critical error\"}"
            }]
        }
    }
}

MOCK_WORKFLOWS = [MOCK_WORKFLOW_1, MOCK_WORKFLOW_2]
