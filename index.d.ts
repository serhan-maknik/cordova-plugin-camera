declare const CameraPlugin:CameraPlugin;


interface CameraPlugin {
    getPicture(
        data:any,
        successCallback:()=>void,
        errorCallback:()=>void,
    ):void,

   
}
