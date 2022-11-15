import { iconPause, iconBattery, iconWeather } from '@assets'
import React from 'react'

const COMMON_STYLESHEET: Array<cytoscape.Stylesheet> = [
   {
      selector: 'node',
      style: {
         label: 'data(label)',
         color: '#242424',
         'padding-top': '4',
         'padding-bottom': '4',
         'padding-left': '4',
         'padding-right': '4',
         'font-family': 'Inter, sans-serif',
         'font-weight': 'bold',
         'font-size': '11',
         'text-transform': 'uppercase',
         'target-text-offset': 10,
         'source-text-offset': 10,
         'text-margin-y': -8,
         'text-border-width': 2,
         'text-border-style': 'solid',
         'text-border-opacity': 1,
         'text-border-color': '#242424',
         'text-background-padding': '3',
         'text-background-color': '#ffffff',
         'text-background-opacity': 1,
      },
   },
   {
      selector: 'edge',
      style: {
         'curve-style': 'bezier',
      },
   },
]

const CNA_STYLESHEET: Array<cytoscape.Stylesheet> = [
   {
      selector: "node[type = 'CLOUD_NETWORK']",
      style: {
         width: '60',
         height: '60',
         shape: 'round-octagon',
      },
   },
   {
      selector: "node[type = 'CLOUD_NETWORK'][traffic = 'inactive']",
      style: {
         'background-color': '#8C8C8C',
      },
   },
   {
      selector: "node[type = 'CLOUD_NETWORK'][traffic = 'low']",
      style: {
         'background-color': '#8AE423',
      },
   },
   {
      selector: "node[type = 'CLOUD_NETWORK'][traffic = 'medium']",
      style: {
         'background-color': '#FDBB2A',
      },
   },
   {
      selector: "node[type = 'CLOUD_NETWORK'][traffic = 'high']",
      style: {
         'background-color': '#DB432C',
      },
   },
]

const SERVER_STYLESHEET: Array<cytoscape.Stylesheet> = [
   {
      selector: "node[type = 'SERVER']",
      style: {
         width: '35',
         height: '35',
         shape: 'ellipse',
         'background-color': '#ffffff',
         'border-opacity': 1,
         'border-width': 5,
      },
   },
   {
      selector: "node[type = 'SERVER'][state = 'inactive']",
      style: {
         'border-color': '#8C8C8C',
      },
   },
   {
      selector: "node[type = 'SERVER'][state = 'active']",
      style: {
         'border-color': '#8AE423',
      },
   },
   {
      selector: "node[type = 'SERVER'][state = 'on_hold']",
      style: {
         'border-color': '#DB432C',
         'background-image': iconPause,
         'background-image-opacity': 1,
         'background-fit': 'contain',
      },
   },
   {
      selector: "node[type = 'SERVER'][state = 'back_up']",
      style: {
         'border-color': '#8AE423',
         'background-image': iconBattery,
         'background-image-opacity': 1,
         'background-fit': 'contain',
      },
   },
]

const GREEN_ENERGY_STYLESHEET: Array<cytoscape.Stylesheet> = [
   {
      selector: "node[type = 'GREEN_ENERGY']",
      style: {
         width: '20',
         height: '20',
         shape: 'ellipse',
         'border-opacity': 1,
         'border-width': 3,
      },
   },
   {
      selector: "node[type = 'GREEN_ENERGY'][state = 'inactive']",
      style: {
         'border-color': '#6E6E6E',
         'background-color': '#8C8C8C',
      },
   },
   {
      selector: "node[type = 'GREEN_ENERGY'][state = 'active']",
      style: {
         'border-color': '#58B905',
         'background-color': '#8AE423',
      },
   },
   {
      selector: "node[type = 'GREEN_ENERGY'][state = 'on_hold']",
      style: {
         'border-color': '#DB432C',
         'background-color': '#DB432C',
         'background-image': iconPause,
         'background-image-opacity': 1,
         'background-fit': 'cover',
      },
   },
]

const MONITORING_STYLESHEET: Array<cytoscape.Stylesheet> = [
   {
      selector: "node[type = 'MONITORING']",
      style: {
         width: '20',
         height: '20',
         shape: 'ellipse',
         'border-color': '#429647',
         'background-color': '#ffffff',
         'background-image': iconWeather,
         'background-image-opacity': 1,
         'background-image-containment': 'over',
         'background-fit': 'cover',
      },
   },
]

export const EDGE_UNIDIRECTED_INACTIVE = {
   width: 1,
   'target-arrow-shape': 'none',
   'line-color': '#242424',
}
export const EDGE_UNIDIRECTED_ACTIVE = {
   width: 1,
   'target-arrow-shape': 'none',
   'line-color': '#58B905',
}

export const GRAPH_STYLESHEET: Array<cytoscape.Stylesheet> =
   COMMON_STYLESHEET.concat(CNA_STYLESHEET)
      .concat(SERVER_STYLESHEET)
      .concat(GREEN_ENERGY_STYLESHEET)
      .concat(MONITORING_STYLESHEET)

export const GRAPH_LAYOUT = {
   name: 'fcose',
   quality: 'draft',
   fit: true,
   animation: true,
   animationDuration: 1000,
   padding: 20,
   packComponents: false,
   nodeRepulsion: 3000,
   edgeElasticity: 0.05,
   gravity: 0.2,
   idealEdgeLength: 40,
   nodeSeparation: 60,
   sampleSize: 50,
}

export const GRAPH_STYLE: React.CSSProperties = {
   width: '100%',
   height: '100%',
}
