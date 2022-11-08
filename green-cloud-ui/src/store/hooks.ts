import { useDispatch, TypedUseSelectorHook, useSelector } from 'react-redux'
import { AppDispatch, RootState } from './store'

/**
 * Default store dispatcher
 *
 * @returns Dispatch
 */
export const useAppDispatch = () => useDispatch<AppDispatch>()

/**
 * Default store selector
 */
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
