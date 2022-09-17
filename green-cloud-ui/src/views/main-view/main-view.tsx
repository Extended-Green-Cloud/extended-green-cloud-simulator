import React, { useEffect } from 'react';
import { Banner, CloudStatisticsPanel, GraphPanel } from '@components'
import { useAppDispatch, useAppSelector } from '@store';
import { socketActions } from 'store/socket/actions';
import { styles} from './main-view-style'

const MainView = () => {
  const dispatch = useAppDispatch()

  useEffect(() => {
    dispatch(socketActions.openSocketConnection())
    return () => {
      dispatch(socketActions.closeSocketConnection())
    }
  }, [])

  return (
    <div style={styles.mainContainer}>
      <Banner />
      <div style={styles.contentContainer}>
        <CloudStatisticsPanel />
        <GraphPanel />
      </div>
    </div>
  );
}

export default MainView
