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

package de.fhg.igd.equinox.test.app.runner.util;

import java.io.OutputStream;

import org.junit.runner.notification.RunListener;

/**
 * Creates run listeners
 * @author Michel Kraemer
 */
public class ListenerFactory {
	/**
	 * The name of the TextRunListener
	 */
	public static final String PLAIN = "plain";
	
	/**
	 * The name of the XmlRunListener
	 */
	public static final String XML = "xml";
	
	/**
	 * Creates a new run listener
	 * @param name the name of the listener type to create. Can be either
	 * "plain" or "xml".
	 * @param out the output stream the listener should use to write the
	 * test results to.
	 * @return the listener instance
	 */
	public static RunListener createRunListener(String name, OutputStream out) {
		if (name.equalsIgnoreCase(PLAIN)) {
			return new TextRunListener(out);
		} else if (name.equalsIgnoreCase(XML)) {
			return new XmlRunListener(out);
		}
		throw new IllegalArgumentException("Unknown listener: " + name);
	}
}
