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

Go to https://cloudplatform.sap.com/try.html and register for a free trial. Once you've done that, you have to log out from the [SAP Cloud Platform Cockpit](https://account.hanatrial.ondemand.com/cockpit) and confirm your registration by following the link in the confirmation mail you should have received by know. Only know you will have the possibility of choosing the "Cloud Foundry Trial" from the SAP CP Cockpit. Notice that there's another SAP CP flavor available from the Cockpit called "Neo Trial". This is SAP's proprietary cloud version which is not compatible with Cloud Foundry and won't be covered here.

If you follow the "Cloud Foundry Trial" you'll be taken to your CF subaccount where you can set up your trial account by choosing your favorite region. On the overview you'll finally find the API endpoint which may look like https://api.cf.us30.hana.ondemand.com (depending on the region you choose).

You can now use this CP API endpoint to log into your SAP CF instance with the same CF CLI which we've already downloaded and used before:

```console
$ CF_HOME=~/.cf_sap cf login -a https://api.cf.us30.hana.ondemand.com
API endpoint: https://api.cf.us30.hana.ondemand.com
Email> ...
Password> ...
Authenticating...
OK
Targeted org P2001183469trial_trial
Targeted space dev

API endpoint:   https://api.cf.us30.hana.ondemand.com (API version: 2.128.0)
User:           ...
Org:            P2001183469trial_trial
Space:          dev
```

Notice that we're using an alternative CF home location by setting the environment variable `CF_HOME=~/.cf_sap`. This is because the CF CLI stores and caches all the connection details and credentials in the users home directory under `.cf/`. So if you want to work on several API endpoints in parallel, you have to hold them apart by specifying `CF_HOME`.

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
  path:       ./helloCF/HelloCF.jar
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

### Buildpacks

As you can see from the lengthy output of the `cf push` command, CF tries to guess what kind of artefact we've pushed and how this artefact can be executed. To do that, it downloads a whole bunch of so called [*buildpacks*](https://docs.pivotal.io/pivotalcf/2-4/buildpacks/) which all do some automatic probing in order to find out if they can execute our application. In the end the `java_buildpack` takes over and executes our sample application.

You can easily list all the available buildpacks with the `buildpacks` command:

```console
$ cf buildpacks
Getting buildpacks...

buildpack                    position   enabled   locked   filename                                             stack
staticfile_buildpack         1          true      false    staticfile_buildpack-cached-cflinuxfs3-v1.4.40.zip   cflinuxfs3
java_buildpack               2          true      false    java-buildpack-offline-cflinuxfs3-v4.17.2.zip        cflinuxfs3
ruby_buildpack               3          true      false    ruby_buildpack-cached-cflinuxfs3-v1.7.34.zip         cflinuxfs3
dotnet_core_buildpack        4          true      false    dotnet-core_buildpack-cached-cflinuxfs3-v2.2.7.zip   cflinuxfs3
nodejs_buildpack             5          true      false    nodejs_buildpack-cached-cflinuxfs3-v1.6.45.zip       cflinuxfs3
go_buildpack                 6          true      false    go_buildpack-cached-cflinuxfs3-v1.8.35.zip           cflinuxfs3
python_buildpack             7          true      false    python_buildpack-cached-cflinuxfs3-v1.6.29.zip       cflinuxfs3
php_buildpack                8          true      false    php_buildpack-cached-cflinuxfs3-v4.3.71.zip          cflinuxfs3
binary_buildpack             9          true      false    binary_buildpack-cached-cflinuxfs3-v1.0.31.zip       cflinuxfs3
staticfile_buildpack         10         true      false    staticfile_buildpack-cached-cflinuxfs2-v1.4.40.zip   cflinuxfs2
java_buildpack               11         true      false    java-buildpack-offline-cflinuxfs2-v4.17.2.zip        cflinuxfs2
ruby_buildpack               12         true      false    ruby_buildpack-cached-cflinuxfs2-v1.7.34.zip         cflinuxfs2
dotnet_core_buildpack        13         true      false    dotnet-core_buildpack-cached-cflinuxfs2-v2.2.7.zip   cflinuxfs2
nodejs_buildpack             14         true      false    nodejs_buildpack-cached-cflinuxfs2-v1.6.45.zip       cflinuxfs2
go_buildpack                 15         true      false    go_buildpack-cached-cflinuxfs2-v1.8.35.zip           cflinuxfs2
python_buildpack             16         true      false    python_buildpack-cached-cflinuxfs2-v1.6.29.zip       cflinuxfs2
php_buildpack                17         true      false    php_buildpack-cached-cflinuxfs2-v4.3.71.zip          cflinuxfs2
binary_buildpack             18         true      false    binary_buildpack-cached-cflinuxfs2-v1.0.31.zip       cflinuxfs2
dotnet_core_buildpack_beta   19         true      false    dotnet-core_buildpack-cached-v1.0.0.zip
hwc_buildpack                20         true      false    hwc_buildpack-cached-windows2016-v3.1.6.zip          windows2016
binary_buildpack             21         true      false    binary_buildpack-cached-windows2016-v1.0.31.zip      windows2016
```

