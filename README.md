# PC Simulator Save Editor Android Port

This is basically PC Simulator Save Editor, but ported to Android to eliminate the need of PojavLauncher.

Developer builds available on the Actions tab.

# Advantages over PCSim Save Editor
- Backwards compatible down to Android 5.0, 1 version below PC Simulator's minimum version.
- No sketchy links. The app is hosted on GitHub and it's safe to use.
- The app takes advantage of Java's performance and efficiency to help decrypt scripts faster and with less memory footprint.
- The app does not collect any data from you. We care seriously about privacy.
- No ads. The app is completely free and open source.
- Save file larger than 30 MB can be opened in this app quickly whereas PCSim Save Editor would crash.
- Extensive documentation.
- Available on PC.

# Contributing
When you contribute to this repository, include the prefixes in your commits:

- `feat: ` if you added a feature
- `bugfix: ` if you fixed a bug
- `minor: ` if you added unnoticeable changes (those that only change the backend)

Open a pull request once you push a commit.
When a commit is made, an action automatically runs which compiles the app.

## Guidelines
Do not commit:
- Anything that requires a permission (not even any permission in the All Permissions menu)
- Anything that is deemed inappropriate.
- Anything that breaks a major feature. (do not break/remove: Decrypt/Encrypt, Open File, Save File, Copy, Decrypt/Encrypt to TXT, Help, three dot menu.)