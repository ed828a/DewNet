# DewLight
this is the light weight version of Dewbe, get rid of DB
## Architecture
The app uses ViewModel to abstract the data from UI and Video Repository as single source of truth for data. 
The Repository fetch the data from the webservice and update the result in PagedLiveDataList and reflect the changes to UI. This project is based on Android Architecture Components -- MVVM

![](https://github.com/ed828a/DewNet/blob/master/archtiture-net.png)



Technology Points:
1. Pagination-- PagedList;
2. LiveData;
3. REST -- Retrofit2;
4. ViewModel;
5. Repository;
6. Dagger2;
7. RxJava2;
8. Youtube DATA API;
9. Youtube Android Player API;
10. JSON 
11. Kotlin
