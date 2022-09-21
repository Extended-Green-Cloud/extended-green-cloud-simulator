export type GraphEdge = {
    data: {
        id: string
        target: string,
        source: string,
        type: string,
        state: string
    },
    state: string
}