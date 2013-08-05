What is AndroidLog?
===================
AndroidLog is an util to write logs for Android platform. You can not only print logs to Logcat as common by using *L.DEBUG* or *L.RELEASE* object, but alse write logs to any persistence storage, for example, AndroidLog provides two common ways: File and SQLite database, you can simplely extends *AsyncLog* class to write logs to any other storage, like network.

All of the API in AndroidLog is the same as Log, so you can simplely change Log to AndroidLog.


How many ways does AndroidLog provide to write logs?
====================================================
1. write logs to Logcat:
	+ *L.DEBUG* will print logs as you call the method of Log
	+ *L.RELEASE* won't print any logs
	+ you can simplely implements *L* interface to print logs as you want to
	
2. write logs to file system:
	> initialize an FileLog object, then call the methods of it. It will serialize the object of *AsyncLog.Marker* to the file system. The file is stored at "AndroidLogs/YourPackageName.log" in the root of your file system.
	
3. write logs to SQLite database:
	> initialize an SQLiteLog object, then call the methods of it. It will storage the information of *AsyncLog.Marker* object to SQLite database.
	
4. write logs to any other persistence storage:
	> simplely implements the interface *AsyncLog.LogWriter*, then pass the object to *Async* constructor
	> if you want to read logs, just extends the class *AsyncLog* and override the method *readLogs()*
	

Notes
=====
+ if using *AsyncLog* or its subclass, you should have only one object in your application as common
+ if using *AsyncLog* or its subclass, you should call the method *quit()* after you quit your application