The output on the SAP Cloud Platform CF instance looks slightly different because every CF vendor can choose which system buildpacks he will offer by default.

```console
$ CF_HOME=~/.cf_sap cf buildpacks
Getting buildpacks...

buildpack                   position   enabled   locked   filename                                      stack
staticfile_buildpack        1          true      false    staticfile_buildpack-cflinuxfs2-v1.4.37.zip   cflinuxfs2
java_buildpack              2          true      false    java-buildpack-cflinuxfs2-v4.17.1.zip         cflinuxfs2
ruby_buildpack              3          true      false    ruby_buildpack-cflinuxfs2-v1.7.29.zip         cflinuxfs2
nodejs_buildpack            4          true      false    nodejs_buildpack-cflinuxfs2-v1.6.40.zip       cflinuxfs2
go_buildpack                5          true      false    go_buildpack-cflinuxfs2-v1.8.31.zip           cflinuxfs2
python_buildpack            6          true      false    python_buildpack-cflinuxfs2-v1.6.27.zip       cflinuxfs2
php_buildpack               7          true      false    php_buildpack-cflinuxfs2-v4.3.68.zip          cflinuxfs2
binary_buildpack            8          true      false    binary_buildpack-cflinuxfs2-v1.0.28.zip       cflinuxfs2
dotnet_core_buildpack       9          true      false    dotnet-core_buildpack-cflinuxfs2-v2.2.4.zip   cflinuxfs2
staticfile_buildpack        10         true      false    staticfile_buildpack-cflinuxfs3-v1.4.37.zip   cflinuxfs3
java_buildpack              11         true      false    java-buildpack-cflinuxfs3-v4.17.1.zip         cflinuxfs3
ruby_buildpack              12         true      false    ruby_buildpack-cflinuxfs3-v1.7.29.zip         cflinuxfs3
sap_java_buildpack_1_8_6    13         true      false    sap_java_buildpack-v1.8.6.zip
sap_java_buildpack          14         true      false    sap_java_buildpack-v1.8.6.zip
sap_java_buildpack_1_7_11   15         true      false    sap_java_buildpack-v1.7.11.zip
sap_java_buildpack_1_8_0    16         true      false    sap_java_buildpack-v1.8.0.zip
dotnet_core_buildpack       17         true      false    dotnet-core_buildpack-cflinuxfs3-v2.2.4.zip   cflinuxfs3
nodejs_buildpack            18         true      false    nodejs_buildpack-cflinuxfs3-v1.6.40.zip       cflinuxfs3
go_buildpack                19         true      false    go_buildpack-cflinuxfs3-v1.8.31.zip           cflinuxfs3
python_buildpack            20         true      false    python_buildpack-cflinuxfs3-v1.6.27.zip       cflinuxfs3
php_buildpack               21         true      false    php_buildpack-cflinuxfs3-v4.3.68.zip          cflinuxfs3
binary_buildpack            22         true      false    binary_buildpack-cflinuxfs3-v1.0.28.zip       cflinuxfs3
nginx_buildpack             23         true      false    nginx-buildpack-cflinuxfs3-v1.0.4.zip         cflinuxfs3
r_buildpack                 24         true      false    r-buildpack-cflinuxfs3-v1.0.3.zip             cflinuxfs3
```

We'll look more closely at the Java buildpack in the [Java buildpack configuration](#java-buildpack-configuration) section.

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

