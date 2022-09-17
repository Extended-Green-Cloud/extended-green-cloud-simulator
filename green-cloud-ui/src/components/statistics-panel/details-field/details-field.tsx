import { DetailField } from '@types'
import React from 'react'
import { styles } from './details-field-style'

interface Props {
    label: string,
    value: string | number
}

export const DetailsField = ({ label, value }: Props) => {
    return (
        <div style={styles.detailsContainer}>
            <div style={styles.label}>{label.toUpperCase()}</div>
            <div style={styles.value}>{value.toString().toUpperCase()}</div>
        </div>
    )
}