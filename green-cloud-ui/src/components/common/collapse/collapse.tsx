import { styles } from './collapse-styles'
import { useState } from 'react'
import Collapsible from 'react-collapsible'

interface Props {
   title: string
   titleClosed?: string
   triggerStyle?: React.CSSProperties
   triggerClosedStyle?: React.CSSProperties
   wrapperStyle?: React.CSSProperties
   children?: React.ReactNode | React.ReactNode[]
}

/**
 * Component representing generic collapsible node
 *
 * @param {string}[title] - title displayed on the collapse
 * @param {string}[titleClosed] - optional title for closed collapse
 * @param {React.CSSProperties}[triggerStyle] - optional additional trigger style
 * @param {React.CSSProperties}[triggerClosedStyle] - optional additional style for closed trigger
 * @param {React.CSSProperties}[wrapperStyle] - optional additional style for the collapse wrapper
 * @param {React.ReactNode | React.ReactNode[]}[children] - content of the collapse
 * @returns JSX Element
 */
const Collapse = ({ title, titleClosed, triggerStyle, triggerClosedStyle, wrapperStyle, children }: Props) => {
   const { collapseStyle, collapseContentStyle } = styles
   const [isOpen, setIsOpen] = useState(false)

   const styleTrigger = { ...collapseStyle, ...triggerStyle }
   const styleTriggerClosed = triggerClosedStyle ? { ...collapseStyle, ...triggerClosedStyle } : styleTrigger
   const styleCollapse = !isOpen ? styleTriggerClosed : styleTrigger

   const trigger = (
      <>
         <span>{!isOpen && titleClosed ? titleClosed : title}</span>
         <span>{isOpen ? '\u25B2' : '\u25BC'}</span>
      </>
   )

   return (
      <Collapsible
         {...{
            trigger,
            triggerStyle: styleCollapse,
            containerElementProps: { style: { ...wrapperStyle } },
            onClose: () => setIsOpen(false),
            onOpen: () => setIsOpen(true),
         }}
      >
         <div style={collapseContentStyle}>{children}</div>
      </Collapsible>
   )
}

export default Collapse
