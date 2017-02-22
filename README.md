# oclBenchNetworkMapper

This tool provides client and server side to distribute the execution of an OpenCL benchmark to execution devices available in the network. Mapping considers available machines and corresponding execution devices (CPU or GPU) on those machines.

### Client Side
- The client provides an interface to select an OpenCL benchmark and a corresponding dataset.
- It connects to servers on available execution machines on the network.
- It makes the decision on how to distribute the workload to connected machines.
- It receives execution statistics and presents them.

### Server Side
- The server receives benchmark and datasets to be executed from a connected client.
- It executes the workload according to specifications by the client.
- It measures execution statistics and returns them.


## NOTE
The project is currently under development and benchmark execution is not yet fully implemented.
Also, currently a client can only connect to a single server at a time.

---

## Build Dependencies
* java 8

(gradle automatically downloads the rest)

## Building the Project
This project uses gradle as build tool. A gradle wrapper is provided so gradle itself does not need to be installed on the build machine.

#### Build and Generate Jar:
```
$ ./gradlew build
```
The initial run will download necessary dependencies and can be slow. Future executions will be much faster.

The generated jar file can be found in ``./build/libs/oclBenchMapper-x.x.x.jar``

#### Build Eclipse Project:
To generate project files for the Eclipse IDE execute:
```
$ ./gradlew eclipse
```
#### Other Build Tasks:
To list further build tasks for gradle execute:
```
$ ./gradlew tasks
```
---

## Running the Client and Server
#### Client
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar client
```
Starts the client with default configurations. Use
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar client -h
```
to see additional command line options.

#### Server
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar server
```
Starts the server with default configurations. Use
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar server -h
```
to see additional command line options.

**ATTENTION: The server needs to be running and accessible before the client is started**


