# Cloud Foundry for Absolut Beginners&trade;

A HowTo for CF dummies like me with some very basic examples.

## Create a Cloud Foundry account

You first have to get an account on one of the many [certified Cloud Foundry platforms](https://www.cloudfoundry.org/certified-platforms/). For this example we will use [Pivotal Cloud Foundry](https://pivotal.io/platform) and [SAP Cloud Platform](https://cloudplatform.sap.com/).

### Pivotal Web Services

First [sign up for a new Pivotal Web Services account](https://try.run.pivotal.io/gettingstarted) and install the [CF CLI client](https://github.com/cloudfoundry/cli#downloads). On Ubuntu that's as simple as:

```console
$ wget -q -O - https://packages.cloudfoundry.org/debian/cli.cloudfoundry.org.key | sudo apt-key add -
echo "deb https://packages.cloudfoundry.org/debian stable main" | sudo tee /etc/apt/sources.list.d/cloudfoundry-cli.list
$ sudo apt-get update
$ sudo apt-get install cf-cli
```

If everything worked you should be able to run:

```console
$ cf help
```

You can now login into your Pivotal CF instance:

```console
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

```java
public class HelloCF {

  static class Handler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      InputStream is = exchange.getRequestBody();
      while (is.read(new byte[512]) != -1); // 'is' will be closed implicitly when we close 'os'
      System.out.println(
          exchange.getRemoteAddress() + " -> " +
          exchange.getLocalAddress() +
          exchange.getRequestURI() + " (" +
          exchange.getProtocol() + ")");
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

```console
$ java -jar ./helloCF/HelloCF.jar
```

and checking that `localhost:8080` is still accessible from your browser.

### Deploying your application to CF

This simple applcation can now easily be deployed to CF:

```console
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

### Routes, Mappings and Ports

You may be wondering why we can access `http://hellocf.cfapps.io` (i.e. port 80 of `hellocf.cfapps.io`) and connect to our webserver which is actually listinening on `localhost:8080`? This is because when we are pushing a new application with `cf push HelloCF -p HelloCF.jar` CF does the following steps for us by default:

It creates a new route (i.e. subdomain) with the same name like the application and connects it to to the application. You can see that in the output of the `push` command:

```
  routes:
+   hellocf.cfapps.io

Creating app HelloCF...
Mapping routes...
```

After successfully pushing of our application, we can query the routes with the `cf routes` command:

```console
$ cf routes
Getting routes for org simonis / space development as volker.simonis ...

space         host      domain      port   path   type   apps      service
development   hellocf   cfapps.io                        HelloCF
```

And the route mappings with `cf curl /v2/route_mappings`:

```console
$ cf curl /v2/route_mappings
{
   "total_results": 1,
   "total_pages": 1,
   "prev_url": null,
   "next_url": null,
   "resources": [
      {
         "metadata": {
            "guid": "0836dcae-e9af-453a-a563-af75e57f3249",
            "url": "/v2/route_mappings/0836dcae-e9af-453a-a563-af75e57f3249",
            "created_at": "2019-03-05T18:08:33Z",
            "updated_at": "2019-03-05T18:08:33Z"
         },
         "entity": {
            "app_port": 8080,
            "app_guid": "0a905a31-019d-416a-8867-b2a23dea1e8a",
            "route_guid": "4c85d1d9-8d9c-4676-8be0-221ed0110d71",
            "app_url": "/v2/apps/0a905a31-019d-416a-8867-b2a23dea1e8a",
            "route_url": "/v2/routes/4c85d1d9-8d9c-4676-8be0-221ed0110d71"
         }
      }
   ]
}
```

As you can see, the global HTTP URL `hellocf.cfapps.io` (i.e. `hellocf.cfapps.io:80`) will be mapped to the local port (i.e. `app_port`) `8080` on the host where our webserver is running. CF also exposes this port in the environment. Our simple webserver simply returns the OS environment and Java properties so when we take a look at the output (i.e. by loading `http://hellocf.cfapps.io`) we can see the following:

```
PORT=8080
VCAP_APP_PORT=8080
CF_INSTANCE_PORT=61158
CF_INSTANCE_PORTS=[{"external":61158,"internal":8080,"external_tls_proxy":61160,"internal_tls_proxy":61001},{"external":61159,"internal":2222,"external_tls_proxy":61161,"internal_tls_proxy":61002}]
```

`PORT` and `VCAP_APP_PORT` (which is the old, deprecated name), both have the value `8080`. So instead of hardcoding our webserver to listen on port `8080` it would be more robust to listen on the port exposed by the `PORT` environment variable instead:

```java
...
int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
...
```

[`CF_INSTANCE_PORT`](https://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html#CF-INSTANCE-PORT) denotes the *external, or host-side, port corresponding to the internal, or container-side, port with value `PORT`*
while [`CF_INSTANCE_PORTS`](https://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html#CF-INSTANCE-PORTS) denotes the *list of mappings between internal, or container-side, and external, or host-side, ports allocated to the instance’s container*. Notice that not all of these internal ports (e.g. `2222` in this example) are necessarily available for the application to bind to, as some of them may be used by system-provided services that also run inside the container.

#### Listening on a custom port

CF has BETA support for [routing traffic to custom application ports](https://docs.run.pivotal.io/devguide/custom-ports.html). However, this feature is currently not implemented directly in the CF CLI but can only be used directly through the [Cloud Controller API endpoints](https://apidocs.cloudfoundry.org/6.10.0/). Changing the default port can be done as follows:

```console
$ cf curl /v2/apps/$(cf app HelloCF --guid) -X PUT -d '{"ports": [1234]}'
{
   "description": "...App ports ports may not be removed while routes are mapped to them...",
   "error_code": "CF-AppInvalid",
   "code": 100001
}
```

As you can see, if we're doing this for the application which we've pushed before, we will get an error message because we can't change the application ports while routes are mapped to them (and CF has created a default route as described before). So we'll first have to unmap that route from our application:

```console
$ cf unmap-route HelloCF cfapps.io --hostname hellocf
Removing route hellocf.cfapps.io from app HelloCF in org simonis / space development as volker.simonis...
OK
$ cf routes
Getting routes for org simonis / space development as volker.simonis ...

space         host      domain      port   path   type   apps   service
development   hellocf   cfapps.io
$ cf curl /v2/route_mappings
{
   "total_results": 0,
   "total_pages": 1,
   "prev_url": null,
   "next_url": null,
   "resources": []
}
```

Notice that we haven't deleted the route (it is still there), we've only unmapped it from our application. Now we can change the local port for our application:

```console
$ cf curl /v2/apps/$(cf app HelloCF --guid) -X PUT -d '{"ports": [1234]}'
{
   "metadata": {
      "guid": "cfd8a910-a3d7-4008-b609-475ba9e7f456",
      ...
   },
   "entity": {
      "name": "HelloCF",
      ...
      "ports": [
         1234
      ],
      ...
   }
}
```

And once we've done that, we can remap our route to the application:

```console
$ cf map-route HelloCF cfapps.io --hostname hellocf
Creating route hellocf.cfapps.io for org simonis / space development as volker.simoni...
OK
Route hellocf.cfapps.io already exists
Adding route hellocf.cfapps.io to app HelloCF in org simonis / space development as volker.simonis...
OK
$ cf curl /v2/route_mappings
{
   ...
         "entity": {
            "app_port": 1234,
            ...
}
```

We gat a warning because the route already exists but if we call our service (the updated one which gets the port from the environment) once again and look at the output, we can see that our applcation is now listening on the local port `1234`:

```
PORT=1234
VCAP_APP_PORT=1234
CF_INSTANCE_PORT=61180
CF_INSTANCE_PORTS=[{"external":61180,"internal":1234,"external_tls_proxy":61182,"internal_tls_proxy":61001},{"external":61181,"internal":2222,"external_tls_proxy":61183,"internal_tls_proxy":61002}]
```

#### Serving several routes on different ports


```console
$ cf curl /v2/apps/$(cf app HelloCF --guid) -X PUT -d '{"ports": [1234, 8080, 4567]}'
```

```console
$ cf stop HelloCF
Stopping app HelloCF in org simonis / space development as ...
OK
```

```console
$ cf delete -r HelloCF
Really delete the app HelloCF?> yes
Deleting app HelloCF in org simonis / space development as ...
OK
```
