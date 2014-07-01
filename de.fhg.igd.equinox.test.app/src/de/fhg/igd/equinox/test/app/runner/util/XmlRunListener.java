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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A run listener creating XML reports which have the
 * same format as the ones created by the JUnit Ant task.
 * @author Michel Kraemer
 */
public class XmlRunListener extends RunListener {
	/**
	 * Constants for XML tags
	 */
	private static final String UNKNOWN = "unknown";
	private static final String TESTSUITE = "testsuite";
	private static final String TESTSUITE_NAME = "name";
	private static final String TESTSUITE_TESTS = "tests";
	private static final String TESTSUITE_FAILURES = "failures";
	private static final String TESTSUITE_ERRORS = "errors";
	private static final String TESTSUITE_SKIP = "skip";
	private static final String TESTSUITE_TIME = "time";
	private static final String PROPERTIES = "properties";
	private static final String TESTCASE = "testcase";
	private static final String TESTCASE_NAME = "name";
	private static final String TESTCASE_CLASSNAME = "classname";
	private static final String TESTCASE_TIME = "time";
	private static final String FAILURE = "failure";
	private static final String FAILURE_MESSAGE = "message";
	private static final String FAILURE_TYPE = "type";
	private static final String SKIPPED = "skipped";
	private static final String SKIPPED_MESSAGE = "message";
	
	/**
	 * The XML document
	 */
	private Document _document;
	
	/**
	 * The document's root element
	 */
	private Element _root;
	
	/**
	 * The element of the current test
	 */
	private Element _currentTest;
	
	/**
	 * The output stream to write the document to
	 */
	private OutputStream _out;
	
	/**
	 * A map of started tests and their respective start time
	 */
	private Map<Description, Long> _startedTests =
		new HashMap<Description, Long>();
	
	/**
	 * Constructs a new listener
	 * @param out the output stream to write the XML document to
	 */
	public XmlRunListener(OutputStream out) {
		_out = out;
	}
	
	/**
	 * @see RunListener#testRunStarted(Description)
	 */
	@Override
	public void testRunStarted(Description description) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		_document = builder.newDocument();
		_root = _document.createElement(TESTSUITE);
		_document.appendChild(_root);
		
		//add required attributes to test suite
		String name = description.getDisplayName();
		_root.setAttribute(TESTSUITE_NAME,
				name != null && !name.equalsIgnoreCase("null") ? name : UNKNOWN);
		
		//add properties (just to match the DTD)
		Element propElement = _document.createElement(PROPERTIES);
		_root.appendChild(propElement);
	}
	
	/**
	 * @see RunListener#testStarted(Description)
	 */
	@Override
	public void testStarted(Description description) throws Exception {
		_currentTest = _document.createElement(TESTCASE);
		_root.appendChild(_currentTest);
		
		//add required attributes
		String name = description.getDisplayName();
		String className = name;
		int bopen = name.indexOf('(');
		if (bopen != -1) {
			int bclose = name.indexOf(')');
			className = name.substring(bopen + 1, bclose);
			name = name.substring(0, bopen);
		}
		_currentTest.setAttribute(TESTCASE_NAME, name);
		_currentTest.setAttribute(TESTCASE_CLASSNAME, className);
		_startedTests.put(description, System.currentTimeMillis());
	}
	
	@Override
	public void testIgnored(Description description) throws Exception {
		boolean started = false;
		if (_currentTest == null) {
			testStarted(description);
			started = true;
		}
		
		Element e = _document.createElement(SKIPPED);
		_currentTest.appendChild(e);
		
		String message = "";
		Ignore ignore = description.getAnnotation(Ignore.class);
		if (ignore != null) {
			message = ignore.value();
		}
		e.setAttribute(SKIPPED_MESSAGE, message);
		
		if (started) {
			testFinished(description);
		}
	}
	
	/**
	 * @see RunListener#testFailure(Failure)
	 */
	@Override
	public void testFailure(Failure failure) throws Exception {
		Element e = _document.createElement(FAILURE);
		_currentTest.appendChild(e);
		
		if (failure.getMessage() != null) {
			e.setAttribute(FAILURE_MESSAGE, failure.getMessage());
		}
		e.setAttribute(FAILURE_TYPE, failure.getException()
				.getClass().getCanonicalName());
		
		String trace = failure.getTrace().replaceAll("\r\n", "\n");
		e.setTextContent(trace);
	}
	
	/**
	 * @see RunListener#testFinished(Description)
	 */
	@Override
	public void testFinished(Description description) throws Exception {
		Long startTime = _startedTests.get(description);
		if (startTime != null) {
			_currentTest.setAttribute(TESTCASE_TIME, String.valueOf(
					(System.currentTimeMillis() - startTime) / 1000.0));
		}
		_currentTest = null;
	}
	
	/**
	 * @see RunListener#testRunFinished(Result)
	 */
	@Override
	public void testRunFinished(Result result) throws Exception {
		_root.setAttribute(TESTSUITE_TESTS,
				String.valueOf(result.getRunCount()));
		_root.setAttribute(TESTSUITE_FAILURES,
				String.valueOf(result.getFailureCount()));
		_root.setAttribute(TESTSUITE_ERRORS, "0");
		_root.setAttribute(TESTSUITE_SKIP,
				String.valueOf(result.getIgnoreCount()));
		_root.setAttribute(TESTSUITE_TIME,
				String.valueOf(result.getRunTime() / 1000.0));
		
		Source src = new DOMSource(_document);
		StreamResult stream = new StreamResult(_out);
		
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {
			throw new IOException("Could not create XML transformer", e);
		}
		try {
			transformer.transform(src, stream);
		} catch (TransformerException e) {
			throw new IOException("Could not write XML file", e);
		}
	}
}
