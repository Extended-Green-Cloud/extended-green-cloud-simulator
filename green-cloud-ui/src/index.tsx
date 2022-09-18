import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles/themes.css'
import reportWebVitals from './reportWebVitals';
import { Provider } from 'react-redux';
import {store} from '@store'
import { MainView } from '@views';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <Provider {...{store}} >
    <MainView />
    </Provider>
  </React.StrictMode>
);

reportWebVitals();
