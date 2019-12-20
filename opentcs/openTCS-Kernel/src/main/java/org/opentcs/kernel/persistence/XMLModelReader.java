/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.File;
import java.io.IOException;
import org.opentcs.kernel.workingset.Model;

/**
 * An interface for an XML model reader.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface XMLModelReader {
  
  /**
   * Extracts the name of the model in the given XML document.
   *
   * @param file The file from which the model is to be read.
   * @return The name of the model in the given XML document.
   * @throws IOException If there was a problem parsing the input.
   * @throws InvalidModelException If there was a problem interpreting the model.
   */
  String readModelName(File file)
      throws IOException, InvalidModelException;

  /**
   * Reads an XML document and transforms its data to a proper <code>Model</code>.
   *
   * @param file The file from which the model is to be read.
   * @param model The <code>Model</code> instance into which the data read is written.
   * @throws IOException If there was a problem parsing the input.
   * @throws InvalidModelException If there was a problem interpreting the model.
   */
  void readXMLModel(File file, Model model)
      throws IOException, InvalidModelException;
}
