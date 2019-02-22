# Cloud Foundry for Dummies

This is a HowTo for a CF dummy like me which contains some very basic CF examples.

## Create a Cloud Foundry account

You first have to get an account on one of the many [certified Cloud Foundry platforms](https://www.cloudfoundry.org/certified-platforms/). For this example we will use [Pivotal Cloud Foundry](https://pivotal.io/platform) and [SAP Cloud Platform](https://cloudplatform.sap.com/).

### Pivotal Web Services

First [sign up for a new Pivotal Web Services account](https://try.run.pivotal.io/gettingstarted) and install the [CF CLI client](https://github.com/cloudfoundry/cli#downloads). On Ubuntu that's as simple as:

```
$ wget -q -O - https://packages.cloudfoundry.org/debian/cli.cloudfoundry.org.key | sudo apt-key add -
echo "deb https://packages.cloudfoundry.org/debian stable main" | sudo tee /etc/apt/sources.list.d/cloudfoundry-cli.list
$ sudo apt-get update
$ sudo apt-get install cf-cli
```

If everything worked you should be able to run:

```
$ cf help
```

You can now login into your Pivotal CF instance:

```
$ cf login -a https://api.run.pivotal.io
API endpoint: https://api.run.pivotal.io
Email> ...
Password> ...
Authenticating...
OK
Targeted org simonis
Targeted space development

API endpoint:   https://api.run.pivotal.io (API version: 2.131.0)
User:           ...
Org:            simonis
Space:          development
```

For managing your CF services online visit https://console.run.pivotal.io/

### SAP Cloud Platform

## Create your first CF application

We will now create our first, trivial CF application. In order to keep it simple and concentrate on CF we won't use any dependencies like Spring or Tomcat. It will be a simple Java server, based on the  [`com.sun.net.httpserver.HttpServer`](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) class that's part of OracleJDK/OpenJDK since Java 6, packed into a `.jar` file:

```
public class HelloCF {

  static class Handler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      ...
      StringBuffer response = new StringBuffer();
      response.append("Environment:\n");
      System.getenv().forEach((k, v) -> { response.append(k + " = " + v + "\n"); });
      response.append("\nProperties:\n");
      System.getProperties().forEach((k, v) -> { response.append(k + " = " + v + "\n"); });
      exchange.sendResponseHeaders(200, response.length());
      OutputStream os = exchange.getResponseBody();
      os.write(response.toString().getBytes());
      os.close();      
    }
  }

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/", new Handler());
    server.start();
  }
}
```

So we basically create a web server listening on port 8080 which serves all requests through the `Handler::handle()` function which simply returns all the environment variables and Java system properties.

You can import the sources as Eclipse project from `./helloCF/java`. If you run the applcation from within Eclipse, it will start a web server to which you can connect from your web browser at `localhost:8080`:

![Accessing HelloCF locally](./doc/helloCF_local.png)

You can now create a `.jar` file from the example by invoking `Eclipse -> Package Explorer -> HelloCF Project -> Export -> Runnable JAR file -> Next`. Select the `HelloCF` run configuration, `./helloCF/HelloCF.jar` as export destination and press `Finish`. You can test that everything worked by invoking:

```
$ java -jar ./helloCF/HelloCF.jar
```

and checking that `localhost:8080` is still accessible from your browser.

### Deploying your application to CF

This simple applcation can now easily be deployed to CF:

```
$ cf login -a https://api.run.pivotal.io
$ cf push HelloCF -p ./helloCF/HelloCF.jar
Pushing app HelloCF to org simonis / space development as ...
Getting app info...
Creating app with these attributes...
+ name:       HelloCF
  path:       /share/Git/HelloCF/helloCF/HelloCF.jar
  routes:
+   hellocf.cfapps.io

Creating app HelloCF...
Mapping routes...
Comparing local files to remote cache...
Packaging files to upload...
Uploading files...
 2.85 KiB / 2.85 KiB [=================================================================================================================================================] 100.00% 1s

Waiting for API to complete processing files...

Staging app and tracing logs...
   Downloading dotnet_core_buildpack_beta...
   Downloading staticfile_buildpack...
   Downloading java_buildpack...
   Downloading dotnet_core_buildpack...
   Downloading nodejs_buildpack...
   Downloaded dotnet_core_buildpack_beta
   Downloading go_buildpack...
   Downloaded dotnet_core_buildpack
   Downloading python_buildpack...
   Downloaded staticfile_buildpack
   Downloading php_buildpack...
   Downloaded java_buildpack
   Downloading binary_buildpack...
   Downloaded nodejs_buildpack
   Downloading ruby_buildpack...
   Downloaded go_buildpack
   Downloaded python_buildpack
   Downloaded php_buildpack
   Downloaded binary_buildpack
   Downloaded ruby_buildpack
   Cell 79f5396a-4d50-4880-91c5-fb071bafc57e creating container for instance 86d88161-6864-4eed-87cf-0f253bebb6f1
   Cell 79f5396a-4d50-4880-91c5-fb071bafc57e successfully created container for instance 86d88161-6864-4eed-87cf-0f253bebb6f1
   Downloading app package...
   Downloaded app package (2.9K)
   -----> Java Buildpack v4.17.2 (offline) | https://github.com/cloudfoundry/java-buildpack.git#47e68da
   -----> Downloading Jvmkill Agent 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/jvmkill/bionic/x86_64/jvmkill-1.16.0_RELEASE.so (found in cache)
   -----> Downloading Open Jdk JRE 1.8.0_202 from https://java-buildpack.cloudfoundry.org/openjdk/bionic/x86_64/openjdk-1.8.0_202.tar.gz (found in cache)
          Expanding Open Jdk JRE to .java-buildpack/open_jdk_jre (1.4s)
          JVM DNS caching disabled in lieu of BOSH DNS caching
   -----> Downloading Open JDK Like Memory Calculator 3.13.0_RELEASE from https://java-buildpack.cloudfoundry.org/memory-calculator/bionic/x86_64/memory-calculator-3.13.0_RELEASE.tar.gz (found in cache)
          Loaded Classes: 9755, Threads: 250
   -----> Downloading Client Certificate Mapper 1.8.0_RELEASE from https://java-buildpack.cloudfoundry.org/client-certificate-mapper/client-certificate-mapper-1.8.0_RELEASE.jar (found in cache)
   -----> Downloading Container Security Provider 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/container-security-provider/container-security-provider-1.16.0_RELEASE.jar (found in cache)
   Exit status 0
   Uploading droplet, build artifacts cache...
   Uploading build artifacts cache...
   Uploading droplet...
   Uploaded build artifacts cache (129B)
   Uploaded droplet (45.5M)
   Uploading complete
   Cell 79f5396a-4d50-4880-91c5-fb071bafc57e stopping instance 86d88161-6864-4eed-87cf-0f253bebb6f1
   Cell 79f5396a-4d50-4880-91c5-fb071bafc57e destroying container for instance 86d88161-6864-4eed-87cf-0f253bebb6f1

Waiting for app to start...

name:              HelloCF
requested state:   started
routes:            hellocf.cfapps.io
last uploaded:     Fri 22 Feb 19:00:36 CET 2019
stack:             cflinuxfs3
buildpacks:        client-certificate-mapper=1.8.0_RELEASE container-security-provider=1.16.0_RELEASE
                   java-buildpack=v4.17.2-offline-https://github.com/cloudfoundry/java-buildpack.git#47e68da java-main java-opts java-security jvmkill-agent=1.16.0_RELEASE
                   open-jd...

type:            web
instances:       1/1
memory usage:    1024M
start command:   JAVA_OPTS="-agentpath:$PWD/.java-buildpack/open_jdk_jre/bin/jvmkill-1.16.0_RELEASE=printHeapHistogram=1 -Djava.io.tmpdir=$TMPDIR -XX:ActiveProcessorCount=$(nproc)
                 -Djava.ext.dirs=$PWD/.java-buildpack/container_security_provider:$PWD/.java-buildpack/open_jdk_jre/lib/ext
                 -Djava.security.properties=$PWD/.java-buildpack/java_security/java.security $JAVA_OPTS" &&
                 CALCULATED_MEMORY=$($PWD/.java-buildpack/open_jdk_jre/bin/java-buildpack-memory-calculator-3.13.0_RELEASE -totMemory=$MEMORY_LIMIT -loadedClasses=9871
                 -poolType=metaspace -stackThreads=250 -vmOptions="$JAVA_OPTS") && echo JVM Memory Configuration: $CALCULATED_MEMORY && JAVA_OPTS="$JAVA_OPTS $CALCULATED_MEMORY"
                 && MALLOC_ARENA_MAX=2 eval exec $PWD/.java-buildpack/open_jdk_jre/bin/java $JAVA_OPTS -cp
                 $PWD/.:$PWD/.:$PWD/.java-buildpack/client_certificate_mapper/client_certificate_mapper-1.8.0_RELEASE.jar io.simonis.HelloCF
     state     since                  cpu    memory        disk       details
#0   running   2019-02-22T18:00:39Z   0.0%   34.9K of 1G   8K of 1G   

```

If everything works as expected, you should be able to access your application by opening `http://hellocf.cfapps.io`:

![Accessing HelloCF from CF](./doc/helloCF_cf.png)

In order for all this to work, there's a lot of magic happening under the hood and we'll explore and explain that in the next sections.

```
$ cf stop HelloCF
Stopping app HelloCF in org simonis / space development as ...
OK
```

```
$ cf delete -r HelloCF
Really delete the app HelloCF?> yes
Deleting app HelloCF in org simonis / space development as ...
OK
```
