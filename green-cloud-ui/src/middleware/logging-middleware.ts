import { Middleware } from '@reduxjs/toolkit'
 
/**
 * Default middleware used to log dispatched actions
 * 
 * @param store root store
 * @returns Middleware
 */
export const loggingMiddleware: Middleware = (store) => {
  return (next) => {
    return (action) => {
      console.log('Action:', action);
      const result = next(action);
      console.log('Next state:', store.getState());
      return result;
    }
  }
}