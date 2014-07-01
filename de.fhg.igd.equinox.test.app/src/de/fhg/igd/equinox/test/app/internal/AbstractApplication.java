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

package de.fhg.igd.equinox.test.app.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * Base class for command line based applications.
 * 
 * @author Simon Templer
 * @param <C> the execution context type
 */
public abstract class AbstractApplication<C> implements IApplication {

	private C executionContext;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		return run((String[]) context.getArguments().get("application.args"), context); //$NON-NLS-1$
	}

	/**
	 * Run the application.
	 * 
	 * @param args the application arguments
	 * @param appContext the application context
	 * @return the return value of the application
	 * @throws Exception if an unrecoverable error occurs processing the
	 *             arguments or running the application
	 * 
	 * @see IApplication#start(IApplicationContext)
	 */
	public Object run(String args[], IApplicationContext appContext) throws Exception {
		try {
			executionContext = createExecutionContext();
			processCommandLineArguments(args, executionContext);
			return run(executionContext, appContext);
		} catch (Exception e) {
			if (e.getMessage() != null)
				System.err.println(e.getMessage());
			else
				e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * @return the execution context (if already created)
	 */
	public C getExecutionContext() {
		return executionContext;
	}

	/**
	 * Run the application.
	 * 
	 * @param executionContext the execution context configured based on the
	 *            application arguments
	 * @param appContext the application context
	 * @return the return value of the application
	 * 
	 * @see IApplication#start(IApplicationContext)
	 */
	protected abstract Object run(C executionContext, IApplicationContext appContext);

	/**
	 * Process the command line arguments.
	 * 
	 * @param args the command line arguments
	 * @param executionContext the execution context to configure
	 * @throws Exception if an unrecoverable error occurs processing the command
	 *             line
	 */
	protected void processCommandLineArguments(String[] args, C executionContext) throws Exception {
		if (args == null)
			return;
		for (int i = 0; i < args.length; i++) {
			// check for args without parameters (i.e., a flag arg)
			processFlag(args[i], executionContext);

			// check for args with parameters. If we are at the last argument or
			// if the next one
			// has a '-' as the first character, then we can't have an arg with
			// a param so continue.
			if (i == args.length - 1 || args[i + 1].startsWith("-")) //$NON-NLS-1$
				continue;
			processParameter(args[i], args[++i], executionContext);
		}
	}

	/**
	 * Process a single command line argument.
	 * 
	 * @param arg the argument
	 * @param executionContext the execution context to configure
	 */
	protected void processFlag(String arg, C executionContext) {
		// override me
	}

	/**
	 * Process a command line parameter
	 * 
	 * @param param the parameter name
	 * @param value the parameter value
	 * @param executionContext the execution context to configure
	 * @throws Exception if an unrecoverable error occurs processing the
	 *             parameter
	 */
	protected void processParameter(String param, String value, C executionContext)
			throws Exception {
		// override me
	}

	/**
	 * Create the application execution context.
	 * 
	 * @return the execution context
	 */
	protected abstract C createExecutionContext();

	@Override
	public void stop() {
		if (executionContext != null) {
			dispose(executionContext);
		}
	}

	/**
	 * Dispose the application execution context.
	 * 
	 * @param executionContext the execution context
	 */
	protected void dispose(C executionContext) {
		// do nothing by default
	}

}
