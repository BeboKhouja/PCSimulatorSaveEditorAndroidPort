<h1 align="center">PC Simulator Save Editor</h1>
<img src="https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/blob/master/app/src/main/pc_simulator_save_editor-playstore.png" width="200" height="200" alt="PC Simulator Save Editor Logo" align="left">

[![Dev Builds](https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/actions/workflows/main.yml/badge.svg)](https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/actions/workflows/main.yml)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/BeboKhouja/PCSimulatorSaveEditorAndroidPort)](https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/actions)
[![Discord](https://img.shields.io/discord/1274384588092866685.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.com/invite/GXRECJjhVr)

- From PCSim Save Editor's source here comes PC Simulator Save Editor Android!
- PC Simulator Save Editor is a save editor that allows you to edit PC Simulator saves!
- It can open huge files (up to 200MB on 8GB RAM) unlike PCSim Save Editor!

# Contributing
We welcome any contributions! For example, you can change the code, the documentation, and also help translate the app!

When commiting, open a pull request, saying what you added, a short description of it, and how to execute it.

# Installing
You can get PC Simulator Save Editor via two methods:

1. You can get it from the [stable releases](https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/releases), or [automatic builds](https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/actions)
However, they are not debuggable and are shrinked to reduce the size of the app.

2. You can [build](#building) from source code.

## Building

* Pull the submodules
```
git submodule init
```


### PC Simulator Save Editor Library
Library is [here](https://github.com/BeboKhouja/PC-Simulator-Save-Editor).
* Build the library
```
./gradlew library:assemble
```
### The App
* Build the app

```
./gradlew app:assembleDebug
```

# Current status
- [ ] Service to decrypt/encrypt very huge files
- [ ] Convert PC Simulator 1.6.0 and below save files to newer versions

# Known issues
- Frequent crashes when opening huge files.
- Doing a lot of automated actions (Insert object, Cleanup, Save Options) could cause the save to be broken (Additional text after reading JSON)

# License
PC Simulator Save Editor Android is licensed under [GPLv3.0](https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort/blob/master/LICENSE).

# Credits & Licenses (if available)
The [Stack Overflow](https://stackoverflow.com) community for resolving a lot of bugs.

# Message to Yiming

Dear Yiming, we made this save editor because we want our users to have freedom, so please do not ban us from the PC Simulator Discord.
We will not advertise this server on the official PC Simulator Discord.