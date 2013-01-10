<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Geoloc</title>
  </head>
  <body>
<script language="JavaScript">
navigator.geolocation.getCurrentPosition(LocationOK, LocationKO);

function LocationOK(position)
{
  var latitude = position.coords.latitude;
  var longitude = position.coords.longitude;
  alert('Location: ' + latitude + ', ' + longitude);
}
function LocationKO()
{
  alert('You\'re lost !');
}
</script>
 
  </body>
</html>
