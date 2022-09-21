import { Middleware } from '@reduxjs/toolkit'
 
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