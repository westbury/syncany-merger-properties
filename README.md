Syncany Plugin for Merging Properties Files
----------------------
This [Syncany](http://www.syncany.org) plugin provides the ability to merge Java properties files.  See http://docs.oracle.com/javase/8/docs/api/java/util/Properties.html for the specification of these files. The plugin can be installed in Syncany using the `sy plugin install` command. For further information about the usage, please refer to the central **[wiki page](https://github.com/binwiederhier/syncany/wiki)**.

Syncany will use this plugin when a Java properties file has been changed on two different machines and so a conflict exists at the 'file' level.  This plugin understands the format of the properties files and so can merge in a sensible manner.  

For example if the same property is added to both files then a conflict is recognized, even if the two properties were added in different parts of the file.

This is a fairly simple merger plugin and so is a good example to use when developing your own merger plugins for Syncany.  For plugin development, please refer to the [plugin development wiki page](https://github.com/binwiederhier/syncany/wiki/Plugin-development).
	
