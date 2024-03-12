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
import java.io.PrintStream;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A run listener creating simple text reports.
 * @author Michel Kraemer
 */
public class TextRunListener extends RunListener {
	/**
	 * The output stream to write the report to
	 */
	private PrintStream _out;
	
	/**
	 * Constructs a new listener
	 * @param out the output stream to write the report to
	 */
	public TextRunListener(OutputStream out) {
		_out = new PrintStream(out);
	}
	
	/**
	 * @see RunListener#testStarted(Description)
	 */
	@Override
	public void testStarted(Description description) throws Exception {
		_out.append(".");
	}
	
	/**
	 * @see RunListener#testFailure(Failure)
	 */
	@Override
	public void testFailure(Failure failure) throws Exception {
		_out.append("E");
	}
	
	/**
	 * @see RunListener#testIgnored(Description)
	 */
	@Override
	public void testIgnored(Description description) throws Exception {
		_out.append("I");
	}
	
	/**
	 * @see RunListener#testRunFinished(Result)
	 */
	@Override
	public void testRunFinished(Result result) throws Exception {
		_out.println();
		_out.println(result.getRunCount() + " test(s) run. Time: " +
				result.getRunTime() + "ms");
		_out.println();
		if (result.getFailureCount() == 0) {
			_out.println("OK");
			return;
		}

		_out.println(result.getFailureCount() + " failures:");
		int c = 1;
		for (Failure f : result.getFailures()) {
			_out.println(c + ") " + f.getTestHeader());
			_out.print(f.getTrace());
			++c;
		}
		
		_out.flush();
	}
}
