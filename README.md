# Demisto IntelliJ Plugin

## Installation
    1. Download `Demisto-Plugin-XXX.zip` from this repo's directory `plugin_zips`.
    2. Go to PyCharm/IntelliJ -> Preferences
    3. Choose `Plugins`
    4. Select `Install plugin from disk`
    5. Choose `Demisto-Plugin-XXX.zip`.
    6. `Demisto IDE Add-on` should now appear in the menu, choose the `Restart PyCharm` option
## Prerequisites 
    - PyCharm, or IntelliJ with Python Community Edition (Python plugin for IntelliJ) installed. 
      Supported PyCharm/IntelliJ versions - 2018.5 onwards
    - Demisto server, and a API key for it 
## Limitations
     The Demisto IntelliJ Plugin is limited to IntelliJ platform based products of versions 2018.5 - 2020.2.* due to performance issues in 2020.3 onwards.
## Demisto setup
    If you want to interact with Demisto you need to enter your relevant Demisto instance parmeters.
    1. Choose and open the project you want to work on in Intellij/PyCharm. For example, Demisto Content repository.
    2. In IntelliJ/PyCharm, go to `Preferences` -> `Tools` -> `Demisto Plugin Setup`
    3. You'll be prompted for access to your keychain, please choose `Allow Always` to avoid issues later on.
    4. Enter your server url, with port if relevant. If working on dev instance on localhost, use http and not https (http://localhost:8080)
    5. Enter your Demisto API key.
        - To generate Demisto API key, in your Demisto instance:
            1. Go to Settings > Integrations > API Keys
            2. Click on `Get Your Key` ->  enter a name for the key -> click Generate key
    6. Click `Test` or `Apply`.
    All done! The plugin would automatically download `CommonServerPython` and `CommonUserPython` from your Demisto
    so you can use the functions you defined in Demisto.
    The plugin also contains a `demistomock` file with relevant Demisto functions such as demisto.results().
    All of these files would be save in your project's root.
    
## Workflow
    The plugin defines a slightly different workflow than Demisto. With the plugin, you can work on your Python code,
    and use the `Demisto Settings` side toolbar to define all of your parameters under its `Automation Settings` or `Integration Settings` tab.
    When you want to run the script, go to the `Run Automation` or `Run Integration` tab of the toolbar, enter your arguments, and click 
    `Export & Run in Demisto`. The script would be uploaded to Demisto, your query would run in Demisto and the results
    would show in a new `Demisto Results` toolbar which would open.
    
    Create a new Demisto package:
    1.  1. In the IDE project view, select the folder in which to create the Demisto package.
        2. In the top menu bar, click on File -> `New Demisto Automation` or `New Demisto Integration`
        3. Enter a name for the Demisto package.
        4. A new directory will be created, which contains a YML file and a Python file. For integrations,
         the directory will also contain files for a product image (PNG format), and a detailed description (MD format).
        5. Write your code :)
    
    2.  1. Create new Python file, or open an existing Python file.
        2. Under the `Demisto Settings` toolbar, you'll have two button named `Create Demisto Automation Configuration` and `Create Demisto Integration Configuration`
        3. Click the button. A new Demisto YML file would created for this script, and you can now define arguments etc. for it.
         
    Edit an existing Demisto file:
    1.  1. In the top menu bar, click on File -> `Open Demisto Configuration`
        2. Select the file you want. (It should be a Demisto YML)
        3. A new directory will be created, which contains a YML file and a Python file. For integrations,
         the directory will also contain files for a product image (PNG format), and a detailed description (MD format). 
         You can now edit the code and use the Demisto Settings panel.
    
    2.  1. Go to a Demisto YML file (open it in through the IDE project view)
        2. Click the `Create Demisto Python` button in the top actions toolbar.
        3. A new directory will be created, which contains a YML file and a Python file. For integrations,
         the directory will also contain files for a product image (PNG format), and a detailed description (MD format). 
         You can now edit the code and use the Demisto Settings panel.

    Export a file to Demisto:
    1. When you run a script, it is also automatically exported to Demisto.
    2. From the top action toolbar, click the Export to Demisto button for either the YML file or Python file.
    
## Logs
   The logs can be found in the following paths, under `idea.log`:
   Mac OS X: `~/Library/Logs/<PRODUCT><VERSION>`
   For example, `~/Library/Logs/PyCharmCE2018.2/idea.log`
   Linux and Other Unix systems: ` ~/.<PRODUCT><VERSION>`
   
   Windows Vista, 7, 8, 10: `<SYSTEM DRIVE>\Users\<USER ACCOUNT NAME>\.<PRODUCT><VERSION>`
   Windows XP: `<SYSTEM DRIVE>\Documents and Settings\<USER ACCOUNT NAME>\.<PRODUCT><VERSION>`
   For example, `c:\Users\John\.PyCharm45\`
   
## Developing the Plugin Code
To build and run the plugin following your code changes, follow these steps:

1. From IntelliJ, open the plugin project.
2. Build the sources and launch the plugin by the following these steps:
* From the *Gradle* tool window, expand *demisto-plugin --> Tasks -->  IntelliJ*
* Run the *buildPlugin* task.
* Run the *runIde* task.

## Code Contributions
We welcome community contribution through pull requests.

## Git configuration
Copy the pre-commit hook from .hooks to .git/hooks. Run the following command from the repository root:

```sh
cp .hooks/* ../.git/hooks
```
