import { Collapse, Modal } from 'components/common'
import { ThirdParty, USED_LIBRARIES } from './third-party-libraries-list'
import { styles } from './third-party-libraries-styles'

interface Props {
   isOpen: boolean
   setIsOpen: (state: boolean) => void
}

const ThirdPartyLibraries = ({ isOpen, setIsOpen }: Props) => {
   const {
      thirdPartyField,
      collapseHeader,
      collapseContent,
      modalStyle,
      contentRecord,
      licenseTable,
      headerRecord,
   } = styles

   const generateThirdPartyFields = () =>
      USED_LIBRARIES.sort((a, b) =>
         a.framework.toLowerCase().localeCompare(b.framework.toLowerCase())
      ).map((field) => generateThirdPartyField(field))

   const generateThirdPartyField = (library: ThirdParty) => {
      return (
         <div style={thirdPartyField}>
            <Collapse
               {...{
                  title: library.framework,
                  triggerStyle: collapseHeader,
                  wrapperStyle: collapseContent,
               }}
            >
               {generateThirdPartyContent(library)}
            </Collapse>
         </div>
      )
   }

   const generateThirdPartyContent = (library: ThirdParty) => {
      return (
         <table style={licenseTable}>
            <tr>
               <td style={headerRecord}>Type</td>
               <td style={contentRecord}>{library.license}</td>
            </tr>
            {library.copyright && (
               <tr>
                  <td style={headerRecord}>Copyright</td>
                  <td style={contentRecord}>{library.copyright}</td>
               </tr>
            )}
            <tr>
               <td style={headerRecord}>Version</td>
               <td style={contentRecord}>{library.version}</td>
            </tr>
            <tr>
               <td style={headerRecord}>License</td>
               <td style={contentRecord}>{library.notice}</td>
            </tr>
         </table>
      )
   }

   return (
      <Modal
         {...{
            isOpen,
            setIsOpen,
            header: 'THIRD PARTY LIBRARIES',
            contentStyle: modalStyle,
            isNested: true,
         }}
      >
         {generateThirdPartyFields()}
      </Modal>
   )
}

export default ThirdPartyLibraries
