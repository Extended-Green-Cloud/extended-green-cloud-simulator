interface Props {
   buttonClassName?: string
   title: string | React.ReactNode
   isDisabled?: boolean
   onClick: () => void
}

/**
 * Component representing styled button
 *
 * @param {string}[buttonClassName] - optional additional button class name
 * @param {string | React.ReactNode}[title] - text or object displayed on the button
 * @param {boolean}[isDisabled] - optional flag indicating if the button should be disabled
 * @param {func}[onClick] - function tirggered on button click
 * @returns JSX Element
 */
const Button = ({ buttonClassName, title, isDisabled = false, onClick }: Props) => {
   return (
      <button
         {...{
            className: buttonClassName + ' common-button',
            onClick,
            disabled: isDisabled,
         }}
      >
         {title}
      </button>
   )
}

export default Button
