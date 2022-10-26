import Card from "components/card/card"
import { styles } from "./client-panel-styles"
import Select, { SingleValue } from 'react-select'
import { agentsActions, useAppDispatch, useAppSelector } from "@store"
import { AgentStore, ClientAgent, JobStatus } from "@types"
import SubtitleContainer from "components/subtitle-container/subtitle-container"
import { useEffect, useMemo, useState } from "react"
import { AgentOption, CLIENTS_ORDER, CLIENT_STATISTICS, GroupedAgentOption } from "./client-panel-config"
import DetailsField from "components/details-field/details-field"
import Badge from "components/badge/badge"
import { FilterOptionOption } from "react-select/dist/declarations/src/filters"
import { Checkbox } from 'pretty-checkbox-react'
import '@djthoms/pretty-checkbox';
import Collapsible from "react-collapsible"


const header = "Client panel"
const description = "Select client from the list to diplay current job statistics"
const selectPlaceholder = "Provide client name"
const selectNoOption = "Client not found"
const selectNoClients = "Client list is empty"

interface JobStatusSelect {
    jobStatus: string,
    isSelected: boolean
}

const JOB_STATUS_MAP = Object.keys(JobStatus).map(key => { return ({ jobStatus: key.replaceAll('_', ' '), isSelected: true }) })

/**
 * Component representing panel displaying details regarding network clients
 * 
 * @returns JSX Element 
 */
const ClientPanel = () => {
    const [jobStatusMap, setJobStatusMap] = useState<JobStatusSelect[]>(JOB_STATUS_MAP)
    const agentState: AgentStore = useAppSelector(state => state.agents)
    const dispatch = useAppDispatch()
    const clients = agentState.clients
    const selectedClient = clients.find(agent => agent.name.toUpperCase() === agentState.selectedClient) ?? null

    useEffect(() => {
        if (clients.length === 0) {
            dispatch(agentsActions.setSelectedClient(null))
        }
    }, [clients, dispatch])

    const selectData = useMemo(() => (clients as ClientAgent[])
        .filter(client => jobStatusMap.find(el => el.jobStatus === client.jobStatusEnum.toString().replaceAll('_', ' '))?.isSelected)
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
        .sort((a, b) => CLIENTS_ORDER.indexOf(a.label) - CLIENTS_ORDER.indexOf(b.label)), [clients, jobStatusMap])


    const customFilter = (option: FilterOptionOption<AgentOption>, inputValue: string) =>
        option.label.includes(inputValue)

    const handleOnChange = (value: SingleValue<AgentOption>) => {
        console.warn(value)
        dispatch(agentsActions.setSelectedClient((value?.label ?? null)))
    }
    const handleNoOption = () => clients.length !== 0 ? selectNoOption : selectNoClients
    const handleSelectChange = (status: string) => {
        setJobStatusMap(prevState => prevState.map(jobStatus => {
            return jobStatus.jobStatus === status ?
                { ...jobStatus, isSelected: !jobStatus.isSelected } :
                jobStatus;
        }))
    }

    const generateSelectorBox = () => {
        const trigger =
            <>
                <span>DISPLAY FILTERS</span>
                <span>{'\u25BC'}</span>
            </>

        return (
            <Collapsible {...{trigger, triggerStyle: styles.collapse}} >
                <div style={styles.checkContainer}>
                    {jobStatusMap.map(entry =>
                        <Checkbox {...{
                            key: entry.jobStatus,
                            style: styles.checkBox,
                            value: entry.jobStatus,
                            checked: entry.isSelected,
                            onChange: () => handleSelectChange(entry.jobStatus),
                            color: "success",
                            shape: "curve",
                            animation: "smooth"
                        }}>
                            {entry.jobStatus}
                        </Checkbox>)}
                </div>
            </Collapsible>
        )
    }

    const generateClientInfo = () => {
        if (selectedClient) {
            return CLIENT_STATISTICS.map(field => {
                const { key, label } = field
                const clientVal = { ...selectedClient as any }[key]
                const value = key === 'jobStatusEnum' ?
                    <Badge text={clientVal} /> :
                    clientVal
                const property = key === 'jobStatusEnum' ?
                    'valueObject' :
                    'value'
                    
                return (<DetailsField {...{ label, [property]: value, key }} />)
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
                {generateSelectorBox()}
                <Select
                    value={{ value: selectedClient, label: selectedClient?.name ?? '' }}
                    onChange={handleOnChange}
                    placeholder={selectPlaceholder}
                    noOptionsMessage={handleNoOption}
                    styles={styles.select}
                    theme={styles.selectTheme}
                    options={selectData}
                    maxMenuHeight={130}
                    isSearchable={true}
                    isClearable={true}
                    menuPosition={'fixed'}
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