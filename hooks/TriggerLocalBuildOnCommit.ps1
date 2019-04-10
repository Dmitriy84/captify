param([string]$path)
Invoke-WebRequest -Uri http://localhost:8080/git/notifyCommit?url=file://$path -Method POST