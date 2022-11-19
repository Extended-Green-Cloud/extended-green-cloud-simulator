import Button from '../button/button'

interface Props {
   buttonClassName?: string
   title: string | React.ReactNode
   setIsOpen: React.Dispatch<React.SetStateAction<boolean>>
}

/**
 * Component representing styled button used to open the modal
 *
 * @param {string}[buttonClassName] - optional additional button class name
 * @param {string| React.ReactNode}[title] - text or object displayed on the button
 * @param {func}[setIsOpen] - function used to change the modal state
 * @returns JSX Element
 */
const ModalButton = ({ buttonClassName, title, setIsOpen }: Props) => {
   return (
      <Button
         {...{
            onClick: () => setIsOpen((curr) => !curr),
            buttonClassName,
            title,
         }}
      />
   )
}

export default ModalButton
