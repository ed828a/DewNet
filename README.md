# DewLight
this is the light weight version of Dewbe, get rid of DB
## Architecture
The app uses ViewModel to abstract the data from UI and Video Repository as single source of truth for data. 
The Repository fetch the data from the webservice and update the result in PagedLiveDataList and reflect the changes to UI.

![](https://github.com/ed828a/DewNet/blob/master/archtiture-net.png)
