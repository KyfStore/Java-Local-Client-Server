# Java Server-Client

## How To Build

To use this Java project, you can run either of the build files:

- build.sh (For MAC/LINUX)
- build.cmd (FOR WINDOWS)

This will compile the code and build it. Once its done, it will automatically startup the server.

## How To Use

Once the server is open you can see that the server GUI is open on port 3000 (**NOTE:** This does not mean it is hosted on `localhost:3000`, it is just a placeholder server ID! Feel free to change this property in the [Server.json](public\dependencies\server.json) file). You will notice that there will be a number of clients currently on your local server. 

## How to add/remove clients from server

To test this experiment,  you have to have at least one server window open. Then, rerun the build file and it will open a new window. You will notice; a new client will join the server, the [server.json](public\dependencies\server.json) increases the clients property, on both screens the current online clients update. You will also notice that when you close a window it also updates and does the same coroutine again.

## Conclusion

Without any dependencies from Maven/Gradle/Any Other Software, online Java software is **not** possible, but local Java clients on a server with the same port/same access to same exact files, it is possible!