export type MultiLevelDetails = {
   key: string
   fields: MultiLevelSubEntries[]
}

export type MultiLevelValues = {
   label: string
   value: string
}

export type MultiLevelSubEntries = {
   key: string
   fields: MultiLevelValues[]
}
