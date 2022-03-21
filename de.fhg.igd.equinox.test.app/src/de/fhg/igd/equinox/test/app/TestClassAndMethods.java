// 
// Copyright (c) 2022 wetransform GmbH
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

package de.fhg.igd.equinox.test.app;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Associates a test class to test methods that should be run on the class.
 */
public class TestClassAndMethods {
	
	private final Class<?> testClass;
	
	private final Set<String> testMethods;

	/**
	 * 
	 * @param testClass the test class
	 * @param testMethods the test methods
	 */
	public TestClassAndMethods(Class<?> testClass, Collection<String> testMethods) {
		super();
		this.testClass = testClass;
		this.testMethods = new TreeSet<String>(testMethods);
	}

	/**
	 * @return the testClass
	 */
	public Class<?> getTestClass() {
		return testClass;
	}

	/**
	 * @return the testMethods
	 */
	public Set<String> getTestMethods() {
		return testMethods;
	}

}
