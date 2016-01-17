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
package org.syncany.plugins;

import org.syncany.config.Config;
import org.syncany.merger.properties.PropertiesMerger;
import org.syncany.merger.properties.PropertiesMergerSettings;
import org.syncany.plugins.merge.Merger;
import org.syncany.plugins.merge.MergerPlugin;
import org.syncany.plugins.merge.MergerSettings;
import org.syncany.plugins.merge.MergingException;

/**
 * @author Nigel Westbury <???>
 */
public class PropertiesMergerPlugin extends MergerPlugin {

	public PropertiesMergerPlugin() {
		super("properties");
	}

	@Override
	public <T extends MergerSettings> T createEmptySettings()
			throws MergingException {
		return (T)new PropertiesMergerSettings();
	}

	@Override
	public <T extends Merger> T createMerger(
			MergerSettings mergerSettings, Config config)
			throws MergingException {
		// TODO Auto-generated method stub
		
		String conflictUserName = (config.getDisplayName() != null) 
		? config.getDisplayName() 
				: config.getMachineName();

		return (T)new PropertiesMerger((PropertiesMergerSettings)mergerSettings, conflictUserName);
	}
}
