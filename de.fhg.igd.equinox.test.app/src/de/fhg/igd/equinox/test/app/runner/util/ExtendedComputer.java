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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Extends {@link Computer} and returns a Suite augmented with runners whose
 * only purpose it to create failures for given {@link Throwable}s.
 * @author Michel Kraemer
 */
public class ExtendedComputer extends Computer {
	/**
	 * The throwables to throw by the additional runners
	 */
	private final Collection<Throwable> _throwables;
	
	/**
	 * Creates a computer
	 * @param throwables the throwables to throw by the additional runners
	 */
	public ExtendedComputer(Collection<Throwable> throwables) {
		_throwables = throwables;
	}
	
	@Override
	public Runner getSuite(final RunnerBuilder builder,
			Class<?>[] classes) throws InitializationError {
		//create runners for test classes
		final List<Runner> runners = new ArrayList<Runner>();
		RunnerBuilder secondBuilder = new RunnerBuilder() {
			@Override
			public Runner runnerForClass(Class<?> testClass) throws Throwable {
				return getRunner(builder, testClass);
			}
		};
		runners.addAll(secondBuilder.runners(null, classes));
		
		//create dummy runners for each throwable
		for (Throwable t : _throwables) {
			runners.add(new ThrowableRunner(t));
		}

		//create suite that uses the created runners
		return new ExtendedSuite(runners);
	}
	
	/**
	 * This class extends {@link Suite} to make a protected constructor visible
	 */
	private static class ExtendedSuite extends Suite {
		public ExtendedSuite(List<Runner> runners) throws InitializationError {
			super(null, runners);
		}
	}
	
	/**
	 * A runner whose only purpose is to create a failure
	 */
	private static class ThrowableRunner extends Runner {
		private Throwable _t;
		
		public ThrowableRunner(Throwable t) {
			_t = t;
		}

		@Override
		public Description getDescription() {
			return Description.createSuiteDescription(_t.getMessage(),
					(Annotation[])null);
		}

		@Override
		public void run(RunNotifier notifier) {
			Description desc = getDescription();
			notifier.fireTestStarted(desc);
			try {
				notifier.fireTestFailure(new Failure(getDescription(), _t));
			} finally {
				notifier.fireTestFinished(desc);
			}
		}
	}
}