We get a warning because the route already exists but if we call our service (the updated one which gets the port from the environment) once again and look at the output, we can see that our application is now listening on the local port `1234`:

```
PORT=1234
VCAP_APP_PORT=1234
CF_INSTANCE_PORT=61180
CF_INSTANCE_PORTS=[{"external":61180,"internal":1234,"external_tls_proxy":61182,"internal_tls_proxy":61001},{"external":61181,"internal":2222,"external_tls_proxy":61183,"internal_tls_proxy":61002}]
```

#### Serving several routes on different ports from a single application

#### Using a custom domain for our application

If you have the possibility to control the DNS records of your own domain it is trivial to configure a custom subdomain for our `HelloCF` application. First we have to register our private domain within our CF organization:

```console
$ cf create-domain simonis simonis.io
Creating domain simonis.io for org simonis as volker.simonis...
OK
```

Once we've done that, we can create a new route from our custom subdomain (`hellocf.simonis.io` in this example) to our `HelloCF` application:

```console
$ cf map-route HelloCF simonis.io --hostname hellocf
Creating route hellocf.simonis.io for org simonis / space development as volker.simonis...
OK
Adding route hellocf.simonis.io to app HelloCF in org simonis / space development as volker.simonis...
OK
```

Before we can now call our application at `http://hellocf.simonis.io` we have to create a new [`CNAME`](https://en.wikipedia.org/wiki/CNAME_record) record in the DNS configuration of our domain which redirects the subdomain `hellocf` of `simonis.io` to `hellocf.cfapps.io`. How this can be done depends on your domain registrar but most of them offer a simple web interface for DNS administration nowadays.

### Java buildpack configuration

Until now we've used the Java buildpack 'as-is'. But the buildpack actually offers a lot of extension points and configuration options some of which we'll explore in the following sections.

#### Selecting the Java version

By default the [Java build pack](https://github.com/cloudfoundry/java-buildpack) currently uses Java 8 as default JDK/JRE. And the build pack can not detect which is the minimal Java version required by our application. So if we replace the following line in our application

```java
while (is.read(new byte[512]) != -1);
```

by the more convenient call to `InputStream::readAllBytes()`

```java
is.readAllBytes();
```

which was introduced in Java 9, repackage our application with Java 11 and simply redeploy it, we will see the following error:

```console
$ cf push HelloCF -p ./helloCF/HelloCF.jar
Pushing app HelloCF to org simonis / space development as volker.simonis...
...
   -----> Java Buildpack v4.17.2 (offline) | https://github.com/cloudfoundry/java-buildpack.git#47e68da
   -----> Downloading Jvmkill Agent 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/jvmkill/bionic/x86_64/jvmkill-1.16.0_RELEASE.so (found in cache)
   -----> Downloading Open Jdk JRE 1.8.0_202 from https://java-buildpack.cloudfoundry.org/openjdk/bionic/x86_64/openjdk-1.8.0_202.tar.gz (found in cache)
...
Waiting for app to start...
Start unsuccessful

TIP: use 'cf logs HelloCF --recent' for more information
FAILED
```

As you can see from the logs, the CF Java build pack chooses *Open Jdk JRE 1.8.0_202* by default. When we use the suggested `logs` command to look at the output of our application, we can confirm that the reason for the application failure was indeed the wrong (i.e. too high) class file version which is not supported by Java 8:

```console
$ cf logs HelloCF --recent
...
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR Error: A JNI error has occurred, please check your installation and try again
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR Exception in thread "main" java.lang.UnsupportedClassVersionError: io/simonis/HelloCF has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to 52.0
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.lang.ClassLoader.defineClass1(Native Method)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.lang.ClassLoader.defineClass(ClassLoader.java:763)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.net.URLClassLoader.defineClass(URLClassLoader.java:468)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.net.URLClassLoader.access$100(URLClassLoader.java:74)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.net.URLClassLoader$1.run(URLClassLoader.java:369)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.net.URLClassLoader$1.run(URLClassLoader.java:363)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.security.AccessController.doPrivileged(Native Method)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.net.URLClassLoader.findClass(URLClassLoader.java:362)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
   2019-03-11T09:52:49.09+0100 [APP/PROC/WEB/0] ERR 	at sun.launcher.LauncherHelper.checkAndLoadMain(LauncherHelper.java:495)
   2019-03-11T09:52:49.11+0100 [APP/PROC/WEB/0] OUT Exit status 1
   2019-03-11T09:52:49.12+0100 [CELL/SSHD/0] OUT Exit status 0
...
```

In order to fix the problem, we have to configure the Java build pack such that it runs our application on Java 11. This can be achieved with the help of environment variables:

```console
$ cf set-env HelloCF JBP_CONFIG_OPEN_JDK_JRE '{ jre: { version: 11.+ } }'
Setting env variable 'JBP_CONFIG_OPEN_JDK_JRE' to '{ jre: { version: 11.+ } }' for app HelloCF in org simonis / space development as volker.simonis...
OK
TIP: Use 'cf restage HelloCF' to ensure your env variable changes take effect
$ cf restage HelloCF
Restaging app HelloCF in org simonis / space development as volker.simonis...
...
   -----> Java Buildpack v4.17.2 (offline) | https://github.com/cloudfoundry/java-buildpack.git#47e68da
   -----> Downloading Jvmkill Agent 1.16.0_RELEASE from https://java-buildpack.cloudfoundry.org/jvmkill/bionic/x86_64/jvmkill-1.16.0_RELEASE.so (found in cache)
   -----> Downloading Open Jdk JRE 11.0.2_09 from https://java-buildpack.cloudfoundry.org/openjdk/bionic/x86_64/openjdk-11.0.2_09.tar.gz (found in cache)
...
Waiting for app to start...

name:              HelloCF
requested state:   started
routes:            hellocf.cfapps.io, hellocf.simonis.io
...
     state     since                  cpu    memory    disk      details
#0   running   2019-03-11T13:45:08Z   0.0%   0 of 1G   0 of 1G   
```

As you can read in the [documentation](https://github.com/cloudfoundry/java-buildpack#configuration-and-extension)
of the Java build pack, the "*configuration can be overridden with an environment variable matching the configuration file you wish to override minus the .yml extension and with a prefix of JBP_CONFIG*". Looking at the Java build pack repository at https://github.com/cloudfoundry/java-buildpack you can see that the `config` subdirectory contains quite some Yaml configuration files. One of the is called `open_jdk_jre.yml` and contains the seetings for the default Java version:

```Yaml
jre:
  version: 1.8.0_+
```

In general, the Java build pack creates abstractions for [containers](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/design.md#container-components), [frameworks](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/design.md#framework-components) and [JREs](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/design.md#jre-components) and all of them can be easily [extended and configured](https://docs.pivotal.io/pivotalcf/2-4/buildpacks/java/java-tips.html).

#### Using an alternative JRE

The CF Java build pack allows not only the configuration of the Java version, but also the choice of an alternative JDK/JRE. The default JDK is configured in [`config/components.yml`](https://github.com/cloudfoundry/java-buildpack/blob/master/config/components.yml) as follows:

```Yaml
jres:
 - "JavaBuildpack::Jre::OpenJdkJRE"
```

but the build pack already offers out of the box some other JDKs like for example [IBM SDK](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/jre-ibm_jre.md), [Azul Zulu](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/jre-zulu_jre.md) or [SapMachine](https://github.com/cloudfoundry/java-buildpack/blob/master/docs/jre-sap_machine_jre.md). To make SapMachine the default JRE we have three possibilities. We could either fork the default Java build pack and edit `config/components.yml` to point to `SapMachineJRE` by default. We could also use `cf set-env` to set `JBP_CONFIG_COMPONENTS` (i.e. the override for `config/components.yml`) to  `'{jres: ["JavaBuildpack::Jre::SapMachineJRE"]}'`.

The third possibility is to set the corresponding environment variable in the so called [App Manifest](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html) file. By default `cf push` always looks for a file called `manifest.yml` in the current directory, but this can be easily overridden with the `-f <Manifest-File>` option.

A manifest for our application may look as follows:

```Yaml
---
applications:
- name: HelloCF
  env:
    JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 11.+ } }'
    JBP_CONFIG_COMPONENTS: '{jres: ["JavaBuildpack::Jre::SapMachineJRE"]}'
```

With such a Manifest file, we can now push with:

```console
$ cf push -p ./helloCF/HelloCF.jar
Pushing from manifest to org simonis / space development as volker.simonis...
Using manifest file manifest.yml
Getting app info...
Creating app with these attributes...
+ name:       HelloCF
  path:       ./helloCF/HelloCF.jar
  env:
+   JBP_CONFIG_OPEN_JDK_JRE
...
```

Notice how the application name and the environment variable for the Java version have been taken from the Manifest file (command line arguments can still override the Manifiest values). Now let's specify `SapMachine` as default JRE (and `java_buildpack` as default buildpack to free CF from the burden to download and probe all the available buildpaks each time we push):

```Yaml
---
applications:
- name: HelloCF
  buildpacks:
  - java_buildpack
  env:
    JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 11.+ } }'
    JBP_CONFIG_COMPONENTS: '{jres: ["JavaBuildpack::Jre::SapMachineJRE"]}'
```

Unfortunately this won't work out of the box in Pivotal Web Services (PWS):

```console
$ cf push -p ./helloCF/HelloCF.jar
Pushing from manifest to org simonis / space development as volker.simonis...
Using manifest file ./manifest.yml
Getting app info...
Creating app with these attributes...
+ name:         HelloCF
  path:         ./helloCF/HelloCF.jar
  buildpacks:
+   java_buildpack
  env:
+   JBP_CONFIG_COMPONENTS
+   JBP_CONFIG_OPEN_JDK_JRE
...
  -----> Java Buildpack v4.17.2 (offline) | https://github.com/cloudfoundry/java-buildpack.git#47e68da
   [Buildpack]                      ERROR Finalize failed with exception #<RuntimeError: Sap Machine JRE error: Unable to find cached file for https://sap.github.io/SapMachine/assets/cf/jre/linux/x86_64/index.yml>
   Sap Machine JRE error: Unable to find cached file for https://sap.github.io/SapMachine/assets/cf/jre/linux/x86_64/index.yml
   Failed to compile droplet: Failed to run finalize script: exit status 1
...
```

The problem is that PWS uses so called "[*offline*](https://www.cloudfoundry.org/blog/packaged-and-offline-buildpacks/)" (or "[*packaged*](https://www.cloudfoundry.org/blog/packaged-and-offline-buildpacks/)") buildpacks which contain a bunch of prepackaged dependencies (see the [Pivotal Network](https://network.pivotal.io/products/java-buildpack) for a full list), but apparently not the alternative JDKs.

However, the default community Java build pack has full support for alternative JDKs and we can easily leverage it by using the GitHub version of the buildpack instead of the offline version in PWS:

```Yaml
---
applications:
- name: HelloCF
  buildpacks:
  - https://github.com/cloudfoundry/java-buildpack.git#v4.17.1
  env:
    JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 11.+ } }'
    JBP_CONFIG_COMPONENTS: '{jres: ["JavaBuildpack::Jre::SapMachineJRE"]}'
```

With this new configuration, everything works smoothly:

```console
$ cf push -p ./helloCF/HelloCF.jar
Pushing from manifest to org simonis / space development as volker.simonis...
Using manifest file manifest.yml
Getting app info...
Creating app with these attributes...
+ name:         HelloCF
  path:         ./helloCF/HelloCF.jar
  buildpacks:
+   https://github.com/cloudfoundry/java-buildpack.git#v4.17.1
  env:
+   JBP_CONFIG_COMPONENTS
+   JBP_CONFIG_OPEN_JDK_JRE
...
   -----> Java Buildpack v4.17.1 | https://github.com/cloudfoundry/java-buildpack.git#ebd0c5a
   -----> Downloading Sap Machine JRE 11.0.2_0.0.b0 from https://github.com/SAP/SapMachine/releases/download/sapmachine-11.0.2/sapmachine-jdk-11.0.2_linux-x64_bin.tar.gz (5.4s)
...
     state     since                  cpu    memory        disk       details
#0   running   2019-03-11T19:05:54Z   0.0%   34.9K of 1G   8K of 1G   
```

SAP Cloud Platform does not use offline buildpacks for the community Java buildpack (but they do so for their proprietary `sap_java_buildpack`), so we can easily select SapMachine on SAP CP without specifying any buildpack at all.
-------------

TBD

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
