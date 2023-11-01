import { Button } from 'components/common'
import { IconCross } from '@assets'
import { styles } from './header-with-delete-styles'

interface Props {
   title: string
   deleteFunction: () => void
   omitDelete?: boolean
}

/**
 * Component represents header with button used to remove some element
 *
 * @param {string}[title] - name displayed on the header
 * @param {func}[deleteFunction] - function deleting some element
 * @param {boolean}[omitDelete] - optional flag allowing to omit delete button
 *
 * @returns JSX Element
 */
const HeaderWithDelete = ({ title, deleteFunction, omitDelete }: Props) => {
   const { wrapper, text } = styles

   return (
      <div style={wrapper}>
         {!omitDelete && (
            <Button
               {...{
                  title: <IconCross size="22px" color="var(--gray-3)" />,
                  onClick: () => deleteFunction(),
                  buttonClassName: ''
               }}
            />
         )}
         <div style={text}>{title}</div>
      </div>
   )
}

export default HeaderWithDelete
