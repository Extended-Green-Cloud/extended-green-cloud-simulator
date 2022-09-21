import { ToastContainer } from "react-toastify"
import 'react-toastify/dist/ReactToastify.css'

/**
 * Default toas configuration
 */
const DefaultToast = () => {
    return (
        <ToastContainer
            progressStyle={{ background: "var(--green-1)"}}
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