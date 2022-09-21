import { Middleware } from '@reduxjs/toolkit'

export const crashMiddleware: Middleware = (store) => (next) => (action) => {
  console.log('Checking for crash');
  try {
    return next(action)
  } catch (error) {
    console.error('Retrieved error: ', error)
    throw error;
  }
}