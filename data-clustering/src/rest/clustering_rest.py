import numpy as np

from flask import Blueprint, jsonify, request, Response

from src.clustering.clustering import Clustering
from src.clustering.cluster_reader import get_encoded_cluster_resources, get_data_for_all_clusters

clustering_bp = Blueprint('clustering', __name__)

@clustering_bp.errorhandler(AttributeError)
def handle_incorrect_attribute(error):
    return Response('Incorrect attribute passed.', 400)

@clustering_bp.route("/jobs", methods=['POST'])
def perform_clustering():
    request_data = request.get_json()
    data_with_labels, _ = Clustering.run_workflow_clustering_for_rest(request_data)
    labels = np.unique(data_with_labels['label'])
    points_per_label = get_data_for_all_clusters(data_with_labels, labels)

    return points_per_label


@clustering_bp.route("/encoded_jobs", methods=['POST'])
def perform_clustering_with_encoding():
    request_data = request.get_json()
    data_with_labels, clustering_features = Clustering.run_workflow_clustering_for_rest(request_data)
    labels = np.unique(data_with_labels['label'])

    points_per_label = get_data_for_all_clusters(data_with_labels, labels)
    encoded_clusters = get_encoded_cluster_resources(data_with_labels, labels, clustering_features)

    response = { 'clustering': points_per_label, 'encoding': encoded_clusters }

    return jsonify(response)
