param($socket, $path)
Invoke-WebRequest -Uri $socket/git/notifyCommit?url=file://$path -Method POST