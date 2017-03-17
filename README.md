# oclBenchNetworkMapper

This tool provides client and server side to distribute the execution of an OpenCL benchmark to execution devices available in the network. Mapping considers available machines and corresponding execution devices (CPU or GPU) on those machines. This tool is mainly developed as a demo tool for the [CHIST-ERA Projects Seminar 2017 in Brussels](http://www.chistera.eu/projects-seminar-2017).

### Client Side
- The client provides an interface to select an OpenCL benchmark and a corresponding dataset. (GUI and Console)
- It connects to servers on available execution machines on the network.
- It makes the decision on how to distribute the workload to connected machines.
- It receives execution statistics and evaluates and presents them.

### Server Side
- The server receives benchmark and dataset specifications to be executed from a connected client.
- Benchmark binaries and data files need to be available on the server, they are not transmitted.
- It executes the workload.
- It measures execution statistics and returns them.

### Available Benchmarks
In this version the available benchmarks are restricted to [Rodinia](http://www.cs.virginia.edu/~skadron/wiki/rodinia/index.php/Rodinia:Accelerating_Compute-Intensive_Applications_with_Accelerators) *Kmeans* and *Lud* benchmarks. Different data set sizes are specified for each benchmark. Additional benchmarks and dataset sizes can be configured using csv configuration files (see [Configuring Benchmarks](#configuring-benchmarks)).

### Available Workload Mapper

The mapper to be used can be selected on client side with the ```-m``` or ```-mapperType``` argument.

#### First come first served mapper (```-m FCFS```)
aps the selected benchmark to the default device on the first available server.

#### Duplicate mapper (```-m DUPLICATE```)
Maps the entire selected benchmark to the default device on each available server.

#### Predictive mapper (```-m PREDICTIVE```)
Maps the selected benchmark to each available server using an execution device predicted as best fit in terms of energy consumption and performance. 

Code features of the benchmark's OpenCL kernel(s) are passed to a model which returns an optimal GPU or CPU mapping for the kernel. The model is trained for a particular server architecture which can be specified when starting the server. Upon connection with the client, the server transfers an architecture id which allows the client to pick a corresponding workload to execution device prediction model. 

For performance reasons predictions are made ahead of execution time and saved in a [configuration file](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/serverConf/dividend_device_predictions.csv) which is used at runtime by the client.

The file with ahead of time predictions can be specified using the ```--offlinePredictions``` command line argument upon client startup.

---

## Running the Client and Server
#### Client
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar client
```
Starts the client with default configuration trying to connect to a single server at `localhost:9090`, using the 
```FCFS``` workload mapper and a console based user interface.

Multiple server addresses can be configured using the `-a` attribute. For example:
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar client -a 192.168.0.1:9090 192.168.173.4:9091

```
The client can be executed using a graphical user interface (see [Client GUI](#client-gui)).
For additional help, execute:
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar client -h
```

#### Server
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar server -A dividend
```
Starts the server with default benchmark configurations (see [Configuring Benchmarks](#configuring-benchmarks) for information on custom benchmark configurations).

It is listening on default port 9090 for client connections which can be specified otherwise using the ```-p``` argument.

```-A dividend``` specifies the server architecture and the given id is passed on to the client upon connection.

Server simulation is available using the ```--simulation``` command and specifying a file with simulation data (see [Non Interactive Client](#non-interactive-client) on how to generate simulation data for a specific server architecture).

To see additional help, execute:
```
$ java -jar ./build/libs/oclBenchMapper-x.x.x.jar server -h
```

**ATTENTION: All configured servers need to be running and accessible before a client is started**

### Client GUI

The client provides a graphical user interface which can be started using the ```--gui``` flag. (I am not a designer, keep that in mind!)

![](https://cloud.githubusercontent.com/assets/17176876/24053124/de6ae1d4-0b2f-11e7-94d1-e387b42e3a7b.png)

The displayed GUI has control elements in the upper part and two graphs displaying energy consumption (left) and execution performance (right). 
* Two combo boxes allow the selection of a benchmark and a corresponding workload. 
* Pressing the ```Run``` button executes the selected benchmark with the given workload on the available servers using the specified workload mapper.
* Pressing the ```Start Automatic``` button initiates an automatic mode. In this mode the client automatically executes the selected benchmark over and over on the available servers. The dataset size is selected randomly for each execution.
* Execution results of all servers are combined and displayed in the two charts.
* The ```mapper``` series shows execution results using the device mapping made by the client.
* Additionally, a graph is displayed for each possible combination of CPU or GPU depending on available servers. This allows comparing mapper results to alternative combinations. For example, the mapper executes a workload on the GPU of server 1 and the CPU of server 2. Alternative mappings for all combinations of server 1 and 2 as well as GPU or CPU are displayed for comparisson.
* Alternative mapping results are not actually executed but taken from a [precomputation file](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/dividend_device_executionStats.csv).
* Each chart shows energy and performance results normalised to the best execution device (CPU or GPU) for the executed benchmark and workload combination. Normalisation uses the same precomputed execution statistics like alternative mappings.

### Non Interactive Client

The client can be executed in ```non-interactive-client``` mode. 
* In this mode no user interface is provided. The client executes a set of commands provided via [input file](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/cmdInputDummy.csv).
* The client can connect to only a single server in this mode.
* Execution statistics are saved in [precomputation file](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/dividend_device_executionStats.csv) format.
* Additionally an optimal execution device to benchmark mapping is generated and saved.

### Configuring Benchmarks

New benchmarks can be configured to be used by the system:

1. Add a name for the new benchmark to the [benchmark configuration](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/dividend_benchmark_config.csv) file. This file needs to be available to both client and server.
2. Add necessary execution commands to the [benchmark execution configuration](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/dividend_rodinia_bench_config.csv). The ```BinDir``` column needs the location of the benchmarks directory relative to the server's working directory. The ```ExecCmd``` column needs the actual command to execute the benchmark minus the name of the data file.
3. Add data set sizes to the [benchmark data configuration](https://github.com/vsee/oclBenchNetworkMapper/blob/master/src/main/resources/dividend_rodinia_data_config.csv). The ```DataArgs``` column expects a data file location relative to the corresponding benchmark's working directory.
4. To properly use the client's GUI, execution statistics and execution device predictions need to be generated and added to the corresponding configurations (see [Non Interactive Client](#non-interactive-client)).

---

## Build Dependencies
* java 8
* AMD power profiling API implemented for OpenCL on server side

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


