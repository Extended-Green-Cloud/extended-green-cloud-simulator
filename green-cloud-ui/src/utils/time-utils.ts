export const convertUnixToTime = (unix: number) => {
   const date = new Date(unix * 1000)
   const year = date.getFullYear()
   const month = date.getMonth()
   const day = date.getDate()
   const hour = date.getHours()
   const min = date.getMinutes()
   return day + '/' + month + '/' + year + ' ' + hour + ':' + min
}
