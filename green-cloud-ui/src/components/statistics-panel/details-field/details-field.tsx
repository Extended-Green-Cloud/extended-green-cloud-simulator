import React from 'react'
import { styles } from './details-field-style'

interface Props {
    label: string,
    value?: any,
    valueObject?: React.ReactNode
}

export const DetailsField = ({ label, value, valueObject }: Props) => {
    const getValue = () => {
        return typeof valueObject !== 'undefined' ?
            <div style={styles.value}>{valueObject}</div> :
            <div style={{ ...styles.value, ...styles.valueText }}>{value.toString().toUpperCase()}</div>
    }
    return (
        <div style={styles.detailsContainer}>
            <div style={styles.label}>{label.toUpperCase()}</div>
            {getValue()}
        </div>
    )
}