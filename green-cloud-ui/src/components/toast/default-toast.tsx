import { ToastContainer } from "react-toastify"
import 'react-toastify/dist/ReactToastify.css'
import './default-toast-styles.css'

/**
 * Default toas configuration
 */
const DefaultToast = () => {
    return (
        <ToastContainer
            toastStyle={{ opacity: 0.9}}
            position="bottom-right"
            autoClose={4000}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick
            rtl={false}
            pauseOnFocusLoss={false}
            draggable
            pauseOnHover
            icon={false}
        />
    )
}

export default DefaultToast