export const convertUnixToTime = (unix: number) => {
   const date = new Date(unix * 1000)
   const year = date.getFullYear()
   const month = date.getMonth()
   const day = date.getDate()
   const hour = date.getHours()
   const min = date.getMinutes()

   const hourFormatted = hour < 10 ? '0' + hour : hour
   const minFormatted = min < 10 ? '0' + min : min

   return (
      day + '/' + month + '/' + year + ' ' + hourFormatted + ':' + minFormatted
   )
}
