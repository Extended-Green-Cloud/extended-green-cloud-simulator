export const convertUnixToTime = (unix: number) => {
   const date = new Date(unix * 1000)
   const year = date.getFullYear()
   const month = date.getMonth()
   const day = date.getDate()
   const hour = date.getHours()
   const min = date.getMinutes()
   const sec = date.getSeconds()

   const hourFormatted = hour < 10 ? '0' + hour : hour
   const minFormatted = min < 10 ? '0' + min : min
   const secFormatted = sec < 10 ? '0' + sec : sec

   return (
      day +
      '/' +
      month +
      '/' +
      year +
      ' ' +
      hourFormatted +
      ':' +
      minFormatted +
      ':' +
      secFormatted
   )
}
