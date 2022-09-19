import React, { useEffect } from 'react';
import { styles } from './main-view-style'
import { AgentStatisticsPanel, Banner, CloudStatisticsPanel, EventPanel, GraphPanel } from '@components'
import { useAppDispatch, socketActions } from '@store';
import { AgentType, EnergyType, MessagePayload, MessageType } from '@types';

export const MOCK_AGENTS: MessagePayload[] = [
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.CLOUD_NETWORK, data: { name: 'CNA1', serverAgents: ['Server1', 'Server2'], maximumCapacity: 100 } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.CLOUD_NETWORK, data: { name: 'CNA2', serverAgents: ['Server3', 'Server4'], maximumCapacity: 200 } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.SERVER, data: { name: 'Server1', greenEnergyAgents: ['Solar1', 'Water1'], cloudNetworkAgent: 'CNA1', maximumCapacity: 50 } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.SERVER, data: { name: 'Server2', greenEnergyAgents: ['Solar2', 'Water2'], cloudNetworkAgent: 'CNA1', maximumCapacity: 70 } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.SERVER, data: { name: 'Server3', greenEnergyAgents: ['Solar3', 'Water3'], cloudNetworkAgent: 'CNA2', maximumCapacity: 60 } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.SERVER, data: { name: 'Server4', greenEnergyAgents: ['Solar4', 'Water4'], cloudNetworkAgent: 'CNA2', maximumCapacity: 20 } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Solar1', monitoringAgent: 'Weather1', maximumCapacity: 100, serverAgent: 'Server1', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.SOLAR } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Solar2', monitoringAgent: 'Weather2', maximumCapacity: 200, serverAgent: 'Server2', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.SOLAR } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Solar3', monitoringAgent: 'Weather3', maximumCapacity: 300, serverAgent: 'Server3', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.SOLAR } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Solar4', monitoringAgent: 'Weather4', maximumCapacity: 50, serverAgent: 'Server4', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.SOLAR } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Water1', monitoringAgent: 'Weather5', maximumCapacity: 100, serverAgent: 'Server1', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.WIND } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Water2', monitoringAgent: 'Weather6', maximumCapacity: 50, serverAgent: 'Server2', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.WIND } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Water3', monitoringAgent: 'Weather7', maximumCapacity: 200, serverAgent: 'Server3', agentLocation: { latitude: '11', longitude: '10' } , energyType: EnergyType.WIND} },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.GREEN_ENERGY, data: { name: 'Water4', monitoringAgent: 'Weather8', maximumCapacity: 50, serverAgent: 'Server4', agentLocation: { latitude: '11', longitude: '10' }, energyType: EnergyType.WIND } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather1', greenEnergyAgent: 'Solar1' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather2', greenEnergyAgent: 'Solar2' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather3', greenEnergyAgent: 'Solar3' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather4', greenEnergyAgent: 'Solar4' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather5', greenEnergyAgent: 'Water1' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather6', greenEnergyAgent: 'Water2' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather7', greenEnergyAgent: 'Water3' } },
  { type: MessageType.REGISTER_AGENT, agentType: AgentType.MONITORING, data: { name: 'Weather8', greenEnergyAgent: 'Water4' } }
]

const MainView = () => {
  const dispatch = useAppDispatch()

  useEffect(() => {
    dispatch(socketActions.openSocketConnection())
    return () => {
      dispatch(socketActions.closeSocketConnection())
    }
    // eslint-disable-next-line
  })

  return (
    <div style={styles.mainContainer}>
      <Banner />
      <div style={styles.contentContainer}>
        <div style={styles.leftContentContainer}>
          <CloudStatisticsPanel />
          <AgentStatisticsPanel />
        </div>
        <GraphPanel />
        <EventPanel />
      </div>
    </div>
  );
}

export default MainView
