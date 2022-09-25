import Card from "components/card/card"
import { styles } from "./client-panel-styles"
import Select, { SingleValue } from 'react-select'
import { useAppSelector } from "@store"
import { Agent, ClientAgent } from "@types"
import SubtitleContainer from "components/subtitle-container/subtitle-container"
import { useEffect, useMemo, useState } from "react"
import { AgentOption, CLIENTS_ORDER, CLIENT_STATISTICS, GroupedAgentOption } from "./client-panel-config"
import DetailsField from "components/details-field/details-field"
import Badge from "components/badge/badge"
import { FilterOptionOption } from "react-select/dist/declarations/src/filters"


const header = "Client panel"
const description = "Select client from the list to diplay current job statistics"
const selectPlaceholder = "Provide client name"
const selectNoOption = "Client not found"
const selectNoClients = "Client list is empty"


/**
 * Component representing panel displaying details regarding network clients
 * 
 * @returns JSX Element 
 */
const ClientPanel = () => {
    const [selectedClient, setSelectedClient] = useState<AgentOption | null>()
    const { clients } = useAppSelector(state => state.agents)

    useEffect(() => {
        if (clients.length === 0) {
            setSelectedClient(null)
        }
    }, [clients])

    const selectData = useMemo(() => (clients as ClientAgent[])
        .reduce((prev, curr) => {
            const prevGroup = prev.find(el => el.label === curr.jobStatusEnum.toString())
            if (prevGroup) {
                prevGroup.options.push({ label: curr.name.toUpperCase(), value: curr })
            } else {
                prev.push({
                    label: curr.jobStatusEnum.toString(),
                    options: [{ label: curr.name.toUpperCase(), value: curr }]
                })
            }
            return prev
        }, [] as GroupedAgentOption[])
        .sort((a, b) => CLIENTS_ORDER.indexOf(a.label) - CLIENTS_ORDER.indexOf(b.label)), [clients])


    const customFilter = (option: FilterOptionOption<AgentOption>, inputValue: string) =>
        option.label.includes(inputValue)

    const handleOnChange = (value: SingleValue<AgentOption>) =>
        setSelectedClient(value)

    const handleNoOption = () => clients.length !== 0 ? selectNoOption : selectNoClients

    const generateClientInfo = () => {
        if (selectedClient) {
            return CLIENT_STATISTICS.map(field => {
                const { key, label } = field
                const clientVal = { ...selectedClient.value as any }[key]
                const value = key === 'jobStatusEnum' ?
                    <Badge text={clientVal} /> :
                    clientVal
                const property = key === 'jobStatusEnum' ?
                    'valueObject' :
                    'value'

                return (<DetailsField {...{ label, [property]: value }} />)
            })
        }
    }

    return (
        <Card {...{
            containerStyle: styles.clientContainer,
            header,
            removeScroll: true
        }}>
            <div style={styles.clientContent}>
                <Select
                    value={selectedClient}
                    onChange={handleOnChange}
                    placeholder={selectPlaceholder}
                    noOptionsMessage={handleNoOption}
                    styles={styles.select}
                    theme={styles.selectTheme}
                    options={selectData}
                    maxMenuHeight={150}
                    isSearchable={true}
                    isClearable={true}
                    isMulti={false}
                    filterOption={customFilter}
                />
                {!selectedClient || clients.length === 0 ?
                    <SubtitleContainer text={description} /> :
                    <div style={styles.clientStatistics}>{generateClientInfo()}</div>
                }
            </div>
        </Card>
    )
}

export default ClientPanel