import { iconPause, iconBattery } from "@assets"
import React from "react"

const COMMON_STYLESHEET: Array<cytoscape.Stylesheet> = [
    {
        selector: "node",
        style: {
            label: "data(label)",
            color: "#242424",
            "padding-top": "4",
            "padding-bottom": "4",
            "padding-left": "4",
            "padding-right": "4",
            "font-family": "Inter, sans-serif",
            "font-weight": "bold",
            "font-size": "10",
            "text-transform": "uppercase",
            "target-text-offset": 10,
            "source-text-offset": 10,
            "text-margin-y": -8,
            "text-border-width": 2,
            "text-border-style": 'solid',
            "text-border-opacity": 1,
            "text-border-color": "#242424",
            "text-background-padding": "3",
            "text-background-color": "#ffffff",
            "text-background-opacity": 1,
        }
    },
    {
        selector: "edge",
        style: {
            "curve-style": "bezier",
        },
    },
    {
        selector: "edge[type = 'unidirected']",
        style: {
            width: 1,
            "target-arrow-shape": "none",
            "line-color": "#242424"
        },
    },
    {
        selector: "edge[type = 'directed'][state = 'active']",
        style: {
            width: 2,
            "target-arrow-shape": "triangle",
            "line-color": "#58B905",
            "line-style": "dashed",
            "target-arrow-color": "#58B905"
        },
    },
    {
        selector: "edge[type = 'directed'][state = 'inactive']",
        style: {
            display: 'none'
        },
    }
]

const CNA_STYLESHEET: Array<cytoscape.Stylesheet> = [
    {
        selector: "node[type = 'CLOUD_NETWORK']",
        style: {
            width: "40",
            height: "40",
            shape: "ellipse",
        }
    },
    {
        selector: "node[type = 'CLOUD_NETWORK'][traffic = 'inactive']",
        style: {
            "background-color": "#8C8C8C",
        }
    },
    {
        selector: "node[type = 'CLOUD_NETWORK'][traffic = 'low']",
        style: {
            "background-color": "#8AE423",
        }
    },
    {
        selector: "node[type = 'CLOUD_NETWORK'][traffic = 'medium']",
        style: {
            "background-color": "#FDBB2A",
        }
    },
    {
        selector: "node[type = 'CLOUD_NETWORK'][traffic = 'high']",
        style: {
            "background-color": "#DB432C",
        }
    }
]

const SERVER_STYLESHEET: Array<cytoscape.Stylesheet> = [
    {
        selector: "node[type = 'SERVER']",
        style: {
            width: "25",
            height: "25",
            shape: "ellipse",
            "background-color": "#ffffff",
            "border-opacity": 1,
            "border-width": 3
        }
    },
    {
        selector: "node[type = 'SERVER'][state = 'inactive']",
        style: {
            "border-color": "#8C8C8C",
        }
    },
    {
        selector: "node[type = 'SERVER'][state = 'active']",
        style: {
            "border-color": "#8AE423",
        }
    },
    {
        selector: "node[type = 'SERVER'][state = 'on_hold']",
        style: {
            "border-color": "#DB432C",
            "background-image": iconPause,
            "background-image-opacity": 1,
            "background-fit": 'contain'
        }
    },
    {
        selector: "node[type = 'SERVER'][state = 'back_up']",
        style: {
            "border-color": "#8AE423",
            "background-image": iconBattery,
            "background-image-opacity": 1,
            "background-fit": 'contain'
        }
    }
]

const GREEN_ENERGY_STYLESHEET: Array<cytoscape.Stylesheet> = [
    {
        selector: "node[type = 'GREEN_ENERGY']",
        style: {
            width: "15",
            height: "15",
            shape: "ellipse",
            "border-opacity": 1,
            "border-width": 3
        }
    },
    {
        selector: "node[type = 'GREEN_ENERGY'][state = 'inactive']",
        style: {
            "border-color": "#6E6E6E",
            "background-color": "#8C8C8C",
        }
    },
    {
        selector: "node[type = 'GREEN_ENERGY'][state = 'active']",
        style: {
            "border-color": "#58B905",
            "background-color": "#8AE423",
        }
    },
    {
        selector: "node[type = 'GREEN_ENERGY'][state = 'on_hold']",
        style: {
            "border-color": "#DB432C",
            "background-color": "#8AE423",
            "background-image": iconPause,
            "background-image-opacity": 1,
            "background-fit": 'contain'
        }
    }
]

const MONITORING_STYLESHEET: Array<cytoscape.Stylesheet> = [
    {
        selector: "node[type = 'MONITORING']",
        style: {
            width: "10",
            height: "10",
            shape: "rectangle",
            "background-color": "#44A70D"
        }
    }
]

export const GRAPH_STYLESHEET: Array<cytoscape.Stylesheet> =
    COMMON_STYLESHEET
        .concat(CNA_STYLESHEET)
        .concat(SERVER_STYLESHEET)
        .concat(GREEN_ENERGY_STYLESHEET)
        .concat(MONITORING_STYLESHEET)

export const GRAPH_LAYOUT = {
    name: 'fcose',
    quality: "proof",
    fit: true,
    padding: 20,
    nodeRepulsion: 6000,
    animation: true,
    nodeDimensionsIncludeLabels: true,
    edgeElasticity: 0.45,
    gravity: 0.2,
    idealEdgeLength: 20,
    nodeSeparation: 15,
    sampleSize: 30,
}

export const GRAPH_STYLE: React.CSSProperties = {
    width: "100%",
    height: "100%"
}