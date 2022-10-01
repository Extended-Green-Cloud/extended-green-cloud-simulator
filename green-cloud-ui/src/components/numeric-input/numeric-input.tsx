import { ChangeEventHandler } from 'react'
import './numeric-input-styles.css'

interface Props {
    label: string,
    placeholder: string,
    value?: number,
    disabled?: boolean
    handleChange: ChangeEventHandler<HTMLInputElement>
}

/**
 * Component represents field which can be used to take numerical input
 * 
 * @param {string}[label] - label describing the field
 * @param {string}[placeholder] - placeholder describing the field
 * @param {number | undefined}[value] - value being the current input
 * @param {boolean}[disabled] - optional parameter indicating whether the field should be disabled
 * @param {void}[handleChange] - callback function to handle input change 
 * @returns JSX Element
 */
const NumericInput = ({
    label,
    placeholder,
    value,
    handleChange,
    disabled
}: Props) => {
    
    const numericInputStyle = disabled ?
        'numeric-input numeric-input-inactive' :
        'numeric-input numeric-input-active'
    const labelStyle =
        ['numeric-input-label',
            disabled ?
                'numeric-input-disabled-label' :
                'numeric-input-active-label'
        ].join(' ')

    return (
        <div className='numeric-input-container'>
            <label className={labelStyle}>{label.toUpperCase()}</label>
            <input {...{
                value: value ?? '',
                onChange: handleChange,
                placeholder,
                type: 'number',
                className: numericInputStyle,
                disabled
            }} />
        </div>
    )
}

export default NumericInput