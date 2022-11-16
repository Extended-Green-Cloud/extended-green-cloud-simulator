export interface TabProps {
   title: string
   isOpen: boolean
}

export const INITIAL_TABS: TabProps[] = [
   { title: 'Adaptation Statistics', isOpen: false },
   { title: 'Adaptation Log', isOpen: false },
   { title: 'Adaptation Goals', isOpen: false },
   { title: 'Event Triggers', isOpen: false },
]

export const ADAPTATION_STATISTICS_FIELDS = [
   { label: 'System Quality Indicatior', key: 'systemIndicator' },
   { label: 'System job success ratio', key: 'jobSuccessRatio' },
   { label: 'Number of performed adaptations', key: 'performedAdaptations' },
   { label: 'Number of weak adaptations', key: 'weakAdaptations' },
   { label: 'Number of strong adaptations', key: 'strongAdaptations' },
]

export const PERCENTAGE_KEYS = ['systemIndicator', 'jobSuccessRatio']
