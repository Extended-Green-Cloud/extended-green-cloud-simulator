module.exports = {
    parseData: function(data) {
        try {
            return JSON.parse(data)
        } catch (e) {
            return data
        }
    }
}