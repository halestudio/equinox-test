// Fraunhofer Institute for Computer Graphics Research (IGD)
// Department Graphical Information Systems (GIS)
//
// Copyright (c) 2014 Fraunhofer IGD
//
// This file is part of equinox-test.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.fhg.igd.equinox.test.app.runner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.runner.notification.RunListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import de.fhg.igd.equinox.test.app.TestClassAndMethods;
import de.fhg.igd.equinox.test.app.TestRunnerConfig;
import de.fhg.igd.equinox.test.app.internal.Activator;
import de.fhg.igd.equinox.test.app.runner.util.GlobUtil;
import de.fhg.igd.equinox.test.app.runner.util.ListenerFactory;
import de.fhg.igd.equinox.test.app.runner.util.XmlRunListener;

/**
 * Runs tests based on a configuration.
 * 
 * @author Simon Templer
 */
public class TestRunner {
	
	private static final Logger log = Logger.getLogger(TestRunner.class.getName());
	
	private final TestRunnerConfig config;
	
	private final List<Throwable> failures;

	/**
	 * Create a new test runner based on the given configuration.
	 * @param config the test runner configuration
	 */
	public TestRunner(TestRunnerConfig config) {
		super();
		this.config = config;
		this.failures = new ArrayList<>();
	}
	
