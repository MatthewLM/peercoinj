### Welcome to paycoinj

The paycoinj library is a Java implementation of the Paycoin protocol, which allows it to maintain a wallet and send/receive transactions without needing a local copy of Paycoin Core. It comes with full documentation and some example apps showing how to use it. paycoinj uses a central server to download block hashes for verification.

### Technologies

* Java 6+
* [Maven 3+](http://maven.apache.org) - for building the project
* [Orchid](https://github.com/subgraph/Orchid) - for secure communications over [TOR](https://www.torproject.org)
* [Google Protocol Buffers](https://code.google.com/p/protobuf/) - for use with serialization and hardware communications
* [PaycoinAbeExplorer](https://github.com/xpydev/PaycoinAbeExplorer) - server code which provides block hashes and acts as block explorer

### Getting started

To get started, it is best to have the latest JDK and Maven installed. The HEAD of the `master` branch contains the latest development code and various production releases are provided on feature branches.

#### Building from the command line

To perform a full build use
```
mvn clean package -Dmaven.test.skip=true
```
You can also run
```
mvn site:site
```
to generate a website with useful information like JavaDocs.

The outputs are under the `target` directory.

#### Building from an IDE

Alternatively, just import the project using your IDE. [IntelliJ](http://www.jetbrains.com/idea/download/) has Maven integration built-in and has a free Community Edition. Simply use `File | Import Project` and locate the `pom.xml` in the root of the cloned project source tree. Be sure to add "Skip Tests" to Build/Clean and Build parameters.

### Example applications

These are found in the `examples` module.

#### Forwarding service

This will download the block chain and eventually print a Paycoin address that it has generated.

If you send coins to that address, it will forward them on to the address you specified.

```
  cd examples
  mvn exec:java -Dexec.mainClass=io.xpydev.paycoinj.examples.ForwardingService -Dexec.args="<insert a paycoin address here>"
```

Note that this example app *does not use checkpointing*, so the initial chain sync will be pretty slow. You can make an app that starts up and does the initial sync much faster by including a checkpoints file; see the documentation for
more info on this technique.
