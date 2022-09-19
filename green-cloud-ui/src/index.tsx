import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles/themes.css'
import reportWebVitals from './reportWebVitals';
import { Provider } from 'react-redux';

import {store} from '@store'
import { MainView } from '@views';
import { DefaultToast } from '@components';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <Provider {...{store}} >
    <MainView />
    <DefaultToast />
    </Provider>
  </React.StrictMode>
);

reportWebVitals();
