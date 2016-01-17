/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2014 Philipp C. Heckel <philipp.heckel@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.merger.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.syncany.config.Config;
import org.syncany.merger.properties.PropertiesMergerSettings;
import org.syncany.plugins.merge.FileVersionContent;
import org.syncany.plugins.merge.Merger;
import org.syncany.plugins.merge.MergerSettings;
import org.syncany.plugins.transfer.StorageException;

/**
 * Implements a {@link Merger} that understands the format of a properties file and can
 * sensibly merge them when there are conflicting versions of a properties file.
 *
 * @author Nigel Westbury
 */
public class PropertiesMerger implements Merger {
	private static final Logger logger = Logger.getLogger(PropertiesMerger.class.getSimpleName());

	protected MergerSettings settings;

	protected Config config;

	/**
	 * Text to be used to describe the source of a change when there are conflicting changes
	 * and the source for 'losing' values are to be identified 
	 */
	protected String conflictUserName;
	
	public PropertiesMerger(PropertiesMergerSettings settings, String conflictUserName) {
		this.settings = settings;
		this.conflictUserName = conflictUserName;
	}

	@Override
	public boolean merge(FileVersionContent latestRemoteFile, File localFile, FileVersionContent commonAncestorFile) {
		Properties properties1 = new Properties();
		Properties properties2 = new Properties();
		Properties propertiesBase = new Properties();

		try (
				InputStream latestRemoteInputStream = latestRemoteFile.openInputStream();
				InputStream commonAncestorInputStream = commonAncestorFile.openInputStream();
				InputStream localInputStream = new FileInputStream(localFile);
		) {
			properties1.load(latestRemoteInputStream);
			properties2.load(localInputStream);
			propertiesBase.load(commonAncestorInputStream);
		} catch (IOException | StorageException e) {
			e.printStackTrace();
			
			// It's possible these are not actually properties files in the
			// Java format.  In that case we can't merge them and the caller
			// must fall back to another merger.
			return false;
		}

		/*
		 * Now the properties are loaded, start the three-way merge.
		 * This is done on a property by property basis.
		 */ 

		Properties mergedProperties = new Properties();

		for (Object idAsObject : propertiesBase.keySet()) {
			String id = (String)idAsObject;
			Object baseValue = propertiesBase.get(id);
			if (properties1.containsKey(id)) {
				Object value1 = properties1.remove(id);
				if (properties2.containsKey(id)) {
					Object value2 = properties2.remove(id);

					if (value1.equals(baseValue)) {
						mergedProperties.put(id, value2);
					} else if (value2.equals(baseValue)) {
						mergedProperties.put(id, value1);
					} else {
						mergedProperties.put(id, value1);
						mergedProperties.put(id + "_changedBy_" + conflictUserName, value2);
					}
				} else {
					/*
					 * 
					 * It's been deleted from property file 2. If there are any
					 * 'significant' changes in property file 1 then we include that
					 * new property value. This may or may not be what the user wants, but
					 * it is a lot easier for the user to delete the property
					 * again if the user wants the property deleted.
					 */
					if (significantChanges(baseValue, value1)) {
						mergedProperties.put(id, value1);
					}					
				}
			} else {
				if (properties2.containsKey(id)) {
					Object value2 = properties2.remove(id);
					/*
					 * It's been deleted from property file 1. If there are any
					 * 'significant' changes in property file 2 then we include that
					 * new property value. This may or may not be what the user wants, but
					 * it is a lot easier for the user to delete the property
					 * again if the user wants the property deleted.
					 */
					if (significantChanges(baseValue, value2)) {
						mergedProperties.put(id, value2);
					}					
				} else {
					// It's been deleted from both.
					// Nothing to do.
				}
			}
		}

		/*
		 * Now process the properties that are deemed to be new.  That is, they did not
		 * exist in the ancestor property file.
		 * 
		 * If any property value is a sub-string of the property value
		 * in the other property file, use the longer property value.
		 */

		/*
		 * Any properties left, just add them.
		 */
		for (Object idAsObject : properties1.keySet()) {
			String id = (String)idAsObject;
			Object value1 = properties1.get(id);
			if (properties2.containsKey(id)) {
				// The property has been added as a new property in both versions.

				Object value2 = properties2.remove(id);

				/* 
				 * If both versions added the same value then we simply use that value.
				 * Otherwise we have a conflict that we resolve only by keeping
				 * the remote version and storing the local version with an alternative
				 * key.
				 */
				mergedProperties.put(id, value1);
				if (!value2.equals(value1)) {
					mergedProperties.put(id + "_addedBy_" + conflictUserName, value2);
				}
			} else {
				// Property does not exist in property file2, so easy, just use value from property file1.
				mergedProperties.put(id, value1);
			}
		}

		/*
		 * Anything left in properties2 will be properties that did not exist in properties1,
		 * so we simply add those.
		 */
		for (Object id : properties2.keySet()) {
			mergedProperties.put(id, properties2.get(id));
		}

		try (
			OutputStream out = new FileOutputStream(localFile);
		) {
			mergedProperties.store(out, "Merged by Syncany");
			return true;
		}
		catch (IOException e) {
			// TODO Create our own runtime exception class
			throw new RuntimeException(e);
		}
	}

	private boolean significantChanges(Object value1, Object value2) {
		return !value1.equals(value2);
	}

	@Override
	public String getMimeType() {
		// Indicate that this merger is not restricted to
		// any particular mime type.
		return null;
	}

	@Override
	public String getExtension() {
		// Indicate that an attempt to use merge files using
		// this merger is only to be done on files with a
		// .properties extension.
		return "properties";
	}
}
