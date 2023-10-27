import React, { useState } from 'react'
import Card from 'components/common/card/card'
import DisplayGraph from 'components/graph-panel/graph/graph-connected'
import { styles } from './graph-panel-styles'
import { Button, TooltipInfo } from 'components/common'
import { Agent } from '@types'
import { EventPanel } from '@components'

interface Props {
   selectedAgent?: Agent | null
}

const buttonTitle = 'TRIGGER EVENT ON THE SYSTEM'
const panelTitle = 'CLOUD NETWORK STRUCTURE'

const notSelected = 'No selected agent. Click on an agent to display available event actions'
const noEventsText = 'Selected agent does not have any available event actions'

const tooltipId = 'event-modal'

/**
 * Component is the graph panel container
 *
 * @returns JSX Element
 */
const GraphPanel = ({ selectedAgent }: Props) => {
   const { graphContainer, headerContainer, headerStyle } = styles
   const [isOpen, setIsOpen] = useState<boolean>(false)

   const isEventButtonDisabled = !selectedAgent || selectedAgent?.events.length === 0
   const buttonClassName = [
      'medium-green-button',
      isEventButtonDisabled ? 'medium-green-button-disabled' : 'medium-green-button-active'
   ].join(' ')
   const tooltipInfo = !selectedAgent
      ? notSelected
      : selectedAgent && selectedAgent?.events.length === 0
      ? noEventsText
      : `Button opens menu allowing to trigger events on ${selectedAgent.name}`

   const header = (
      <div style={headerContainer}>
         <div style={headerStyle}>{panelTitle}</div>
         <TooltipInfo {...{ id: tooltipId, header: tooltipInfo, place: 'bottom' }} />
         <div data-tooltip-id={tooltipId}>
            <Button
               {...{
                  title: buttonTitle,
                  onClick: () => setIsOpen(!isOpen),
                  buttonClassName,
                  isDisabled: isEventButtonDisabled
               }}
            />
         </div>
      </div>
   )

   return (
      <Card {...{ header, containerStyle: graphContainer, removeScroll: true }}>
         <EventPanel {...{ isOpen, setIsOpen }} />
         <DisplayGraph />
      </Card>
   )
}

export default GraphPanel
