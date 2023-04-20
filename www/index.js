const exec=require("cordova/exec");


module.exports={
    getPicture:(data,successCallback,errorCallback)=>{
        const obj={
            data:data
        };
        exec(successCallback,errorCallback,"CameraPlugin","takePicture",[obj]);
    },
    
}
