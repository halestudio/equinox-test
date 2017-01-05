// Copyright (c) 2016 wetransform GmbH
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

package de.fhg.igd.equinox.test.app.extension;

import org.eclipse.core.runtime.IConfigurationElement;
import org.junit.runner.notification.RunListener;

import de.fhg.igd.eclipse.util.extension.AbstractConfigurationFactory;
import de.fhg.igd.eclipse.util.extension.AbstractExtension;

/**
 * RunListener extension
 * 
 * @author Simon Templer
 */
public class RunListenerExtension extends
		AbstractExtension<RunListener, RunListenerFactory> {

	private static class ConfigurationFactory extends AbstractConfigurationFactory<RunListener> implements RunListenerFactory {

		/**
		 * Constructor.
		 * 
		 * @param conf the configuration element
		 */
		public ConfigurationFactory(IConfigurationElement conf) {
			super(conf, "class");
		}

		@Override
		public void dispose(RunListener instance) {
			// nothing to do
		}

		@Override
		public String getIdentifier() {
			return conf.getAttribute("id");
		}

		@Override
		public String getDisplayName() {
			return getIdentifier();
		}

	}

	/**
	 * The extension point ID
	 */
	public static final String ID = "de.fhg.igd.equinox.test.listeners"; //$NON-NLS-1$

	private static RunListenerExtension instance;

	/**
	 * Get the extension instance
	 * 
	 * @return the function wizard extension
	 */
	public static RunListenerExtension getInstance() {
		if (instance == null) {
			instance = new RunListenerExtension();
		}
		return instance;
	}

	/**
	 * Default constructor
	 */
	private RunListenerExtension() {
		super(ID);
	}

	/**
	 * @see AbstractExtension#createFactory(IConfigurationElement)
	 */
	@Override
	protected RunListenerFactory createFactory(IConfigurationElement conf)
			throws Exception {
		if (conf.getName().equals("listener")) { //$NON-NLS-1$
			return new ConfigurationFactory(conf);
		}

		return null;
	}

}
