import numpy as np

from typing import List
from src.models.Workflow import Workflow


class Order():
    '''
    Class representing a single client order
    '''

    def __init__(self, workflows: List[Workflow]) -> None:
        '''
        Method initialize client order given list of workflows.

        Parameters:
        workflows - list of workflows being order items
        '''
        self.id = workflows[0].metadata.orderId
        self.name = workflows[0].metadata.orderName
        self.status = workflows[0].order_status
        self.workflows = workflows
        self.workflows_no = len(workflows)

        self.cpu = 0
        self.memory = 0
        self.ephemeral_storage = 0
        self.storage = 0
        self.processed_size = 0

        for workflow in workflows:
            self.cpu += workflow.processingDetails.cpu
            self.memory += workflow.processingDetails.memory
            self.ephemeral_storage += workflow.processingDetails.ephemeralStorage
            self.storage += workflow.processingDetails.storage
            self.processed_size += workflow.processingDetails.processedSize

        self.get_order_duration()

    def get_order_duration(self) -> None:
        '''
        Method computes and stores the duration of the order execution.
        '''
        start_dates = [
            workflow.processingDetails.startTime for workflow in self.workflows if workflow.processingDetails.startTime]
        finish_dates = [
            workflow.processingDetails.endTime for workflow in self.workflows if workflow.processingDetails.endTime]

        self.duration = 0 if len(start_dates) == 0 or len(finish_dates) == 0 \
            else (min(finish_dates) - min(start_dates)).total_seconds()
