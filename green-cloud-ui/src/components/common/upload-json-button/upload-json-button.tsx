import React, { useRef } from 'react'
import Button from '../button/button'
import { styles } from './upload-json-button-styles'

interface Props {
   buttonText: string
   handleUploadedContent?: (content: any) => void
}

/**
 * Component represents button that uploads the JSON content from file on the computer
 *
 * @param {string}[buttonText] - text displayed on upload button
 * @param {UpdateResource}[handleUploadedContent] - optional function used to handle uploaded JSON
 *
 * @returns JSX Element
 */
const UploadJSONButton = ({ buttonText, handleUploadedContent }: Props) => {
   const fileRef = useRef<HTMLInputElement>(null)
   const { buttonContainer } = styles
   const addNewResourceButton = [
      'medium-green-button-active',
      'medium-green-button',
      'full-width-button',
      'no-top-margin-button'
   ].join(' ')

   const readFromFile: React.ChangeEventHandler<HTMLInputElement> = (event) => {
      const { files } = event.target
      const fr = new FileReader()

      if (files) {
         fr.readAsText(files[0], 'UTF-8')
         fr.onload = (e) => {
            if (e.target?.result) {
               const content = e.target?.result as string
               const parsedJSON = JSON.parse(content)

               if (handleUploadedContent) {
                  handleUploadedContent(parsedJSON)
               }

               if (fileRef.current) {
                  fileRef.current.value = ''
               }
            }
         }
      }
   }

   return (
      <>
         <div style={buttonContainer}>
            <Button
               {...{
                  title: buttonText.toUpperCase(),
                  onClick: () => fileRef?.current?.click(),
                  buttonClassName: addNewResourceButton
               }}
            />
         </div>
         <input
            type="file"
            id="upload-json-file"
            accept="application/json"
            ref={fileRef}
            onChange={readFromFile}
            hidden
         />
      </>
   )
}

export default UploadJSONButton
