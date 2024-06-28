import { styles } from './collapse-styles'
import { useState } from 'react'
import Collapsible from 'react-collapsible'
import { IconArrowDown, IconArrowUp } from '@assets'

interface Props {
   title: string | React.ReactNode
   titleClosed?: string
   triggerStyle?: React.CSSProperties
   triggerClosedStyle?: React.CSSProperties
   wrapperStyle?: React.CSSProperties
   contentStyle?: React.CSSProperties
   children?: React.ReactNode | React.ReactNode[]
   disabled?: boolean
}

/**
 * Component representing generic collapsible node
 *
 * @param {string | React.ReactNode}[title] - title displayed on the collapse
 * @param {string}[titleClosed] - optional title for closed collapse
 * @param {React.CSSProperties}[triggerStyle] - optional additional trigger style
 * @param {React.CSSProperties}[triggerClosedStyle] - optional additional style for closed trigger
 * @param {React.CSSProperties}[wrapperStyle] - optional additional style for the collapse wrapper
 * @param {React.CSSProperties}[contentStyle] - optional additional style for the collapse content
 * @param {React.ReactNode | React.ReactNode[]}[children] - content of the collapse
 * @param {boolean}[disabled] - optional flag indicating if collapsible should be disabled
 * @returns JSX Element
 */
const Collapse = ({
   title,
   titleClosed,
   triggerStyle,
   triggerClosedStyle,
   wrapperStyle,
   contentStyle,
   disabled,
   children
}: Props) => {
   const { collapseStyle, collapseContentStyle, dropdownDisabledWrapper, triggerIcon } = styles
   const [isOpen, setIsOpen] = useState(false)

   const styleDisabled = disabled ? dropdownDisabledWrapper : undefined
   const styleTrigger = { ...collapseStyle, ...triggerStyle }
   const styleTriggerClosed = triggerClosedStyle ? { ...collapseStyle, ...triggerClosedStyle } : styleTrigger
   const styleCollapse = !isOpen ? styleTriggerClosed : styleTrigger
   const styleContent = { ...collapseContentStyle, ...contentStyle }

   const trigger = (
      <>
         {typeof title === 'string' ? <span>{!isOpen && titleClosed ? titleClosed : title}</span> : title}
         <span>{isOpen ? <IconArrowUp {...triggerIcon} /> : <IconArrowDown {...triggerIcon} />}</span>
      </>
   )

   return (
      <Collapsible
         {...{
            trigger,
            triggerStyle: styleCollapse,
            containerElementProps: { style: { ...styleDisabled, ...wrapperStyle } },
            triggerDisabled: disabled,
            onClosing: () => setIsOpen(false),
            onOpening: () => setIsOpen(true)
         }}
      >
         <div style={styleContent}>{children}</div>
      </Collapsible>
   )
}

export default Collapse
