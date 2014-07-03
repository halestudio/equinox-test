equinox-test
============

Equinox application that executes JUnit Tests inside the running OSGi framework.

About
-----

If you have an OSGi or Equinox based application with JUnit tests,
you can use this Equinox app to execute your tests inside a specific framework configuration.
This is especially useful for integration tests, but can also be used for unit tests.
If specified, the application will output an XML test report (similar to Maven Surefire tests).

Why?
----

We have run into many problems with OSGi based tests depending on framework configuration like available OSGi services.
As we could not solve this easily with for instance Maven Tycho, we created this solution, which we find is straightforward and easy to use, as it mostly relies on existing functionality of Equinox and for the configuration makes use of the User Interfaces supplied by Eclipse.

Setup
-----

For defining your framework configuration the easiest way is to create a product configuration in Eclipse.
There you can define the bundles (or features) that should be part of the OSGi test framework easily and with great support through the UI.
You can validate the product to check for missing dependencies and provide a configiration on bundle auto-start,
start levels and even needed heap space for the VM.

To use the Equinox test application you need to include the `de.fhg.igd.equinox.test.app` bundle in the product,
and select the application `de.fhg.igd.equinox.test` as application on the Overview page of the Product editor.

Add all bundles/features that contain the tests you want to execute to the product, as well as their dependencies
and needed configuration (e.g. to start specific OSGi services).

The application will by default execute all JUnit test classes that end with **Test** and are contained in a bundle
or fragment that ends with **.test**.

Launching
---------

You can launch the product directly from Eclipse using *Launch an Eclipse application* in the top right corner of the product editor.
In Eclipse you can also easily debug your test application. Alternatively you can run the application also from the built product, e.g. as created with Maven Tycho (like we do for production) or exported from Eclipse.

By default the application will print the test output and results to the console, to write the test reports as XML add the program argument `-out <path-to-file>`, specifying the file the test reports should be written to.

Configuration
-------------

Currently the output path for the test report (see above) is the only configuration option.
This works well for us as we rely on the convention of test class and bundle names.
In the future additional options might be added, e.g. for only executing a specific test.
Suggestions (or pull requests) are welcome!

Build
-----

Maven Tycho is used for creating bundles and an Update Site for this project. Use:

```
mvn install
```
