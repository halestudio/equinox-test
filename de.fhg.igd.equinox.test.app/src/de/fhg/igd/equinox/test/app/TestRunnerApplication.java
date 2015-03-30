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

package de.fhg.igd.equinox.test.app;

import java.io.File;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.fhg.igd.equinox.test.app.internal.AbstractApplication;
import de.fhg.igd.equinox.test.app.runner.TestRunner;

/**
 * Test runner application.
 * 
 * @author Simon Templer
 */
public class TestRunnerApplication extends AbstractApplication<TestRunnerConfigImpl> {

	@Override
	protected void processParameter(String param, String value,
			TestRunnerConfigImpl executionContext) throws Exception {
		switch (param) {
		case "-out":
			executionContext.setOutputFile(new File(value));
			break;
		case "-class":
			// custom class pattern
			executionContext.getClassPatterns().add(value);
			break;
		}
	}

	@Override
	protected void processFlag(String arg, TestRunnerConfigImpl executionContext) {
		switch(arg) {
		case "-unit":
			// unit test pattern
			executionContext.getClassPatterns().add("*Test");
			break;
		case "-integration":
			// integration test pattern
			executionContext.getClassPatterns().add("*IT");
			break;
		}
	}

	@Override
	protected Object run(TestRunnerConfigImpl executionContext,
			IApplicationContext appContext) {
		if (executionContext.getClassPatterns().isEmpty()) {
			// by default execute tests with Unit test pattern
			executionContext.getClassPatterns().add("*Test");
		}
		new TestRunner(executionContext).execute();
		return IApplication.EXIT_OK;
	}

	@Override
	protected TestRunnerConfigImpl createExecutionContext() {
		return new TestRunnerConfigImpl();
	}

}
