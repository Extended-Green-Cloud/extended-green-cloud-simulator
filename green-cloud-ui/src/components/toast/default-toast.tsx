import { cssTransition, ToastContainer } from "react-toastify"
import 'react-toastify/dist/ReactToastify.css'
import './default-toast-styles.css'
import "animate.css/animate.min.css";

const bounce = cssTransition({
    collapseDuration: 1000,
    enter: "animate__animated animate__bounceIn",
    exit: "animate__animated animate__bounceOut"
});

/**
 * Default toas configuration
 */
const DefaultToast = () => {
    return (
        <ToastContainer
            toastStyle={{ opacity: 0.9, marginTop: '-15px' }}
            position="top-center"
            autoClose={5000}
            limit={1}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick
            rtl={false}
            pauseOnFocusLoss={false}
            draggable
            pauseOnHover
            transition={bounce}
            icon={false}
        />
    )
}

export default DefaultToast