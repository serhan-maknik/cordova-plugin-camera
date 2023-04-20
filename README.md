# cordova-plugin-camera

## Install
   ```
   cordova plugin add cordova-plugin-service
   ```
## Html
```html
<meta http-equiv="Content-Security-Policy"
        content=" style-src 'self' 'unsafe-inline'; media-src *; img-src 'self' data: content:;">

<button onclick="takePicture()" >Take Picture</button>
<img id="myImage" style="width: 100vw;"></img>

```

## javascript

```js
 function takePicture(){
    CameraPlugin.getPicture({
         quality:70,
         requiredSize:80
    },function(imageData){    
         var image = document.getElementById('myImage');
         image.src = "data:image/jpeg;base64," + imageData;
    },function(message){
        console.log(message);
    })
}
```
