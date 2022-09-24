import Card from "components/card/card"
import { AgentOption, styles } from "./client-panel-styles"
import Select, { SingleValue } from 'react-select'
import { useAppSelector } from "@store"
import { Agent, AgentType } from "@types"
import SubtitleContainer from "components/statistics-panel/subtitle-container/subtitle-container"
import { useState } from "react"
import { CLIENT_STATISTICS } from "./client-panel-config"
import DetailsField from "components/statistics-panel/details-field/details-field"
import Badge from "components/badge/badge"

const header = "Client panel"
const description = "Select client from the list to diplay current job statistics"
const selectPlaceholder = "Provide client name"
const selectNoOption = "Client not found"
const selectNoClients = "Client list is empty"

const ClientPanel = () => {
    const { agents } = useAppSelector(state => state.agents)
    const [selectedClient, setSelectedClient] = useState<Agent>()
    const clients = agents.filter(agent => agent.type === AgentType.CLIENT)

    const selectData = clients.map(client => {
        return ({
            value: client,
            label: client.name.toUpperCase()
        })
    })

    const handleOnChange = (value: SingleValue<AgentOption>) =>
        setSelectedClient(value?.value)

    const generateClientInfo = () => {
        if (selectedClient) {
            console.log(selectedClient)
            return CLIENT_STATISTICS.map(field => {
                const { key, label } = field
                const clientVal = { ...selectedClient as any }[key]
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
                    onChange={handleOnChange}
                    placeholder={selectPlaceholder}
                    noOptionsMessage={() => clients.length !== 0 ? selectNoOption : selectNoClients}
                    styles={styles.select}
                    options={selectData}
                    maxMenuHeight={150}
                    theme={styles.selectTheme}
                    isSearchable
                    isClearable
                    isMulti={false}
                />
                {!selectedClient && <SubtitleContainer text={description} />}
                <div style={styles.clientStatistics}>
                    {generateClientInfo()}
                </div>
            </div>
        </Card>
    )
}

export default ClientPanel