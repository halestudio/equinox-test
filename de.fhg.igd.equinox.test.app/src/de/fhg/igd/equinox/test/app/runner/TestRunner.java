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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.junit.runner.notification.RunListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import de.fhg.igd.equinox.test.app.TestRunnerConfig;
import de.fhg.igd.equinox.test.app.internal.Activator;
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
		//TODO based on configuration
		testClasses = allTestClasses();
		
		// step 2 - prepare executor & execute tests
		File outputFile = config.getOutputFile();
		if (outputFile == null) {
			TestExecutor exec = new TestExecutor();
			exec.executeTests(testClasses, failures);
		}
		else {
			try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) { 
				RunListener rl = new XmlRunListener(out);
				TestExecutor exec = new TestExecutor(rl);
				exec.executeTests(testClasses, failures);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to write test results to output file " + outputFile.getAbsolutePath(), e);
			}
		}
		
		log.info("Test runner completed.");
	}

	/**
	 * Determine test classes for all bundles.
	 * @return the list of test classes
	 */
	private List<Class<?>> allTestClasses() {
		Bundle[] bundles = Activator.getContext().getBundles();
		List<Class<?>> testClasses = new ArrayList<>();
		
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().endsWith(".test")) {
				testClasses.addAll(findTestClasses(bundle));
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
		List<Class<?>> testClasses = new ArrayList<>();
		
		BundleWiring bw = bundle.adapt(BundleWiring.class);
		if (bw != null) {
			for (URL resource : bw.findEntries("/", "*Test.class", BundleWiring.FINDENTRIES_RECURSE)) {
				// determine class name
				String resourcePath = resource.getPath();
				if (!resourcePath.startsWith("/target/")) {
					String className = resourcePath.substring(0, resourcePath.length() - 6).replaceAll("/", ".");
					if (className.startsWith(".")) {
						className = className.substring(1);
					}
					if (className.startsWith("bin.")) {
						className = className.substring(4);
					}
					try {
						Class<?> testClass = bw.getClassLoader().loadClass(className);
						int modifiers = testClass.getModifiers();
						if (!Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers)) {
							// only accept non-abstract public classes as test classes
							testClasses.add(testClass);
						}
					} catch (ClassNotFoundException | NoClassDefFoundError e) {
						log.severe(MessageFormat.format("Failed to load class {0}", className));
						failures.add(e);
					}
				}
			}
		}
		
		return testClasses;
	}

}
