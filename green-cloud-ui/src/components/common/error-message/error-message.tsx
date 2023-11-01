import { IconExclamation } from '@assets'
import { styles } from './error-message-styles'

interface Props {
   errorText: string
   errorType: string
}

/**
 * Component represents text that indicate error
 *
 * @param {string}[errorText] - error message
 * @param {string}[errorType] - type of error
 *
 * @returns JSX Element
 */
const ErrorMessage = ({ errorText, errorType }: Props) => {
   const { errorWrapper, errorTextStyle } = styles

   return (
      <>
         {errorText !== '' && (
            <div style={errorWrapper}>
               <IconExclamation {...{ size: '1.5em', color: 'var(--red-1)' }} />
               <div style={errorTextStyle}>{`${errorType}! ${errorText}`}</div>
            </div>
         )}
      </>
   )
}

export default ErrorMessage
