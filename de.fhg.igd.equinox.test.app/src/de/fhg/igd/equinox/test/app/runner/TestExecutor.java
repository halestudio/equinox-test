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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import de.fhg.igd.equinox.test.app.TestClassAndMethods;
import de.fhg.igd.equinox.test.app.extension.RunListenerExtension;
import de.fhg.igd.equinox.test.app.extension.RunListenerFactory;
import de.fhg.igd.equinox.test.app.runner.util.ExtendedComputer;
import de.fhg.igd.equinox.test.app.runner.util.ListenerFactory;

/**
 * Executes JUnit tests.
 * 
 * @author Simon Templer
 */
public class TestExecutor {
	/**
	 * The run listener which collects test results.
	 */
	private final RunListener listener;
	
	/**
	 * Creates a test executor that write the test results to the console.
	 */
	public TestExecutor() {
		this(ListenerFactory.PLAIN, System.out);
	}
		
	/**
	 * Create a test executor with a named run listener.
	 * @param listenerName the listener name 
	 * @param out the ouput stream to write the test results to
	 */
	protected TestExecutor(String listenerName, OutputStream out) {
		this(ListenerFactory.createRunListener(listenerName, out)); 
	}
	
	/**
	 * Create a test executor with the given run listener.
	 * @param runListener the test run listener
	 */
	public TestExecutor(RunListener runListener) {
		this.listener = runListener;
	}
	
	/**
	 * Execute the tests specified by the given class names
	 * 
	 * @param testClasses the test classes
	 * @param additionalFailures throwables representing additional failures
	 * to report
	 * 
	 * @return the list of errors
	 */
	public List<String> executeTests(Collection<Class<?>> testClasses, Collection<Throwable> additionalFailures, PrintStream outStream) {
		JUnitCore junit = new JUnitCore();
		if (listener != null) {
			junit.addListener(listener);
		}
		
		// add additional listeners registered in extension point
		for (RunListenerFactory factory : RunListenerExtension.getInstance().getFactories()) {
			outStream.println("Adding test run listener " + factory.getIdentifier());
			try {
				RunListener listener = factory.createExtensionObject();
				junit.addListener(listener);
			} catch (Exception e) {
				outStream.println("Initialising test run listener failed");
				e.printStackTrace(outStream);
			}
		}
		
		List<String> errors = new ArrayList<String>();
		
		// run test classes
		Result result = junit.run(new ExtendedComputer(additionalFailures),
				testClasses.toArray(new Class[testClasses.size()]));
		for (Failure failure : result.getFailures()) {
			errors.add(failure.toString());
		}
		
		outStream.println(">");
		outStream.println("> Completed executing tests from " + testClasses.size() + " test classes");
		outStream.println(">");
		
		return errors;
	}
	
	/**
	 * Execute the tests specified by the given test classes and method names
	 * 
	 * @param testClasses the test classes with the associated test methods to run
	 * 
	 * @return the list of errors
	 */
	public List<String> executeTests(Collection<TestClassAndMethods> testClasses, PrintStream outStream) {
		JUnitCore junit = new JUnitCore();
		if (listener != null) {
			junit.addListener(listener);
		}
		
		// add additional listeners registered in extension point
		for (RunListenerFactory factory : RunListenerExtension.getInstance().getFactories()) {
			outStream.println("Adding test run listener " + factory.getIdentifier());
			try {
				RunListener listener = factory.createExtensionObject();
				junit.addListener(listener);
			} catch (Exception e) {
				outStream.println("Initialising test run listener failed");
				e.printStackTrace(outStream);
			}
		}
		
		List<String> errors = new ArrayList<String>();
		
		// run test class methods
		int methCount = 0;
		for (TestClassAndMethods testClass : testClasses) {
			for (String method : testClass.getTestMethods()) {
				outStream.println(">");
				outStream.println("> Running test method " + testClass.getTestClass().getName() + "#" + method);
				outStream.println(">");
				
				methCount++;
				Result result = junit.run( Request.method(testClass.getTestClass(), method) );
				for (Failure failure : result.getFailures()) {
					errors.add(failure.toString());
				}
			}
		}
		
		outStream.println(">");
		outStream.println("> Completed executing tests from " + methCount + " test methods");
		outStream.println(">");
		
		return errors;
	}
	
}
