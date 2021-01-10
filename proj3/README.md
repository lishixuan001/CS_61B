# GitLet
> Local Git Command File Storage & Transfer System

## Setup
Compile the project by using command 

```sh
make
```

Then in command line setup 

```sh
alias gitlet="java gitlet.Main"
```

Now we can use the gitlet as a local github system

## Application
The usage of GitLet is the same as git commands. Example commands are

### Commands
#### File 
* gitlet init
* gitlet add [file name]
* gitlet commit [message]
* gitlet rm [file name]
* gitlet log 
* gitlet global-log
* gitlet find [commit message]
* gitlet status 

#### Branch
* gitlet checkout -- [file name]
* gitlet checkout [commit id] -- [file name]
* gitlet checkout [branch name]
* gitlet branch [branch name]
* gitlet rm-branch [branch name]
* gitlet reset [commit id]
* gitlet merge [branch name]

#### Remote
* gitlet add-remote [remote name]
* gitlet rm-remote [remote name]
* gitlet push [remote name] [remote branch name]
* gitlet fetch [remote name] [remote branch name]
* gitlet pull [remote name] [remote branch name]

