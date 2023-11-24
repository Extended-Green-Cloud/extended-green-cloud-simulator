import { JobCreator } from './job-creator'

export interface ClientCreator {
   jobCreator: JobCreator
   clientName: string
}
