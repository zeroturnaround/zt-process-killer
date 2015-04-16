@echo off
echo Sleeping for %1 seconds...

rem http://stackoverflow.com/questions/1672338/how-to-sleep-for-5-seconds-in-windowss-command-prompt-or-dos

rem "timeout" command would fail:
rem ERROR: Input redirection is not supported, exiting the process immediately.

powershell -command "Start-Sleep -s %1"

echo Finished!