	/**
	 * Run the tests.
	 */
	public void execute() {
		log.info("Starting test runner...");
		
		// step 1 - collect/select test classes
		List<Class<?>> testClasses;
		testClasses = collectTestClasses();
		
		/*
		 * Use error stream by default in case someone messes with System.out (e.g. closes the stream).
		 */
		PrintStream out = System.err;
		
		// step 2 - prepare executor & execute tests
		File outputFile = config.getOutputFile();
		if (outputFile == null) {
			TestExecutor exec = new TestExecutor(ListenerFactory.PLAIN, out);
			runTests(exec, testClasses, out);
		}
		else {
			try (OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFile))) { 
				RunListener rl = new XmlRunListener(fileOut);
				TestExecutor exec = new TestExecutor(rl);
				runTests(exec, testClasses, out);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to write test results to output file " + 
						outputFile.getAbsolutePath(), e);
			}
		}
		
		log.info("Test runner completed.");
	}

	/**
	 * Run tests for the given test classes.
	 * 
	 * @param exec the test executor
	 * @param testClasses the test classes
	 */
	private void runTests(TestExecutor exec, List<Class<?>> testClasses, PrintStream outStream) {
		if (config.getMethodNames().isEmpty()) {
			// run all test classes and their tests
			exec.executeTests(testClasses, failures, outStream);
		}
		else {
			// determine methods for classes
			
			List<TestClassAndMethods> tests = new ArrayList<TestClassAndMethods>();
			for (Class<?> testClass : testClasses) {
				Collection<String> methods = findTestMethods(testClass);
				if (methods != null && !methods.isEmpty()) {
					tests.add(new TestClassAndMethods(testClass, methods));
				}
			}
			
			exec.executeTests(tests, outStream);
		}
	}

	/**
	 * Determine the tests methods to run for a given test class.
	 * 
	 * @param testClass the test class
	 * @return the test methods to run or <code>null</code>
	 */
	private Collection<String> findTestMethods(Class<?> testClass) {
		if (testClass == null || config.getMethodNames().isEmpty()) {
			return null;
		}
		
		Set<String> methodNames = new HashSet<String>();
		
		for (Method method : testClass.getMethods()) {
			//TODO check if method may be a valid test method?
			// for now assume users only specify valid methods
			
			//TODO also allow specifying glob or regex patterns?
			// for now the full name must be specified
			if (config.getMethodNames().contains(method.getName())) {
				methodNames.add(method.getName());
			}
		}
		
		return methodNames;
	}

	/**
	 * Determine test classes for all bundles.
	 * @return the list of test classes
	 */
	private List<Class<?>> collectTestClasses() {
		Bundle[] bundles = Activator.getContext().getBundles();
		List<Class<?>> testClasses = new ArrayList<>();
		
		
		for (Bundle bundle : bundles) {
			if (config.getBundles().isEmpty()) {
				// use default test bundles (end on .test)
				if (bundle.getSymbolicName().endsWith(".test")) {
					testClasses.addAll(findTestClasses(bundle));
				}
			}
			else {
				//TODO also allow specifying glob or regex patterns?
				// for now the full name must be specified
				
				if (config.getBundles().contains(bundle.getSymbolicName())) {
					testClasses.addAll(findTestClasses(bundle));
				}
			}
		}
		
		return testClasses;
	}

	/**
	 * Find test classes in the given bundle.
	 * @param bundle the bundle
	 * @return the test classes contained in the bundle
	 */
	private Collection<? extends Class<?>> findTestClasses(Bundle bundle) {
		Set<Class<?>> testClasses = new LinkedHashSet<>();

		for (String pattern : config.getClassPatterns()) {
			testClasses.addAll(collectTests(bundle, pattern));
		}

		return testClasses;
	}

	/**
	 * Collect test classes in a specific bundle.
	 * @param bundle the bundle to search for test classes
	 * @param classPattern the class name pattern, use asterisk as wildcard
	 * @return the collected test classes
	 */
	private Collection<Class<?>> collectTests(Bundle bundle, String classPattern) {
		Set<Class<?>> testClasses = new HashSet<>();
		
		// replace dots by slash to allow package identification
		int lastDot = classPattern.lastIndexOf('.');
		String pkg = null;
		if (lastDot >= 0) {
			pkg = classPattern.substring(0, lastDot);
			classPattern = (lastDot + 1 == classPattern.length()) ? null : classPattern.substring(lastDot + 1);
		}
		if (classPattern != null) {
			classPattern = classPattern + ".class";
		}
		else {
			classPattern = "*.class";
		}
		
		Enumeration<URL> entries = bundle.findEntries("/", classPattern, true);
		if (entries != null) {
			while (entries.hasMoreElements()) {
				URL resource = entries.nextElement();

				// determine class name
				String resourcePath = resource.getPath();
				if (!resourcePath.startsWith("/target/")) {
					String className = resourcePath.substring(0,
							resourcePath.length() - 6).replaceAll("/", ".");
					if (className.startsWith(".")) {
						className = className.substring(1);
					}
					if (className.startsWith("bin.")) {
						className = className.substring(4);
					}
					try {
						Class<?> testClass = loadClass(bundle, className);
						
						boolean reject = false;
						
						if (pkg != null && !pkg.isEmpty()) {
							reject = !matchPackage(pkg, testClass.getPackage().getName());
						}

						if (!reject) {
							int modifiers = testClass.getModifiers();
							if (!Modifier.isAbstract(modifiers)
									&& Modifier.isPublic(modifiers)) {
								// only accept non-abstract public classes as test
								// classes
								testClasses.add(testClass);
							}
						}
						
					} catch (ClassNotFoundException | NoClassDefFoundError e) {
						log.severe(MessageFormat.format(
								"Failed to load class {0}", className));
						failures.add(e);
					}
				}
			}
		}
		return testClasses;
	}

	/**
	 * Match a package pattern against a package.
	 * 
	 * @param pkgGlobPattern the package glob pattern
	 * @param pkg the package to match
	 * @return if the package matches the pattern
	 */
	private boolean matchPackage(String pkgGlobPattern, String pkg) {
		String regex = GlobUtil.convertGlobToRegex(pkgGlobPattern);
		return Pattern.matches(regex, pkg);
	}

	/**
	 * Load a class from a given bundle.
	 * @param bundle the bundle
	 * @param className the class name
	 * @return the loaded class if found
	 * @throws ClassNotFoundException if the class could not be found
	 */
	private Class<?> loadClass(Bundle bundle, String className)
			throws ClassNotFoundException {
		Class<?> testClass = null;
		BundleWiring bw = bundle.adapt(BundleWiring.class);

		if (bw != null) {
			if (bw.getClassLoader() != null) {
				testClass = bw.getClassLoader().loadClass(className);
			} else {
				// most likely a fragment
				List<BundleWire> hosts = bw
						.getRequiredWires(BundleRevision.HOST_NAMESPACE);
				if (hosts != null && !hosts.isEmpty()) {
					Exception lastE = null;
					for (BundleWire host : hosts) {
						try {
							testClass = host.getProviderWiring()
									.getClassLoader().loadClass(className);
							break;
						} catch (Exception e) {
							lastE = e;
						}
					}
					if (testClass == null) {
						throw new ClassNotFoundException(MessageFormat.format(
								"Failed to load class {0} from fragment hosts",
								className), lastE);
					}
				} else {
					throw new ClassNotFoundException(
							MessageFormat
									.format("Found no host bundle for fragment {1} to load class {0}",
											className, bundle.getSymbolicName()));
				}
			}
		} else {
			testClass = bundle.loadClass(className);
		}
		if (testClass == null) {
			throw new ClassNotFoundException(MessageFormat.format(
					"Failed to load class {0}", className));
		}

		return testClass;
	}
	
}
